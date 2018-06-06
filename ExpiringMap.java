package codingtest;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of a key-value map with expiring entries.
 * 
 * @author Dale Angus (daleangus@hotmail.com)
 *
 * @param <K>
 * @param <V>
 */
public class ExpiringMap<K, V> {

	private ConcurrentHashMap<K, Object[]> map = null;

	/**
	 * Implementation uses a ConcurrentHashMap.
	 */
	public ExpiringMap() {
		map = new ConcurrentHashMap<K, Object[]>();
	}

	/**
	 * Method is only accessible by classes in the same package
	 * 
	 * @param key
	 * @param value
	 * @param durationMs
	 *            - time duration in milliseconds before this key-value expires
	 */
	void put(K key, V value, long durationMs) {
		map.put(key, new Object[] { value, System.currentTimeMillis() + durationMs });
	}

	/**
	 * Method is only accessible by classes in the same package
	 * 
	 * @param key
	 * @return value - if entry has not expired; null if expired
	 */
	@SuppressWarnings("unchecked")
	V get(V key) {
		if (map.get(key) != null && (Long) map.get(key)[1] - System.currentTimeMillis() < 0) {
			map.remove(key); // ok to cleanup?
			return null;
		}
		Object[] o = map.get(key);
		return (o == null ? null : (V) o[0]);
	}
}
