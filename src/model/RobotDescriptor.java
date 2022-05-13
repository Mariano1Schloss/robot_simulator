package model;

import org.json.simple.JSONObject;
import java.util.Map;
import java.util.HashMap;

public class RobotDescriptor extends EntityDescriptor implements Situated{
	
	protected Map<String,String> properties;	
	
	public RobotDescriptor (int [] location, int id, String name, String subtype) {
		super(location);
		properties = new HashMap<String, String>();
		properties.put("id",id+"");
		properties.put("name",name);
		properties.put("subtype", subtype); // ++++
	}

	public ComponentType getComponentType(){
		return ComponentType.robot;
	}

	public String getSubtype() { return properties.get("subtype");} // +++++

	public void setSubtype(String subtype) { properties.put("subtype", subtype);} // +++++

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
		return "{type: " + getComponentType() + ", subtype: " + getSubtype() + ", name: " + getName()+ ", id: " + getId() + ", x: " + x  + ", y: " + y + "}";
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJSON() {
		JSONObject jo = new JSONObject();
		jo.put("type","robot");
		jo.put("subtype",getSubtype()); // +++
		jo.put("name",getName());
		jo.put("id",getId()+"");
		jo.put("x",""+x);
		jo.put("y",""+y);
		return jo;
	}
}
