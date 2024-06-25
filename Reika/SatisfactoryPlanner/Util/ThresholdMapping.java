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
import java.util.Comparator;
import java.util.TreeMap;


public class ThresholdMapping<V> {

	private final TreeMap<Double, V> data;

	public ThresholdMapping() {
		data = new TreeMap();
	}

	public ThresholdMapping(Comparator c) {
		data = new TreeMap(c);
	}

	public V addMapping(double thresh, V value) {
		return data.put(thresh, value);
	}

	public Double getKeyForValue(double v, boolean ceil) {
		Double d = ceil ? data.ceilingKey(v) : data.floorKey(v);
		return d != null ? d : null;
	}

	public V getForValue(double v, boolean ceil) {
		Double d = ceil ? data.ceilingKey(v) : data.floorKey(v);
		return d != null ? data.get(d) : null;
	}

	public V remove(double val) {
		return data.remove(val);
	}

	public Collection<Double> keySet() {
		return Collections.unmodifiableCollection(data.keySet());
	}

	public double firstValue() {
		return data.isEmpty() ? 0 : data.firstKey();
	}

	public double lastValue() {
		return data.isEmpty() ? 0 : data.lastKey();
	}

	public Collection<V> values() {
		return Collections.unmodifiableCollection(data.values());
	}

	@Override
	public String toString() {
		return data.toString();
	}

}
