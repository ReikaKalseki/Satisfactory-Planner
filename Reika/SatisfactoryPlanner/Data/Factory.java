package Reika.SatisfactoryPlanner.Data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.base.Charsets;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;
import Reika.SatisfactoryPlanner.Data.Constants.ToggleableVisiblityGroup;
import Reika.SatisfactoryPlanner.Data.Warning.ExcessResourceWarning;
import Reika.SatisfactoryPlanner.Data.Warning.InsufficientResourceWarning;
import Reika.SatisfactoryPlanner.Data.Warning.ResourceIconName;
import Reika.SatisfactoryPlanner.Data.Warning.WarningSeverity;
import Reika.SatisfactoryPlanner.GUI.ControllerBase;
import Reika.SatisfactoryPlanner.GUI.GuiSystem;
import Reika.SatisfactoryPlanner.GUI.MainGuiController;
import Reika.SatisfactoryPlanner.GUI.RecipeMatrix;
import Reika.SatisfactoryPlanner.GUI.ScaledRecipeMatrix;
import Reika.SatisfactoryPlanner.Util.CountMap;
import Reika.SatisfactoryPlanner.Util.FactoryListener;
import Reika.SatisfactoryPlanner.Util.MultiMap;

import javafx.scene.layout.GridPane;

public class Factory {

	public static final File saveDir = Main.getRelativeFile("Factories");

	private final CountMap<Recipe> recipes = new CountMap();
	private final ArrayList<Recipe> recipeList = new ArrayList();

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

	static {
		saveDir.mkdirs();
	}

	public Factory() {

	}

	public Factory addCallback(FactoryListener r) {
		changeCallback.add(r);
		return this;
	}

	public void addRecipe(Recipe r) {
		if (r == null || recipeList.contains(r))
			return;
		recipeList.add(r);
		this.setCount(r, 0);
		Collections.sort(recipeList);
		this.notifyListeners();
	}

	public void removeRecipe(Recipe r) {
		if (recipeList.remove(r)) {
			recipes.remove(r);
			this.notifyListeners();
		}
	}

	public GridPane createRawMatrix(ControllerBase con) throws IOException {
		bulkChanging = true;
		GridPane ret = matrix.createGrid(con);
		bulkChanging = false;
		return ret;
	}

	public GridPane createNetMatrix(ControllerBase con) throws IOException {
		bulkChanging = true;
		GridPane ret = scaleMatrix.createGrid(con);
		bulkChanging = false;
		return ret;
	}

	public void addExternalSupply(ResourceSupply res) {
		resourceSources.addValue(res.getResource(), res);
		this.notifyListeners();
	}

	public void removeExternalSupply(ResourceSupply res) {
		resourceSources.remove(res.getResource(), res);
		this.notifyListeners();
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
		this.notifyListeners();
	}

	public void removeProduct(Consumable c) {
		if (desiredProducts.remove(c)) {
			this.notifyListeners();
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
		int amt = 0;
		for (Recipe r : this.getRecipes()) {
			Float get = r.getIngredientsPerMinute().get(c);
			if (get != null)
				amt += get.floatValue()*this.getCount(r);
		}
		return amt;
	}

	public float getTotalProduction(Consumable c) {
		int amt = 0;
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

	public int getCount(Recipe r) {
		return recipes.get(r);
	}

	public void setCount(Recipe r, int amt) {
		recipes.set(r, amt);
		this.notifyListeners();
	}

	public int getCount(Generator g) {
		return generators.get(g);
	}

	public void setCount(Generator g, int amt) {
		generators.set(g, amt);
		this.notifyListeners();
	}

	public CountMap<FunctionalBuilding> getBuildings() {
		CountMap<FunctionalBuilding> map = new CountMap();
		for (Recipe r : recipeList)
			map.increment(r.productionBuilding, this.getCount(r));
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
		this.notifyListeners();
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
			float has = this.getTotalAvailable(c);
			float need = this.getTotalConsumption(c);
			if (has > need) {
				call.accept(new ExcessResourceWarning(c, need, has));
			}
		}
		for (Consumable c : desiredProducts) {
			if (this.getTotalProduction(c) <= 0)
				call.accept(new Warning(WarningSeverity.SEVERE, "Not producing desired product: "+c.displayName, new ResourceIconName(c)));
		}
		for (ResourceSupply res : resourceSources.allValues(false)) {
			res.getWarnings(call);
		}
	}

	public void notifyListeners() {
		if (bulkChanging)
			return;
		for (FactoryListener rr : changeCallback)
			rr.onContentsChange();
	}

	public void clear() {
		boolean wasBulk = bulkChanging;
		bulkChanging = true;
		recipeList.clear();
		recipes.clear();
		generators.clear();
		resourceSources.clear();
		desiredProducts.clear();
		toggles.clear();
		bulkChanging = wasBulk;
		if (!bulkChanging)
			this.notifyListeners();
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
			block.put("count", this.recipes.get(r));
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

		FileUtils.write(f, root.toString(4), Charsets.UTF_8);
	}

	public void reload() throws Exception {
		this.load(currentFile);
	}

	private void load(File f) throws Exception {
		bulkChanging = true;
		this.clear();

		this.setCurrentFile(f);

		String file = FileUtils.readFileToString(f, Charsets.UTF_8);
		JSONObject root = new JSONObject(file);
		name = root.getString("name");

		JSONArray recipes = root.getJSONArray("recipes");
		JSONArray generators = root.getJSONArray("generators");
		JSONArray resources = root.getJSONArray("resources");
		JSONArray products = root.getJSONArray("products");
		JSONArray toggles = root.has("toggles") ? root.getJSONArray("toggles") : null;
		for (Object o : recipes) {
			JSONObject block = (JSONObject)o;
			Recipe r = Database.lookupRecipe(block.getString("id"));
			recipeList.add(r);
			this.recipes.set(r, block.getInt("count"));
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
		this.notifyListeners();
	}

	private void setCurrentFile(File f) {
		currentFile = f;
		for (FactoryListener rr : changeCallback)
			rr.onFileChange();
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

}
