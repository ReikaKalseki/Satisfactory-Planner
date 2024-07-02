package Reika.SatisfactoryPlanner.Data;

import java.util.Map;

import Reika.SatisfactoryPlanner.Util.CountMap;
import Reika.SatisfactoryPlanner.Util.Logging;

public class Building extends Resource implements Comparable<Building> {

	private final CountMap<Item> constructionCost = new CountMap();
	public final float basePowerCostMW;

	public Building(String id, String dis, String icon, float mw) {
		super(id, dis, icon);
		basePowerCostMW = mw;
		Logging.instance.log("Registered building type "+this);
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

	@Override
	public int compareTo(Building o) { //TODO
		return 0;
	}

}
