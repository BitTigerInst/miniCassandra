import client.Client;
import dht.chord.*;
import java.util.ArrayList;
import dht.node.NodeImpl.Operation;
import org.junit.Test;

public class funcTest1 {

	public static void init(ArrayList<String> ipList, ArrayList<Integer> portList) {
		ipList.add("127.0.0.1");
		ipList.add("127.0.0.1");
		ipList.add("127.0.0.1");
		portList.add(9245);
		portList.add(9246);
		portList.add(9247);
	}

	@Test
	public void funcTest1() throws Exception {
		//1.init cluster
		int SERVERS = 3;
		int CLIENT_ID = 1;
		int RING_LEN = 10;
		ArrayList<String> ipList = new ArrayList<String>();
		ArrayList<Integer> portList = new ArrayList<Integer>();
		init(ipList, portList);
		//2.create a cluster
		Chord.CreateCluster(SERVERS, RING_LEN, ipList, portList);
		//3.create a client
		Client client = Client.CreateClient(CLIENT_ID, ipList, portList);
		//4.do some operation and verify it
		client.exec("Tom", "old:", Operation.PUT);
		client.exec("Tom", "25", Operation.APPEND);
		client.exec("Curry", "Team:", Operation.PUT);
		String tom_old = client.exec("Tom", null, Operation.GET);
		if (!tom_old.equals("old:25")) {
			System.out.println("expect:old:25, but got" + tom_old);
			return;
		}
		client.exec("Tom", "Team:Rockets", Operation.PUT);
		client.exec("Curry", "Warriors", Operation.APPEND);
		String curry_team = client.exec("Curry", null, Operation.GET);
		if (!curry_team.equals("Team:Warriors")) {
			System.out.println("expect:Team:Warriors, but got" + curry_team);
			return;
		}
		//5.destroy the cluster
		Chord.DestroyCluster();
		System.out.println("passed basic operation test");
	}
}
