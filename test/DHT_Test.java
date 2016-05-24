import client.Client;
import dht.chord.*;
import java.util.ArrayList;
import dht.node.NodeImpl.Operation;

public class DHT_Test {

	public static void main(String[] args) {
		
		//1.init cluster
		int SERVER_INT = 4;
		int CLIENT_ID = 1;
		int RING_LEN = 10;
		ArrayList<String> ip_list = new ArrayList<>();
		ArrayList<Integer> port_list = new ArrayList<>();
		
		
		//2.create a cluster
		Chord cluster = Chord.CreateCluster(SERVER_INT, RING_LEN, ip_list, port_list);

		//3.create a client
		Client client = Client.CreateClient(CLIENT_ID, ip_list, port_list);
		
		//4.do some operation and verify it
		client.exec("Tom", "old:", Operation.PUT);
		client.exec("Tom", "25", Operation.APPEND);
		client.exec("Curry", "Team:", Operation.PUT);
		String tom_old = client.exec("Tom", null, Operation.GET);
		if(!tom_old.equals("old:25")){
			System.out.println("expect:old:25, but got" + tom_old);
			return;
		}
		client.exec("Tom", "Team:Rockets", Operation.PUT);
		client.exec("Curry", "Warriors", Operation.APPEND);
		String curry_team = client.exec("Curry", null, Operation.GET);
		if(!curry_team.equals("Team:Warriors")) {
			System.out.println("expect:Team:Warriors, but got" + curry_team);
			return;
		}

		System.out.println("passed basic operation test");
	}
	
}
