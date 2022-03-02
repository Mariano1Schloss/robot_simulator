package components;

import model.ComponentType;
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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/* This class defines the different operations that the robot can do on the grid */

public abstract class Turtlebot implements Situated, SimulationComponent {	
	protected Message clientMqtt;
	protected String name;
	protected int id;
	protected int debug;
	protected int x, y;
	protected Orientation orientation;
	//protected boolean waitAnswer = false;
	protected boolean goalReached;	
	protected int seed;
	protected BufferedWriter writer;
	protected int field;
	protected String sttime;
	
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

	public void setLog(String st) {
		sttime = st;
		if(debug==2){
			try{
				writer = new BufferedWriter(new FileWriter(sttime+"/"+name+".log")); 
			} catch(IOException ioe){
				System.out.println(ioe);
				System.exit(1);
			}
		}
	}

	public int getId() {
		return id;
	}

	public boolean isGoalReached(){		
		return goalReached;
	}

	public void setGoalReached(boolean gr){
		goalReached = gr;
		if(debug==2){
			try{
				writer.write("goal reached"); 
				writer.close();
			} catch(IOException ioe){
				System.out.println(ioe);
			}
		}
	}	

	protected abstract void init() ;

	public ComponentType getComponentType() {
		return ComponentType.robot;
	}

	public abstract void setLocation(int x, int y) ; 

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

/*	public boolean isWaitAnswer() {
		return waitAnswer;
	}

	public void setWaitAnswer(boolean wa) {
		waitAnswer = wa;
	}
*/
	public String display(){
		return id+"";
	}

	public abstract void handleMessage(String topic, JSONObject content) ;

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