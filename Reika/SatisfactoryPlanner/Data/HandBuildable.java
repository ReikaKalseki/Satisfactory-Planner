package Reika.SatisfactoryPlanner.Data;

import java.util.Map;

import Reika.SatisfactoryPlanner.Util.CountMap;

public abstract class HandBuildable extends Resource {

	private final CountMap<Item> constructionCost = new CountMap();

	public HandBuildable(String id, String dis, String icon) {
		super(id, dis, icon);
	}

	public final HandBuildable addIngredient(Item i, int amt) {
		constructionCost.set(i, amt);
		return this;
	}

	public final int getConstructionCost(Item i) {
		return constructionCost.get(i);
	}

	public final Map<Item, Integer> getConstructionCost() {
		return constructionCost.view();
	}

}
