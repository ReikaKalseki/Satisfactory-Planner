package Reika.SatisfactoryPlanner.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import org.json.JSONArray;
import org.json.JSONObject;

import Reika.SatisfactoryPlanner.FactoryListener;
import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Data.Constants.BeltTier;
import Reika.SatisfactoryPlanner.Data.Constants.PipeTier;
import Reika.SatisfactoryPlanner.Data.Constants.RateLimitedSupplyLine;
import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;
import Reika.SatisfactoryPlanner.Data.Constants.ToggleableVisiblityGroup;
import Reika.SatisfactoryPlanner.Data.Warning.ExcessResourceWarning;
import Reika.SatisfactoryPlanner.Data.Warning.InsufficientResourceWarning;
import Reika.SatisfactoryPlanner.Data.Warning.MultipleBeltsWarning;
import Reika.SatisfactoryPlanner.Data.Warning.ResourceIconName;
import Reika.SatisfactoryPlanner.Data.Warning.WarningSeverity;
import Reika.SatisfactoryPlanner.GUI.GuiSystem;
import Reika.SatisfactoryPlanner.GUI.GuiUtil;
import Reika.SatisfactoryPlanner.GUI.MainGuiController;
import Reika.SatisfactoryPlanner.GUI.RecipeMatrix;
import Reika.SatisfactoryPlanner.GUI.ScaledRecipeMatrix;
import Reika.SatisfactoryPlanner.Util.CountMap;
import Reika.SatisfactoryPlanner.Util.JSONUtil;
import Reika.SatisfactoryPlanner.Util.Logging;
import Reika.SatisfactoryPlanner.Util.MultiMap;

import javafx.scene.Node;

public class Factory {

	public static final File saveDir = Main.getRelativeFile("Factories");

	//private final CountMap<Recipe> recipes = new CountMap();
	private final HashMap<Recipe, Float> recipes = new HashMap();
	private final ArrayList<Recipe> recipeList = new ArrayList();
	private final ArrayList<RecipeProductLoop> recipeLoops = new ArrayList();

	private final HashMap<Generator, FuelChoices> generators = new HashMap();

	private final ArrayList<FactoryListener> changeCallback = new ArrayList();

	private final RecipeMatrix matrix = new RecipeMatrix(this);
	private final ScaledRecipeMatrix scaleMatrix = new ScaledRecipeMatrix(matrix);

	private final MultiMap<Consumable, ResourceSupply> resourceSources = new MultiMap();
	private final ArrayList<Consumable> desiredProducts = new ArrayList();

	//private final HashMap<Consumable, ItemFlow> flow = new HashMap();
	private final ItemAmountTracker production = new ItemAmountTracker();
	private final ItemAmountTracker externalInput = new ItemAmountTracker();
	private final ItemAmountTracker consumption = new ItemAmountTracker();

	private final EnumSet<ToggleableVisiblityGroup> toggles = EnumSet.allOf(ToggleableVisiblityGroup.class);

	public String name;

	private boolean skipNotify;

	private File currentFile;

	private MainGuiController gui;

	static {
		saveDir.mkdirs();
	}

	public Factory() {
		//matrix.buildGrid();
		//scaleMatrix.buildGrid();

		for (Generator g : Database.getAllGenerators())
			generators.put(g, new FuelChoices(g));
	}

	public Factory addCallback(FactoryListener r) {
		if (changeCallback.contains(r))
			throw new IllegalStateException("Listener "+r+" already registered!");
		changeCallback.add(r);
		Collections.sort(changeCallback);
		return this;
	}

	public void addRecipe(Recipe r) {
		if (r == null || recipeList.contains(r))
			return;
		for (Recipe r2 : recipeList) {
			RecipeProductLoop c = r.loopsWith(r2);
			if (c != null)
				recipeLoops.add(c);
		}
		recipeList.add(r);
		recipes.put(r, 0F);
		Collections.sort(recipeList);
		this.rebuildFlows();
		this.notifyListeners(c -> c.onAddRecipe(r));
	}

	public void removeRecipe(Recipe r) {
		recipeLoops.removeIf(p -> p.recipe1.equals(r) || p.recipe2.equals(r));
		if (recipeList.remove(r)) {
			recipes.remove(r);
			this.rebuildFlows();
			this.notifyListeners(c -> c.onRemoveRecipe(r));
		}
	}

