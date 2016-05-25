package leveldb;

public interface IStorageService {
	public void put(String key, String value);
	
	public void append(String key, String value);
	
	public String get(String key);
	
	public void delete(String key);
	
	public void destroy();
}
