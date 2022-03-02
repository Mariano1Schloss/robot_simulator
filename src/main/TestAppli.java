package main;

import environnementburger.GridManagement;
import mqtt.Message;
import mqtt.Mqtt;
import mqtt.NoMqtt;
import java.awt.Color;
import org.json.simple.JSONObject;
import components.TurtlebotFactory;
import java.io.File;
import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;  

public class TestAppli {
	
	protected static int WAITTIME;
	protected static int MQTT;
	protected static int DISPLAY;
	protected static int DEBUG;
	protected static int SIMULATION;
	protected static int DISPLAYWIDTH;
	protected static int DISPLAYHEIGHT;
	protected static String DISPLAYTITLE;
	protected static int NBROBOT;
	protected static int NBOBSTACLE;
	protected static int ROWS;
	protected static int SEED;
	protected static int FIELD;
	protected static int COLUMNS;
	protected static Color COLORROBOT;
	protected static Color COLORGOAL;
	protected static Color COLOROBSTACLE;
	protected static Color COLOROTHER;

	public static void main(String[] args) throws Exception {
		String sttime = "log-"+java.time.LocalDateTime.now();
		File f = new File(sttime);
		f.mkdir();
		IniFile ifile= new IniFile("prop.ini");
		TestAppli.MQTT = ifile.getIntValue("configuration","mqtt");
		TestAppli.WAITTIME = ifile.getIntValue("configuration","waittime");
		TestAppli.DISPLAY = ifile.getIntValue("configuration","display");
		TestAppli.DEBUG = ifile.getIntValue("configuration","debug");
		TestAppli.SIMULATION = ifile.getIntValue("configuration","simulation");
		TestAppli.NBROBOT =  ifile.getIntValue("configuration", "robot");
		TestAppli.NBOBSTACLE =  ifile.getIntValue("configuration", "obstacle");
		TestAppli.SEED =  ifile.getIntValue("configuration", "seed");
		TestAppli.FIELD =  ifile.getIntValue("configuration", "field");
		TestAppli.ROWS =  ifile.getIntValue("environment", "rows");
		TestAppli.COLUMNS =  ifile.getIntValue("environment", "columns");
		if(TestAppli.DISPLAY == 1) { 
			TestAppli.DISPLAYWIDTH =  ifile.getIntValue("display","width");
			TestAppli.DISPLAYHEIGHT = ifile.getIntValue("display","height");
			TestAppli.DISPLAYTITLE = ifile.getStringValue("display","title");
			TestAppli.COLORROBOT = ifile.getColorValue("color","robot");
			TestAppli.COLORGOAL = ifile.getColorValue("color","goal");
			TestAppli.COLOROBSTACLE = ifile.getColorValue("color","obstacle");
			TestAppli.COLOROTHER = ifile.getColorValue("color","other");
		}
		
		Message mqttClient;
		if(TestAppli.MQTT == 1) {
			mqttClient = new Mqtt("android", TestAppli.DEBUG);
		}
		else {
			mqttClient = new NoMqtt("android", TestAppli.DEBUG);
		}

		GridManagement env = new GridManagement();
		env.setMessage(mqttClient);		
		mqttClient.setAppli(env);		
		env.initSubscribe();	
		
		TurtlebotFactory tf = new TurtlebotFactory(sttime);
		tf.setMessage(mqttClient);		
		mqttClient.setAppli(tf);
		tf.initSubscribe();

		JSONObject mymes = new JSONObject();
		mymes.put("display", TestAppli.DISPLAY+"");
		mqttClient.publish("configuration/display",mymes.toJSONString());
		mymes = new JSONObject();
		mymes.put("debug", TestAppli.DEBUG+"");
		mqttClient.publish("configuration/debug",mymes.toJSONString());	
		mymes = new JSONObject();
		mymes.put("waittime", TestAppli.WAITTIME+"");
		mqttClient.publish("configuration/waittime",mymes.toJSONString());	
		mymes = new JSONObject();
		mymes.put("simulation", TestAppli.SIMULATION+"");
		mqttClient.publish("configuration/simulation",mymes.toJSONString());	
		mymes = new JSONObject();
		mymes.put("seed", TestAppli.SEED+"");
		mqttClient.publish("configuration/seed",mymes.toJSONString());
		mymes = new JSONObject();
		mymes.put("field", TestAppli.FIELD+"");
		mqttClient.publish("configuration/field",mymes.toJSONString());
		if(TestAppli.DISPLAY == 1) { 
			mymes = new JSONObject();
			mymes.put("displaywidth", TestAppli.DISPLAYWIDTH+"");
			mqttClient.publish("display/width",mymes.toJSONString());
			mymes = new JSONObject();
			mymes.put("displayheight", TestAppli.DISPLAYHEIGHT+"");	
			mqttClient.publish("display/height",mymes.toJSONString());
			mymes = new JSONObject();
			mymes.put("displaytitle", TestAppli.DISPLAYTITLE);
			mqttClient.publish("display/title",mymes.toJSONString());
			mymes = new JSONObject();
			mymes.put("color", Integer.toString(TestAppli.COLORROBOT.getRGB()));
			mqttClient.publish("display/robot",mymes.toJSONString());
			mymes = new JSONObject();
			mymes.put("color", Integer.toString(TestAppli.COLORGOAL.getRGB()));
			mqttClient.publish("display/goal",mymes.toJSONString());
			mymes = new JSONObject();
			mymes.put("color", Integer.toString(TestAppli.COLOROBSTACLE.getRGB()));
			mqttClient.publish("display/obstacle",mymes.toJSONString());
			mymes = new JSONObject();
			mymes.put("color", Integer.toString(TestAppli.COLOROTHER.getRGB()));
			mqttClient.publish("display/other",mymes.toJSONString());
		}
		mymes = new JSONObject();
		mymes.put("rows", TestAppli.ROWS+"");
		mqttClient.publish("environment/rows",mymes.toJSONString());	
		mymes = new JSONObject();
		mymes.put("columns", TestAppli.COLUMNS+"");
		mqttClient.publish("environment/columns",mymes.toJSONString());
		mymes = new JSONObject();
		mymes.put("nbObstacle", TestAppli.NBOBSTACLE+"");
		mqttClient.publish("configuration/nbObstacle", mymes.toJSONString());		
		mymes = new JSONObject();
		mymes.put("columns", TestAppli.COLUMNS+"");
		mymes.put("rows", TestAppli.ROWS+"");
		mqttClient.publish("environment/grid",mymes.toJSONString());	
		mymes = new JSONObject();
		mymes.put("nbRobot", TestAppli.NBROBOT+"");
		mqttClient.publish("configuration/nbRobot", mymes.toJSONString());
		tf.initTurtle();
		env.publishInitRobot();
		env.publishGridSize();
		tf.initTurtleGrid();
		tf.schedule(100);	
		/*tf.publishRobotInit();
		try {
		    Thread.sleep(TestAppli.WAITTIME);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
		tf.testMove("burger_5");
		try {
		    Thread.sleep(TestAppli.WAITTIME);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
		env.moveRobot(5,3,7,1,7);
		if(TestAppli.DISPLAY == 1)
			env.refresh();
		if(TestAppli.DEBUG == 1) { 
			env.displayGrid();
		}
		System.out.println(tf);*/
	}
}
