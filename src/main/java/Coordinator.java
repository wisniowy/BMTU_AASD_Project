import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import environment.Coordinate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

public class Coordinator extends  AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Map<ActorRef, ConservatorStatus> conservatorsStatus = new HashMap<>();
    private final Map<ActorRef, Coordinate> conservatorsLastLocation = new HashMap<>();

    private final List<Fault> unresolvedFaults = new ArrayList<>();
    private final Map<Integer, ActorRef> faultIdToReporter = new HashMap<>();

    private final Cancellable tick = getContext().getSystem().scheduler().scheduleAtFixedRate(Duration.ofSeconds(5),
            Duration.ofSeconds(2), () -> getSelf().tell("handle_reports", getSelf()), getContext().getDispatcher());

    private int faultCounter = 1;

    private enum ConservatorStatus {
        BUSY,
        FREE
    }

    public static Props props() {
        return Props.create(Coordinator.class, Coordinator::new);
    }

    @Override
    public void preStart() throws Exception {
        log.info("Coordinator actor started");
     }

    @Override
    public void postStop() throws Exception {
        log.info("Coordinator actor stopped");
        tick.cancel();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("stop", s -> getContext().stop(getSelf()))
                .matchEquals("handle_reports", s -> handleReports())
                .match(UpdateLocationMessage.class, this::handleUpdateLocationMessage)
                .match(FaultResolvedMessage.class, this::handleFaultResolvedMessage)
                .match(SendReportMessage.class, this::handleSendReportMessage)
                .build();
    }

    private void handleReports() {
        ListIterator<Fault> iter = unresolvedFaults.listIterator();

        while(iter.hasNext()){
            boolean handleReportResult = handleReport(iter.next());

            if(handleReportResult){
                iter.remove();
            } else {
                log.info("There is no free conservators");
                break;
            }
        }
    }

    /**
     * Assigning report to conservator
     *
     * @param fault - report message
     * @return true if handling finished with success (conservator was assigned to report) or false otherwise
     * (there is no free conservators)
     */
    private boolean handleReport(final Fault fault) {
        final Coordinate destCoordinate = fault.getLocation();

        final Optional<ActorRef> nearestConservator = conservatorsLastLocation.entrySet().stream()
                .filter(c -> conservatorsStatus.get(c.getKey()) == ConservatorStatus.FREE)
                .map(x -> new HashMap.SimpleEntry<>(x.getKey(), destCoordinate.distanceFrom(x.getValue())))
                .min(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey);

        if (!nearestConservator.isPresent()) {
            return false;
        }

        final ActorRef nearestConservatorRef = nearestConservator.get();
        nearestConservatorRef.tell(new RequestAssistanceMessage(fault), getSelf());
        conservatorsStatus.put(nearestConservatorRef, ConservatorStatus.BUSY);

        return true;
    }

    private void handleFaultResolvedMessage(final FaultResolvedMessage faultResolvedMessage) {
        logReceivingMsg(faultResolvedMessage.toString());

        final int faultId = faultResolvedMessage.getFaultId();
        final ActorRef reporter = faultIdToReporter.get(faultId);
        reporter.tell(new UpdateReport(0), getSelf());
        faultIdToReporter.remove(faultId);
    }

    private void handleUpdateLocationMessage(final UpdateLocationMessage updateLocationMessage) {
        logReceivingMsg(updateLocationMessage.toString());

        final ActorRef conservator = getSender();

        if (!conservatorsStatus.containsKey(conservator)) {
            conservatorsStatus.put(conservator, ConservatorStatus.FREE);
        }

        conservatorsLastLocation.put(conservator, updateLocationMessage.getLocation());
    }

    private void handleSendReportMessage(final SendReportMessage report) {
        logReceivingMsg(report.toString());
        final int faultId = faultCounter++;
        unresolvedFaults.add(new Fault(faultId, report.getLocation()));
        faultIdToReporter.put(faultId, getSender());
    }

    private void logReceivingMsg(final String msg) {
        log.info("Coordinator received msg: {}", msg);
    }

    private Coordinator() {
    }
}
