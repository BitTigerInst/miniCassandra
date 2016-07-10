package leveldb;

import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;
import static org.iq80.leveldb.impl.Iq80DBFactory.factory;
import java.io.File;
import java.io.IOException;
import org.iq80.leveldb.*;
import dht.node.NodeImpl;
import util.Debug;

public class StorageServiceImpl implements IStorageService{

	private Options     options;
	private DB          db;
	private NodeImpl    node;

	public StorageServiceImpl(NodeImpl node, String name) throws IOException {
		this.node = node;
		File file = new File(name);
		options = new Options();
		options.createIfMissing(true);
		db = factory.open(file, options);
	}

	@Override
	public DB get_db() {
		return db;
	}

	@Override
	public void put(String key, String value) {
		db.put(bytes(key), bytes(value));
		Debug.debug("Node[" + node.get_hashcode() + "] PUT Key:" + key + "Value:" + value);
	}

	@Override
	public void append(String key, String content) {
		String value = get(key);
		if(value==null) {
			put(key, content);
		} else {
			value = value + content;
		}
		Debug.debug("Node[" + node.get_hashcode() + "] APPEND Key:" + key + "Value:" + value);
	}

	@Override
	public String get(String key) {
		String value = asString(db.get(bytes(key)));
		Debug.debug("Node[" + node.get_hashcode() + "] GET Key:" + key + "Value:" + value);
		return value;
	}

	@Override
	public void delete(String key) {
		db.delete(bytes(key));
		Debug.debug("Node[" + node.get_hashcode() + "] DELETE Key:" + key);
	}

	@Override
	public void destroy() {
		try {
			db.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
}
