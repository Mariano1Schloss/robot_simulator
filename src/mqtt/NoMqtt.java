package mqtt;

import main.TestAppli;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/* Inspired from: https://gist.github.com/m2mIO-gister/5275324 */
/* This class creates an mqtt client and parses received messages */
public class NoMqtt extends Message{
    protected Map<String,String> topics;   
    protected Set<String> subtopics;       

    public NoMqtt(String name, int debug) {
        super(name, debug);
        topics = new HashMap<String,String>();
        subtopics = new HashSet<String>();
    }    

    /* Publish to topic */
    public void publish(String topic, String publishMessage) {
        super.publish(topic,publishMessage);
        topics.put(topic,publishMessage);
        if(subtopics.contains(topic)) {
            try{
                messageArrived(topic,publishMessage);
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* Subscribe to topic */
    public void subscribe(String topic) {
        super.subscribe(topic);
        subtopics.add(topic);
    }

    /* Unsubscribe to topic */
    public void unsubscribe(String topic) {
        super.subscribe(topic);
        subtopics.remove(topic);   
    }
}