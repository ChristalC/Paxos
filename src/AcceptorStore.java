/**
 * AcceptorStore class
 */

public class AcceptorStore {
    private int promisedId;
    private int acceptedId;
    private EventRecord acceptedER;

    /* Constructor */
    public AcceptorStore() {
        promisedId = -1;
        acceptedId = -1;
        acceptedER = null;
    }
}
