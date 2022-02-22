package components;

import mqtt.Message;

import org.json.simple.JSONObject;

public interface SimulationComponent {

	public void handleMessage(String topic, JSONObject message) ;
		
}
