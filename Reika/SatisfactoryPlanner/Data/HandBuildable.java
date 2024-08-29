package Reika.SatisfactoryPlanner.Data;

import java.util.Collections;
import java.util.Map;

public abstract class HandBuildable extends Resource {

	//private final CountMap<Item> constructionCost = new CountMap();

	private Recipe recipe;

	public HandBuildable(String id, String dis, String icon) {
		super(id, dis, icon);
	}
	/*
	public final HandBuildable addIngredient(Item i, int amt) {
		constructionCost.set(i, amt);
		return this;
	}
	 */
	public void setRecipe(Recipe r) {
		recipe = r;
	}

	public Recipe getRecipe() {
		return recipe;
	}

	public final int getConstructionCost(Item i) {
		return recipe.getDirectCost().get(i);//constructionCost.get(i);
	}

	public final Map<Item, Integer> getConstructionCost() {
		return Collections.unmodifiableMap((Map<Item, Integer>)(Object)recipe.getDirectCost());//constructionCost.view();
	}

}
