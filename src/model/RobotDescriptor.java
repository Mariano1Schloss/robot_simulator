package model;

import org.json.simple.JSONObject;

import java.awt.*;
import java.util.Map;
import java.util.HashMap;

public class RobotDescriptor extends EntityDescriptor implements Situated{
	
	protected Map<String,String> properties;
	protected String color;
	protected String team;
	
	public RobotDescriptor (int [] location, int id, String name,String team) {
		super(location);
		this.team=team;
		properties = new HashMap<String, String>();
		properties.put("id",id+"");
		properties.put("name",name);
		properties.put("color", color);
		properties.put("team", team);

		//add color attribute

	}

	public void setTeam(String team) {
		this.team = team;
		properties.replace("team",team);
	}

	public String getTeam() {
		return team;
	}

	public ComponentType getComponentType(){
		return ComponentType.robot;
	}

	public String display(){
		return getId()+"";
	}

	public int getId() {
		return Integer.parseInt(properties.get("id"));
	}

	public void setId(int id) {
		properties.put("id",id+"");
	}

	public String getName() {
		return properties.get("name");	
	}

	public void setName(String name) {
		properties.put("name", name);
	}

	public String toString() {
		return "{type: " + getComponentType() + ", name: " + getName()+", team: " + getTeam()+ ", id: " + getId() + ", x: " + x  + ", y: " + y + "}";
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJSON() {
		JSONObject jo = new JSONObject();
		jo.put("type","robot");
		jo.put("name",getName());
		jo.put("id",getId()+"");
		jo.put("x",""+x);
		jo.put("y",""+y);
		jo.put("team",""+team);

		return jo;
	}
}
