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

    /**
     * handleLearnerNotice: Given msg, update allEvents and calendar stored in
     * current node
     * @param msg of type LEARNER_NOTICE
     */
    public void handleLearnerNotice(PaxosMessage msg) {
        int logId = msg.getLogId();
        EventRecord er = msg.getER();
        System.out.println("handleLearnerNotice er = " + er);
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

    /**
     * handleLearnerRequest: The function reads the given request msg, get the
     * requested logId from current node. If the current node includes the
     * logId, reply back to the requester. Else, ignore the request.
     * @param msg of type LEARNER_REQUEST
     */
    public void handleLearnerRequest(PaxosMessage msg) {
        NodeAddress addr = Constants.NODEID_ADDR_MAP.get(msg.getNodeId());
        int requestedLogId = msg.getLogId();
        System.out.println("requested logid = " + requestedLogId);

        ArrayList<EventRecord> learnedER = node.getAllEvents();
        System.out.println("learnedER.size() = " + learnedER.size());
        if (requestedLogId < learnedER.size()) {
            System.out.println("learnedER.get(requestedLogId) = " +
                    learnedER.get(requestedLogId));
        }
        if (learnedER.size() > requestedLogId &&
                learnedER.get(requestedLogId) != null) {
            System.out.println("handleLearnerRequest sending reply");
            PaxosMessage replyMsg = new PaxosMessage(
                    PaxosMessageType.LEARNER_NOTICE, -1, requestedLogId,
                    -1, nodeId, learnedER.get(requestedLogId));
            try {
                replyMsg.sendToAddr(addr.getIp(), addr.getPort());
            } catch (Exception e) {
                System.err.println("HandleLearnerRequest replyMsg failed " +
                        e);
            }
        }
    }
}