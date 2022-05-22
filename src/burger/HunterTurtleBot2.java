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
public class HunterTurtleBot2 extends Turtlebot {
    protected Random rnd;
    protected Grid grid;
    protected String proie;
    protected int killedRobotId;
    //protected String team;

    public HunterTurtleBot2(int id, String name, int seed, int field, Message clientMqtt, int debug, String team) {
        super(id, name, seed, field, clientMqtt, debug,team);
        rnd = new Random(seed);
        killedRobotId=0;
        if (team.equals("vipere")) proie="renard";
        else if (team.equals("renard"))proie="poule";
        else if (team.equals("poule"))proie="vipere";
    }

    public String getProie() {
        return proie;
    }

    public void setProie(String proie) {
        this.proie = proie;
    }

    public int getKilledRobotId() {
        return killedRobotId;
    }

    public void setKilledRobotId(int killedRobotId) {
        this.killedRobotId = killedRobotId;
    }

    protected void init() {
        clientMqtt.subscribe("inform/grid/init");
        clientMqtt.subscribe(name + "/position/init");
        clientMqtt.subscribe(name + "/grid/init");
        clientMqtt.subscribe(name +id+ "/grid/update");
        clientMqtt.subscribe(name + "/action");
        clientMqtt.subscribe("robot"+id+"/captured");
    }