	public Node createRawMatrix() {
		//if (recipes.isEmpty())
		//	return null;
		return matrix.getGrid();
	}

	public Node createNetMatrix() {
		//if (recipes.isEmpty())
		//	return null;
		return scaleMatrix.getGrid();
	}

	public void updateMatrixStatus(Consumable c) {
		matrix.updateStatuses(c);
		scaleMatrix.updateStatuses(c);
	}
	/*
	public void refreshMatrices() {
		matrix.refreshGridPositioning();
		scaleMatrix.refreshGridPositioning();
	}
	 */
	public void addExternalSupply(ResourceSupply res) {
		resourceSources.addValue(res.getResource(), res);
		this.rebuildFlows();
		this.updateMatrixStatus(res.getResource());
		this.notifyListeners(c -> c.onAddSupply(res));
	}

	public void removeExternalSupply(ResourceSupply res) {
		resourceSources.remove(res.getResource(), res);
		this.rebuildFlows();
		this.updateMatrixStatus(res.getResource());
		this.notifyListeners(c -> c.onRemoveSupply(res));
	}

	public int getExternalSupply(Consumable c) {
		int ret = 0;
		for (ResourceSupply res : resourceSources.get(c))
			ret += res.getYield();
		return ret;
	}

	public Collection<ResourceSupply> getSupplies() {
		return Collections.unmodifiableCollection(resourceSources.allValues(false));
	}

	public boolean isDesiredFinalProduct(Consumable c) {
		return desiredProducts.contains(c);
	}

	public void addProduct(Consumable c) {
		if (desiredProducts.contains(c))
			return;
		desiredProducts.add(c);
		this.updateMatrixStatus(c);
		this.notifyListeners(l -> l.onAddProduct(c));
	}

	public void removeProduct(Consumable c) {
		if (desiredProducts.remove(c)) {
			this.updateMatrixStatus(c);
			this.notifyListeners(l -> l.onRemoveProduct(c));
		}
	}

	public List<Consumable> getDesiredProducts() {
		return Collections.unmodifiableList(desiredProducts);
	}

	public List<Recipe> getRecipes() {
		return Collections.unmodifiableList(recipeList);
	}

	public Set<Consumable> getAllIngredients() {/*
		HashSet<Consumable> ret = new HashSet();
		for (Recipe r : this.getRecipes())
			ret.addAll(r.getIngredientsPerMinute().keySet());
		return ret;*/
		return consumption.getItems();
	}

	public Set<Consumable> getAllProducedItems() {/*
		HashSet<Consumable> ret = new HashSet();
		for (Recipe r : this.getRecipes())
			ret.addAll(r.getProductsPerMinute().keySet());
		return ret;*/
		return production.getItems();
	}

	public Set<Consumable> getAllSuppliedItems() {
		return externalInput.getItems();
	}

	public float getTotalConsumption(Consumable c) {/*
		float amt = 0;
		for (Recipe r : this.getRecipes()) {
			Float get = r.getIngredientsPerMinute().get(c);
			if (get != null)
				amt += get.floatValue()*this.getCount(r);
		}
		return amt;*/
		return consumption.get(c);
	}

	public float getTotalProduction(Consumable c) {/*
		float amt = 0;
		for (Recipe r : this.getRecipes()) {
			Float get = r.getProductsPerMinute().get(c);
			if (get != null)
				amt += get.floatValue()*this.getCount(r);
		}
		return amt;*/
		return production.get(c);
	}

	public float getExternalInput(Consumable c) {
		return externalInput.get(c);
	}
	/*
	public float getNetProduction(Consumable c) {
		return this.getTotalAvailable(c)-this.getTotalConsumption(c);
	}*/
	/*
	public ItemFlow getFlow(Consumable c) {
		return flow.get(c);
	}

	public Collection<Consumable> getAllRelevantItems() {
		return Collections.unmodifiableCollection(flow.keySet());
	}
	 */

	public float getCount(Recipe r) {
		return recipes.get(r);
	}

	public void setCount(Recipe r, float amt) {
		recipes.put(r, amt);
		this.rebuildFlows();
		this.notifyListeners(c -> c.onSetCount(r, amt));
	}

