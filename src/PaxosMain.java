/**
 * The Main class for Paxos version
 */

import java.io.*;
import java.lang.reflect.Array;
import java.lang.System.*;
import java.net.ServerSocket;
import java.util.*;

import static java.lang.System.exit;
import static java.util.Map.entry;

public class PaxosMain {
    /* Main function */
    public static void main(String[] args) {
        int curNodeId = -1;
        try {
            curNodeId = parseArgs(args);
        } catch (Exception e) {
            System.err.println(e);
            exit(1);
        }

        System.out.println("Node id = " + curNodeId);
        Node node = new Node(curNodeId);

        /* Create listen thread */
        int port = Constants.NODEID_ADDR_MAP.get(curNodeId).getPort();
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
        } catch (Exception e) {
            System.err.println("Cannot create server socket");
            exit(1);
        }
        ListenChannel listenThread = new ListenChannel(server, node);
        listenThread.start();

        /* Pick up records that might have been missed before node start */
        node.updateMissingEvents();

        Scanner sc = new Scanner(System.in);
        boolean endProgram = false;
        while (sc.hasNextLine() && endProgram == false) {
            String input = sc.nextLine();
            handleCommand(input, node);
        }
        System.out.println("Ending the program");
        node.close();
        System.out.println("Program ended");
    }

    private static void handleCommand(String input, Node node) {
        Scanner sc = new Scanner(input);
        String operation = null;
        if (sc.hasNext()) {
            operation = sc.next();
            System.out.println("operation = " + operation);
        } else {
            return;
        }
        switch (operation) {
            case "add":
                handleAddCommand(sc, node);
                break;
            case "delete":
                handleDeleteCommand(sc, node);
                break;
            case "view":
                handleViewCommand(sc, node);
                break;
            case "exit":
                node.close();
                exit(0);
                break;
            default:
                handleInvalidCommand();
                break;
        }
    }

    private static void handleDeleteCommand(Scanner sc, Node node) {
        if (!sc.hasNext()) {
            System.err.println("Appointment id to delete is missing.");
            return;
        }
        String apptDeleteId = sc.next();
        boolean deleteResult = node.deleteAppointment(apptDeleteId);
        if (!deleteResult) {
            System.out.println("Appointment does not exist");
        }
    }

    private static void handleViewCommand(Scanner sc, Node node) {
        if (sc.hasNext()) {
            node.displayCalendarAll();
        } else {
            node.displayCalendar(node.getNodeId());
        }
    }

    private static void handleAddCommand(Scanner sc, Node node) {
        String apptName = null;
        if (sc.hasNext()) {
            apptName = sc.next();
        } else {
            System.err.println("Invalid appointment name");
            return;
        }

        int day = -1;
        if (sc.hasNextInt()) {
            day = sc.nextInt();
        } else {
            System.err.println("Invalid appointment day");
            return;
        }

        int start = -1;
        if (sc.hasNextInt()) {
            start = sc.nextInt();
        } else {
            System.err.println("Invalid appointment start time");
            return;
        }

        int end = -1;
        if (sc.hasNextInt()) {
            end = sc.nextInt();
        } else {
            System.err.println("Invalid appointment end time");
            return;
        }

        ArrayList<Integer> participants = new ArrayList<>();
        if (sc.hasNextInt()) {
            participants.add(sc.nextInt());
            while (sc.hasNextInt()) {
                participants.add(Integer.valueOf(sc.nextInt()));
            }
        } else {
            System.err.println("Invalid appointment participant");
        }

        boolean addApptResult = node.addAppointment(apptName, day, start, end,
                participants);
        if (!addApptResult) {
            System.out.println("Appointment cannot be added, conflicts");
        } else {
            System.out.println("Appointment \"" + apptName + "\"added");
        }
    }

    private static void handleInvalidCommand() {
        System.err.println("Invalid command");
    }

    /**
     * parseArgs: allows one and only one argument as nodeId
     */
    private static int parseArgs(String[] args) throws Exception {
        if (args.length != 1) {
            throw new Exception("Incorrect argument number");
        }

        String nodeIdStr = args[0];
        if (!isNonnegInteger(nodeIdStr, 10)) {
            throw new Exception("Invalid nodeId");
        }

        int nodeId = Integer.parseInt(nodeIdStr);
        if (Constants.NODEID_ADDR_MAP.size() <= nodeId) {
            throw new Exception("Invalid nodeId");
        }
        return nodeId;
    }

    /** Helpers **/

    /**
     * isNonnegInteger: Check if given String s is a non-negative integer
     * @param s
     * @param radix
     * @return
     */
    public static boolean isNonnegInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (Character.digit(s.charAt(i),radix) < 0) {
                return false;
            }
        }
        return true;
    }
}
