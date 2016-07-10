package leveldb;

import org.iq80.leveldb.DB;

public interface IStorageService {
	void put(String key, String value);
	
	void append(String key, String value);

	String get(String key);
	
	void delete(String key);
	
	void destroy();

	DB get_db();
}
