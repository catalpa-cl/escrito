package de.unidue.ltl.edu.scoring.features.general;

import java.util.LinkedHashMap;
import java.util.Map;


public class LRUCache<K, V> extends LinkedHashMap<K, V> {

	private static final long serialVersionUID = 1L;
	private int size;

	public LRUCache(int size) {
		super(size, 0.75f, true);
		this.size = size;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > size;
	}

	public static <K, V> LRUCache<K, V> newInstance(int size) {
		return new LRUCache<K, V>(size);
	}

}
