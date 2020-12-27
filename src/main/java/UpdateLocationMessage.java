import environment.Coordinate;

public class UpdateLocationMessage {
    final private Coordinate location;

    public UpdateLocationMessage(final Coordinate location) {
        this.location = location;
    }

    public Coordinate getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "UpdateLocationMessage{" +
                "location=" + location +
                '}';
    }
}
