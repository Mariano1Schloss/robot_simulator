package components;

import model.ComponentType;
import model.EmptyCell;
import model.UnknownCell;
import model.Grid;
import model.Situated;
import burger.Orientation;
import mqtt.Message;
import model.ObstacleDescriptor;
import model.RobotDescriptor;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/* This class defines the different operations that the robot can do on the grid */

public abstract class Turtlebot implements Situated, SimulationComponent {	
	protected Message clientMqtt;
	protected String name;
	protected int id;
	protected int debug;
	protected Grid grid;
	protected int x, y;
	protected Orientation orientation;
	protected boolean waitAnswer = false;
	protected boolean goalReached;	
	protected int seed;
	protected int field;

	protected Turtlebot(int id, String name, int seed, int field, Message clientMqtt, int debug) {
		this.name = name;
		this.debug = debug;
		this.id = id;		
		this.clientMqtt = clientMqtt;
		orientation = Orientation.right;
		goalReached = false;
		this.seed = seed;
		this.field = field;
	}

	public int getId() {
		return id;
	}

	public boolean isGoalReached(){
		return goalReached;
	}

	public void setGoalReached(boolean gr){
		goalReached = gr;
	}	

	protected void init() {
		clientMqtt.subscribe("inform/grid/init");
    	clientMqtt.subscribe(name + "/position/init");		
		clientMqtt.subscribe(name + "/grid/init");		
		clientMqtt.subscribe(name + "/grid/update");		
		clientMqtt.subscribe(name + "/action");		
	}

	public ComponentType getComponentType() {
		return ComponentType.robot;
	}

	public void setLocation(int x, int y) {
		int xo = this.x;
		int yo = this.y;
		this.x = x;
		this.y = y;
		grid.moveSituatedComponent(xo, yo, x, y);
	}

	public int getX() {
		return x;
	}

	public int getY(){
		return y;
	}

	public void setX(int x) {
		this.x =x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getField(){
		return field;
	}

	public Orientation getCurrentOrientation() {
		return orientation;
	}

	public void setCurrentOrientation(Orientation or) {
		orientation = or;
	}

	public Grid getGrid() {
		return grid;
	}

	public void setGrid(Grid grid) {
		this.grid = grid;
	}

	public boolean isWaitAnswer() {
		return waitAnswer;
	}

	public void setWaitAnswer(boolean wa) {
		waitAnswer = wa;
	}

	public String display(){
		return id+"";
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
    	    			grid.forceSituatedComponent(new RobotDescriptor(to, idr, namer));
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
        			s = new RobotDescriptor(to, idr, namer);
        		}
        		else {
        			//System.out.println("Add EmptyCell " + xo + ", " + yo);
        			s = new EmptyCell(xo,yo);
        		}
        		grid.forceSituatedComponent(s);
      		}
        }
	}

	public abstract void moveRight(int step);
	public abstract void moveLeft(int step);
	public abstract void moveForward();
	public abstract void move(int step);

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean equals(Object o) {
		if(o instanceof Turtlebot) {
			return id == ((Turtlebot)o).getId();
		}
		return false;
	}

	public String toString() {
		return "{" + name + "; " + id + "; " + x + "; " + y + "; " + orientation + "}";
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJSON() {
		JSONObject jo = new JSONObject();
		jo.put("type","turtlebot");
		jo.put("name",name);
		jo.put("id",""+id);
		jo.put("x",""+x);
		jo.put("y",""+y);
		return jo;
	}

}