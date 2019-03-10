/**
 * PaxosMessage class
 */

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.*;

public class PaxosMessage implements Serializable {
    private PaxosMessageType msgType;
    private int pId;
    private int logId;
    private int acceptedId;
    private int nodeId;
    private EventRecord er;

    /* Constructor */
    public PaxosMessage(PaxosMessageType tp, int p_id, int log_id,
                        int accepted_id, int node_id,
                        EventRecord event_record) {
        msgType = tp;
        pId = p_id;
        logId = log_id;
        acceptedId = accepted_id;
        nodeId = node_id;
        er = event_record;
    }

    /* Getters */
    public PaxosMessageType getMsgType() {
        return msgType;
    }

    public int getPId() {
        return pId;
    }

    public int getLogId() {
        return logId;
    }

    public int getPromisedId() {
        return acceptedId;
    }

    public int getNodeId() {
        return nodeId;
    }

    public EventRecord getER() {
        return er;
    }

    public void sendToAll() throws Exception {
        for (NodeAddress addr: Constants.NODEID_ADDR_MAP.values()) {
//            System.out.println("send to ip: " + addr.getIp() + ", port: " +
//                    addr.getPort());
            sendToAddr(addr.getIp(), addr.getPort());
        }
    }

    public void sendToAddr(String ip, int port) throws Exception {
        Socket socket = new Socket(ip, port);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        try {
            oos.writeObject(this);
        } catch (Exception e) {
            System.err.println("send message exception " + e);
        } finally {
            oos.close();
        }
    }
}
