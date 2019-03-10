/**
 * Learner class
 */

import java.util.*;

public class Learner {
    private Node node;
    private int nodeId;

    /* Constructor */
    public Learner(Node node_obj) {
        node = node_obj;
        nodeId = node_obj.getNodeId();
    }

    public void handleLearnerNotice(PaxosMessage msg) {
        int logId = msg.getLogId();
        EventRecord er = msg.getER();
//        System.out.println("er = " + er);
        try {
            node.addToAllEvents(logId, er);
        } catch (Exception e) {
            System.err.println("learner addToAllEvents failed + " + e);
        }
        try {
            node.updateCalendar(er);
        } catch (Exception e) {
            System.err.println("learner updateCalendar failed + " + e);
        }
    }

    public void handleLearnerRequest(PaxosMessage msg) {
        NodeAddress addr = Constants.NODEID_ADDR_MAP.get(msg.getNodeId());
        int requestedLogId = msg.getLogId();

        ArrayList<EventRecord> learnedER = node.getAllEvents();
        if (learnedER.size() > requestedLogId &&
                learnedER.get(requestedLogId) != null) {
            PaxosMessage replyMsg = new PaxosMessage(
                    PaxosMessageType.LEARNER_NOTICE, -1, requestedLogId,
                    -1, nodeId, learnedER.get(requestedLogId));
            try {
                replyMsg.sendToAddr(addr.getIp(), addr.getPort());
            } catch (Exception e) {
                ;
            }
        }
    }
}