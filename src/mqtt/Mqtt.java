package mqtt;

import main.TestAppli;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/* Inspired from: https://gist.github.com/m2mIO-gister/5275324 */
/* This class creates an mqtt client and parses received messages */
public class Mqtt extends Message implements MqttCallback {
    
//    private String broker = "tcp://10.200.3.101:1883";
//    private String broker = "tcp://tailor.cloudmqtt.com:12491";
    private String broker = "tcp://127.0.0.1:1883";  
    private MqttClient mqttClient;
    private MemoryPersistence persistence;

    public Mqtt(String name, int debug) {
        super(name, debug);
        persistence = new MemoryPersistence();        
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setKeepAliveInterval(10);
        //mqttConnectOptions.setUserName("vclydlgd");
        //mqttConnectOptions.setPassword("TpzXBXjXI7yDsqdsded".toCharArray());

        try {
            mqttClient = new MqttClient(broker, name);
            mqttClient.setCallback(this);
            mqttClient.connect(mqttConnectOptions);

        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        if(debug == 1) {
            System.out.println("Connection lost " + cause);
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        super.messageArrived(topic,message.toString());
    }
   

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    /* Publish to topic */
    public void publish(String topic, String publishMessage) {
        super.publish(topic,publishMessage);        
        int pubQos = 1;
        MqttMessage mqttMessage = new MqttMessage(publishMessage.getBytes());
        mqttMessage.setQos(pubQos);
        
        try {            
            mqttClient.publish(topic, mqttMessage);            
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    
    /* Subscribe to topic */
    
    public void subscribe(String topic) {
        super.subscribe(topic);
        try {
            int subQos = 1;
            mqttClient.subscribe(topic, subQos);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public MqttClient getMqttClient() {
        return mqttClient;
    }

    public void setMqttClient(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }
}