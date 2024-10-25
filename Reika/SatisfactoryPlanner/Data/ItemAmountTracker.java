package Reika.SatisfactoryPlanner.Data;

import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.math.Fraction;

import Reika.SatisfactoryPlanner.Data.Objects.Consumable;

public class ItemAmountTracker {

	private final TreeMap<Consumable, Fraction> data = new TreeMap();

	public void add(Consumable c, Fraction amt) {
		data.put(c, this.get(c).add(amt));
	}

	public Fraction get(Consumable c) {
		Fraction get = data.get(c);
		return get == null ? Fraction.ZERO : get;
	}

	public Set<Consumable> getItems() {
		return Collections.unmodifiableSet(data.keySet());
	}

	public void clear() {
		data.clear();
	}

}
