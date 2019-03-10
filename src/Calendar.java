///**
// * Calendar class
// */
//import java.io.Serializable;
//import java.util.*;
//
//public class Calendar implements Serializable {
//    private Set<Appointment> appointments;
//
//    /* Constructor */
//    public Calendar() {
//        appointments = new TreeSet<Appointment>();
//    }
//
//    /**
//     * canInsert: determines if it is valid to add a given appointment to
//     * current calendar
//     */
//    public boolean canInsert(Appointment newAppt) {
//        Date startTime = newAppt.getStartTime();
//        Date endTime = newAppt.getEndTime();
//        for (Appointment appt: appointments) {
//            if (endTime.after(appt.getStartTime()) &&
//                    appt.getEndTime().after(startTime)) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//
//}