	public int getCount(Generator g, Fuel f) {
		return generators.get(g).getCount(f);
	}

	public void setCount(Generator g, Fuel f, int amt) {
		int old = generators.get(g).getCount(f);
		generators.get(g).setCount(f, amt);
		this.rebuildFlows();
		this.notifyListeners(c -> c.onSetCount(g, f, old, amt));
	}

	public void updateIO() {
		this.rebuildFlows();
		this.notifyListeners(c -> c.onUpdateIO());
	}

	private void rebuildFlows() {
		//		flow.clear();
		production.clear();
		externalInput.clear();
		consumption.clear();
		for (Recipe r : this.getRecipes()) {
			for (Entry<Consumable, Float> e : r.getIngredientsPerMinute().entrySet()) {
				//this.getOrCreateFlow(e.getKey()).consumption += e.getValue();
				consumption.add(e.getKey(), e.getValue()*recipes.get(r));
			}
			for (Entry<Consumable, Float> e : r.getProductsPerMinute().entrySet()) {
				//this.getOrCreateFlow(e.getKey()).production += e.getValue();
				production.add(e.getKey(), e.getValue()*recipes.get(r));
			}
		}
		for (ResourceSupply r : this.getSupplies()) {
			//this.getOrCreateFlow(r.getResource()).externalInput += r.getYield();
			externalInput.add(r.getResource(), r.getYield());
		}
		for (FuelChoices fc : generators.values()) {
			//this.getOrCreateFlow(r.getResource()).externalInput += r.getYield();
			for (Fuel f : fc.generator.getFuels()) {
				int amt = fc.getCount(f);
				if (amt <= 0)
					continue;
				consumption.add(f.item, f.primaryBurnRate*amt);
				if (f.secondaryItem != null)
					consumption.add(f.secondaryItem, f.secondaryBurnRate*amt);
				if (f.byproduct != null)
					production.add(f.byproduct, f.getByproductRate()*amt);
			}
		}/*
		for (Consumable c : externalInput.getItems())
			this.updateMatrixStatus(c);
		for (Consumable c : production.getItems())
			this.updateMatrixStatus(c);
		for (Consumable c : consumption.getItems())
			this.updateMatrixStatus(c);*/
	}
	/*
	private ItemFlow getOrCreateFlow(Consumable c) {
		ItemFlow f = flow.get(c);
		if (f == null) {
			f = new ItemFlow(c);
			flow.put(c, f);
		}
		return f;
	}
	 */
	public CountMap<FunctionalBuilding> getBuildings() {
		CountMap<FunctionalBuilding> map = new CountMap();
		for (Recipe r : recipeList)
			map.increment(r.productionBuilding, (int)Math.ceil(this.getCount(r)));
		for (FuelChoices f : generators.values())
			map.increment(f.generator, f.getTotal());
		for (ResourceSupply res : resourceSources.allValues(false)) {
			if (res instanceof ExtractableResource)
				map.increment(((ExtractableResource)res).getBuilding(), 1);
		}
		return map;
	}

	public int getCount(Generator g) {
		return generators.get(g).getTotal();
	}
	/*
	public CountMap<Item> getConstructionCost() {
		CountMap<Item> map = new CountMap();
		for (Recipe r : recipeList) {
			for (Entry<Item, Integer> e : r.productionBuilding.getConstructionCost().entrySet()) {
				map.increment(e.getKey(), e.getValue()*recipes.get(r));
			}
		}
		return map;
	}*/

	public float getNetPowerProduction() {
		float ret = 0;
		for (Generator g : generators.keySet())
			ret += g.powerGenerationMW*this.getCount(g);
		for (Recipe r : recipeList)
			ret -= r.productionBuilding.basePowerCostMW*this.getCount(r);
		return ret;
	}

	public void setToggle(ToggleableVisiblityGroup tv, boolean state) {
		if (state)
			toggles.add(tv);
		else
			toggles.remove(tv);
		this.notifyListeners(c -> c.onSetToggle(tv, state));
	}

	public boolean getToggle(ToggleableVisiblityGroup tv) {
		return toggles.contains(tv);
	}

