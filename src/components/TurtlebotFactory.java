package components;

import burger.*;
import model.ComponentType;
import mqtt.Message;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import mqtt.Message;

import java.util.HashMap;
import java.util.Map;

/* This class defines the different operations that the robot can do on the grid */

public class TurtlebotFactory implements SimulationComponent {	
	
	private HashMap<String, Turtlebot> mesRobots;
	private final String turtlebotName = "burger_";	
	protected Message clientMqtt;
	protected int simulation;
	protected int debug;
	protected int display;
	protected int waittime;
	protected int seed;
	protected int field;
	protected String sttime;
	protected Team team= Team.viperes;
	
	public TurtlebotFactory(String sttime) {
		this.simulation = 1;
		this.debug = 0;
		this.display = 0;
		this.waittime = 0;
		this.sttime = sttime;
		mesRobots = new HashMap<String, Turtlebot>();
	}

	public void setMessage(Message mqtt) {
		clientMqtt = mqtt;
	}

	public void handleMessage(String topic, JSONObject content){
		if (topic.contains("configuration/nbRobot")) {
           	initRobots(content);
        }
        else if (topic.contains("configuration/debug")) {
           	debug = Integer.parseInt((String)content.get("debug"));
        }
        else if (topic.contains("configuration/field")) {
           	field = Integer.parseInt((String)content.get("field"));
        }
        else if (topic.contains("configuration/seed")) {
           	seed = Integer.parseInt((String)content.get("seed"));
        }
        else if (topic.contains("configuration/display")) {
           	display = Integer.parseInt((String)content.get("display"));
        }
        else if (topic.contains("configuration/simulation")) {
           	simulation = Integer.parseInt((String)content.get("simulation"));
        }
        else if (topic.contains("configuration/waittime")) {
    	    waittime = Integer.parseInt((String)content.get("waittime"));
        }
	}

	public void moveRobot(Turtlebot t) {
		JSONObject jo = new JSONObject();
       	jo.put("name", t.getName());
       	jo.put("action", "move");
       	jo.put("step", "1");
		jo.put("team", t.getTeam());

		clientMqtt.publish(t.getName() +"/action", jo.toJSONString());
	}

	public void schedule(int nbStep) {
		for(int i = 0; i < nbStep; i++){
			for(Turtlebot t: mesRobots.values()) {
				updateGrid(t);
				moveRobot(t);
				HunterTurtleBot2 rb = (HunterTurtleBot2) t;
				//System.out.println("Robot : "+rb);
				//System.out.println("RobotKilledID : "+rb.getKilledRobotId());
				//System.out.println("test "+mesRobots.get(rb.getKilledRobotId()) );
				//bloc suivant à utiliser si on veut faire mourir les robots
				/*if (rb.getKilledRobotId()!=0){
					if (mesRobots.get("burger_"+rb.getKilledRobotId())!=null){
							mesRobots.remove("burger_"+rb.getKilledRobotId());
							System.out.println("Robot "+rb.getProie()+rb.getKilledRobotId()+"has been killed by"+rb.getTeam()+rb.getId());
							System.out.println("my_robots now : "+mesRobots);
							break;
					}
					((HunterTurtleBot2) t).setKilledRobotId(0);
				}*/
			}
			try {
				Thread.sleep(waittime);
			}catch(InterruptedException ie){
				System.out.println(ie);
			}

		}
		for(Turtlebot t: mesRobots.values()) {
			t.setGoalReached(true);
		}
		System.out.println("END");
	}

	public void updateGrid(Turtlebot t) {
		JSONObject jo = new JSONObject();
       	jo.put("name", t.getName());
      	jo.put("field",t.getField()+"");
       	jo.put("x",t.getX()+"");
       	jo.put("y",t.getY()+"");
		jo.put("team", t.getTeam());
		jo.put("id", t.getId()+"");

		System.out.println("FROM FACTORY : robot"+t.getId()+" team : "+t.getTeam());

		clientMqtt.publish("robot/grid", jo.toJSONString());
	}

	public void next(int id) {
		if(!finish()) {
			int next = id + 1;
			while(true) {
				JSONObject message = new JSONObject();
				if (next == mesRobots.size() + 2)
					next = 2;
				String stn = turtlebotName + next;
				Turtlebot t = mesRobots.get(stn);
				if(t.isGoalReached()) {
					next++;
					continue;
				}
				JSONObject robot = new JSONObject();
				robot.put("id", turtlebotName + next);
				message.put("robot", robot);
				message.put("next", next);
				clientMqtt.publish(stn + "/nextStep", message.toJSONString());
				return;
			}
		}
		JSONObject msg = new JSONObject();
		msg.put("end", 1+"");
		clientMqtt.publish("/end", msg.toJSONString());
	}

