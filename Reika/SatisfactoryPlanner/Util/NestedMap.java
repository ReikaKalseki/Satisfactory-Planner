/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.SatisfactoryPlanner.Util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import Reika.SatisfactoryPlanner.Util.MultiMap.CollectionType;


public class NestedMap<K, M, V> {

	private final HashMap<K, HashMap<M, V>> data = new HashMap();
	private final MultiMap<M, K> innerSet = new MultiMap(CollectionType.HASHSET);
	private final HashMap<V, Integer> valueSet = new HashMap();

	public NestedMap() {

	}

	public V put(K key, M inner, V value) {
		HashMap<M, V> map = data.get(key);
		if (map == null) {
			map = new HashMap();
			data.put(key, map);
		}
		innerSet.addValue(inner, key);
		this.addValue(value);
		return map.put(inner, value);
	}

	private void addValue(V value) {
		Integer get = valueSet.get(value);
		if (get == null) {
			valueSet.put(value, 1);
		}
		else {
			valueSet.put(value, get+1);
		}
	}

	private void removeValue(V value) {
		Integer get = valueSet.get(value);
		if (get == null)
			return;
		if (get <= 1) {
			valueSet.remove(value);
		}
		else {
			valueSet.put(value, get-1);
		}
	}

	public V get(K key, M inner) {
		HashMap<M, V> map = data.get(key);
		return map != null ? map.get(inner) : null;
	}

	public void remove(K key) {
		HashMap<M, V> map = data.remove(key);
		if (map != null) {
			for (M inner : map.keySet()) {
				innerSet.remove(inner, key);
			}
			for (V value : map.values()) {
				this.removeValue(value);
			}
		}
	}

	public V remove(K key, M inner) {
		HashMap<M, V> map = data.get(key);
		if (map != null) {
			if (map.containsKey(inner)) {
				innerSet.remove(inner, key);
				this.removeValue(map.get(inner));
			}
			return map.remove(inner);
		}
		return null;
	}

	public void removeAll(M inner) {
		Collection<K> keys = innerSet.get(inner);
		for (K key : keys) {
			this.remove(key, inner);
		}
	}

	public int size() {
		return valueSet.size();
	}

	public void putAll(NestedMap map) {
		data.putAll(map.data);
		innerSet.putAll(map.innerSet);
		valueSet.putAll(map.valueSet);
	}

	public void clear() {
		data.clear();
		innerSet.clear();
		valueSet.clear();
	}

	public Set<K> keySet() {
		return data.keySet();
	}

	public Collection<M> innerSet() {
		return innerSet.keySet();
	}

	public Collection<V> values() {
		return Collections.unmodifiableCollection(valueSet.keySet());
	}

	public boolean isEmpty() {
		return data.isEmpty();
	}

	@Override
	public String toString() {
		return data.toString();
	}

	public boolean containsValue(V value) {
		return valueSet.containsKey(value);
	}

	public boolean containsKey(K key) {
		return data.containsKey(key);
	}

	public boolean containsInnerKey(M inner) {
		return innerSet.containsKey(inner);
	}

	public Collection<M> getAllKeysIn(K key) {
		HashMap<M, V> map = data.get(key);
		return map != null ? map.keySet() : null;
	}

	public Collection<V> getAllValuesIn(K key) {
		HashMap<M, V> map = data.get(key);
		return map != null ? map.values() : null;
	}

	public Map<M, V> getMap(K key) {
		HashMap<M, V> map = data.get(key);
		return map != null ? Collections.unmodifiableMap(map) : null;
	}

}
