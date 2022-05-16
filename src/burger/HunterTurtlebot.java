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
//TODO cas ou getclosest renvoit rien
public class HunterTurtlebot extends Turtlebot{
    protected Random rnd;
    protected Grid grid;
    protected ComponentType proie;
    protected ComponentType team;

    public HunterTurtlebot(int id, String name, int seed, int field, Message clientMqtt, int debug,String team) {
        super(id, name, seed, field, clientMqtt, debug,team);
        rnd = new Random(seed);
/*
        if (team==ComponentType.vipere)proie=ComponentType.renard;
        else if (team==ComponentType.renard)proie=ComponentType.poule;
        else if (team==ComponentType.poule)proie=ComponentType.vipere;*/


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
                if(typeCell.equals("robot")) {
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
                        grid.forceSituatedComponent(new RobotDescriptor(to, idr, namer,null));
                    }
                } else {
                    Situated sg = grid.getCell(yo,xo);
                    Situated s;
                    if(sg.getComponentType() == ComponentType.unknown) {
                        if(typeCell.equals("obstacle")){
                            //System.out.println("Add ObstacleCell");
                            s = new ObstacleDescriptor(to);
                        } else {
                            //System.out.println("Add EmptyCell " + xo + ", " + yo);
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
                    s = new RobotDescriptor(to, idr, namer,null);
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

    public void randomOrientation() {
        double d = Math.random();
        Orientation oldo = orientation;
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

    public void move(int step) {
        //fonctions de Jules qui renvoit un objet Situated

        String actionr = "move_forward";
        String result = x + "," + y + "," + orientation + "," + grid.getCellsToString(y,x) + ",";
        Situated cible =getClosest();

        for(int i = 0; i < step; i++) {
            //onrécupère la liste des cellules adjacentes
            //On définit l'orientation "cible" en fonction des coordonnées cibles
            //Disjonction des cas selon l'orientation précédente et le caractère vide de la cellule
            EmptyCell[] ec = grid.getAdjacentEmptyCell(x,y);
            System.out.println ("Empty cells : "+ec);
            if (cible==null){
                moveForward();
            }
            else {
                System.out.println(("cible : " + cible));
                Orientation targetOrientation = setNewOrientation(cible);

                if (orientation == Orientation.up) {
                    if (targetOrientation == Orientation.up) {
                        if (ec[3] != null) {
                            moveForward();
                        }
                    }
                    if (targetOrientation == Orientation.down) {
                        moveLeft(1);
                    }
                    if (targetOrientation == Orientation.left) {
                        moveLeft(1);
                    }
                    if (targetOrientation == Orientation.right) {
                        moveRight(1);
                    }
                } else if (orientation == Orientation.down) {
                    if (targetOrientation == Orientation.up) {
                        moveLeft(1);
                    }
                    if (targetOrientation == Orientation.down) {
                        if (ec[2] != null) {
                            moveForward();
                        }
                    }
                    if (targetOrientation == Orientation.left) {
                        moveRight(1);
                    }
                    if (targetOrientation == Orientation.right) {
                        moveLeft(1);
                    }
                } else if (orientation == Orientation.right) {
                    if (targetOrientation == Orientation.up) {
                        moveLeft(1);
                    }
                    if (targetOrientation == Orientation.down) {
                        moveRight(1);
                    }
                    if (targetOrientation == Orientation.left) {
                        moveLeft(1);
                    }
                    if (targetOrientation == Orientation.right) {
                        if (ec[1] != null) {
                            moveForward();
                        }
                    }
                } else if (orientation == Orientation.left) {
                    if (targetOrientation == Orientation.up) {
                        moveRight(1);
                    }
                    if (targetOrientation == Orientation.down) {
                        moveLeft(1);
                    }
                    if (targetOrientation == Orientation.left) {
                        if (ec[0] != null) {
                            moveForward();
                        }
                    }
                    if (targetOrientation == Orientation.right) {
                        moveLeft(1);
                    }
                }
            }
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

    public double getDistance( int xo, int yo) {
        return Math.sqrt(Math.pow((xo-getX()),2) + Math.pow((yo-getY()),2));
    }

    public Situated getClosest() {
        double distance = 10^35;
        Situated official = null;
        System.out.println("Grille champ de vision " + this.proie + this.id + " : " + getGrid().getAdjacentEmptyCell(this.x,this.y));
        for (Situated[] l:grid.getGrid()) {
            for (Situated s:l) {
                if(s.getComponentType() == proie) {
                    //RobotDescriptor ss = (RobotDescriptor) s;
                    double distanceUpdate = getDistance( s.getX(), s.getY());
                    if (distanceUpdate < distance) {
                        distance = distanceUpdate;
                        official = s;
                    }
                }
            }
        }
       if (official!=null) System.out.println("Closest found : "+official.display());
        return official;

    }

    public Orientation setNewOrientation(Situated s) {
        Orientation orientationN = Orientation.up;
        double pente = Math.abs((s.getY()-getY())/(s.getX()-getX()));

        // quart supérieur gauche
        if (s.getX() < getX() && s.getY() > getY()) {
            if (pente <= 1) {
                orientationN = Orientation.down;
            } else {
                orientationN = Orientation.left;
            }
        }
        // quart supérieur droit
        if (s.getX() > getX() && s.getY() > getY()) {
            if (pente > 1) {
                orientationN = Orientation.left;
            } else {
                orientationN = Orientation.up;
            }
        }
        // quart inférieur droit
        if (s.getX() > getX() && s.getY() < getY()) {
            if (pente > 1) {
                orientationN = Orientation.right;
            } else {
                orientationN = Orientation.up;
            }
        }
        // quart inférieur gauche
        if (s.getX() < getX() && s.getY() < getY()) {
            if (pente > 1) {
                orientationN = Orientation.right;
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