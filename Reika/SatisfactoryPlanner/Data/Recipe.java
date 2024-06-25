package Reika.SatisfactoryPlanner.Data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Recipe implements Comparable<Recipe> {

	public final String name;
	public final boolean isAlternate;

	private final HashMap<Consumable, Integer> costsPerMinute = new HashMap();
	private final HashMap<Consumable, Integer> productPerMinute = new HashMap();

	public Recipe(String n) {
		this(n, false);
	}

	public Recipe(String n, boolean alt) {
		name = n;
		isAlternate = alt;
	}

	@Override
	public String toString() {
		return name;
	}

	public Recipe addIngredient(Consumable i, int amt) {
		costsPerMinute.put(i, amt);
		return this;
	}

	public Recipe addProduct(Consumable i, int amt) {
		productPerMinute.put(i, amt);
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

}
