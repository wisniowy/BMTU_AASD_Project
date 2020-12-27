public class RequestAssistanceMessage {

    final Fault fault;

    public RequestAssistanceMessage(final Fault fault) {
        this.fault = fault;
    }

    public Fault getFault() {
        return fault;
    }

    @Override
    public String toString() {
        return "RequestAssistanceMessage{" +
                "fault=" + fault +
                '}';
    }
}
