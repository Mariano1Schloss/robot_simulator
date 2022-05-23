package environnementburger;

import components.SimulationComponent;
import model.ComponentType;
import components.Obstacle;
import model.EmptyCell;
import model.UnknownCell;
import model.RobotDescriptor;
import main.TestAppli;
import model.Grid;
import model.Goal;
import model.Situated;
import mqtt.Message;

import java.awt.Color;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/* Some changes have been made in this class :
	- We had to take account that robot can switch team so each team is assigned to different color
	-Every time a robot must be printed in the display window, we look at its color and set the block color accordingly
	- When the RobotDescriptors for the grid are created ("configuration/nBRobots") we create three teams with equal size with the different teams (using the variable nBRobot from prop.ini file)
	- We subscribed to two  other topics to deal with thes case wher a robot gets killed or captured ("robot/captured", "robot/killed")
	- We don't need the notion of "goals" in our problem so I have commented the relative blocs
 */

public class GridManagement implements SimulationComponent {
	protected Grid grid;
	private ArrayList<Goal> goals;
	private static final String turtlebotName = "burger_";
	protected int nbObstacles;
	protected int nbRobots;
	protected Message clientMqtt;
	protected String name;
	protected int debug;
	protected int rows;
	protected int columns;
	protected int display;
	protected int displaywidth;
	protected int displayheight;
	protected String displaytitle;
	protected Color colorrobot;
	//Color variables assigned to the teams
	protected Color colorvipere;
	protected Color colorpoule;
	protected Color colorrenard;

	protected Color colorobstacle;
	protected Color colorgoal;
	protected Color colorother;
	protected Color colorunknown;

	ColorGrid cg;
	
	protected int seed;

	public void initSubscribe() {
		clientMqtt.subscribe("robot/nextPosition");	
		clientMqtt.subscribe("configuration/nbRobot");	
		clientMqtt.subscribe("configuration/nbObstacle");	
		clientMqtt.subscribe("configuration/seed");
		clientMqtt.subscribe("configuration/display");	
		clientMqtt.subscribe("configuration/debug");	
		clientMqtt.subscribe("configuration/robot/grid");	
		clientMqtt.subscribe("robot/grid");	
		clientMqtt.subscribe("environment/grid");
		clientMqtt.subscribe("robot/killed");
		clientMqtt.subscribe("robot/captured");

	}

	public void publishState(JSONObject content) {
		String robotName = (String)((JSONObject)content.get("robot")).get("id");
		//JSONObject state = giveState(content,robotName);
		//clientMqtt.publish(robotName+"/robot/state", state.toJSONString());	
	}

	public int isGoal(int x, int y) {
		for(Goal g : goals) {
			if(g.getX() == x && g.getY() == y)
				return g.getRobot();
		}
		return 0;
	}
	
	public void createColorGrid(int width, int height, String title){
		cg = new ColorGrid(width,height,grid.getColumns(),grid.getRows(), title);
		for(int i = 0; i < grid.getRows(); i++) {
			for(int j = 0; j < grid.getColumns(); j++) {
				Situated elt = grid.getCell(i, j);
				if(elt.getComponentType() == ComponentType.empty) {					
					if(isGoal(j,i) < 0) {
						cg.setBlockColor(j,i,colorgoal);
						//cg.setBlockColor(j,i,colorother);
					} else {
						cg.setBlockColor(j,i,colorother);
					}
				}
//ICI
				/*else if(elt.getComponentType() == ComponentType.robot)
					cg.setBlockColor(j,i,colorrobot);*/
				else if (elt.getComponentType() == ComponentType.robot) {
				 	if (elt.getTeam().equals("poule"))
						cg.setBlockColor(j, i, colorpoule);
					 else if (elt.getTeam().equals("vipere") )
						cg.setBlockColor(j, i, colorvipere);
				 	else if (elt.getTeam().equals("renard"))
						cg.setBlockColor(j, i, colorrenard);
				}

				else if(elt.getComponentType() == ComponentType.obstacle)
					cg.setBlockColor(j,i,colorobstacle);
				else
					cg.setBlockColor(j,i,colorunknown);
			}
		}
		cg.init();
	}

	public GridManagement(){
		this.name = "grid";
		this.debug = 0;
		this.display = 0;
		goals = new ArrayList<Goal>();
	}

