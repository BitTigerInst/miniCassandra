package service;

//API for client
public interface IService {
	/**
	 * Put
	 */
	public void put(String key, String value);

	/**
	 * Get
	 */
	public String get(String key);

	/**
	 * Append
	 */
	public void append(String key, String value);

	/**
	 * Delete
	 */
	public void delete(String key, String value);

}