	public boolean isExcess(Consumable c) {
		if (this.isDesiredFinalProduct(c))
			return false;
		//ItemFlow f = this.getFlow(c);
		//return f != null && f.getTotalAvailable() > f.getConsumption();
		return this.getTotalProduction(c)+this.getExternalSupply(c) > this.getTotalConsumption(c);
	}

	public void getWarnings(Consumer<Warning> call) {/*
		for (ItemFlow f : flow.values()) {
			if (f.isDeficit()) {
				call.accept(new InsufficientResourceWarning(f));
			}
			if (!desiredProducts.contains(f.item) && f.getTotalAvailable() > f.getConsumption())
				call.accept(new ExcessResourceWarning(f));
			RateLimitedSupplyLine lim = f.item instanceof Fluid ? PipeTier.TWO : BeltTier.FIVE;
			if (f.getTotalAvailable() > lim.getMaxThroughput())
				call.accept(new MultipleBeltsWarning(f.item, f.getTotalAvailable(), lim));
		}*/
		for (Consumable c : this.getAllIngredients()) {
			float has = this.getExternalInput(c)+this.getTotalProduction(c);
			float need = this.getTotalConsumption(c);
			if (has < need) {
				call.accept(new InsufficientResourceWarning(c, need, has));
			}
		}
		for (Consumable c : this.getAllProducedItems()) {
			if (desiredProducts.contains(c))
				continue;
			float prod = this.getTotalProduction(c);
			float sup = this.getExternalSupply(c);
			float has = prod+sup;
			float need = this.getTotalConsumption(c);
			if (has > need) {
				call.accept(new ExcessResourceWarning(c, need, has));
			}
			RateLimitedSupplyLine lim = c instanceof Fluid ? PipeTier.TWO : BeltTier.FIVE;
			if (has > lim.getMaxThroughput())
				call.accept(new MultipleBeltsWarning(c, has, lim));
		}
		for (RecipeProductLoop p : recipeLoops) {
			String msg = "A production loop exists between "+p.recipe1.displayName+" and "+p.recipe2.displayName;
			if (this.getExternalSupply(p.item1) > 0)
				call.accept(new Warning(p.item1 instanceof Fluid ? WarningSeverity.SEVERE : WarningSeverity.MINOR, msg+", with "+p.item1.displayName+" also being supplied externally. This risks a deadlock", new ResourceIconName(p.item1)));
			if (this.getExternalSupply(p.item2) > 0)
				call.accept(new Warning(p.item2 instanceof Fluid ? WarningSeverity.SEVERE : WarningSeverity.MINOR, msg+", with "+p.item2.displayName+" also being supplied externally. This risks a deadlock", new ResourceIconName(p.item2)));
		}
		for (Consumable c : desiredProducts) {
			if (this.getTotalProduction(c) <= 0)
				call.accept(new Warning(WarningSeverity.SEVERE, "Not producing desired product: "+c.displayName, new ResourceIconName(c)));
		}
		for (ResourceSupply res : resourceSources.allValues(false)) {
			res.getWarnings(call);
		}
	}

	public void notifyListeners(Consumer<FactoryListener> c) {
		if (skipNotify)
			return;
		for (FactoryListener rr : changeCallback)
			c.accept(rr);
	}

	public void clear() {
		boolean wasBulk = skipNotify;
		skipNotify = true;
		recipeList.clear();
		recipeLoops.clear();
		recipes.clear();
		for (FuelChoices f : generators.values())
			f.clear();
		resourceSources.clear();
		desiredProducts.clear();
		toggles.clear();
		this.rebuildFlows();
		skipNotify = wasBulk;
		if (!skipNotify)
			this.notifyListeners(c -> c.onCleared());
	}

	public void doBulkOperation(Runnable r, Consumer<FactoryListener> c) {
		skipNotify = true;
		r.run();
		skipNotify = false;
		this.notifyListeners(c);
	}

	public File getDefaultFile() {
		return new File(saveDir, name+".factory");
	}

	public boolean hasExistingFile() {
		return currentFile != null && currentFile.exists();
	}

	public File getFile() {
		return currentFile;
	}

	public void save() {
		if (currentFile != null)
			GuiUtil.queueTask(() -> this.save(currentFile));
	}

