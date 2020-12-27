import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import environment.Area;
import environment.Coordinate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.stream.IntStream;

public class SystemSupervisor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final int numberOfReporters;
    private final int numberOfConservators;

    private final HashMap<Area, Collection<ActorRef>> areas;


    @Override
    public void preStart() throws Exception {
        log.info("System-supervisor started");

        for (final Area area : areas.keySet()) {
            final Collection<ActorRef> areasActors = areas.get(area);

            final ActorRef coordinatorRef = getContext().getSystem().actorOf(
                    Coordinator.props(), String.format("area%d-coordinator", area.getId()));
            areasActors.add(coordinatorRef);

            IntStream.range(0, numberOfReporters).forEach(id -> {
                ActorRef reporterRef = getContext().actorOf(
                        Reporter.props(id, coordinatorRef, new Coordinate(id * 10, id * 10)), String.format("area%d-reporter%d", area.getId(), id));
                areas.get(area).add(reporterRef);
            });

//            IntStream.range(0, numberOfConservators).forEach(id -> {
//                ActorRef conservatorRef = getContext().actorOf(
//                        Conservator.props(id, coordinatorRef, new Coordinate(id + 4, id + 4)), String.format("area%d-conservator%d", area.getId(), id));
//                areas.get(area).add(conservatorRef);
//            });

            int id = 0;
            for (final Coordinate postition : area.getConservatorsInitialPosition()) {
                final ActorRef conservatorRef = getContext().actorOf(
                        Conservator.props(id, coordinatorRef, postition, area),
                        String.format("area%d-conservator%d", area.getId(), id++));
                areas.get(area).add(conservatorRef);
            }
        }
    }

    @Override
    public void postStop() throws Exception {

        for (final Area area : areas.keySet()) {
            final Collection<ActorRef> areasActors = areas.get(area);

        }

        log.info("System-supervisor stopped");
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("stop", this::handleStop)
                .build();
    }

    private  void handleStop(final String s) {
        for (final Area area : areas.keySet()) {
            final Collection<ActorRef> areasActors = areas.get(area);
            areasActors.forEach(actorRef -> actorRef.forward(s, getContext()));
        }
        getContext().stop(getSelf());
    }

    public static Props props(final Collection<Area> areas, final int numberOfReporters, final int numberOfConservators) {
        return Props.create(SystemSupervisor.class, () -> new SystemSupervisor(areas, numberOfReporters, numberOfConservators));
    }

    private SystemSupervisor(final Collection<Area> areas, final int numberOfReporters, final int numberOfConservators) {
        this.numberOfConservators = numberOfConservators;
        this.numberOfReporters = numberOfReporters;
        this.areas = new HashMap<Area, Collection<ActorRef>>() {{
            for(Area area : areas) {
                put(area, new ArrayList<>());
            }
        }};
    }


    //    private static int ID = 1;

//    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
//    Cancellable reporterCreator = getContext().getSystem().scheduler().scheduleAtFixedRate(
//            FiniteDuration.Zero(),
//            FiniteDuration.apply(3,TimeUnit.SECONDS),
//            getSelf(),
//            "create_reporter",
//            getContext().getSystem().dispatcher(),
//            ActorRef.noSender());

//    private final HashMap<Integer, ActorRef> reporters = new HashMap<>();
//    private ActorRef coordinator;


//

//
//    public Receive createReceive() {
//        return receiveBuilder()
//                .matchEquals("create_reporter", this::createReporter)
//                .matchEquals("stop", s -> {
//                    reporters.values().forEach(reporter -> reporter.forward(s, getContext()));
//                    reporterCreator.cancel();
//                    getContext().stop(getSelf());
//
//                })
//                .build();
//    }
//
//    private void createReporter(final String s) {
//        ActorRef reporter = context().actorOf(Reporter.props(ID, coordinator), "Reporter_" + ID);
//        reporters.put(ID++, reporter);
}

