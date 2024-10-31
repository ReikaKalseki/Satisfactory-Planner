package Reika.SatisfactoryPlanner.Data;

import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;

import Reika.SatisfactoryPlanner.Data.Objects.Consumable;

public class ItemAmountTracker {

	private final TreeMap<Consumable, Double> data = new TreeMap();

	public void add(Consumable c, double amt) {
		data.put(c, this.get(c)+amt);
	}

	public double get(Consumable c) {
		Double get = data.get(c);
		return get == null ? 0 : get.doubleValue();
	}

	public Set<Consumable> getItems() {
		return Collections.unmodifiableSet(data.keySet());
	}

	public void clear() {
		data.clear();
	}

}
