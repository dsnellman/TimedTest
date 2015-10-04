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

public class ChildComp extends ComponentDefinition {


    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ChildComp.class);

    private Positive<Network> network = requires(Network.class);
    private Positive<Timer> timer = requires(Timer.class);

    private DecoratedAddress self;
    private List<DecoratedAddress> bootstrapNodes;
    private TimedControler tc;
    private Random rand;

    public ChildComp(ChildInit init) {
        this.self = init.self;
        log.debug("initiating test node:{}", init.self);
        this.tc = init.tcb.registerComponent(self.getId(), this);


        this.rand = new Random(self.getId());
        this.bootstrapNodes = new ArrayList<DecoratedAddress>(init.bootstrapNodes);

        subscribe(handleStart, control);
        //subscribe(handleNetPing, network);
        //subscribe(handleNetPong, network);
        subscribe(handleTimeout, timer);
    }

    private Handler<Start> handleStart = new Handler<Start>() {

        @Override
        public void handle(Start event) {
            tc.advance(ChildComp.this, rand.nextInt(10));
            log.debug("starting test node:{}", self);
            schedulePeriodicShuffle();
        }

    };

    private Handler handleTimeout = new Handler<StatusTimeout>() {

        @Override
        public void handle(StatusTimeout timeout) {
            tc.advance(ChildComp.this, rand.nextInt(10));
            log.debug("{} time:{}", self, System.currentTimeMillis());
        }
    };

    private void schedulePeriodicShuffle() {
        SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(1000, 1000);
        StatusTimeout sc = new StatusTimeout(spt);
        spt.setTimeoutEvent(sc);
        trigger(spt, timer);
    }

    public static class ChildInit extends Init<ChildComp> {

        public TimedControlerBuilder tcb;
        public final DecoratedAddress self;
        public final List<DecoratedAddress> bootstrapNodes;

        public ChildInit(DecoratedAddress self, List<DecoratedAddress> bootstrapNodes, TimedControlerBuilder tcb) {
            this.self = self;
            this.bootstrapNodes = bootstrapNodes;
            this.tcb = tcb;
        }

    }

    public static class StatusTimeout extends Timeout {

        public StatusTimeout(SchedulePeriodicTimeout spt) {
            super(spt);
        }
    }

}
