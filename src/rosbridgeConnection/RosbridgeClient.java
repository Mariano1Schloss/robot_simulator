package rosbridgeConnection;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.net.URISyntaxException;

/* This class handles the connection with the Rosbridge websocket server */
public class RosbridgeClient {
    private WebSocketClient wsc;


    public WebSocketClient getWsc () {
        return wsc;
    }

    public void setWsc (WebSocketClient wsc) {
        this.wsc = wsc;
    }

    /* Connection to the webSocket */
    public RosbridgeClient(String host, String port) {
        try {
            wsc = new WebSocketClient(new URI("ws://" + host + ":" + port)) {
                @Override
                public void onOpen(ServerHandshake arg0) {
                    System.out.println("Connection opened");
                }

                @Override
                public void onMessage(String arg0) {

                }

                @Override
                public void onError(Exception arg0) {
                    // TODO Auto-generated method stub
                    System.out.println("Error");
                    arg0.printStackTrace();
                }

                @Override
                public void onClose(int arg0, String arg1, boolean arg2) {
                    // TODO Auto-generated method stub
                    System.out.println("Connection closed");
                }
            };
            wsc.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        wsc.close();
    }


}
