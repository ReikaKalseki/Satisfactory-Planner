package Reika.SatisfactoryPlanner.Data;

import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;

import Reika.SatisfactoryPlanner.Data.Objects.Consumable;

public class ItemAmountTracker {

	private final TreeMap<Consumable, Float> data = new TreeMap();

	public void add(Consumable c, float amt) {
		data.put(c, this.get(c)+amt);
	}

	public float get(Consumable c) {
		Float get = data.get(c);
		return get == null ? 0 : get.floatValue();
	}

	public Set<Consumable> getItems() {
		return Collections.unmodifiableSet(data.keySet());
	}

	public void clear() {
		data.clear();
	}

}
