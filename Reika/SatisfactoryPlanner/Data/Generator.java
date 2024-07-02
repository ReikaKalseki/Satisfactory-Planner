package Reika.SatisfactoryPlanner.Data;

import java.util.Map;

import Reika.SatisfactoryPlanner.Util.CountMap;

public class Generator extends Building {

	public final float powerGenerationMW;
	private final CountMap<Consumable> fuelCost = new CountMap();

	public Generator(String id, String dis, String icon, float mw) {
		super(id, dis, icon, 0);
		powerGenerationMW = mw;
	}

	public Generator addFuel(Consumable i, int amt) {
		fuelCost.set(i, amt);
		return this;
	}

	public int getFuelCost(Item i) {
		return fuelCost.get(i);
	}

	public Map<Consumable, Integer> getFuelCost() {
		return fuelCost.view();
	}

}
