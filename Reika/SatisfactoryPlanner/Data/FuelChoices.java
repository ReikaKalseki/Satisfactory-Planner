package Reika.SatisfactoryPlanner.Data;

import Reika.SatisfactoryPlanner.Util.CountMap;

public class FuelChoices {

	public final Generator generator;

	private final CountMap<Fuel> counts = new CountMap();

	public FuelChoices(Generator g) {
		generator = g;
	}

	public void setCount(Fuel f, int amt) {
		if (!generator.getFuels().contains(f))
			throw new IllegalArgumentException(generator.displayName+" does not accept "+f.item.displayName+" as fuel!");
		counts.set(f, amt);
	}

	public int getCount(Fuel f) {
		return counts.get(f);
	}

	public int getTotal() {
		int sum = 0;
		for (int val : counts.view().values())
			sum += val;
		return sum;
	}

	public void clear() {
		counts.clear();
	}

}
