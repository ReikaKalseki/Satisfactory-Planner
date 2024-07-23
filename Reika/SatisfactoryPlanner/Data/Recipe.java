package Reika.SatisfactoryPlanner.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

public class Recipe implements Comparable<Recipe> {

	private static int maxIngredients;
	private static int maxProducts;

	public final String id;
	public final String displayName;
	public final boolean isAlternate;
	public final FunctionalBuilding productionBuilding;

	public final float craftingTime;
	public final float timeCoefficient;

	public final boolean isFicsmas;

	private final TreeMap<Consumable, Integer> costsRaw = new TreeMap();
	private final TreeMap<Consumable, Float> costsPerMinute = new TreeMap();
	private final TreeMap<Consumable, Float> productPerMinute = new TreeMap();

	private final ArrayList<Milestone> unlocks = new ArrayList();

	private int minimumTier = 999;
	/*
	public Recipe(String id, String dn, Building b, float time) {
		this(id, dn, b, false, time);
	}
	 */
	public Recipe(String id, String dn, FunctionalBuilding b, float time, boolean ficsmas) {
		this.id = id;
		displayName = dn;
		productionBuilding = b;
		isAlternate = id.startsWith("Recipe_Alternate");
		craftingTime = time;
		timeCoefficient = 60F/craftingTime;

		isFicsmas = ficsmas;
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

	public boolean isPackaging() {
		return productionBuilding == Database.lookupBuilding("Build_Packager_C") && displayName.startsWith("Packaged");
	}

	public boolean isUnpackaging() {
		return productionBuilding == Database.lookupBuilding("Build_Packager_C") && id.startsWith("Recipe_Unpackage");
	}

	public RecipeProductLoop loopsWith(Recipe r) {
		Consumable c1 = null;
		Consumable c2 = null;
		for (Consumable c : costsPerMinute.keySet()) {
			if (r.productPerMinute.containsKey(c)) {
				c1 = c;
				break;
			}
		}
		for (Consumable c : r.costsPerMinute.keySet()) {
			if (productPerMinute.containsKey(c)) {
				c2 = c;
				break;
			}
		}
		return c1 != null && c2 != null ? new RecipeProductLoop(c1, c2, r, this) : null;
	}

	public Consumable getSoleProduct() {
		return productPerMinute.size() == 1 ? productPerMinute.firstKey() : null;
	}

	public boolean isSoleProduct(Predicate<Consumable> check) {
		Consumable c = this.getSoleProduct();
		return c != null && check.test(c);
	}

	public boolean isFindables() {
		return id.startsWith("Recipe_PowerCrystalShard");
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
