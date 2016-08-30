package leveldb;

import dht.node.NodeImpl;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import util.Debug;
import java.io.File;
import java.io.IOException;
import static org.iq80.leveldb.impl.Iq80DBFactory.*;

public class StorageServiceImpl implements IStorageService{

	private Options     options;
	private DB          db;
	private NodeImpl    node;
    private File        file;

	public StorageServiceImpl(NodeImpl node, String name) throws IOException {
		this.node = node;
		file = new File(name);
		if (!file.exists()) {
			file.mkdir();
		}
		options = new Options();
		options.createIfMissing(true);
		db = factory.open(file, options);
	}

	public DB getDb() {
		return db;
	}

	public void put(String key, String value) {
		db.put(bytes(key), bytes(value));
		Debug.debug("Node[" + node.getHashcode() + "] PUT Key:" + key + "Value:" + value);
	}

	public void append(String key, String content) {
		String value = get(key);
		if(value == null) {
			put(key, content);
		} else {
			value = value + content;
		}
		Debug.debug("Node[" + node.getHashcode() + "] APPEND Key:" + key + "Value:" + value);
	}

	public String get(String key) {
		String value = asString(db.get(bytes(key)));
		Debug.debug("Node[" + node.getHashcode() + "] GET Key:" + key + "Value:" + value);
		return value;
	}

	public void delete(String key) {
		db.delete(bytes(key));
		Debug.debug("Node[" + node.getHashcode() + "] DELETE Key:" + key);
	}

	public void destroy() {
		try {
            file.delete();
			db.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
}
