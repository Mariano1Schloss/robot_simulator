package burger;

import model.ComponentType;
import model.Situated;
import components.Turtlebot;
import model.EmptyCell;
import model.UnknownCell;
import model.Grid;
import mqtt.Message;
import java.util.Random;
import model.ObstacleDescriptor;
import model.RobotDescriptor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.List;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
//Subcribe to color topic
public class HunterTurtleBot extends Turtlebot {
    protected Random rnd;
    protected Grid grid;
    protected String proie;
    protected String team;

    public HunterTurtleBot(int id, String name, int seed, int field, Message clientMqtt, int debug, String team) {
        super(id, name, seed, field, clientMqtt, debug, team);
        rnd = new Random(seed);
        if (team.equalsIgnoreCase("vipere")) proie = "renard";
        else if (team.equalsIgnoreCase("renard")) proie = "poule";
        else if (team.equalsIgnoreCase("poule")) proie = "vipere";
    }

    protected void init() {
        clientMqtt.subscribe("inform/grid/init");
        clientMqtt.subscribe(name + "/position/init");
        clientMqtt.subscribe(name + "/grid/init");
        clientMqtt.subscribe(name + "/grid/update");
        clientMqtt.subscribe(name + "/action");
    }

    public void handleMessage(String topic, JSONObject content){
        if (topic.contains(name+"/grid/update")) {

            JSONArray ja = (JSONArray)content.get("cells");
            List<Situated> ls = grid.get(ComponentType.robot);

            for(int i=0; i < ja.size(); i++) {
                JSONObject jo = (JSONObject)ja.get(i);
                String typeCell = (String)jo.get("type");
                int xo = Integer.parseInt((String)jo.get("x"));
                int yo = Integer.parseInt((String)jo.get("y"));
                int[] to = new int[]{xo,yo};

                if(typeCell.equals("poule") || typeCell.equals("renard") || typeCell.equals("vipere") || typeCell.equals("robot")) {
                    int idr = Integer.parseInt((String)jo.get("id"));
                    boolean findr = false;
                    for(Situated sss:ls) {
                        if(sss != this){
                            RobotDescriptor rd = (RobotDescriptor)sss;
                            if(rd.getId() == idr) {
                                grid.moveSituatedComponent(rd.getX(), rd.getY(), xo, yo);
                                findr = true;
                                break;
                            }
                        }
                    }
                    if(!findr) {
                        String namer = (String)jo.get("name");
                        String subtyper = (String)jo.get("subtype");
                        grid.forceSituatedComponent(new RobotDescriptor(to, idr, namer, subtyper));
                    }
                } else {
                    Situated sg = grid.getCell(yo,xo);
                    Situated s;
                    if(sg.getComponentType() == ComponentType.unknown) {
                        if(typeCell.equals("obstacle")){

                            s = new ObstacleDescriptor(to);
                        } else {

                            s = new EmptyCell(xo,yo);
                        }
                        grid.forceSituatedComponent(s);
                    }
                }
            }


            if(debug == 1) {
                System.out.println("---- " + name + " ----");
                grid.display();
            }

        } else if (topic.contains(name+"/action")) {
            int stepr = Integer.parseInt((String)content.get("step"));
            move(stepr);
        } else if (topic.contains("inform/grid/init")) {
            int rows = Integer.parseInt((String)content.get("rows"));
            int columns = Integer.parseInt((String)content.get("columns"));
            grid = new Grid(rows, columns, seed);
            grid.initUnknown();
            grid.forceSituatedComponent(this);
        }
        else if (topic.contains(name+"/position/init")) {
            x = Integer.parseInt((String)content.get("x"));
            y = Integer.parseInt((String)content.get("y"));
        }
        else if (topic.contains(name+"/grid/init")) {
            JSONArray ja = (JSONArray)content.get("cells");
            for(int i=0; i < ja.size(); i++) {
                JSONObject jo = (JSONObject)ja.get(i);
                String typeCell = (String)jo.get("type");
                int xo = Integer.parseInt((String)jo.get("x"));
                int yo = Integer.parseInt((String)jo.get("y"));
                int[] to = new int[]{xo,yo};
                Situated s;
                if(typeCell.equals("obstacle")){
                    //System.out.println("Add ObstacleCell");
                    s = new ObstacleDescriptor(to);
                }
                else if(typeCell.equals("robot")){
                    //System.out.println("Add RobotCell");
                    int idr = Integer.parseInt((String)jo.get("id"));
                    String namer = (String)jo.get("name");
                    String subtyper = (String)jo.get("subtype");
                    s = new RobotDescriptor(to, idr, namer, subtyper);
                }
                else {
                    //System.out.println("Add EmptyCell " + xo + ", " + yo);
                    s = new EmptyCell(xo,yo);
                }
                grid.forceSituatedComponent(s);
            }
        }
    }

    public void setLocation(int x, int y) {
        int xo = this.x;
        int yo = this.y;
        this.x = x;
        this.y = y;
        grid.moveSituatedComponent(xo, yo, x, y);
    }

    public Grid getGrid() {
        return grid;
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }


    public Situated getClosest() {
        double distance = Math.pow(10, 30);
        Situated official = null;
        for (Situated[] l:grid.getGrid()) {
            for (Situated s:l) {
                if(s.getSubtype().equalsIgnoreCase(proie) && s!=this) {

                    //RobotDescriptor ss = (RobotDescriptor) s;
                    double distanceUpdate = getDistance(s.getX(), s.getY());
                    if (distanceUpdate <= distance) {
                        distance = distanceUpdate;
                        official = s;
                    }
                }
            }
        }
        System.out.println("le robot le plus proche de : " + this + " est " + official);
        System.out.println("la distance entre eux est de : " + distance);
        return official;
    }

