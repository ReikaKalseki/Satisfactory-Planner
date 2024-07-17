package Reika.SatisfactoryPlanner.Data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import org.json.JSONArray;
import org.json.JSONObject;

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
import Reika.SatisfactoryPlanner.GUI.MainGuiController;
import Reika.SatisfactoryPlanner.GUI.RecipeMatrix;
import Reika.SatisfactoryPlanner.GUI.ScaledRecipeMatrix;
import Reika.SatisfactoryPlanner.Util.CountMap;
import Reika.SatisfactoryPlanner.Util.FactoryListener;
import Reika.SatisfactoryPlanner.Util.JSONUtil;
import Reika.SatisfactoryPlanner.Util.MultiMap;

import javafx.scene.Node;

public class Factory {

	public static final File saveDir = Main.getRelativeFile("Factories");

	//private final CountMap<Recipe> recipes = new CountMap();
	private final HashMap<Recipe, Float> recipes = new HashMap();
	private final ArrayList<Recipe> recipeList = new ArrayList();
	private final ArrayList<RecipeProductLoop> recipeLoops = new ArrayList();

	private final CountMap<Generator> generators = new CountMap();

	private final ArrayList<FactoryListener> changeCallback = new ArrayList();

	private final RecipeMatrix matrix = new RecipeMatrix(this);
	private final ScaledRecipeMatrix scaleMatrix = new ScaledRecipeMatrix(matrix);

	private final MultiMap<Consumable, ResourceSupply> resourceSources = new MultiMap();
	private final ArrayList<Consumable> desiredProducts = new ArrayList();

	private final EnumSet<ToggleableVisiblityGroup> toggles = EnumSet.allOf(ToggleableVisiblityGroup.class);

	public String name;

	private boolean bulkChanging;

	private File currentFile;

	private MainGuiController gui;

	static {
		saveDir.mkdirs();
	}

	public Factory() {

	}

	public Factory addCallback(FactoryListener r) {
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
		this.setCount(r, 0);
		Collections.sort(recipeList);
		this.notifyListeners(c -> c.onAddRecipe(r));
	}

	public void removeRecipe(Recipe r) {
		recipeLoops.removeIf(p -> p.recipe1.equals(r) || p.recipe2.equals(r));
		if (recipeList.remove(r)) {
			recipes.remove(r);
			this.notifyListeners(c -> c.onRemoveRecipe(r));
		}
	}

	public Node createRawMatrix() throws IOException {
		if (recipes.isEmpty())
			return null;
		bulkChanging = true;
		matrix.createGrid();
		bulkChanging = false;
		return matrix.getGrid();
	}

	public Node createNetMatrix() throws IOException {
		if (recipes.isEmpty())
			return null;
		bulkChanging = true;
		scaleMatrix.createGrid();
		bulkChanging = false;
		return scaleMatrix.getGrid();
	}

	public void addExternalSupply(ResourceSupply res) {
		resourceSources.addValue(res.getResource(), res);
		this.notifyListeners(c -> c.onAddSupply(res));
	}

	public void removeExternalSupply(ResourceSupply res) {
		resourceSources.remove(res.getResource(), res);
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
		this.notifyListeners(l -> l.onAddProduct(c));
	}

	public void removeProduct(Consumable c) {
		if (desiredProducts.remove(c)) {
			this.notifyListeners(l -> l.onRemoveProduct(c));
		}
	}

	public List<Consumable> getDesiredProducts() {
		return Collections.unmodifiableList(desiredProducts);
	}

	public List<Recipe> getRecipes() {
		return Collections.unmodifiableList(recipeList);
	}

	public HashSet<Consumable> getAllIngredients() {
		HashSet<Consumable> ret = new HashSet();
		for (Recipe r : this.getRecipes())
			ret.addAll(r.getIngredientsPerMinute().keySet());
		return ret;
	}

	public HashSet<Consumable> getAllProducedItems() {
		HashSet<Consumable> ret = new HashSet();
		for (Recipe r : this.getRecipes())
			ret.addAll(r.getProductsPerMinute().keySet());
		return ret;
	}

