public class FaultResolvedMessage {

    private final int faultId;

    public FaultResolvedMessage(int faultId) {
        this.faultId = faultId;
    }

    public int getFaultId() {
        return faultId;
    }

    @Override
    public String toString() {
        return "FaultResolvedMessage{" +
                "faultId=" + faultId +
                '}';
    }
}
