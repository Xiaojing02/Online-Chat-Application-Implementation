import java.util.ArrayList;

public class User {

    String name;
    String status = "unknown";
    boolean connected = false;
    ArrayList<String> messageList =new ArrayList<String>(10);

    public void setName(String name) {

        this.name = name;

    }

    public void setStatus(String status) {

        this.status = status;

    }

    public void setConnected (boolean connected) {

        this.connected = connected;

    }

    public void setMessage(String message) {

        messageList.add(message);

    }

}
