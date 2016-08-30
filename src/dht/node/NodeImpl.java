package dht.node;

import dht.chord.FingerTable;
import leveldb.IStorageService;
import leveldb.StorageServiceImpl;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.impl.Iq80DBFactory;
import rpc.IRpcMethod;
import rpc.RpcFramework;
import util.Debug;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;

public class NodeImpl extends Thread implements INode, IRpcMethod, Serializable {
	private int               RING_LEN;
    private int               bits;
	private InetSocketAddress address;
	private FingerTable       table;
	private InetSocketAddress predecessor;
	private boolean           isStable;
	private boolean           isRunning;
	private IRpcMethod        Itrans;
	private IStorageService   storageProxy;
	private int               hashcode;
	private RpcFramework      rpcFramework;
	private static int        fileCount = 0;
	private static int        MAX_WAIT_STABLE_CYCLE = 30;
	private static int        WAIT_STABLE_CYCLE_MS = 1000;

	private NodeImpl(InetSocketAddress address, int bits) throws Exception {
        this.bits = bits;
		this.RING_LEN = 1 << bits;
		this.address = address;
		this.hashcode = hashing(this.hashCode());
        table = new FingerTable();
		Itrans = this;
		storageProxy = new StorageServiceImpl(this, generateFileName());
		rpcFramework = new RpcFramework(true);
		Debug.debug("Create Server Address:" + address.getHostName() + ", port:" + address.getPort());
	}
	
	public enum Operation implements Serializable {
		PUT,
		APPEND,
		GET,
		DELETE,
	}

	public enum Type implements Serializable {
		JOIN,
		LEAVE,
	}