    public void handleMessage(String topic, JSONObject content){
        if (topic.contains(name+id+"/grid/update")) {

            JSONArray ja = (JSONArray)content.get("cells");
            List<Situated> ls = grid.get(ComponentType.robot);

            for(int i=0; i < ja.size(); i++) {
                JSONObject jo = (JSONObject)ja.get(i);
                String typeCell = (String)jo.get("type");
                String teamCell = (String)jo.get("team");

                int xo = Integer.parseInt((String)jo.get("x"));
                int yo = Integer.parseInt((String)jo.get("y"));
                int[] to = new int[]{xo,yo};

                if(typeCell.equals("robot")) {
                    int idr = Integer.parseInt((String)jo.get("id"));
                    boolean findr = false;
                    //on bouge les robots qu'on a déjà dans notre grid et on regarde si le robot id n°idr est nouveau avec la variable findr
                    for(Situated sss:ls) {
                        System.out.println("grid Rs from id : "+id+" "+ sss.toString());
                        if (sss.equals(this) ){
                            HunterTurtleBot2 rd = (HunterTurtleBot2)sss;
                            if(rd.getId() == idr) {
                                grid.moveSituatedComponent(rd.getX(), rd.getY(), xo, yo);
                                findr = true;
                            }
                        }
                        else {
                            RobotDescriptor rd = (RobotDescriptor) sss;
                            if(rd.getId() == idr) {
                                grid.moveSituatedComponent(rd.getX(), rd.getY(), xo, yo);
                                if(!rd.getTeam().equals(teamCell))rd.setTeam(teamCell);//in case the team has changed
                                findr = true;
                            }                        }
                    }
                    if(!findr) {
                        String namer = (String)jo.get("name");
                        grid.forceSituatedComponent(new RobotDescriptor(to, idr, namer, teamCell));
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

            /*if(id==2) {
                System.out.println("---- " + name + " ----");
                grid.display();
            }*/
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
        else if (topic.contains("robot"+id+"/captured")) {
            String oldTeam= (String) content.get("team");
            if (oldTeam.equals("vipere")){
                this.team="poule";
                this.proie="vipere";
            }else if (oldTeam.equals("poule")){
                this.team="renard";
                this.proie="poule";
            }else if (oldTeam.equals("renard")){
                this.team="vipere";
                this.proie="renard";
            }
            String newTeam=team;
            JSONObject capturedRobot = new JSONObject();
            capturedRobot.put("id", ""+id);
            capturedRobot.put("name", ""+name);
            capturedRobot.put("team", oldTeam);
            capturedRobot.put("newteam", newTeam);

            clientMqtt.publish("robot/captured",capturedRobot.toJSONString());
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
                    String teamr=(String)jo.get("team");
                    s = new RobotDescriptor(to, idr, namer,teamr);
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
        if (id==2){
            System.out.println("-----------------------");
            System.out.println("grille vue due robot id"+id+" : ");
            grid.display();
            System.out.println("-----------------------");
        }
        List <Situated> nearRobots= grid.get(ComponentType.robot);
        System.out.println("size nearrobots  : "+nearRobots.size());
        for (Situated s:nearRobots){
            System.out.println(s.toString());
            //System.out.println("this proie : "+this.proie+" victim team : " +s.getTeam()+" equal condition : "+!s.equals(this));
            System.out.println("this team : "+this.team+" s team : "+s.getTeam());
            if (s.getTeam().equals(this.proie)  && !s.equals(this)){
                System.out.println(team+id+" a trouve une proie");
                RobotDescriptor rb=(RobotDescriptor) s;
                int closestRobotId= rb.getId();
                double distanceUpdate = getDistance(s.getX(), s.getY());
                System.out.println("distance : "+distanceUpdate);
                if (distanceUpdate <= distance) {
                    distance = distanceUpdate;
                    official = s;
                    System.out.println("distance avec le robot le polus proche : "+distance);
                    if (distance <=1){
                        //if we want to kill the robots
                            /*killedRobotId=closestRobotId;
                            System.out.println("Robot id :"+killedRobotId+" is going to die");
                            System.out.println("attacker with position x:"+x+" y:"+y +"victim with position x:"+s.getX()+" y:"+s.getY() );
                            JSONObject killedRobot = new JSONObject();
                            killedRobot.put("id", ""+closestRobotId);
                            killedRobot.put("name", ""+((RobotDescriptor) s).getName());
                            killedRobot.put("x", ""+s.getX());
                            killedRobot.put("y", ""+s.getY());
                            killedRobot.put("team", s.getTeam());
                            grid.removeSituatedComponent(s.getX(),s.getY());
                            //System.out.println("MOVE MOVE " + xo + " " + yo + " --> " + x + " " + y);
                            clientMqtt.publish("robot/killed", killedRobot.toJSONString());*/
                        System.out.println(team+id+" a capturé une proie");
                        System.out.println("la proie :"+s.getTeam()+closestRobotId);
                        //si on veut changer d'équipe
                        JSONObject capturedRobot = new JSONObject();
                        capturedRobot.put("id", ""+closestRobotId);
                        capturedRobot.put("name", ""+((RobotDescriptor) s).getName());
                        capturedRobot.put("team", s.getTeam());
                        clientMqtt.publish("robot"+closestRobotId+"/captured",capturedRobot.toJSONString());
                       ((RobotDescriptor) s).setTeam(this.team);

                    }
                }
            }
        }
        /*for (Situated[] l:grid.getGrid()) {
            for (Situated s:l) {
                if(s.getComponentType()==ComponentType.robot&& s.getTeam().equals(this.proie)  && !s.equals(this)) {
                    System.out.println(team+id+" a trouve une proie");
                    RobotDescriptor rb=(RobotDescriptor) s;
                    int closestRobotId= rb.getId();
                    double distanceUpdate = getDistance(s.getX(), s.getY());
                    //System.out.println("distance : "+distanceUpdate);
                    if (distanceUpdate <= distance) {
                        distance = distanceUpdate;
                        official = s;
                        System.out.println("distance avec le robot le polus proche : "+distance);
                        if (distance <=1){
                            //if we want to kill the robots
                            *//*killedRobotId=closestRobotId;
                            System.out.println("Robot id :"+killedRobotId+" is going to die");
                            System.out.println("attacker with position x:"+x+" y:"+y +"victim with position x:"+s.getX()+" y:"+s.getY() );
                            JSONObject killedRobot = new JSONObject();
                            killedRobot.put("id", ""+closestRobotId);
                            killedRobot.put("name", ""+((RobotDescriptor) s).getName());
                            killedRobot.put("x", ""+s.getX());
                            killedRobot.put("y", ""+s.getY());
                            killedRobot.put("team", s.getTeam());
                            grid.removeSituatedComponent(s.getX(),s.getY());
                            //System.out.println("MOVE MOVE " + xo + " " + yo + " --> " + x + " " + y);
                            clientMqtt.publish("robot/killed", killedRobot.toJSONString());*//*
                            System.out.println(team+id+" a capturé une proie");
                            System.out.println("la proie :"+s.getTeam()+closestRobotId);
                            //si on veut changer d'équipe
                            JSONObject capturedRobot = new JSONObject();
                            capturedRobot.put("id", ""+closestRobotId);
                            capturedRobot.put("name", ""+((RobotDescriptor) s).getName());
                            capturedRobot.put("team", s.getTeam());
                            clientMqtt.publish("robot"+closestRobotId+"/captured",capturedRobot.toJSONString());
                            ((RobotDescriptor) s).setTeam(this.team);

                        }
                    }
                }
            }
        }*/
        /*System.out.println("le robot le plus proche de : " + this + " est " + official);
        System.out.println("la distance entre eux est de : " + distance);*/
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
        EmptyCell[] ec = grid.getAdjacentEmptyCell(x,y);
        System.out.println(id+" team : "+this.team);
        if (closest == null) {
            //randomOrientation();
            if(possibleToMovForward(ec))moveForward();
            else moveLeft(1);
        } else {
            //System.out.println("change l'orientation de " + this);
            orientation = setNewOrientation(closest);
           // System.out.println("l'orientation est maintenant : " + orientation);
            String actionr = "move_forward";
            String result = x + "," + y + "," + orientation + "," + grid.getCellsToString(y,x) + ",";
            for(int i = 0; i < step; i++) {
                //EmptyCell[] ec = grid.getAdjacentEmptyCell(x,y);
                if(orientation==Orientation.up&&ec[3] != null||orientation==Orientation.down&&ec[2] != null||orientation==Orientation.right&&ec[1] != null||orientation==Orientation.left&&ec[0] != null) {
                    moveForward();
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
        /*if (id==2)System.out.println("Position vipere x:"+x +" y:"+y);
        else if (id==6)System.out.println("Position poule x:"+x +" y:"+y);*/
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
        robotj.put("team", team);

        //System.out.println("MOVE MOVE " + xo + " " + yo + " --> " + x + " " + y);
        clientMqtt.publish("robot/nextPosition", robotj.toJSONString());
        //System.out.println(robotj.toJSONString());
    }

    public void moveBackward() {

    }

    public boolean possibleToMovForward(Situated []ec) {
        if(orientation==Orientation.up&&ec[3] != null||orientation==Orientation.down&&ec[2] != null||orientation==Orientation.right&&ec[1] != null||orientation==Orientation.left&&ec[0] != null) {
            return true;
        }
        return false;
    }
}