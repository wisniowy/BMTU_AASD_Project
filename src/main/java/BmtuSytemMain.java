import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import akka.actor.ActorSystem;
import akka.actor.ActorRef;
import environment.Area;


public class BmtuSytemMain {

    public static void main(String[] args) throws IOException {
        ActorSystem system = ActorSystem.create("bmtu-system");

        try {
            // Create top level supervisor
            Collection<Area> areas = Arrays.asList(new Area(new File("areas/area1.txt")));
            ActorRef supervisor = system.actorOf(
                    SystemSupervisor.props(areas, 1, 1), "system-supervisor");

            System.out.println("Press ENTER to exit the system");
            System.in.read();
        } finally {
            system.terminate();
        }
    }
}
