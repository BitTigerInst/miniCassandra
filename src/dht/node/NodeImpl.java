package dht.node;

import java.net.*;
import leveldb.StorageServiceImpl;
import dht.chord.*;
import util.*;

public class NodeImpl<E> implements INode{
	private static int        RING_LEN = 1000000;
	private InetSocketAddress address;
	private FingerTable       table;
	private NodeImpl<E>       successor;
	private NodeImpl<E>    	  predecessor;
	private boolean           is_stable;
	private boolean           is_running;

	private StorageServiceImpl<E> storage_proxy;

	NodeImpl(InetSocketAddress address, FingerTable fTable) {
		this.address = address;
	}

	public static enum Operation {
		PUT,
		APPEND,
		GET,
		DELETE,
	}

	public InetAddress get_addr() {
		return this.address.getAddress();
	}

	public int get_hashcode() {
		return hash(this.hashCode());
	}

	public int get_port() {
		return this.address.getPort();
	}

	public boolean is_belong_me(String key) {
		int hashcode = hash(key.hashCode());
		if(hashcode > this.predecessor.get_hashcode() && hashcode <= this.get_hashcode()) {
			return true;
		}else {
			return false;
		}
	}

	//server execute a query from client
	//First calculate the holder for this
	//data according to the hashcode of key
	public String NodeImpl exec(String key, String value, Operation oper) {
		if(is_stable && is_running) {
			//should operate at current server
			if(is_belong_me(key)){
				switch(oper) {
				case PUT:
					storage_proxy.put(key, value);
					break;
				case APPEND:
					storage_proxy.append(key, value);
					break;
				case GET:
					return storage_proxy.get(key);
				case DELETE:
					storage_proxy.delete(key);
					break;
				}
			}else {
				int id = calculate(hash(key.hashCode()));
				send_to_other(table[id], key, value, oper);
			}
		}else if(!is_stable && is_running){
			
		}else {
			Debug.debug("Warning: this server is not alived!!");
		}
	}

	//caluculate the right or the most close server id for this hashcode
	private int calculate(int hashcode) {
		
	}

	//send the query to appropriate server
	private void send_to_other(InetSocketAddress socket, String key, String value, Operation oper) {
		
	}

	public void joinChordRing(NodeImpl<E> node) {
	    
	}

	public void leaveChordRing(NodeImpl<E> node) {
		
	}

	public int hash(int id) {
		return id % RING_LEN;
	}
}