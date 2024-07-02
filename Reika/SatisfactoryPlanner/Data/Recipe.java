package Reika.SatisfactoryPlanner.Data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import Reika.SatisfactoryPlanner.Util.Logging;

public class Recipe implements Comparable<Recipe> {

	private static int maxIngredients;
	private static int maxProducts;

	public final String id;
	public final String displayName;
	public final boolean isAlternate;
	public final Building productionBuilding;

	public final float craftingTime;
	public final float timeCoefficient;

	private final HashMap<Consumable, Float> costsPerMinute = new HashMap();
	private final HashMap<Consumable, Float> productPerMinute = new HashMap();
	/*
	public Recipe(String id, String dn, Building b, float time) {
		this(id, dn, b, false, time);
	}
	 */
	public Recipe(String id, String dn, Building b, float time) {
		this.id = id;
		displayName = dn;
		productionBuilding = b;
		isAlternate = id.startsWith("Recipe_Alternate");
		craftingTime = time;
		timeCoefficient = 60F/craftingTime;
		Logging.instance.log("Registered recipe type "+this);
	}

	@Override
	public String toString() {
		return displayName;
	}

	public Recipe addIngredient(Consumable i, int amt) {
		costsPerMinute.put(i, amt*craftingTime);
		maxIngredients = Math.max(costsPerMinute.size(), maxIngredients);
		return this;
	}

	public Recipe addProduct(Consumable i, int amt) {
		productPerMinute.put(i, amt*craftingTime);
		maxProducts = Math.max(productPerMinute.size(), maxProducts);
		return this;
	}

	public Map<Consumable, Float> getCost() {
		return Collections.unmodifiableMap(costsPerMinute);
	}

	public Map<Consumable, Float> getProducts() {
		return Collections.unmodifiableMap(productPerMinute);
	}

	@Override
	public int compareTo(Recipe o) {
		return String.CASE_INSENSITIVE_ORDER.compare(id, o.id);
	}

	public static int getMaxIngredients() {
		return maxIngredients;
	}

	public static int getMaxProducts() {
		return maxProducts;
	}

}
