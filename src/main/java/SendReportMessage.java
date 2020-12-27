import environment.Coordinate;

public final class SendReportMessage {

    final int reporterId;
    final Coordinate location;


    public SendReportMessage(int msgId, final Coordinate location) {
        this.reporterId = msgId;
        this.location = location;
    }

    public int getReporterId() {
        return reporterId;
    }

    public Coordinate getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "SendReportMessage{" +
                "reporterId=" + reporterId +
                ", location=" + location +
                '}';
    }
}
