package burger;

import components.Turtlebot;

import mqtt.Message;
import rosbridgeConnection.RosbridgeClient;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class RealTurtlebot extends Turtlebot{

	protected RosbridgeClient clientRosbridge;
	public static int waitTimeCommunication = 300;
	public static int waitTimeAction= 4000;
	public static String ip = "10.200.3.101"; //"10.3.143.1";
	public static String port = "9090";

	public RealTurtlebot(int id, String name, int seed, int field, Message clientMqtt, int debug) {
		super(id, name, seed, field, clientMqtt, debug);
		clientRosbridge = new RosbridgeClient(RealTurtlebot.ip, RealTurtlebot.port);
	}

	public String display(){
		return super.toString();
	}

	public boolean isGoalReached(){
		return false;
	}

	public void move(int step) {
		System.out.println(step);
	}

	public void moveForward() {
		JSONObject message = new JSONObject();
		message.put("topic", "/" + name + "/robot_command");
		JSONObject msg = new JSONObject();
		msg.put("data", "forward");
		message.put("msg", msg);
		message.put("op", "publish");

		clientRosbridge.getWsc().send(message.toJSONString());
		System.out.println("Message ROS" + message.toJSONString());
		try {
			Thread.sleep(RealTurtlebot.waitTimeAction);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void moveBackward() {
		JSONObject message = new JSONObject();
		message.put("topic", "/" + name + "/robot_command");
		JSONObject msg = new JSONObject();
		msg.put("data", "backward");
		message.put("msg", msg);
		message.put("op", "publish");

		clientRosbridge.getWsc().send(message.toJSONString());

		try {
			Thread.sleep(RealTurtlebot.waitTimeAction);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/* Rotate the robot to the left */
	public void moveLeft(int step) {
		for (int i = 0; i < step; i++) {
			JSONObject message = new JSONObject();
			message.put("topic", "/" + name + "/robot_command");
			JSONObject msg = new JSONObject();
			msg.put("data", "left");
			message.put("msg", msg);
			message.put("op", "publish");

			clientRosbridge.getWsc().send(message.toJSONString());

			try {
				Thread.sleep(RealTurtlebot.waitTimeAction);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/* Rotate the robot to the right */
	public void moveRight(int step) {
		for (int i = 0; i < step; i++) {
			JSONObject message = new JSONObject();
			message.put("topic", "/" + name + "/robot_command");
			JSONObject msg = new JSONObject();
			msg.put("data", "right");
			message.put("msg", msg);
			message.put("op", "publish");

			clientRosbridge.getWsc().send(message.toJSONString());

			try {
				Thread.sleep(RealTurtlebot.waitTimeAction);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}


	/* Stop the robot */
	public void stopRobot() {
		JSONObject message = new JSONObject();
		message.put("topic", "/" + name + "/cmd_vel");
		JSONObject msg = new JSONObject();
		JSONObject linear = new JSONObject();
		linear.put("x", 0.0);
		linear.put("y", 0.0);
		linear.put("z", 0.0);
		JSONObject angular = new JSONObject();
		angular.put("x", 0.0);
		angular.put("y", 0.0);
		angular.put("z", 0.0);

		msg.put("linear", linear);
		msg.put("angular", angular);

		message.put("msg", msg);

		message.put("op", "publish");

		clientRosbridge.getWsc().send(message.toJSONString());
	}	

	/* Move the robot straight */
	public void moveRobot() {
		JSONObject message = new JSONObject();
		message.put("topic", "/" + name + "/cmd_vel");
		JSONObject msg = new JSONObject();
		JSONObject twist1 = new JSONObject();
		JSONObject linear = new JSONObject();
		linear.put("x", 0.040);
		linear.put("y", 0.0);
		linear.put("z", 0.0);
		JSONObject angular = new JSONObject();
		angular.put("x", 0.0);
		angular.put("y", 0.0);
		angular.put("z", 0.0);

		twist1.put("linear", linear);
		twist1.put("angular", angular);

		msg.put("linear", linear);
		msg.put("angular", angular);

		message.put("msg", msg);

		message.put("op", "publish");

		clientRosbridge.getWsc().send(message.toJSONString());

		try {
			Thread.sleep(RealTurtlebot.waitTimeAction);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		stopRobot();
	}

	/* Rotate the robot to the left */
	public void rotateLeft(int step) {
		for (int i = 0; i < step; i++) {
			JSONObject message = new JSONObject();
			message.put("topic", "/" + name + "/cmd_vel");
			JSONObject msg = new JSONObject();
			JSONObject linear = new JSONObject();
			linear.put("x", 0.0);
			linear.put("y", 0.0);
			linear.put("z", 0.0);
			JSONObject angular = new JSONObject();
			angular.put("x", 0.0);
			angular.put("y", 0.0);
			angular.put("z", 1.0);

			msg.put("linear", linear);
			msg.put("angular", angular);

			message.put("msg", msg);

			message.put("op", "publish");

			clientRosbridge.getWsc().send(message.toJSONString());

			/*
			 * Time required to rotate the robot 90 degrees while doing 1m/s is 1.6 seconds
			 */
			try {
				Thread.sleep(RealTurtlebot.waitTimeCommunication);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			stopRobot();
		}
	}

	/* Rotate the robot to the right */
	public void rotateRight(int step) {
		for (int i = 0; i < step; i++) {
			JSONObject message = new JSONObject();
			message.put("topic", "/" + name + "/cmd_vel");
			JSONObject msg = new JSONObject();
			JSONObject linear = new JSONObject();
			linear.put("x", 0.0);
			linear.put("y", 0.0);
			linear.put("z", 0.0);
			JSONObject angular = new JSONObject();
			angular.put("x", 0.0);
			angular.put("y", 0.0);
			angular.put("z", -1.0);

			msg.put("linear", linear);
			msg.put("angular", angular);

			message.put("msg", msg);

			message.put("op", "publish");

			clientRosbridge.getWsc().send(message.toJSONString());

			/*
			 * Time required to rotate the robot 90 degrees while doing 1m/s is 1.6 seconds
			 */
			try {
				Thread.sleep(RealTurtlebot.waitTimeCommunication);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			stopRobot();
		}
	}
}