import client.Client;
import dht.chord.*;
import java.util.ArrayList;
import dht.node.NodeImpl.Operation;
import org.apache.log4j.Logger;
import org.junit.Test;

public class funcTest1 {

    public static final Logger logger = Logger.getLogger(funcTest1.class);

    public static void init(ArrayList<String> ipList, ArrayList<Integer> portList) {
        ipList.add("127.0.0.1");
        ipList.add("127.0.0.1");
        //ipList.add("127.0.0.1");
        portList.add(9245);
        portList.add(9246);
        //portList.add(9247);
    }

    @Test
    public void funcTest1() throws Exception {
        //1.init cluster
        int SERVERS = 2;
        int CLIENT_ID = 1;
        int RING_LEN = 10;
        ArrayList<String> ipList = new ArrayList<>();
        ArrayList<Integer> portList = new ArrayList<>();
        init(ipList, portList);

        //2.create a cluster
        Chord.CreateCluster(SERVERS, RING_LEN, ipList, portList);

        //3.create a client
        Client client = Client.CreateClient(CLIENT_ID, ipList, portList);

        //4.do some operation and verify it
        client.exec("Tom", "old:", Operation.PUT);
        client.exec("Tom", "25", Operation.APPEND);
        String tom_old = client.exec("Tom", null, Operation.GET);
        if (!tom_old.equals("old:25")) {
            logger.error("expect: old:25, got: " + tom_old);
            return;
        }
        client.exec("Curry", "Team:", Operation.PUT);
        client.exec("Tom", "Team:Rockets", Operation.PUT);
        client.exec("Curry", "Warriors", Operation.APPEND);
        String curry_team = client.exec("Curry", null, Operation.GET);
        if (!curry_team.equals("Team:Warriors")) {
            logger.error("expect: Team:Warriors, got: " + curry_team);
            return;
        }

        //5.destroy the cluster
        Chord.DestroyCluster();
        logger.info("passed basic operation test");
    }
}