	public boolean finish() {
		int i = 0;
		for (Turtlebot t : mesRobots.values())
			if (!t.isGoalReached())
				return false;
		return true;
	}

	/*public void testMove(String robotN){
		Turtlebot t = mesRobots.get(robotN);
		JSONObject pos = new JSONObject();
		pos.put("x1", t.getX()+"");
		pos.put("y1", t.getY()+"");
		t.setLocation(1,7);
		pos.put("x2", t.getX()+"");
		pos.put("y2", t.getY()+"");
		clientMqtt.publish(robotN+"/position", pos.toJSONString());
	}*/

	public void initSubscribe() {		
		clientMqtt.subscribe("configuration/nbRobot");
		clientMqtt.subscribe("configuration/debug");
		clientMqtt.subscribe("configuration/display");
		clientMqtt.subscribe("configuration/simulation");
		clientMqtt.subscribe("configuration/waittime");
		clientMqtt.subscribe("configuration/seed");
		clientMqtt.subscribe("configuration/field");
	}
//créer un robot
	public Turtlebot factory(int id, String name, Message clientMqtt, String team) {
		if (mesRobots.containsKey(name))
	    	return mesRobots.get(name);	    
	    Turtlebot turtle;
	    if(simulation == 0) {
	    	if(debug == 1) {
	    		System.out.println("Create real robot");
	    	}
	    	turtle = new RealTurtlebot(id, name, seed, field, clientMqtt, debug,null);
	    	if(debug==2 && sttime != null) {
	    		//turtle.setLog(sttime);
	    	}
	    } else {
	    	if(debug == 1) {
	    		System.out.println("Create simulated robot");
	    	}
	    	turtle = new HunterTurtleBot2(id, name, seed, field, clientMqtt, debug,team);
	    	//turtle = new SmartTurtlebot(id, name, seed, field, clientMqtt, debug,team);
	    	if(debug==2 && sttime != null) {
	    		turtle.setLog(sttime);
	    	}	    	
	    }
	    mesRobots.put(name, turtle);
	    return turtle;
	}

	public String toString() {
		String st = "{";
		for(Map.Entry<String, Turtlebot> entry : mesRobots.entrySet()) {
    		String key = entry.getKey();
    		Turtlebot value = entry.getValue();
    		st += "{" + key + " : " + value + "}";
    		st += "\n";
		}
		st += "}";
		return st;
	}

	public void initTurtle(){
		for(Turtlebot t: mesRobots.values()) {
    		t.init();
    		clientMqtt.setAppli(t);
		}
	}

	public void initTurtleGrid(){
		for(Turtlebot t: mesRobots.values()) {
    		JSONObject jo = new JSONObject();
        	jo.put("name", t.getName());
        	jo.put("field",t.getField()+"");
        	jo.put("x",t.getX()+"");
        	jo.put("y",t.getY()+"");
        	clientMqtt.publish("configuration/robot/grid", jo.toJSONString());
        }
    }

	public Turtlebot get(String idRobot) {
		return mesRobots.get(idRobot);
	}

	public void initRobots(JSONObject nbRobot) {
		int nbr = Integer.parseInt((String) nbRobot.get("nbRobot"));
		if( debug == 1) {
			System.out.println(nbr);
		}

		factory(2, turtlebotName + 2, clientMqtt,"vipere");
		factory(3, turtlebotName + 3, clientMqtt,"renard");
		factory(4, turtlebotName + 4, clientMqtt,"poule");
		factory(5, turtlebotName + 5, clientMqtt,"vipere");
		factory(6, turtlebotName + 6, clientMqtt,"renard");
		factory(7, turtlebotName + 7, clientMqtt,"poule");
		factory(8, turtlebotName + 8, clientMqtt,"vipere");
		factory(9, turtlebotName + 9, clientMqtt,"renard");
		factory(10, turtlebotName + 10, clientMqtt,"poule");
		factory(11, turtlebotName + 11, clientMqtt,"vipere");
		factory(12, turtlebotName + 12, clientMqtt,"renard");
		factory(13, turtlebotName + 13, clientMqtt,"poule");


	}
}