	public void publishGridSize(){
		// size of the the complete grid
		JSONObject gridsize = new JSONObject();
		gridsize.put("rows", ""+grid.getRows());
		gridsize.put("columns", ""+grid.getColumns());
		clientMqtt.publish("inform/grid/init", gridsize.toJSONString());
	}

	public void publishObstacles(){
		JSONArray ja = new JSONArray();
		List<Situated> lo = grid.get(ComponentType.obstacle);
		for (Situated ob:lo) {		
			JSONObject jo = new JSONObject();
			jo.put("x", ob.getX()+"");
			jo.put("y", ob.getY()+"");
			ja.add(jo);
		}
		JSONObject obst = new JSONObject();
		obst.put("obstacles", ja);
		clientMqtt.publish("inform/grid/obstacles", obst.toJSONString());
	}

	public void init() {
		for (int i = 0; i < nbObstacles; i++) {
			int[] pos = grid.locate();
			Obstacle obs = new Obstacle(pos);
			grid.putSituatedComponent(obs);			
		}
		if(display == 1) { 
			createColorGrid(displaywidth, displayheight, displaytitle);			
		}						
	}

	public String getName() {
		return name;
	}

	public void setMessage(Message mqtt) {
		clientMqtt = mqtt;
	}
	
	public void refresh(){
		cg.refresh();
	}

	public boolean moveRobot(int id, int x1, int y1, int x2, int y2) {
		Situated elt = grid.getCell(y1, x1);
		if(elt.getComponentType() == ComponentType.robot) {
			RobotDescriptor eltR = (RobotDescriptor)elt;
			if(eltR.getId() == id) {
				grid.moveSituatedComponent(x1,y1,x2,y2);
//ICI
				if(display == 1) { 
					cg.setBlockColor(x1,y1,colorother);
					//cg.setBlockColor(x2,y2,colorrobot);
					if (eltR.getTeam().equals("poule"))cg.setBlockColor(x2,y2,colorpoule);
					else if (eltR.getTeam().equals("vipere"))cg.setBlockColor(x2,y2,colorvipere);
					else if (eltR.getTeam().equals("renard"))cg.setBlockColor(x2,y2,colorrenard);

				}
				return true;
			}
		}
		return false;
	}
	