    public double getDistance(int xo, int yo) {
        return Math.sqrt(Math.pow(xo-this.getX(), 2) + Math.pow(yo-this.getY(),2));
    }

    public Orientation setNewOrientation(Situated s) {

        Orientation orientationN = Orientation.up;
        //System.out.println(s.getX() + " ahah " + this.getX());


        // quart supérieur gauche
        if (s.getX() < getX() && s.getY() > getY()) {
            double pente = Math.abs((s.getY()-getY())/(s.getX()-getX()));
            if (pente <= 1) {
                orientationN = Orientation.down;
            } else {
                orientationN = Orientation.right;
            }
        }
        // quart supérieur droit
        if (s.getX() > getX() && s.getY() > getY()) {
            double pente = Math.abs((s.getY()-getY())/(s.getX()-getX()));
            if (pente > 1) {
                orientationN = Orientation.right;
            } else {
                orientationN = Orientation.up;
            }
        }
        // quart inférieur droit
        if (s.getX() > getX() && s.getY() < getY()) {
            double pente = Math.abs((s.getY()-getY())/(s.getX()-getX()));
            if (pente > 1) {
                orientationN = Orientation.left;
            } else {
                orientationN = Orientation.up;
            }
        }
        // quart inférieur gauche
        if (s.getX() < getX() && s.getY() < getY()) {
            double pente = Math.abs((s.getY()-getY())/(s.getX()-getX()));
            if (pente > 1) {
                orientationN = Orientation.left;
            } else {
                orientationN = Orientation.down;
            }
        }

        if (s.getX() == getX() && s.getY() > getY()) {
            orientationN = Orientation.left;
        } else if (s.getX() == getX() && s.getY() > getY()) {
            orientationN = Orientation.right;
        } else if (s.getX() > getX() && s.getY() == getY()) {
            orientationN = Orientation.up;
        } else if (s.getX() < getX() && s.getY() == getY()) {
            orientationN = Orientation.down;
        }

        return orientationN;
    }



    public void move(int step) {
        Situated closest = getClosest();

        if (closest == null) {
            randomOrientation();
            moveForward();
        } else {
            System.out.println("change l'orientation de " + this);
            orientation = setNewOrientation(closest);
            System.out.println("l'orientation est maintenant : " + orientation);


            String actionr = "move_forward";
            String result = x + "," + y + "," + orientation + "," + grid.getCellsToString(y,x) + ",";
            for(int i = 0; i < step; i++) {
                EmptyCell[] ec = grid.getAdjacentEmptyCell(x,y);

                moveForward();

            }
            if(debug==2){
                try{
                    writer.write(result + actionr);
                    writer.newLine();
                    writer.flush();
                } catch(IOException ioe){
                    System.out.println(ioe);
                }
            }

        }


    }

    public void randomOrientation() {
        double d = Math.random();
        if(d < 0.25) {
            if(orientation != Orientation.up)
                orientation = Orientation.up;
            else
                orientation = Orientation.down;
        }
        else if(d < 0.5) {
            if(orientation != Orientation.down)
                orientation = Orientation.down;
            else
                orientation = Orientation.up;
        }
        else if(d < 0.75) {
            if(orientation != Orientation.left)
                orientation = Orientation.left;
            else
                orientation = Orientation.right;
        }
        else {
            if(orientation != Orientation.right)
                orientation = Orientation.right;
            else
                orientation = Orientation.left;
        }
    }

    public void moveLeft(int step) {
        Orientation oldo = orientation;
        for(int i = 0; i < step; i++){
            if(orientation == Orientation.up) {
                orientation = Orientation.left;
            }
            else if(orientation == Orientation.left) {
                orientation = Orientation.down;
            }
            else if(orientation == Orientation.right) {
                orientation = Orientation.up;
            }
            else {
                orientation = Orientation.right;
            }
        }
    }

    public void moveRight(int step) {
        Orientation oldo = orientation;
        for(int i = 0; i < step; i++){
            if(orientation == Orientation.up) {
                orientation = Orientation.right;
            }
            else if(orientation == Orientation.left) {
                orientation = Orientation.up;
            }
            else if(orientation == Orientation.right) {
                orientation = Orientation.down;
            }
            else {
                orientation = Orientation.left;
            }
        }
    }

    public void moveForward() {
        int xo = x;
        int yo = y;
        if(orientation == Orientation.up) {
            x += 1;
            x = Math.min(x,grid.getColumns()-1);
        }
        else if(orientation == Orientation.left) {
            y -= 1;
            y = Math.max(y,0);
        }
        else if(orientation == Orientation.right) {
            y += 1;
            y = Math.min(y,grid.getRows()-1);
        }
        else {
            x -= 1;
            x = Math.max(x,0);
        }
        grid.moveSituatedComponent(xo,yo,x,y);
        JSONObject robotj = new JSONObject();
        robotj.put("name", name);
        robotj.put("id", ""+id);
        robotj.put("x", ""+x);
        robotj.put("y", ""+y);
        robotj.put("xo", ""+xo);
        robotj.put("yo", ""+yo);
        //System.out.println("MOVE MOVE " + xo + " " + yo + " --> " + x + " " + y);
        clientMqtt.publish("robot/nextPosition", robotj.toJSONString());
    }

    public void moveBackward() {

    }
}