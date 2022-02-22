package mqtt;

import main.TestAppli;
import components.SimulationComponent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

/* This class creates an mqtt client and parses received messages */
public abstract class Message <SC extends SimulationComponent> {
    
    protected String name;// id session for several mqtt instances
    protected Set<SC> applis;
    protected int debug;
    
    public Message(String name, int debug) {
        this.name = name;
        this.debug = debug;
        applis = new HashSet<SC>();
    }
    
    public void setAppli(SC appli) {
    	applis.add(appli);
    }

    public void displaySubscribers(){
        for(SC appli: applis) {
            System.out.print(appli + " ");
        }
        System.out.println();
    }

    /* Publish to topic */
    public void publish(String topic, String publishMessage) {
        if(debug == 1) {
            System.out.println("Publishing the message: " + publishMessage + " to topic: " + topic);
        }
    }

    /* Subscribe to topic */
    public void subscribe(String topic) {
        if(debug == 1) {
            System.out.println("Subscribing to topic: " + topic);
        }
    }

    public void messageArrived(String topic, String message) throws Exception {
        if(debug == 1) {
            System.out.println("Message arrived");
            System.out.println("Topic: " + topic);
            System.out.println("Message: " + message.toString());
        }
        
        JSONParser jsonParser = new JSONParser();
        JSONObject content = (JSONObject) jsonParser.parse(message);
        
        for(SC appli:applis){
            appli.handleMessage(topic, content);
            //notifyAll();
        }
    }

    /* Unsubscribe to topic */
    public void unsubscribe(String topic) {
        if(debug == 1) {
            System.out.println("Unsubscribing to topic: " + topic);       
        }
    }
    
    public String getName() {
    	return name;
    }
    
    public void setName(String name) {
    	this.name = name;
    }
}