    @Override
    public void run() {
        try {
            rpcFramework.export(Itrans, address.getPort());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	private String generateFileName() {
		return getAddr().toString().substring(1) + "_" + fileCount++;
	}

	public static NodeImpl createNode(String ip, int port, int bits) throws Exception {
		InetSocketAddress address = new InetSocketAddress(ip, port);
		return new NodeImpl(address, bits);
	}

	public void destroyNode() {
		this.storageProxy.destroy();
		this.rpcFramework.destroy();
	}
	
	public InetAddress getAddr() {
		return address.getAddress();
	}

	public int getHashcode() {
		return hashcode;
	}

	public int getPort() {
		return address.getPort();
	}

    /**
     * hashcode in (left, right) should sharding to current node
     */
	private boolean inRange(int id, int left, int right) {
		if ((left == right && left == id) ||
                (left < right && id > left && id <= right) ||
                (left > right && ((id >= 0 && id <= right) || (id < RING_LEN && id > left)))) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * judge if this operation own to current server
	 */
	public boolean isBelongMe(int hashcode) {
		int predeHash = hashing(predecessor.getAddress().hashCode());
		return inRange(hashcode, predeHash, this.getHashcode());
	}

	/**
	 * wait the cluster be stable
	 */
	public void waitStable() {
		int cycle = 0;
		while (!isStable && cycle < MAX_WAIT_STABLE_CYCLE) {
			try {
				Thread.sleep(WAIT_STABLE_CYCLE_MS);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			++cycle;
		}
		if (cycle==MAX_WAIT_STABLE_CYCLE) {
			throw new RuntimeException("Join Chord Ring failure");
		}
	}

	/**
	 * Firstly calculating the owner for this
	 * operation according to the hashcode of key.
	 * Secondly executing the operation.
	 */
	public String exec(String key, String value, Operation oper) {
		if (isStable && isRunning) {
			if (isBelongMe(hashing(key.hashCode()))) {
				switch (oper) {
					case PUT:
						storageProxy.put(key, value);
						break;
					case APPEND:
						storageProxy.append(key, value);
						break;
					case GET:
						return storageProxy.get(key);
					case DELETE:
						storageProxy.delete(key);
						break;
				}
			} else {
				int id = calculate(hashing(key.hashCode()));
				return sendToOther(table.getNode(id), key, value, oper);
			}
		} else if(!isStable && isRunning) {
			waitStable();
			exec(key, value, oper);
		} else {
			throw new IllegalArgumentException("Warning: this server is not alive!!");
		}
		return null;
	}

	/**
	 * calculate the correct or the most close server id for this hashcode
	 */
	private int calculate(int hashcode) {
		int listSize = table.getListSize();
		InetSocketAddress firstNode = table.getNode(0);
		InetSocketAddress lastNode = table.getNode(table.getListSize() - 1);
		int firstNodeHashcode = hashing(firstNode.getAddress().getHostAddress().hashCode());
		if(listSize == 1 || hashcode == firstNodeHashcode) {
			return 0;
		}
		int lastNodeHashcode = hashing(lastNode.getAddress().getHostAddress().hashCode());
		if(hashcode >= lastNodeHashcode || (hashcode >= 0 && hashcode < firstNodeHashcode)) {
			return listSize - 1;
		}
		for(int idx = 1; idx <= listSize - 2; idx++) {
			InetSocketAddress node = table.getNode(idx);
			InetSocketAddress nodeSucc = table.getNode(idx + 1);
			int nodeHashcode = hashing(node.getAddress().getHostAddress().hashCode());
			int nodeSuccHashcode = hashing(nodeSucc.getAddress().getHostAddress().hashCode());
			if(hashcode >= nodeHashcode && hashcode < nodeSuccHashcode) {
				return idx;
			}
		}
		return -1;
	}

	/**
	 * send the operation to appropriate server
	 * by searching finger table
	 */
	private String sendToOther(InetSocketAddress addr, String key, String value, Operation oper) {
		try {
			IRpcMethod service = RpcFramework.refer(IRpcMethod.class, addr.getHostName(), addr.getPort());
			switch(oper) {
			case GET:
				return service.rpcCallGet(key);
			default:
				service.rpcCallPad(key, value, oper);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private InetSocketAddress getSuccessor() {
		if(table.getListSize() > 0) {
			return table.getNode(0);
		} else {
			return null;
		}
	}

	
	private InetSocketAddress getPredecessor() {
		return predecessor;
	}
	
	
	public InetSocketAddress rpcGetSuccessor(int hashcode) {
		Debug.debug("calculate the successor node for the new node");
		if(isBelongMe(hashcode)) {
			return address;
		}
		int idx = calculate(hashcode);
		InetSocketAddress addr = table.getNode(idx);
		try {
			IRpcMethod service = RpcFramework.refer(IRpcMethod.class, addr.getAddress().getHostAddress(), addr.getPort());
			return service.rpcGetSuccessor(hashcode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 *  send a joinChordRing rpc method request to a known node
	 */
	public void joinChordRing(NodeImpl node) {
		try {
            if (node != null) {
                IRpcMethod service = RpcFramework.refer(IRpcMethod.class, node.getAddr().getHostAddress(), node.getPort());
                ArrayList<InetSocketAddress> fingerTable = service.rpcJoinChordRing(this);
                table.setFingerTableList(fingerTable);
            } else {
                //first node in cluster
                table.setFingerTableList(new ArrayList<>());
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
		Debug.debug("server " + this.getHashcode() + " is joining ring successfully");
        this.isRunning = true;
	}

	/**
	 *  send a leaveChordRing rpc method request to another node
	 */
	public void leaveChordRing() {
		try {
			InetSocketAddress successor = getSuccessor();
			if(successor==null) {
				return;
			}
			IRpcMethod service = RpcFramework.refer(IRpcMethod.class, 
														successor.getAddress().getHostAddress(), 
																successor.getPort());
			service.rpcLeaveChordRing();
			Debug.debug("Server " + this.getHashcode() + " is leaving the ring successfully");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public int hashing(int id) {
		return id % RING_LEN;
	}

	public void rpcCallPad(String key, String value, Operation oper) {
		exec(key, value, oper);
	}

	public String rpcCallGet(String key) {
		return exec(key, null, Operation.GET);
	}


    /**
     * when a new node want to join the cluster, it should use this method
     * call a known cluster node.
     */
	public ArrayList<InetSocketAddress> rpcJoinChordRing(NodeImpl node) throws Exception {
		ArrayList<InetSocketAddress> tableList = null;
		if(isRunning) {
			if(isStable) {
				isStable = false;
				tableList = handleJoinChordRing(node);
				isStable = true;
			} else {
				waitStable();
				Debug.debug("Warning:A server want to join a unstable chord ring!");
			}
		}
		return tableList;
	}
	
	private ArrayList<InetSocketAddress> initFingerTable(int hashCode) throws Exception {
        ArrayList<InetSocketAddress> tableList = new ArrayList<InetSocketAddress>();
        for (int i = 0, j = 1; i < bits; ++i, j *= 2) {
            int idx = hashCode + j;
            if (idx > RING_LEN) {
                idx -= RING_LEN;
            }
            tableList.add(rpcGetSuccessor(idx));
        }
		return tableList;
	}
	
	private void updateOthers(Type type) {
		for (int i = 0, j = 1; i < bits; i++, j *= 2) {
			InetSocketAddress p = rpcGetPredecessor(hashing(hashcode - j));//mod Size
			IRpcMethod service;
			try {
				service = RpcFramework.refer(IRpcMethod.class, p.getAddress().getHostAddress(), p.getPort());
				service.rpcUpdateServerFingerTable(type, p, hashcode, i);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void moveData(InetSocketAddress succAddr) {
		IRpcMethod service;
		ArrayList<String> data = new ArrayList<String>();
		try {
			service = RpcFramework.refer(IRpcMethod.class, succAddr.getAddress().getHostAddress(), succAddr.getPort());
			data = service.rpcGetRemotedatq();
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (int i = 0; i < data.size(); i+=2) {
			exec(data.get(i), data.get(i + 1), Operation.PUT);
		}
	}
	
	private ArrayList<InetSocketAddress> handleJoinChordRing(NodeImpl newNode) throws Exception {
		ArrayList<InetSocketAddress> tableList;
		InetSocketAddress succAddr = rpcGetSuccessor(hashing(newNode.hashCode()));
		IRpcMethod service;
		try {
			service = RpcFramework.refer(IRpcMethod.class, succAddr.getAddress().getHostAddress(), succAddr.getPort());
            InetSocketAddress predecessor = service.rpcGetPred();
			service.rpcChangePred(newNode.address);
            service = RpcFramework.refer(IRpcMethod.class, predecessor.getAddress().getHostAddress(), predecessor.getPort());
            service.rpcChangeSucc(newNode.address);
        } catch (Exception e) {
			e.printStackTrace();
		}
		// init finger table
		tableList = initFingerTable(hashing(newNode.hashCode()));
		// update node info
		updateOthers(Type.JOIN);
		// move the data
		moveData(succAddr);
		return tableList;
	}

	public ArrayList<String> rpcGetRemotedatq() throws IOException {
		DBIterator iterator = storageProxy.getDb().iterator();
		ArrayList<String> ret = new ArrayList<String>();
		try {
		  for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
		    String key = Iq80DBFactory.asString(iterator.peekNext().getKey());
		    String value = Iq80DBFactory.asString(iterator.peekNext().getValue());
		    if (!isBelongMe(hashing(key.hashCode()))) {
		    	ret.add(key);
		    	ret.add(value);
		    	storageProxy.delete(key);
		    }
		  }
		} finally {
		  // Make sure you close the iterator to avoid resource leaks.
		  iterator.close();
		}
		return ret;
	}
	
	public void rpcLeaveChordRing() {
		if(isRunning) {
			if(isStable) {
				isStable = false;
				handleLeaveChordRing();
				isStable = true;
			} else {
				Debug.debug("Warning:a server want to join a unstable chord ring!");
			}
		}
	}

	private void handleLeaveChordRing() {
		//1.update finger table for every server?
		updateOthers(Type.LEAVE);
		
		//2.move data from current server to other server
		// unsure about it
		// relocate_data();
	}

	public void rpcUpdateServerFingerTable(Type type, InetSocketAddress addr, int nodeHashcode, int i) {
		// the boundary is not clear
		if (inRange(nodeHashcode, this.hashcode, hashing(table.getNode(i).hashCode()))) {
			switch (type) {
				case JOIN:
					table.replace(i, addr);
					break;
				case LEAVE:
					int ithID = this.hashcode + (int) Math.pow(2, i);
					InetSocketAddress newAddr = rpcGetSuccessor(ithID);
					table.replace(i, newAddr);
					break;
			}
			InetSocketAddress next = table.getNode(0);
			IRpcMethod service;
			try {
				service = RpcFramework.refer(IRpcMethod.class, next.getAddress().getHostAddress(), next.getPort());
				service.rpcUpdateServerFingerTable(type, addr, nodeHashcode, i);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public InetSocketAddress rpcGetSucc() {
		return getSuccessor();
	}

	public InetSocketAddress rpcGetPred() {
		return getPredecessor();
	}

    @Override
    public FingerTable rpcGetFingerTable() {
        return table;
    }

    public void rpcChangePred(InetSocketAddress addr) {
		predecessor = addr;
	}

    public void rpcChangeSucc(InetSocketAddress addr) {
        table.replace(0, addr);
    }

	public InetSocketAddress rpcGetPredecessor(int hashcode) {
		InetSocketAddress succ = rpcGetSuccessor(hashcode);
		IRpcMethod service;
		InetSocketAddress pred = null;
		try {
			service = RpcFramework.refer(IRpcMethod.class, succ.getAddress().getHostAddress(), succ.getPort());
			pred = service.rpcGetPred();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pred;
	}
}