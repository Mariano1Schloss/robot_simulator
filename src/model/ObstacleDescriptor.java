package model;

import org.json.simple.JSONObject;

public class ObstacleDescriptor extends EntityDescriptor implements Situated{
	
	public ObstacleDescriptor (int [] location) {
		super(location);
	}

	public ComponentType getComponentType(){
		return ComponentType.obstacle;
	}

	public String display(){
		return "1";
	}

	public String toString() {
		return "{type: " + getComponentType() + ", x: " + x  + ", y: " + y + "}"; 
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJSON() {
		JSONObject jo = new JSONObject();
		jo.put("type","obstacle");
		jo.put("x",""+x);
		jo.put("y",""+y);
		return jo;
	}
}
