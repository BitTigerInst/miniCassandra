package leveldb;

import dht.node.NodeImpl;
import org.apache.log4j.Logger;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import static org.iq80.leveldb.impl.Iq80DBFactory.*;

public class StorageServiceImpl<T, K> implements IStorageService, Serializable {

	private Options     options;
	private DB          db;
	private NodeImpl    node;
    private File        file;
	private Logger      logger;

	public StorageServiceImpl(NodeImpl node, String name) throws IOException {
		this.node = node;
		this.logger = Logger.getLogger(StorageServiceImpl.class);
		options = new Options();
		options.createIfMissing(true);
		db = factory.open(new File(name), options);
	}

	public DB getDb() {
		return db;
	}

	public void put(String key, String value) {
		db.put(bytes(key), bytes(value));
        logger.info("Node[" + node.getHashcode() + "] PUT Key:" + key + "Value:" + value);
	}

	public void append(String key, String content) {
		String value = get(key);
		if(value != null) {
			value += content;
		}
        put(key, value);
		logger.info("Node[" + node.getHashcode() + "] APPEND Key:" + key + "Value:" + value);
	}

	public String get(String key) {
		String value = asString(db.get(bytes(key)));
		logger.info("Node[" + node.getHashcode() + "] GET Key:" + key + "Value:" + value);
		return value;
	}

	public void delete(String key) {
		db.delete(bytes(key));
		logger.info("Node[" + node.getHashcode() + "] DELETE Key:" + key);
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
