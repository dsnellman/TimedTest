import se.sics.p2ptoolbox.simulator.cmd.impl.StartNodeCmd;
import se.sics.p2ptoolbox.simulator.dsl.SimulationScenario;
import se.sics.p2ptoolbox.simulator.dsl.adaptor.Operation1;
import se.sics.p2ptoolbox.simulator.dsl.distribution.ConstantDistribution;
import se.sics.p2ptoolbox.simulator.dsl.distribution.extra.BasicIntSequentialDistribution;
import se.sics.p2ptoolbox.simulator.dsl.distribution.extra.GenIntSequentialDistribution;
import se.sics.p2ptoolbox.util.network.impl.BasicAddress;
import se.sics.p2ptoolbox.util.network.impl.DecoratedAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Scenario {

    private static final Map<Integer, DecoratedAddress> nodeAddressMap = new HashMap<Integer, DecoratedAddress>();

    static {
        InetAddress localHost;
        try {
            localHost = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex);
        }
        nodeAddressMap.put(1, new DecoratedAddress(new BasicAddress(localHost, 12345, 1)));
        nodeAddressMap.put(2, new DecoratedAddress(new BasicAddress(localHost, 12345, 2)));
    }

    static Operation1<StartNodeCmd, Integer> startNodeOp = new Operation1<StartNodeCmd, Integer>() {

        @Override
        public StartNodeCmd generate(final Integer nodeId) {
            return new StartNodeCmd<TestComp, DecoratedAddress>() {

                @Override
                public Integer getNodeId() {
                    return nodeId;
                }

                @Override
                public Class getNodeComponentDefinition() {
                    return TestComp.class;
                }

                @Override
                public TestComp.TestInit getNodeComponentInit(DecoratedAddress aggregatorServer, Set<DecoratedAddress> bootstrapNodes) {
                    return new TestComp.TestInit(nodeAddressMap.get(nodeId), bootstrapNodes);
                }

                @Override
                public DecoratedAddress getAddress() {
                    return nodeAddressMap.get(nodeId);
                }

                @Override
                public int bootstrapSize() {
                    return 5;
                }

            };
        }
    };

    public static SimulationScenario simpleBoot() {
        SimulationScenario scen = new SimulationScenario() {
            {
                StochasticProcess startPeers = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(4, startNodeOp, new GenIntSequentialDistribution(new Integer[]{1,2,3,4}));
                        //raise(1, startNodeOp, new ConstantDistribution<Integer>(Integer.class, 2));
                    }
                };




                startPeers.start();
                terminateAfterTerminationOf(10000, startPeers);

            }
        };

        return scen;
    }
}
