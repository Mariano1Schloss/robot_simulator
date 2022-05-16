package components;

import model.ComponentType;
import model.Situated;

import org.json.simple.JSONObject;

public class Obstacle implements Situated{
	
	protected int x, y;
	//public static final int obstacleFlag = -1;
	
	public Obstacle (int [] location) {
		this.x= location[0];
		this.y = location[1];
	}

	public JSONObject toJSON() {
		JSONObject jo = new JSONObject();
		jo.put("type","obstacle");
		jo.put("x",""+x);
		jo.put("y",""+y);
		return jo;
	}

	public ComponentType getComponentType() 
	{
		return ComponentType.obstacle;
	}

	@Override
	public String getTeam() {
		return null;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public String toString() {
		return "{type: " + getComponentType() + ", x: " + x  + ", y: " + y + "}"; 
	}

	public String display() {
		return "1"; 
	}
}
