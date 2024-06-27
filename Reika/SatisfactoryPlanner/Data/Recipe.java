package Reika.SatisfactoryPlanner.Data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Recipe implements Comparable<Recipe> {

	private static int maxIngredients;
	private static int maxProducts;

	public final String name;
	public final boolean isAlternate;
	public final Building productionBuilding;

	private final HashMap<Consumable, Integer> costsPerMinute = new HashMap();
	private final HashMap<Consumable, Integer> productPerMinute = new HashMap();

	public Recipe(String n, Building b) {
		this(n, b, false);
	}

	public Recipe(String n, Building b, boolean alt) {
		name = n;
		productionBuilding = b;
		isAlternate = alt;
	}

	@Override
	public String toString() {
		return name;
	}

	public Recipe addIngredient(Consumable i, int amt) {
		costsPerMinute.put(i, amt);
		maxIngredients = Math.max(costsPerMinute.size(), maxIngredients);
		return this;
	}

	public Recipe addProduct(Consumable i, int amt) {
		productPerMinute.put(i, amt);
		maxProducts = Math.max(productPerMinute.size(), maxProducts);
		return this;
	}

	public Map<Consumable, Integer> getCost() {
		return Collections.unmodifiableMap(costsPerMinute);
	}

	public Map<Consumable, Integer> getProducts() {
		return Collections.unmodifiableMap(productPerMinute);
	}

	@Override
	public int compareTo(Recipe o) {
		return String.CASE_INSENSITIVE_ORDER.compare(name, o.name);
	}

	public static int getMaxIngredients() {
		return maxIngredients;
	}

	public static int getMaxProducts() {
		return maxProducts;
	}

}
