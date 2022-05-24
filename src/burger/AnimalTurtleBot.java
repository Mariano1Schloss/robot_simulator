package burger;

import model.ComponentType;
import model.Situated;
import components.Turtlebot;
import model.EmptyCell;
import model.Grid;
import mqtt.Message;

import java.util.ArrayList;
import java.util.Random;
import model.ObstacleDescriptor;
import model.RobotDescriptor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.List;
import java.io.IOException;
/*
Cette classe implémente le modèle de robots utile à notre problème. Ce type de robots a deux caractéristiques essentielles :
    - Son équipe (variable team renard/vipere/poule) à laquelle est attribuée une proie et un prédateur
    celle-ci est succeptible de changer quand  les robots sont capturés (elle change et devient l'équipe du chasseur qui l'a capturé)
    - Une stratégie, qui va influencer le comportement des robots en fonction des positions des proies et des prédateurs environnants
    pour l'instant les stratégies sont liées aux équipes et elles changent par conséquent au changement d'équipe. L'impact des stratégies
    se retrouve dans la fonction move() qui va appeler selon les cas des fonctions différentes d'étude des voisins et de mise à jour de l'orientation du robot
 */
public class AnimalTurtleBot extends Turtlebot {
    protected Random rnd;
    protected Grid grid;
    protected String proie;//variable that describes the team that the robot is hunting
    protected String predatory;//variable that describes the team that the robot is running hunted by
    //Variable used only if we want the captured robot to be removed (see  getClosest below,schedule function in Turtlbotfactory and killed/robot topic in GridManagement)
    protected String currentStrategy;
    protected String strategyPoule;
    protected String strategyRenard;
    protected String strategyVipere;

    public AnimalTurtleBot(int id, String name, int seed, int field, Message clientMqtt, int debug, String team, String currentStrategy, String strategyPoule, String strategyRenard, String strategyVipere) {
        super(id, name, seed, field, clientMqtt, debug,team);
        //We define the proie variable regarding the Robot team
        if (team.equals("vipere")) proie="renard";
        else if (team.equals("renard"))proie="poule";
        else if (team.equals("poule"))proie="vipere";

        //We define the predatory variable regarding the Robot team
        if (team.equals("vipere")) predatory="poule";
        else if (team.equals("renard"))predatory="vipere";
        else if (team.equals("poule"))predatory="renard";

        this.currentStrategy=currentStrategy;
        this.strategyPoule=strategyPoule;
        this.strategyRenard=strategyRenard;
        this.strategyVipere=strategyVipere;
    }
    //Getters and setters
    public String getProie() {return proie;}

    public void setProie(String proie) {this.proie = proie;}


    //Subscribe to the topics we are interested in
    protected void init() {
        clientMqtt.subscribe("inform/grid/init");
        clientMqtt.subscribe(name + "/position/init");
        clientMqtt.subscribe(name + "/grid/init");
        clientMqtt.subscribe(name +id+ "/grid/update");
        clientMqtt.subscribe(name + "/action");
        clientMqtt.subscribe("robot"+id+"/captured");
    }

