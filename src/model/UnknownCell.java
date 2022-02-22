package model;

import org.json.simple.JSONObject;

public class UnknownCell implements Situated {
	
	int x, y;
	
	public UnknownCell(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJSON() {
		JSONObject jo = new JSONObject();
		jo.put("type","unknown");
		jo.put("x",""+x);
		jo.put("y",""+y);
		return jo;
	}
	
	public 	ComponentType getComponentType() {
		return ComponentType.unknown;
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

	public void setY(int dy) {
		this.y = y;
		
	}

	public void setLocation(int x, int y) {
		setX(x);
		setY(y);
		
	}

	public String display() {		
		return "?";
	}

}