	public void displayGrid(){
		grid.display();
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject gridToJSONObject(int x, int y, int field) {
			
		// size of the the complete grid
		JSONObject jsongrid = new JSONObject();
		
		// obstacles definition
		
		JSONArray gt = new JSONArray();
		int xm = Math.max(x-field,0);
		int xM = Math.min(x+field,grid.getColumns()-1);
		int ym = Math.max(y-field,0);
		int yM = Math.min(y+field,grid.getRows()-1);

		//System.out.println("field " + field + " x " + x + " y " + y);
		//System.out.println("xm " + xm + " xM " + xM + " ym " + ym + " yM "+ yM);

		for(int i = xm; i <= xM; i++){
			for(int j = ym; j <= yM; j++){
				if(i != x || j != y) {
					Situated s = grid.getCell(j,i);
					//System.out.println("j " + j + " i " + i + " cell " + s);
					JSONObject jo = new JSONObject();
					jo.put("type", s.getComponentType()+"");
					if(s.getComponentType() == ComponentType.robot) {
						RobotDescriptor rd = (RobotDescriptor)s;
						jo.put("name", rd.getName());	
						jo.put("id", rd.getId()+"");
						jo.put("team", rd.getTeam());

					}
					jo.put("x", s.getX()+"");
					jo.put("y", s.getY()+"");
					gt.add(jo);
				}
			}
		}

		jsongrid.put("x", x);
		jsongrid.put("y", y);
		jsongrid.put("field", field);
		jsongrid.put("cells", gt);
		
		return jsongrid;
	}
	
	public void publishInitRobot() {
		List<Situated> ls = grid.get(ComponentType.robot);
		for(Situated s:ls){
			RobotDescriptor rb = (RobotDescriptor)s;
			JSONObject jo = new JSONObject();
			jo.put("name", rb.getName());
			jo.put("id", rb.getId()+"");
			jo.put("x", rb.getX()+"");
			jo.put("y", rb.getY()+"");
			jo.put("team", rb.getTeam());

			clientMqtt.publish(rb.getName()+"/position/init", jo.toJSONString());
		}
	}

	public void handleMessage(String topic, JSONObject content){
		//System.out.println("Message:"+ content.toJSONString());
		if (topic.contains("robot/nextPosition")) {
			//System.out.println("UPDATE ROBOT");
			String rn = (String)content.get("name");
			int idr = Integer.parseInt((String)content.get("id"));
			int xr = Integer.parseInt((String)content.get("x"));
			int yr = Integer.parseInt((String)content.get("y"));
			int xor = Integer.parseInt((String)content.get("xo"));
			int yor = Integer.parseInt((String)content.get("yo"));
			//System.out.println("MOVE MOVE " + xor + " " + yor + " --> " + xr + " " + yr);
			grid.moveSituatedComponent(xor,yor,xr,yr);
			if(display == 1) {
				//cg.setBlockColor(xor, yor, colorother);				
				if(isGoal(xor,yor)<0) {
					cg.setBlockColor(xor,yor,colorgoal);
					//cg.setBlockColor(xor,yor,colorother);
				} else {
					cg.setBlockColor(xor,yor,colorother);
				}
//ICI
				//System.out.println("team of robot or next position : "+(String)content.get("team"));
				if (((String)content.get("team")).equals("poule"))cg.setBlockColor(xr,yr,colorpoule);
				else if (((String)content.get("team")).equals("vipere"))cg.setBlockColor(xr,yr,colorvipere);
				else if (((String)content.get("team")).equals("renard"))cg.setBlockColor(xr,yr,colorrenard);
				else cg.setBlockColor(xr, yr, colorrobot);

				cg.refresh();
			}
			if(debug == 1) {
				grid.display();
			}
		}else if (topic.contains("configuration/nbRobot")) {
			//création des ROBOTS DESCRIPTORS
           	nbRobots = Integer.parseInt((String)content.get("nbRobot"));
			   System.out.println("le nombre de robots : "+nbRobots);
			int id=2;
           	for(int i = 2; i < nbRobots/3+2; i++) {
           		int[] posVipere = grid.locate();
				int[] posPoule = grid.locate();
				int[] posRenard = grid.locate();
				//int[] posRobot = grid.locate();
				//we create three teams with equal size with the different teams (using the variable nBRobot from prop.ini file

				grid.putSituatedComponent(new RobotDescriptor(posVipere, id, GridManagement.turtlebotName+id,"vipere"));
				id++;
				grid.putSituatedComponent(new RobotDescriptor(posPoule, id, GridManagement.turtlebotName+id,"renard"));
				id++;
				grid.putSituatedComponent(new RobotDescriptor(posRenard, id, GridManagement.turtlebotName+id,"poule"));
				id++;


				if(display == 1) {
//ICI
					cg.setBlockColor(posPoule[0], posPoule[1],colorpoule);
					cg.setBlockColor(posVipere[0], posVipere[1],colorvipere);
					cg.setBlockColor(posRenard[0], posRenard[1],colorrenard);
					cg.refresh();
				}				
			}
			//We are not using this part as we are not using goals
			   //création aléatoire des goals
			/*for (int i = 2; i < nbRobots+2; i++) {
				int[] pos = grid.locate();
				// ec = (EmptyCell)grid.getCell(pos[1], pos[0]);
				goals.add(new Goal(pos[0],pos[1],-1*i));
				if(display == 1) {
					//cg.setBlockColor(pos[0], pos[1], colorgoal);
					cg.setBlockColor(pos[0], pos[1], colorother);

					cg.refresh();
				}	
			}*/
			if(debug == 1) {
				grid.display(goals);
			}
        }        
        else if (topic.contains("configuration/robot/grid")) {
            String nameR = (String)content.get("name");
            int fieldr = Integer.parseInt((String)content.get("field"));
            int xr = Integer.parseInt((String)content.get("x"));
            int yr = Integer.parseInt((String)content.get("y"));
            JSONObject jo = gridToJSONObject(xr, yr, fieldr);
            clientMqtt.publish(nameR+"/grid/init", jo.toJSONString());
        }
        else if (topic.contains("robot/grid")) {
            String nameR = (String)content.get("name");
			String teamR = (String)content.get("team");
			int idR=Integer.parseInt((String)content.get("id"));
			int fieldr = Integer.parseInt((String)content.get("field"));
            int xr = Integer.parseInt((String)content.get("x"));
            int yr = Integer.parseInt((String)content.get("y"));
            JSONObject jo = gridToJSONObject(xr, yr, fieldr);
            clientMqtt.publish(nameR+idR+"/grid/update", jo.toJSONString());
			System.out.println("the actual grid : ");
			grid.display();
        }
		else if (topic.contains("robot/nextPosition")) {
            publishState(content);
        } 
		else if (topic.contains("configuration/debug")) {
    	    debug = Integer.parseInt((String)content.get("debug"));
        }
        else if (topic.contains("configuration/display")) {
    	    display = Integer.parseInt((String)content.get("display"));
    	    if(display==1){
   				clientMqtt.subscribe("display/width");
				clientMqtt.subscribe("display/height");
				clientMqtt.subscribe("display/title");
				clientMqtt.subscribe("display/robot");
				clientMqtt.subscribe("display/renard");
				clientMqtt.subscribe("display/vipere");
				clientMqtt.subscribe("display/poule");

				clientMqtt.subscribe("display/goal");
				clientMqtt.subscribe("display/obstacle");
				clientMqtt.subscribe("display/other");
				clientMqtt.subscribe("display/unknown");
    	    }
        }
        else if (topic.contains("configuration/seed")) {
    	    seed = Integer.parseInt((String)content.get("seed"));
        }
        else if (topic.contains("configuration/nbObstacle")) {
    	    nbObstacles = Integer.parseInt((String)content.get("nbObstacle"));
        }
        else if (topic.contains("environment/grid")) {
    	    rows = Integer.parseInt((String)content.get("rows"));
    	    columns = Integer.parseInt((String)content.get("columns"));
    	    grid = new Grid(rows, columns, seed);
			grid.initEmpty();
			init();
        }
		else if (topic.contains("robot/killed")) {
			//We remove the killed robot from the grid and set the cell
			int xR = Integer.parseInt((String)content.get("x"));
			int yR=Integer.parseInt((String)content.get("y"));
			int id=Integer.parseInt((String)content.get("id"));
			grid.removeSituatedComponent(xR,yR);
			System.out.println("Robot id : "+id+" has been removed from the grid with position x:"+xR+" y:"+yR);
			if (display==1){
				cg.setBlockColor(xR,yR,colorother);
			}
		}
		else if (topic.contains("robot/captured")) {
			int id=Integer.parseInt((String)content.get("id"));//The id of the captured Robot
			String team=(String) content.get("team");
			String newTeam=(String) content.get("newteam");
			RobotDescriptor capturedRobot=grid.getById(id);//We search the corresponding Robot in the Grid
			System.out.println("Robot id : "+id+" from team "+team+" has beeen captured");
			if(capturedRobot!=null){
				capturedRobot.setTeam(newTeam);//We set the robot team
				System.out.println("newteam : "+newTeam);
				System.out.println(capturedRobot.toString());
			}else System.out.println("no robot with id : "+id+"has been found");
		}
        else if(display == 1) {
			if (topic.contains("display/width")) {
    	        displaywidth = Integer.parseInt((String)content.get("displaywidth"));
        	}
			else if (topic.contains("display/height")) {
            	displayheight = Integer.parseInt((String)content.get("displayheight"));
        	}
			else if (topic.contains("display/title")) {
            	displaytitle = (String)content.get("displaytitle");
        	}
//ICI
        	else if (topic.contains("display/robot")) {
            	colorrobot = new Color(Integer.parseInt((String)content.get("color")));
        	}
			else if (topic.contains("display/poule")) {
				colorpoule = new Color(Integer.parseInt((String)content.get("color")));
			}
			else if (topic.contains("display/vipere")) {
				colorvipere = new Color(Integer.parseInt((String)content.get("color")));
			}
			else if (topic.contains("display/renard")) {
				colorrenard = new Color(Integer.parseInt((String)content.get("color")));
			}
        	else if (topic.contains("display/goal")) {
            	colorgoal = new Color(Integer.parseInt((String)content.get("color")));
        	}
        	else if (topic.contains("display/obstacle")) {
            	colorobstacle = new Color(Integer.parseInt((String)content.get("color")));
        	}
        	else if (topic.contains("display/other")) {
            	colorother = new Color(Integer.parseInt((String)content.get("color")));
        	}
        }
	}
}
