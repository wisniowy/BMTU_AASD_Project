import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import environment.Area;
import environment.Coordinate;
import environment.ShortestPathFinder;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;

public class Conservator extends AbstractActor {
    private final int id;
    private final ActorRef coordinator;
    private final Coordinate location;
    private final Area area;
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Cancellable tick = getContext().getSystem().scheduler().scheduleAtFixedRate(Duration.ofSeconds(1),
            Duration.ofSeconds(2), () -> getSelf().tell("update_location", getSelf()), getContext().getDispatcher());

    private Optional<Fault> currentFault = Optional.empty();
    private InternalStatus status = InternalStatus.FREE;

    private enum InternalStatus {
        ON_THE_WAY,
        RESOLVING_FAULT,
        FREE
    }

    public static Props props(final int id, final ActorRef coordinator, final Coordinate location, final Area area) {
        return Props.create(Conservator.class, () -> new Conservator(id, coordinator, location, area));
    }

    @Override
    public void preStart() throws Exception {
        log.info("Conservator actor {} started", id);
    }

    @Override
    public void postStop() throws Exception {
        log.info("Conservator actor {} stopped", id);
        tick.cancel();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("update_location", s -> handleUpdateLocation())
                .matchEquals("fault_resolved", s -> handleFaultResolved())
                .match(RequestAssistanceMessage.class, this::handleRequestAssistanceMessage)
                .matchEquals("stop", s -> getContext().stop(getSelf()))
                .build();
    }

    private void handleUpdateLocation() {
        if (status == InternalStatus.ON_THE_WAY) {
            final Coordinate destLocation = currentFault.get().getLocation();
            if (destLocation.equals(location)) {
                startWorkingOnReport();
            } else {
                move(destLocation);
            }
        } else if (status == InternalStatus.FREE) {
            coordinator.tell(new UpdateLocationMessage(location), getSelf());
        }
    }

    private void handleFaultResolved() {
        coordinator.tell(new FaultResolvedMessage(currentFault.get().getId()), getSelf());
        currentFault = Optional.empty();
    }

    private void handleRequestAssistanceMessage(final RequestAssistanceMessage requestAssistanceMessage) {
        logReceivingMsg(requestAssistanceMessage.toString());
        currentFault = Optional.of(requestAssistanceMessage.getFault());

        log.info("On the way to {}", currentFault.get());
        log.warning(ShortestPathFinder.solve(area, location, requestAssistanceMessage.getFault().getLocation()).toString());
        status = InternalStatus.ON_THE_WAY;
    }

    private void startWorkingOnReport() {
        log.info("Started working on {}", currentFault.get());
        final Random random = new Random();

        getContext().getSystem().scheduler()
                .scheduleOnce(
                        Duration.ofSeconds(random.ints(3, (11)).findFirst().getAsInt()),
                        () -> getSelf().tell("fault_resolved", getSelf()),
                        getContext().getDispatcher());

        status = InternalStatus.RESOLVING_FAULT;
    }

    private void move(final Coordinate destLocation) {
        final int x_dist = destLocation.getX() - location.getX();
        final int y_dist = destLocation.getY() - location.getY();

        int new_x = location.getX();
        int new_y = location.getY();

        if (Math.abs(x_dist) >= Math.abs(y_dist)) {
            if (destLocation.getX() > location.getX()) {
                new_x++;
            } else {
                new_x--;
            }
        } else {
            if (destLocation.getY() > location.getY()) {
                new_y++;
            } else {
                new_y--;
            }
        }

        location.setX(new_x);
        location.setY(new_y);
    }

    private Conservator(final int id, final ActorRef coordinator, final Coordinate location, Area area) {
        this.id = id;
        this.coordinator = coordinator;
        this.location = location;
        this.area = area;
    }

    private void logReceivingMsg(final String msg) {
        log.info("Conservator received msg: {}", msg);
    }
}
