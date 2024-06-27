package Reika.SatisfactoryPlanner.Data;

import java.util.Map;

import Reika.SatisfactoryPlanner.Util.CountMap;

public class Building extends Resource {

	private final CountMap<Item> constructionCost = new CountMap();
	public final int basePowerCostMW;

	public Building(String name, String icon, int mw) {
		super(name, icon);
		basePowerCostMW = mw;
	}

	public Building addIngredient(Item i, int amt) {
		constructionCost.set(i, amt);
		return this;
	}

	public int getConstructionCost(Item i) {
		return constructionCost.get(i);
	}

	public Map<Item, Integer> getConstructionCost() {
		return constructionCost.view();
	}

	@Override
	protected String getIconFolder() {
		return "Buildings";
	}

}
