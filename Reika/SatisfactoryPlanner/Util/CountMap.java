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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class CountMap<V> {

	private final HashMap<V, Integer> data = new HashMap();
	private int total;

	public CountMap() {

	}

	public void increment(V key) {
		this.increment(key, 1);
	}

	public void increment(V key, int num) {
		Integer get = data.get(key);
		int has = get != null ? get.intValue() : 0;
		int next = has+num;
		this.set(key, next);
		total += num;
	}

	public void increment(CountMap<V> map) {
		for (V val : map.data.keySet()) {
			this.increment(val, map.data.get(val));
		}
	}

	public void subtract(V key, int num) {
		int has = this.get(key);
		if (num >= has)
			this.remove(key);
		else
			this.increment(key, -num);
	}

	public void set(V key, int num) {
		if (num != 0)
			data.put(key, num);
		else
			data.remove(key);
	}

	public int remove(V key) {
		Integer amt = data.remove(key);
		if (amt == null)
			amt = 0;
		total -= amt;
		return amt;
	}

	public int get(V key) {
		Integer get = data.get(key);
		return get != null ? get.intValue() : 0;
	}

	public int size() {
		return data.size();
	}

	public int getTotalCount() {
		return total;
	}

	@Override
	public int hashCode() {
		return data.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof CountMap && ((CountMap)o).data.equals(data);
	}

	@Override
	public String toString() {
		return data.toString();
	}

	public Set<V> keySet() {
		return Collections.unmodifiableSet(data.keySet());
	}

	public boolean containsKey(V key) {
		return data.containsKey(key);
	}

	public void clear() {
		data.clear();
	}

	public double getFraction(V k) {
		if (total == 0)
			return 0;
		return this.get(k)/(double)this.getTotalCount();
	}

	public boolean isEmpty() {
		return data.isEmpty();
	}

	public Map<V, Integer> view() {
		return Collections.unmodifiableMap(data);
	}

	public static <K> void incrementMapByMap(Map<K, Double> map, Map<K, Double> by) {
		incrementMapByMap(map, by, 1);
	}

	public static <K> void incrementMapByMap(Map<K, Double> map, Map<K, Double> by, double scale) {
		for (Entry<K, Double> e : by.entrySet()) {
			K c = e.getKey();
			Double has = map.get(c);
			map.put(c, (has == null ? 0 : has.doubleValue())+e.getValue()*scale);
		}
	}

}