	public float getTotalConsumption(Consumable c) {
		float amt = 0;
		for (Recipe r : this.getRecipes()) {
			Float get = r.getIngredientsPerMinute().get(c);
			if (get != null)
				amt += get.floatValue()*this.getCount(r);
		}
		return amt;
	}

	public float getTotalProduction(Consumable c) {
		float amt = 0;
		for (Recipe r : this.getRecipes()) {
			Float get = r.getProductsPerMinute().get(c);
			if (get != null)
				amt += get.floatValue()*this.getCount(r);
		}
		return amt;
	}

	public float getNetProduction(Consumable c) {
		return this.getTotalAvailable(c)-this.getTotalConsumption(c);
	}

	public float getCount(Recipe r) {
		return recipes.get(r);
	}

	public void setCount(Recipe r, float amt) {
		recipes.put(r, amt);
		this.notifyListeners(c -> c.onSetCount(r, amt));
	}

	public int getCount(Generator g) {
		return generators.get(g);
	}

	public void setCount(Generator g, int amt) {
		generators.set(g, amt);
		this.notifyListeners(c -> c.onSetCount(g, amt));
	}

	public CountMap<FunctionalBuilding> getBuildings() {
		CountMap<FunctionalBuilding> map = new CountMap();
		for (Recipe r : recipeList)
			map.increment(r.productionBuilding, (int)Math.ceil(this.getCount(r)));
		for (Generator g : generators.keySet())
			map.increment(g, this.getCount(g));
		for (ResourceSupply res : resourceSources.allValues(false)) {
			if (res instanceof ExtractableResource)
				map.increment(((ExtractableResource)res).getBuilding(), 1);
		}
		return map;
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

	public float getTotalAvailable(Consumable c) {
		return this.getTotalProduction(c)+this.getExternalSupply(c);
	}

	public boolean isExcess(Consumable c) {
		return !this.isDesiredFinalProduct(c) && this.getTotalAvailable(c) > this.getTotalConsumption(c);
	}

	public void getWarnings(Consumer<Warning> call) {
		for (Consumable c : this.getAllIngredients()) {
			float has = this.getTotalAvailable(c);
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
		if (bulkChanging)
			return;
		for (FactoryListener rr : changeCallback)
			c.accept(rr);
	}

	public void clear() {
		boolean wasBulk = bulkChanging;
		bulkChanging = true;
		recipeList.clear();
		recipeLoops.clear();
		recipes.clear();
		generators.clear();
		resourceSources.clear();
		desiredProducts.clear();
		toggles.clear();
		bulkChanging = wasBulk;
		if (!bulkChanging)
			this.notifyListeners(c -> c.onCleared());
	}

	public void doBulkOperation(Runnable r, Consumer<FactoryListener> c) {
		bulkChanging = true;
		r.run();
		bulkChanging = false;
		this.notifyListeners(c);
	}

	public File getDefaultFile() {
		return new File(saveDir, name+".factory");
	}

	public boolean hasExistingFile() {
		return currentFile != null && currentFile.exists();
	}

	public void save() throws IOException {
		if (currentFile != null)
			this.save(currentFile);
	}

	public void save(File f) throws IOException {
		this.setCurrentFile(f);
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
			block.put("count", this.generators.get(g));
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
	}

	public void reload() throws Exception {
		this.load(currentFile);
	}

	private void load(File f) throws Exception {
		bulkChanging = true;
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
			this.generators.set(r, block.getInt("count"));
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

		bulkChanging = false;
		this.notifyListeners(c -> c.onLoaded());
	}

	private void setCurrentFile(File f) {
		currentFile = f;
		this.notifyListeners(c -> c.onSetFile(f));
		Main.addRecentFile(f);
		((MainGuiController)GuiSystem.MainWindow.getGUI().controller).buildRecentList();
	}

	public static Factory loadFactory(File f, FactoryListener... l) throws Exception {
		Factory ret = new Factory();
		for (FactoryListener fl : l) {
			ret.addCallback(fl);
			fl.setFactory(ret);
		}
		ret.load(f);
		return ret;
	}

	public void setUI(MainGuiController gui) {
		this.gui = gui;
		matrix.setUI(gui);
		scaleMatrix.setUI(gui);
	}

}
