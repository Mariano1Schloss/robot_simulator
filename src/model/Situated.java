package model;

import org.json.simple.JSONObject;

public interface Situated {
	public ComponentType getComponentType();	
	public int getX();
	public int getY();
	public void setX(int x);
	public void setY(int y);
	public void setLocation(int x, int y);
	public String display();
	public JSONObject toJSON();
}
