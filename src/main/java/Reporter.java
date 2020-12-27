import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import environment.Coordinate;

public class Reporter extends AbstractActor {
    private final int id;
    private final ActorRef coordinator;
    private final Coordinate location;
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static Props props(final int id, final ActorRef coordinator, final Coordinate location) {
        return Props.create(Reporter.class, () -> new Reporter(id, coordinator, location));
    }

    @Override
    public void preStart() throws Exception {
        log.info("Reporter actor {} started", id);
        coordinator.tell(new SendReportMessage(id, location), getSelf());
    }

    @Override
    public void postStop() throws Exception {
        log.info("Reporter actor {} stopped", id);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(UpdateReport.class, this::handleUpdateReport)
                .matchEquals("stop", s -> getContext().stop(getSelf()))
                .build();
    }

    private void handleUpdateReport(final UpdateReport updateReport) {
        log.info("Reporter received {} msg {} ", UpdateReport.class.getName(), updateReport.getId());
        getContext().stop(getSelf());
    }

    private Reporter(final int id, final ActorRef coordinator, final Coordinate location) {
        this.id = id;
        this.coordinator = coordinator;
        this.location = location;
    }
}
