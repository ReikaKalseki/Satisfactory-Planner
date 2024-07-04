package Reika.SatisfactoryPlanner.Data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.base.Charsets;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;
import Reika.SatisfactoryPlanner.GUI.ControllerBase;
import Reika.SatisfactoryPlanner.GUI.GuiUtil;
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

	public String name;

	private boolean bulkChanging;

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

	public List<Consumable> getProducts() {
		return Collections.unmodifiableList(desiredProducts);
	}

	public List<Recipe> getRecipes() {
		return Collections.unmodifiableList(recipeList);
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

	public void notifyListeners() {
		if (bulkChanging)
			return;
		for (FactoryListener rr : changeCallback)
			rr.onContentsChange();
	}

	public void clear() {
		bulkChanging = true;
		recipeList.clear();
		recipes.clear();
		generators.clear();
		resourceSources.clear();
		desiredProducts.clear();
		bulkChanging = false;
		this.notifyListeners();
	}

	public void save() throws IOException {
		File f = new File(saveDir, name+".factory");
		if (f.exists()) {
			if (!GuiUtil.getConfirmation("Overwrite existing file?"))
				return;
		}
		JSONObject root = new JSONObject();
		JSONArray recipes = new JSONArray();
		JSONArray generators = new JSONArray();
		JSONArray resources = new JSONArray();
		JSONArray products = new JSONArray();
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

		FileUtils.write(f, root.toString(4), Charsets.UTF_8);
	}

	public void reload() throws Exception {
		this.load(new File(saveDir, name+".factory"));
	}

	private void load(File f) throws Exception {
		bulkChanging = true;
		this.clear();

		String file = FileUtils.readFileToString(f, Charsets.UTF_8);
		JSONObject root = new JSONObject(file);
		name = root.getString("name");

		JSONArray recipes = root.getJSONArray("recipes");
		JSONArray generators = root.getJSONArray("generators");
		JSONArray resources = root.getJSONArray("resources");
		JSONArray products = root.getJSONArray("products");
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

		bulkChanging = false;
		this.notifyListeners();
	}

	public static Factory loadFactory(File f) throws Exception {
		Factory ret = new Factory();
		ret.load(f);
		return ret;
	}

}
