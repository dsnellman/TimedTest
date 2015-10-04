import org.slf4j.LoggerFactory;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;
import se.sics.p2ptoolbox.simulator.timed.api.Timed;
import se.sics.p2ptoolbox.simulator.timed.api.TimedControler;
import se.sics.p2ptoolbox.simulator.timed.api.TimedControlerBuilder;
import se.sics.p2ptoolbox.util.network.impl.DecoratedAddress;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class TestComp extends ComponentDefinition {


    private static final org.slf4j.Logger log = LoggerFactory.getLogger(TestComp.class);

    private Positive<Network> network = requires(Network.class);
    private Positive<Timer> timer = requires(Timer.class);

    private DecoratedAddress self;
    private List<DecoratedAddress> bootstrapNodes;

    private TimedControler tc;
    private Random rand;

    private Component Child;

    public TestComp(TestInit init) {
        this.self = init.self;
        log.debug("initiating test node:{}", init.self);
        tc = init.tcb.registerComponent(self.getId(), this);


        this.rand = new Random(self.getId());
        this.bootstrapNodes = new ArrayList<DecoratedAddress>(init.bootstrapNodes);

        Child = create(ChildComp.class, new ChildComp.ChildInit(self, bootstrapNodes, init.tcb));
        connect(Child.getNegative(Timer.class), timer);
        connect(Child.getNegative(Network.class), network);

        subscribe(handleStart, control);
        //subscribe(handleNetPing, network);
        //subscribe(handleNetPong, network);
        subscribe(handleTimeout, timer);
    }

    private Handler<Start> handleStart = new Handler<Start>() {

        @Override
        public void handle(Start event) {
            tc.advance(TestComp.this, rand.nextInt(10));
            log.debug("starting test node:{}", self);
            schedulePeriodicShuffle();
        }

    };

    private Handler handleTimeout = new Handler<StatusTimeout>() {

        @Override
        public void handle(StatusTimeout timeout) {
            tc.advance(TestComp.this, rand.nextInt(10));
            log.debug("{} time:{}", self, System.currentTimeMillis());
        }
    };

    private void schedulePeriodicShuffle() {
        SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(1000, 1000);
        StatusTimeout sc = new StatusTimeout(spt);
        spt.setTimeoutEvent(sc);
        trigger(spt, timer);
    }

    public static class TestInit extends Init<TestComp> implements Timed {

        public TimedControlerBuilder tcb;
        public final DecoratedAddress self;
        public final Set<DecoratedAddress> bootstrapNodes;

        public TestInit(DecoratedAddress self, Set<DecoratedAddress> bootstrapNodes) {
            this.self = self;
            this.bootstrapNodes = bootstrapNodes;
        }

        @Override
        public void set(TimedControlerBuilder tcb) {
            this.tcb = tcb;
        }
    }

    public static class StatusTimeout extends Timeout {

        public StatusTimeout(SchedulePeriodicTimeout spt) {
            super(spt);
        }
    }

}