    //Function that we trigger different functions depending on the topics received
    public void handleMessage(String topic, JSONObject content){
        if (topic.contains(name+id+"/grid/update")) {
            //GridManagement sends us the updated grid adapted to our vision field (Variable ja)
            JSONArray ja = (JSONArray)content.get("cells");
            //We put the Robotdescriptor components from the robot's grid into a list
            List<Situated> ls = grid.get(ComponentType.robot);
            //we initiate the variable robotsInTheActualizedGrid that will contain the ids from the robots in the updated grid (the one send by GrdManagement)
            List <Integer> robotsInTheActualizedGrid=new ArrayList<Integer>();

            for(int i=0; i < ja.size(); i++) {
                JSONObject jo = (JSONObject)ja.get(i);
                String typeCell = (String)jo.get("type");//Component type from the cell
                String teamCell = (String)jo.get("team");//Team of the component (null if not a Robot)
                //Coordinates:
                int xo = Integer.parseInt((String)jo.get("x"));
                int yo = Integer.parseInt((String)jo.get("y"));
                int[] to = new int[]{xo,yo};

                if(typeCell.equals("robot")) {
                    int idr = Integer.parseInt((String)jo.get("id"));
                    robotsInTheActualizedGrid.add(idr);
                    boolean findr = false;//Boolean variable to check if the component is already in our grid
                    //on bouge les robots qu'on a déjà dans notre grid et on regarde si le robot id n°idr est nouveau avec la variable findr
                    for(Situated sss:ls) {
                        /*Je sépare les cas où sss=this ou non car dans un cas sss est de type Turtlebot, dans les autres RobotDescriptor*/
                        if (sss.equals(this) ){
                            AnimalTurtleBot rd = (AnimalTurtleBot)sss;
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
                    //Si findr=false (robot nouveau dans la grid) on crée de force un composant dans notre grid
                    if(!findr) {
                        String namer = (String)jo.get("name");
                        grid.forceSituatedComponent(new RobotDescriptor(to, idr, namer, teamCell));
                    }
                } else {//If the component type is different from "robot" (obstacle, unknown)
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
            //We remove the robots that are no longer in our sight
            if(robotsInTheActualizedGrid!=null){
                for (Situated s :ls){
                    if(!s.equals(this)){
                        RobotDescriptor rd=(RobotDescriptor) s;
                        if(!robotsInTheActualizedGrid.contains(rd.getId()))grid.removeSituatedComponent(rd.getX(), rd.getY());
                    }
                }
            }

            if(debug == 1) {
                System.out.println("---- " + name + " ----");
                grid.display();
            }
            if(id==4||id==2) {
                System.out.println("---- " + name + " ----");
                grid.display();
            }

        } else if (topic.contains(name+"/action")) {//the topic "/action" triggers the move function
            int stepr = Integer.parseInt((String)content.get("step"));
            move(stepr);//The move function uses getDistance(), getClosest(), isPossibletoMoveforward(), setNewOrientation() functions
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
        else if (topic.contains("robot"+id+"/captured")) {//topic published from another robot  with the getClosest() function, indicating that this robot has been captured
            //We set the right "new" team :
            String oldTeam= (String) content.get("team");
            if (oldTeam.equals("vipere")){
                this.team="poule";
                this.currentStrategy=strategyPoule;
                this.proie="vipere";
                this.predatory="renard";
            }else if (oldTeam.equals("poule")){
                this.team="renard";
                this.currentStrategy=strategyRenard;
                this.proie="poule";
                this.predatory="vipere";
            }else if (oldTeam.equals("renard")){
                this.team="vipere";
                this.currentStrategy=strategyVipere;
                this.proie="renard";
                this.predatory="poule";
            }
            //Then we publish another topic that will be received by GridManagement to update the actual Grid
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
                    s = new ObstacleDescriptor(to);
                }
                else if(typeCell.equals("robot")){
                    int idr = Integer.parseInt((String)jo.get("id"));
                    String namer = (String)jo.get("name");
                    String teamr=(String)jo.get("team");
                    s = new RobotDescriptor(to, idr, namer,teamr);
                }
                else {
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

//Function that will return the closest proie of the robot
    public Situated getClosestHunter() {
        double distance = Math.pow(10, 30);
        Situated official = null;

        //We put the Robotdescriptor components from the robot's grid into a list
        List <Situated> nearRobots= grid.get(ComponentType.robot);
        for (Situated s:nearRobots){
            //System.out.println(s.toString());
                        //We check if the robot is a potential "proie"
            if (s.getTeam().equals(this.proie)  && !s.equals(this)){
                System.out.println(team+id+" a trouve une proie");
                RobotDescriptor rb=(RobotDescriptor) s;
                int closestRobotId= rb.getId();
                //We calculate the distance that separates this robot from its proie
                double distanceUpdate = getDistance(s.getX(), s.getY());
                System.out.println("distance : "+distanceUpdate);
                if (distanceUpdate <= distance) {
                    distance = distanceUpdate;
                    official = s;
                    System.out.println("distance avec le robot le polus proche : "+distance);
                    //we check if thes robots are in adjacent cells (distance <= 1) in this case the robot is captured or killed depending on the configuration
                    if (distance <=1){
                        System.out.println(team+id+" a capturé une proie");
                        System.out.println("la proie :"+s.getTeam()+closestRobotId);
                        //si on veut changer d'équipe :
                        //On va publier un topic qui sera reçu par le HunterTurtlbot capturé pour le lui indiquer
                        JSONObject capturedRobot = new JSONObject();
                        capturedRobot.put("id", ""+closestRobotId);
                        capturedRobot.put("name", ""+((RobotDescriptor) s).getName());
                        capturedRobot.put("team", s.getTeam());
                        clientMqtt.publish("robot"+closestRobotId+"/captured",capturedRobot.toJSONString());
                       ((RobotDescriptor) s).setTeam(this.team);//On update notre propre grille
                    }
                }
            }
        }
        return official;
    }
//Function that will return the closest predator of the robot
    public Situated getClosestRunner() {
        double distance = Math.pow(10, 30);
        Situated official = null;
        //We put the Robotdescriptor components from the robot's grid into a list
        List <Situated> nearRobots= grid.get(ComponentType.robot);
        //System.out.println("size nearrobots  : "+nearRobots.size());
        for (Situated s:nearRobots){
            //We check if the robot is a potential "predatory"

            if (s.getTeam().equals(this.predatory)  && !s.equals(this)){
                RobotDescriptor rb=(RobotDescriptor) s;
                int closestRobotId= rb.getId();
                //We calculate the distance that separates the two robots, if the distance is smaller than the one defined in the distance variable, ditance is updated
                double distanceUpdate = getDistance(s.getX(), s.getY());
                System.out.println(team+id+" a trouve une predatory");
                System.out.println("distance : "+distanceUpdate);
                if (distanceUpdate <= distance) {
                    distance = distanceUpdate;
                    official = s;
                    //we check if thes robots are in adjacent cells (distance <= 1) in this case the robot is captured or killed depending on the configuration
                }
            }else if (s.getTeam().equals(this.proie)  && !s.equals(this)){ //We check if the robot is a potential "proie" adjacent to the robot
                RobotDescriptor rb=(RobotDescriptor) s;
                int closestRobotId= rb.getId();
                //We calculate the distance that separates the two robots
                double distanceProie = getDistance(s.getX(), s.getY());
                //we check if thes robots are in adjacent cells (distance <= 1) in this case the robot is captured or killed depending on the configuration
                if (distanceProie <=1){
                    System.out.println(team+id+" a capturé une proie");
                    System.out.println("la proie :"+s.getTeam()+closestRobotId);
                    //si on veut changer d'équipe :
                    //On va publier un topic qui sera reçu par le HunterTurtlbot capturé pour le lui indiquer
                    JSONObject capturedRobot = new JSONObject();
                    capturedRobot.put("id", ""+closestRobotId);
                    capturedRobot.put("name", ""+((RobotDescriptor) s).getName());
                    capturedRobot.put("team", s.getTeam());
                    clientMqtt.publish("robot"+closestRobotId+"/captured",capturedRobot.toJSONString());
                    ((RobotDescriptor) s).setTeam(this.team);//On update notre propre grille
                }

            }
        }

        return official;
    }

    public double getDistance(int xo, int yo) {
        return Math.sqrt(Math.pow(xo-this.getX(), 2) + Math.pow(yo-this.getY(),2));//distance vectorielle
    }

    public Orientation setNewOrientation(Situated s,boolean isHunting) {
        /*In the function, we manually set the orientation of the turtlebot :
            - we set the orientation depending of the relative position of the two robots (we separate the cases below)
            - We chose to directly change the orientation without calling the rotating functions (moveLeft(), moveRight()) for the simulation to be more visual
            - The boolean isHunting variable is use to differentiate the scenarios depending on the strategy. By default, the new oriention is defined such as the one
            that suits the best to get closer from the target. But if we want to run away from the target, the new orientation is flipped
         */
        Orientation orientationN = Orientation.up;

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
        //Cas où les robots sont alignés selin l'axe X ou Y
        if (s.getX() == getX() && s.getY() > getY()) {
            orientationN = Orientation.left;
        } else if (s.getX() == getX() && s.getY() > getY()) {
            orientationN = Orientation.right;
        } else if (s.getX() > getX() && s.getY() == getY()) {
            orientationN = Orientation.up;
        } else if (s.getX() < getX() && s.getY() == getY()) {
            orientationN = Orientation.down;
        }
        if(!isHunting){
            //We set the opposite orientation:
            if(orientationN==Orientation.down)orientationN=Orientation.up;
            else if (orientationN==Orientation.up)orientationN=Orientation.down;
            else if (orientationN==Orientation.right)orientationN=Orientation.left;
            else if (orientationN==Orientation.left)orientationN=Orientation.right;
            return orientationN;
        }
        return orientationN;//the new Orientation
    }



    public void move(int step) {
        Situated closest;
        if(currentStrategy.equals("hunter"))closest = getClosestHunter();//We search the closest robot
        else if (currentStrategy.equals("runner")) closest = getClosestRunner();//We search the closest robot
        else closest=null;

        EmptyCell[] ec = grid.getAdjacentEmptyCell(x,y);//We put the available cell into the ec list
        if (closest == null) {
            //randomOrientation();
            //By default, if no proie is found , the robot moves forward. Another option is to set a random orientation
            if(possibleToMovForward(ec))moveForward();//We check if a forward move is possible, if not, we change the orientation
            else moveLeft(1);
        } else {
            //If a proie  is found we set the orientation is set and we move forward
            if(currentStrategy.equals("hunter"))orientation = setNewOrientation(closest,true);
            else if (currentStrategy.equals("runner"))orientation = setNewOrientation(closest,false);
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
    }

    public void randomOrientation() {//define a random orientation
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

        clientMqtt.publish("robot/nextPosition", robotj.toJSONString());
        //System.out.println(robotj.toJSONString());
    }

    public void moveBackward() {

    }

    public boolean possibleToMovForward(Situated []ec) {//Check if the forward move is possible regarding the adjacent empty cells
        if(orientation==Orientation.up&&ec[3] != null||orientation==Orientation.down&&ec[2] != null||orientation==Orientation.right&&ec[1] != null||orientation==Orientation.left&&ec[0] != null) {
            return true;
        }
        return false;
    }
}