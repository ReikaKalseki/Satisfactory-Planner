package Reika.SatisfactoryPlanner.Data;

import java.util.HashMap;
import java.util.Map;

import Reika.SatisfactoryPlanner.Data.Objects.Consumable;
import Reika.SatisfactoryPlanner.Data.Objects.Recipe;

public class RecipeDecompositionChain {

	private final HashMap<Consumable, Recipe> availableRecipes = new HashMap();

	public void setRecipe(Recipe r, Consumable c) {
		availableRecipes.put(c, r);
	}

	public boolean contains(Recipe r) {
		return availableRecipes.containsKey(r);
	}

	public void removeRecipe(Recipe r) {
		availableRecipes.remove(r);
	}

	public void clear() {
		availableRecipes.clear();
	}

	public void compute(Recipe root) {
		Map<Consumable, Double> cost = root.getIngredientsPerMinute();
	}

}