	public void save(File f) {
		GuiUtil.queueTask(() -> {
			JSONObject root = new JSONObject();
			JSONArray recipes = new JSONArray();
			JSONArray generators = new JSONArray();
			JSONArray resources = new JSONArray();
			JSONArray products = new JSONArray();
			JSONArray toggles = new JSONArray();
			root.put("name", name);

			for (Recipe r : recipeList) {
				JSONObject block = new JSONObject();
				block.put("id", r.id);
				block.put("count", String.format("%.3f", this.recipes.get(r)));
				recipes.put(block);
			}
			root.put("recipes", recipes);

			for (Generator g : this.generators.keySet()) {
				JSONObject block = new JSONObject();
				block.put("id", g.id);
				for (Fuel ff : g.getFuels()) {
					block.put("count_"+ff.item.id, this.generators.get(g).getCount(ff));
				}
				generators.put(block);
			}
			root.put("generators", generators);

			for (ResourceSupply r : resourceSources.allValues(false)) {
				JSONObject block = new JSONObject();
				block.put("type", r.getType().name());
				r.save(block);
				resources.put(block);
			}
			root.put("resources", resources);

			for (Consumable c : desiredProducts) {
				products.put(c.id);
			}
			root.put("products", products);

			for (ToggleableVisiblityGroup c : this.toggles) {
				toggles.put(c.name());
			}
			root.put("toggles", toggles);

			JSONUtil.saveFile(f, root);
			this.setCurrentFile(f);
		});
	}

	public void reload() {
		GuiUtil.queueTask(() -> this.load(currentFile));
	}

	private void load(File f) throws Exception {
		skipNotify = true;
		this.clear();

		this.setCurrentFile(f);

		JSONObject root = JSONUtil.readFile(f);
		name = root.getString("name");

		JSONArray recipes = root.getJSONArray("recipes");
		JSONArray generators = root.getJSONArray("generators");
		JSONArray resources = root.getJSONArray("resources");
		JSONArray products = root.getJSONArray("products");
		JSONArray toggles = root.has("toggles") ? root.getJSONArray("toggles") : null;
		for (Object o : recipes) {
			JSONObject block = (JSONObject)o;
			Recipe r = Database.lookupRecipe(block.getString("id"));
			/*
			recipeList.add(r);
			this.recipes.set(r, block.getInt("count"));
			 */
			this.addRecipe(r);
			this.setCount(r, block.getFloat("count"));
		}
		for (Object o : generators) {
			JSONObject block = (JSONObject)o;
			Generator r = (Generator)Database.lookupBuilding(block.getString("id"));
			for (Fuel ff : r.getFuels()) {
				String key = "count_"+ff.item.id;
				if (block.has(key))
					this.generators.get(r).setCount(ff, block.getInt(key));
			}
		}
		for (Object o : resources) {
			JSONObject block = (JSONObject)o;
			ResourceSupplyType type = ResourceSupplyType.valueOf(block.getString("type"));
			ResourceSupply res = type.construct(block);
			resourceSources.addValue(res.getResource(), res);
		}
		for (Object o : products) {
			desiredProducts.add(Database.lookupItem((String)o));
		}
		if (toggles == null) {
			this.toggles.addAll(EnumSet.allOf(ToggleableVisiblityGroup.class));
		}
		else {
			for (Object o : toggles) {
				this.toggles.add(ToggleableVisiblityGroup.valueOf((String)o));
			}
		}

		this.rebuildFlows();
		skipNotify = false;
		this.notifyListeners(c -> c.onLoaded());
	}

	private void setCurrentFile(File f) {
		currentFile = f;
		this.notifyListeners(c -> c.onSetFile(f));
		Main.addRecentFile(f);
		GuiSystem.getMainGUI().controller.buildRecentList();
	}

	public static void loadFactory(File f, FactoryListener... l) {
		Logging.instance.log("Loading factory from "+f.getAbsolutePath());
		GuiUtil.queueTask(() -> {
			Factory ret = new Factory();
			for (FactoryListener fl : l) {
				ret.addCallback(fl);
				fl.setFactory(ret);
			}
			ret.load(f);
		});
	}

	public void setUI(MainGuiController gui) {
		this.gui = gui;
		matrix.setUI(gui);
		scaleMatrix.setUI(gui);
	}

}
