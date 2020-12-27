import environment.Coordinate;

public class Fault {

    private final int id;
    private final Coordinate faultLocation;

    public Fault(final int id, final Coordinate faultLocation) {
        this.id = id;
        this.faultLocation = faultLocation;
    }

    public int getId() {
        return id;
    }

    public Coordinate getLocation() {
        return faultLocation;
    }

    @Override
    public String toString() {
        return "Fault{" +
                "id=" + id +
                ", faultLocation=" + faultLocation +
                '}';
    }
}
