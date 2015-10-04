import se.sics.p2ptoolbox.simulator.dsl.SimulationScenario;
import se.sics.p2ptoolbox.simulator.timed.api.TimedLauncher;

public class Main {


    public static void main(String[] args) {


        SimulationScenario scenario = Scenario.simpleBoot();
        scenario.setSeed(1234);
        scenario.simulate(TimedLauncher.class);
    }
}
