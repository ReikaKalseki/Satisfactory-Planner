package Reika.SatisfactoryPlanner.Data.Objects.Buildables;

import java.util.Collections;
import java.util.Map;

import Reika.SatisfactoryPlanner.Data.Objects.Item;
import Reika.SatisfactoryPlanner.Data.Objects.Recipe;
import Reika.SatisfactoryPlanner.Data.Objects.Resource;

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
