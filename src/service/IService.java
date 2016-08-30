package service;

//API for client
public interface IService {
	/**
	 * Put
	 */
	void put(String key, String value);

	/**
	 * Get
	 */
	String get(String key);

	/**
	 * Append
	 */
	void append(String key, String value);

	/**
	 * Delete
	 */
	void delete(String key, String value);
}
