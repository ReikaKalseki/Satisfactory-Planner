package Reika.SatisfactoryPlanner.Data;

import java.util.HashMap;

import Reika.SatisfactoryPlanner.Data.Objects.Fuel;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.Generator;

public class FuelChoices {

	public final Generator generator;

	private final HashMap<Fuel, Double> counts = new HashMap();

	public FuelChoices(Generator g) {
		generator = g;
	}

	public void setCount(Fuel f, double amt) {
		if (!generator.getFuels().contains(f))
			throw new IllegalArgumentException(generator.displayName+" does not accept "+f.item.displayName+" as fuel!");
		counts.put(f, amt);
	}

	public double getCount(Fuel f) {
		Double d = counts.get(f);
		return d == null ? 0 : d.doubleValue();
	}

	public double getTotal() {
		double sum = 0;
		for (double val : counts.values())
			sum += val;
		return sum;
	}

	public void clear() {
		counts.clear();
	}

}
