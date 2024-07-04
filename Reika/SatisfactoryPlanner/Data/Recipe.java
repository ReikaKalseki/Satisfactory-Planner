package Reika.SatisfactoryPlanner.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class Recipe implements Comparable<Recipe> {

	private static int maxIngredients;
	private static int maxProducts;

	public final String id;
	public final String displayName;
	public final boolean isAlternate;
	public final FunctionalBuilding productionBuilding;

	public final float craftingTime;
	public final float timeCoefficient;

	private final TreeMap<Consumable, Integer> costsRaw = new TreeMap();
	private final TreeMap<Consumable, Float> costsPerMinute = new TreeMap();
	private final TreeMap<Consumable, Float> productPerMinute = new TreeMap();

	private final ArrayList<Milestone> unlocks = new ArrayList();

	private int minimumTier = 200;
	/*
	public Recipe(String id, String dn, Building b, float time) {
		this(id, dn, b, false, time);
	}
	 */
	public Recipe(String id, String dn, FunctionalBuilding b, float time) {
		this.id = id;
		displayName = dn;
		productionBuilding = b;
		isAlternate = id.startsWith("Recipe_Alternate");
		craftingTime = time;
		timeCoefficient = 60F/craftingTime;
	}

	@Override
	public String toString() {
		return displayName;
	}

	public Recipe addIngredient(Consumable i, int amt) {
		costsRaw.put(i, amt);
		costsPerMinute.put(i, amt*timeCoefficient);
		maxIngredients = Math.max(costsPerMinute.size(), maxIngredients);
		return this;
	}

	public Recipe addProduct(Consumable i, int amt) {
		productPerMinute.put(i, amt*timeCoefficient);
		maxProducts = Math.max(productPerMinute.size(), maxProducts);
		return this;
	}

	public Map<Consumable, Integer> getDirectCost() {
		return Collections.unmodifiableMap(costsRaw);
	}

	public Map<Consumable, Float> getIngredientsPerMinute() {
		return Collections.unmodifiableMap(costsPerMinute);
	}

	public Map<Consumable, Float> getProductsPerMinute() {
		return Collections.unmodifiableMap(productPerMinute);
	}

	@Override
	public int compareTo(Recipe o) {
		if (minimumTier == o.minimumTier) {
			if (productionBuilding == o.productionBuilding)
				return String.CASE_INSENSITIVE_ORDER.compare(id, o.id);
			else
				return productionBuilding.compareTo(o.productionBuilding);
		}
		else {
			return Integer.compare(minimumTier, o.minimumTier);
		}
	}

	public static int getMaxIngredients() {
		return maxIngredients;
	}

	public static int getMaxProducts() {
		return maxProducts;
	}

	public void addMilestone(Milestone m) {
		unlocks.add(m);
		minimumTier = Math.min(minimumTier, m.getTier());
	}

	public int getTier() {
		return minimumTier;
	}

}
