package model;

import org.json.simple.JSONObject;

public class Goal implements Situated {
	
	int x, y;	
	int robot;
	
	public Goal(int x, int y) {
		this.x = x;
		this.y = y;		
	}

	public Goal(int x, int y, int robot) {
		this.x = x;
		this.y = y;		
		this.robot = robot;
	}

	public int getRobot(){
		return robot;
	}

	public void setRobot(int ref){
		robot = ref;
	}

	@Override
	public 	ComponentType getComponentType() {
		return ComponentType.goal;
	}
	
	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public void setX(int x) {
		this.x = x;
		
	}

	@Override
	public void setY(int dy) {
		this.y = y;
		
	}

	@Override
	public void setLocation(int x, int y) {
		setX(x);
		setY(y);
		
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJSON() {
		JSONObject jo = new JSONObject();
		jo.put("type","goal");
		jo.put("x",""+x);
		jo.put("y",""+y);
		return jo;
	}

	public String toString(){
		return "Goal (" + robot + ") - x: " + x + ",y: "+ y;
	}

	@Override
	public String display() {		
		return ""+robot;
	}

}
