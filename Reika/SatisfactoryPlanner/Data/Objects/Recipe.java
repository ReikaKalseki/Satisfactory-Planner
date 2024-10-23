package Reika.SatisfactoryPlanner.Data.Objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.ItemConsumerProducer;
import Reika.SatisfactoryPlanner.Data.PowerOverride;
import Reika.SatisfactoryPlanner.Data.RecipeProductLoop;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.CraftingBuilding;
import Reika.SatisfactoryPlanner.Util.JavaUtil;

public class Recipe implements ItemConsumerProducer, Comparable<Recipe> {

	private static int maxIngredients;
	private static int maxProducts;

	public final String id;
	public final String displayName;
	public final boolean isAlternate;
	public final CraftingBuilding productionBuilding;

	public final float craftingTime;
	public final float timeCoefficient;

	public final boolean isFicsmas;

	private boolean usesFluids;

	private final TreeMap<Consumable, Integer> costsRaw = new TreeMap();
	private final TreeMap<Consumable, Integer> productsRaw = new TreeMap();
	private final TreeMap<Consumable, Float> costsPerMinute = new TreeMap();
	private final TreeMap<Consumable, Float> productPerMinute = new TreeMap();

	private final HashSet<Milestone> unlocks = new HashSet();

	private int minimumTier = 999;

	private String sourceMod;
	public PowerOverride powerOverride;
	/*
	public Recipe(String id, String dn, Building b, float time) {
		this(id, dn, b, false, time);
	}
	 */
	public Recipe(String id, String dn, CraftingBuilding b, float time, boolean ficsmas) {
		this.id = id;
		displayName = dn;
		productionBuilding = b;
		isAlternate = id.startsWith("Recipe_Alternate") && !id.equalsIgnoreCase("Recipe_Alternate_PolyesterFabric_C");
		craftingTime = time;
		timeCoefficient = 60F/craftingTime;

		isFicsmas = ficsmas;
	}

	public Recipe markModded(String mod) {
		sourceMod = mod;
		return this;
	}

	public String getMod() {
		return sourceMod;
	}

	@Override
	public String toString() {
		return displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	@Override
	public Resource getLocationIcon() {
		return productionBuilding;
	}

	public Recipe addIngredient(Consumable i, int amt) {
		costsRaw.put(i, amt);
		costsPerMinute.put(i, amt*timeCoefficient);
		maxIngredients = Math.max(costsPerMinute.size(), maxIngredients);
		usesFluids |= i instanceof Fluid;
		return this;
	}

	public Recipe addProduct(Consumable i, int amt) {
		productsRaw.put(i, amt);
		productPerMinute.put(i, amt*timeCoefficient);
		maxProducts = Math.max(productPerMinute.size(), maxProducts);
		usesFluids |= i instanceof Fluid;
		return this;
	}

	public boolean usesFluids() {
		return usesFluids;
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

	public Map<Consumable, Integer> getDirectProducts() {
		return Collections.unmodifiableMap(productsRaw);
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
			if (productionBuilding == o.productionBuilding) {
				if ((sourceMod != null) == (o.sourceMod != null)) {
					if (isAlternate == o.isAlternate)
						return String.CASE_INSENSITIVE_ORDER.compare(id, o.id);
					else
						return isAlternate ? 1 : -1;
				}
				else {
					return sourceMod == null ? -1 : 1;
				}
			}
			else {
				return productionBuilding.compareTo(o.productionBuilding);
			}
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

	public void updateBuildingTier() {
		if (productionBuilding != null) {
			int bt = productionBuilding.getRecipe().getTier();
			minimumTier = minimumTier == 999 ? bt : Math.max(minimumTier, bt); //INCREASE to since cannot produce with
		}
	}

	public int getTier() {
		return Math.min(Milestone.getMaxTier(), minimumTier);
	}

	public void clearIngredients() {
		costsPerMinute.clear();
		costsRaw.clear();
		usesFluids = !productPerMinute.isEmpty() && productPerMinute.keySet().stream().anyMatch(c -> c instanceof Fluid);
	}

	public void clearProducts() {
		productPerMinute.clear();
		usesFluids = !costsRaw.isEmpty() && costsRaw.keySet().stream().anyMatch(c -> c instanceof Fluid);
	}

	public float getPowerCost() {
		return powerOverride != null ? powerOverride.getAveragePower() : productionBuilding.basePowerCostMW;
	}

	public float getMinPowerCost() {
		return powerOverride != null ? powerOverride.getMinimumPower() : productionBuilding.basePowerCostMW;
	}

	public float getMaxPowerCost() {
		return powerOverride != null ? powerOverride.getPeakPower() : productionBuilding.basePowerCostMW;
	}

	public Collection<Milestone> getMilestones() {
		return Collections.unmodifiableCollection(unlocks);
	}

	public static Collection<Recipe> getAllRecipesMaking(Consumable c) {
		ArrayList<Recipe> li = new ArrayList();
		for (Recipe r : Database.getAllAutoRecipes()) {
			if (r.productsRaw.containsKey(c))
				li.add(r);
		}
		return li;
	}

	public static Collection<Recipe> getAllRecipesUsing(Consumable c) {
		ArrayList<Recipe> li = new ArrayList();
		for (Recipe r : Database.getAllAutoRecipes()) {
			if (r.costsRaw.containsKey(c))
				li.add(r);
		}
		return li;
	}

	public void resort() {
		JavaUtil.resort(costsRaw);
		JavaUtil.resort(costsPerMinute);
		JavaUtil.resort(productsRaw);
		JavaUtil.resort(productPerMinute);
	}

}
