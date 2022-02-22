package burger;

import components.Turtlebot;
import model.EmptyCell;

import mqtt.Message;
import java.util.Random;
import org.json.simple.JSONObject;

public class SmartTurtlebot extends Turtlebot{
	protected Random rnd;

	public SmartTurtlebot(int id, String name, int seed, int field, Message clientMqtt, int debug) {
		super(id, name, seed, field, clientMqtt, debug);
		rnd = new Random(seed);	
	}

	public void randomOrientation() {
		double d = Math.random();
		if(d < 0.25) {
			if(orientation != Orientation.up) 
				orientation = Orientation.up;
			else 
				orientation = Orientation.down;
		}
		else if(d < 0.5) {
			if(orientation != Orientation.down) 
				orientation = Orientation.down;
			else 
				orientation = Orientation.up;
		}
		else if(d < 0.75) {
			if(orientation != Orientation.left) 
				orientation = Orientation.left;
			else 
				orientation = Orientation.right;
		}
		else {
			if(orientation != Orientation.right) 
				orientation = Orientation.right;
			else 
				orientation = Orientation.left;
		}
	}

	public void move(int step) {
		for(int i = 0; i < step; i++) {
			EmptyCell[] ec = grid.getAdjacentEmptyCell(x,y);
			if(orientation == Orientation.up) {
				if(ec[3] != null) 
					moveForward();
				else 
					randomOrientation();
			}
			else if(orientation == Orientation.down) {
				if(ec[2] != null) 
					moveForward();
				else 
					randomOrientation();
			}
			else if(orientation == Orientation.right) {
				if(ec[1] != null) 
					moveForward();
				else 
					randomOrientation();
			}
			else if(orientation == Orientation.left) {
				if(ec[0] != null) 
					moveForward();
				else 
					randomOrientation();
			}
		}
	}

	public void moveLeft(int step) {
		for(int i = 0; i < step; i++){
			if(orientation == Orientation.up) {
				orientation = Orientation.left;
			}
			if(orientation == Orientation.left) {
				orientation = Orientation.down;
			}
			if(orientation == Orientation.right) {
				orientation = Orientation.up;
			}
			else {
				orientation = Orientation.right;
			}
		}
	}

	public void moveRight(int step) {
		for(int i = 0; i < step; i++){
			if(orientation == Orientation.up) {
				orientation = Orientation.right;
			}
			if(orientation == Orientation.left) {
				orientation = Orientation.up;
			}
			if(orientation == Orientation.right) {
				orientation = Orientation.down;
			}
			else {
				orientation = Orientation.left;
			}
		}	
	}

	public void moveForward() {
		//System.out.println("MOVE MOVE");
		int xo = x;
		int yo = y;
		if(orientation == Orientation.up) {
			x += 1;
			x = Math.min(x,grid.getColumns());
		}
		else if(orientation == Orientation.left) {
			y -= 1;
			y = Math.max(y,0);
		}
		else if(orientation == Orientation.right) {
			y += 1;
			y = Math.min(y,grid.getRows());
		}
		else {
			x -= 1;
			x = Math.max(x,0);
		}	
		grid.moveSituatedComponent(xo,yo,x,y);
		JSONObject robotj = new JSONObject();
		robotj.put("name", name);
		robotj.put("id", ""+id);
		robotj.put("x", ""+x);
		robotj.put("y", ""+y);
		robotj.put("xo", ""+xo);
		robotj.put("yo", ""+yo);
		//System.out.println("MOVE MOVE " + xo + " " + yo + " --> " + x + " " + y);
		clientMqtt.publish("robot/nextPosition", robotj.toJSONString());
	}

	public void moveBackward() {
		if(orientation == Orientation.up) {
			x -= 1;
		}
		if(orientation == Orientation.left) {
			y += 1;
		}
		if(orientation == Orientation.right) {
			y -= 1;
		}
		else {
			x += 1;
		}	
	}
}