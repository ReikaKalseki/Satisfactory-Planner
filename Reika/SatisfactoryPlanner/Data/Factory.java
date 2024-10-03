package Reika.SatisfactoryPlanner.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.json.JSONArray;
import org.json.JSONObject;

import Reika.SatisfactoryPlanner.FactoryListener;
import Reika.SatisfactoryPlanner.InclusionPattern;
import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Data.Constants.BeltTier;
import Reika.SatisfactoryPlanner.Data.Constants.PipeTier;
import Reika.SatisfactoryPlanner.Data.Constants.RateLimitedSupplyLine;
import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;
import Reika.SatisfactoryPlanner.Data.Constants.ToggleableVisiblityGroup;
import Reika.SatisfactoryPlanner.Data.Warning.ExcessResourceWarning;
import Reika.SatisfactoryPlanner.Data.Warning.InsufficientResourceWarning;
import Reika.SatisfactoryPlanner.Data.Warning.MultipleBeltsWarning;
import Reika.SatisfactoryPlanner.Data.Warning.NoSurplusResourceWarning;
import Reika.SatisfactoryPlanner.Data.Warning.ResourceIconName;
import Reika.SatisfactoryPlanner.Data.Warning.WarningSeverity;
import Reika.SatisfactoryPlanner.GUI.GuiSystem;
import Reika.SatisfactoryPlanner.GUI.GuiUtil;
import Reika.SatisfactoryPlanner.GUI.RecipeMatrix;
import Reika.SatisfactoryPlanner.GUI.RecipeMatrixContainer;
import Reika.SatisfactoryPlanner.GUI.RecipeMatrixContainer.MatrixType;
import Reika.SatisfactoryPlanner.GUI.ScaledRecipeMatrix;
import Reika.SatisfactoryPlanner.GUI.WaitDialogManager;
import Reika.SatisfactoryPlanner.Util.CountMap;
import Reika.SatisfactoryPlanner.Util.JSONUtil;
import Reika.SatisfactoryPlanner.Util.Logging;
import Reika.SatisfactoryPlanner.Util.MultiMap;

public class Factory {

	public static final File saveDir = Main.getRelativeFile("Factories");

	//private final CountMap<Recipe> recipes = new CountMap();
	private final HashMap<Recipe, Float> recipes = new HashMap();
	private final ArrayList<Recipe> recipeList = new ArrayList();
	private final ArrayList<RecipeProductLoop> recipeLoops = new ArrayList();

	private final HashMap<Generator, FuelChoices> generators = new HashMap();

	private final ArrayList<FactoryListener> changeCallback = new ArrayList();

	private final RecipeMatrix matrix;
	private final ScaledRecipeMatrix scaleMatrix;

	private final EnumSet<MatrixType> invalidMatrices = EnumSet.noneOf(MatrixType.class);

	private final MultiMap<Consumable, ResourceSupply> resourceSources = new MultiMap();
	private final MultiMap<String, FromFactorySupply> factorySources = new MultiMap();
	private final ArrayList<Consumable> desiredProducts = new ArrayList();

	//private final HashMap<Consumable, ItemFlow> flow = new HashMap();
	private final ItemAmountTracker production = new ItemAmountTracker();
	private final ItemAmountTracker mines = new ItemAmountTracker();
	private final ItemAmountTracker externalInput = new ItemAmountTracker();
	private final ItemAmountTracker consumption = new ItemAmountTracker();

	private final EnumSet<ToggleableVisiblityGroup> toggles = EnumSet.allOf(ToggleableVisiblityGroup.class);

	public String name = "";

	private boolean skipNotify;
	private boolean loading;

	private File currentFile;

	private RecipeMatrixContainer gui;

	public InclusionPattern generatorMatrixRule = InclusionPattern.INDIVIDUAL;
	public InclusionPattern resourceMatrixRule = InclusionPattern.MERGE;

	static {
		saveDir.mkdirs();
	}

	public Factory() {
		this(false);
	}

	private Factory(boolean nonUI) {
		matrix = nonUI ? null : new RecipeMatrix(this);
		scaleMatrix = nonUI ? null : new ScaledRecipeMatrix(matrix);

		//matrix.buildGrid();
		//scaleMatrix.buildGrid();

		for (Generator g : Database.getAllGenerators())
			generators.put(g, new FuelChoices(g));

		toggles.removeIf(tv -> !tv.defaultValue);
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
		if (loading)
			return;
		this.rebuildFlows();
		this.notifyListeners(c -> c.onAddRecipe(r));
		if (!skipNotify)
			this.queueMatrixAlign();
	}

	public void removeRecipe(Recipe r) {
		recipeLoops.removeIf(p -> p.recipe1.equals(r) || p.recipe2.equals(r));
		if (recipeList.remove(r)) {
			recipes.remove(r);
			if (!loading) {
				this.rebuildFlows();
				this.notifyListeners(c -> c.onRemoveRecipe(r));
				if (!skipNotify)
					this.queueMatrixAlign();
			}
		}
	}

	public void removeRecipes(Collection<Recipe> c) {
		skipNotify = true;
		for (Recipe r : c) {
			if (recipeList.remove(r)) {
				recipeLoops.removeIf(p -> p.recipe1.equals(r) || p.recipe2.equals(r));
				recipes.remove(r);
			}
		}
		skipNotify = false;
		if (loading)
			return;
		this.rebuildFlows();
		this.notifyListeners(l -> l.onRemoveRecipes(c));
		this.queueMatrixAlign();
	}

	public void clearRecipes() {
		this.removeRecipes(new ArrayList(recipes.keySet()));
	}

	public void rebuildMatrices(boolean updateIO) {
		if (matrix == null) //non-UI factory
			return;
		matrix.rebuild(updateIO);
		scaleMatrix.rebuild(updateIO);

		this.alignMatrices();
	}

	private void queueMatrixAlign() {
		if (matrix == null)
			return;
		GuiUtil.runOnJFXThread(() -> this.alignMatrices());
	}

	private void alignMatrices() {
		if (!invalidMatrices.isEmpty() || matrix == null)
			return;
		Logging.instance.log("Aligning matrices");
		matrix.alignWith(scaleMatrix);
		scaleMatrix.alignWith(matrix);
	}

	public void setGridBuilt(MatrixType mt, boolean built) {
		Logging.instance.log("Setting matrix "+mt+" status: "+built);
		if (built)
			invalidMatrices.remove(mt);
		else
			invalidMatrices.add(mt);

		if (invalidMatrices.isEmpty())
			this.queueMatrixAlign();
	}

	public void updateMatrixStatus(Consumable c) {
		if (skipNotify)
			return;
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
		this.registerSupply(res);
		if (loading)
			return;
		this.rebuildFlows();
		this.updateMatrixStatus(res.getResource());
		this.notifyListeners(c -> c.onAddSupply(res));
		if (!skipNotify)
			this.queueMatrixAlign();
	}

	public void addExternalSupplies(Collection<? extends ResourceSupply> c) {
		HashSet<Consumable> set = new HashSet();
		for (ResourceSupply res : c) {
			this.registerSupply(res);
			set.add(res.getResource());
		}
		if (loading)
			return;
		this.rebuildFlows();
		for (Consumable item : set)
			this.updateMatrixStatus(item);
		this.notifyListeners(l -> l.onAddSupplies(c));
		if (!skipNotify)
			this.queueMatrixAlign();
	}

	private void registerSupply(ResourceSupply res) {
		resourceSources.addValue(res.getResource(), res);
		if (res instanceof FromFactorySupply) {
			FromFactorySupply ffr = (FromFactorySupply)res;
			factorySources.addValue(ffr.sourceFactory, ffr);
		}
	}

	public void removeExternalSupply(ResourceSupply res) {
		this.unregisterSupply(res);
		if (loading)
			return;
		this.rebuildFlows();
		this.updateMatrixStatus(res.getResource());
		this.notifyListeners(c -> c.onRemoveSupply(res));
		if (!skipNotify)
			this.queueMatrixAlign();
	}

	public void removeExternalSupplies(Collection<? extends ResourceSupply> c) {
		HashSet<Consumable> items = new HashSet();
		for (ResourceSupply res : c) {
			items.add(res.getResource());
			this.unregisterSupply(res);
		}
		if (loading)
			return;
		this.rebuildFlows();
		for (Consumable cc : items)
			this.updateMatrixStatus(cc);
		this.notifyListeners(l -> l.onRemoveSupplies(c));
		if (!skipNotify)
			this.queueMatrixAlign();
	}

	private void changeExternalSupplies(Collection<? extends ResourceSupply> add, Collection<? extends ResourceSupply> remove) {
		HashSet<Consumable> set = new HashSet();
		for (ResourceSupply res : add) {
			this.registerSupply(res);
			set.add(res.getResource());
		}

		for (ResourceSupply res : remove) {
			this.unregisterSupply(res);
			set.add(res.getResource());
		}

		if (loading)
			return;

		this.rebuildFlows();
		for (Consumable item : set)
			this.updateMatrixStatus(item);

		this.notifyListeners(l -> l.onAddSupplies(add));
		this.notifyListeners(l -> l.onRemoveSupplies(remove));

		if (!skipNotify)
			this.queueMatrixAlign();
	}

	private void unregisterSupply(ResourceSupply res) {
		resourceSources.remove(res.getResource(), res);
		if (res instanceof FromFactorySupply) {
			FromFactorySupply ffr = (FromFactorySupply)res;
			factorySources.remove(ffr.sourceFactory, ffr);
		}
	}

	public void addFactorySupplies(File f) {
		if (f != null) {
			this.handleFactoryFromFile(f, fac -> {
				this.addFactorySupplies(f, fac);
			});
		}
	}

	private void addFactorySupplies(File f, Factory fac) {
		Collection<FromFactorySupply> li = new ArrayList();
		for (Consumable c : fac.getDesiredProducts()) { //could also look at all produced items but this seems more right
			float amt = fac.getTotalProduction(c)+fac.getExternalInput(c, false)-fac.getTotalConsumption(c);
			if (amt > 0) {
				li.add(new FromFactorySupply(c, amt, fac.name, f));
			}
		}
		this.addExternalSupplies(li);
	}

	public void updateFactorySupplies(File f) {
		if (f != null) {
			this.handleFactoryFromFile(f, fac -> {
				this.removeExternalSupplies(factorySources.get(fac.name));
				this.addFactorySupplies(f, fac);
			});
		}
	}

	private void handleFactoryFromFile(File f, Consumer<Factory> c) {
		AtomicReference<Factory> ref = new AtomicReference();
		GuiUtil.queueTask("Parsing factory as input", (id) -> {
			Future<Factory> fut = Factory.loadFactoryData(f);
			while (!fut.isDone())
				Thread.sleep(50);
			ref.set(fut.get());
		}, (id) -> { //this part needs to be done on JFX because adding to factory adds to UI
			c.accept(ref.get());
		});
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
		if (loading)
			return;
		this.updateMatrixStatus(c);
		this.notifyListeners(l -> l.onAddProduct(c));
	}

	public void removeProduct(Consumable c) {
		if (desiredProducts.remove(c)) {
			if (!loading) {
				this.updateMatrixStatus(c);
				this.notifyListeners(l -> l.onRemoveProduct(c));
			}
		}
	}

	public void removeProducts(Collection<Consumable> c) {
		if (desiredProducts.removeAll(c)) {
			if (!loading) {
				for (Consumable cc : c)
					this.updateMatrixStatus(cc);
				this.notifyListeners(l -> l.onRemoveProducts(c));
			}
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

	public Set<Consumable> getAllMinedItems() {
		return mines.getItems();
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

	public float getExternalInput(Consumable c, boolean minesOnly) {
		return minesOnly ? mines.get(c) : externalInput.get(c);
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
		if (loading)
			return;
		this.rebuildFlows();
		if (invalidMatrices.isEmpty())
			this.notifyListeners(c -> c.onSetCount(r, amt));
		if (!skipNotify)
			this.queueMatrixAlign();
	}

	public int getCount(Generator g, Fuel f) {
		return generators.get(g).getCount(f);
	}

	public int getTotalGeneratorCount() {
		int ret = 0;
		for (FuelChoices fc : generators.values())
			ret += fc.getTotal();
		return ret;
	}

	public void setCount(Generator g, Fuel f, int amt) {
		int old = generators.get(g).getCount(f);
		generators.get(g).setCount(f, amt);
		if (loading)
			return;
		this.rebuildFlows();
		this.notifyListeners(c -> c.onSetCount(g, f, old, amt));
		if (!skipNotify)
			this.queueMatrixAlign();
	}

	public void updateIO() {
		this.rebuildFlows();
		this.notifyListeners(c -> c.onUpdateIO());
	}

	public void updateResourceSupply(ResourceSupply r) {
		if (loading)
			return;
		this.notifyListeners(c -> c.onUpdateSupply(r));
		this.updateIO();
	}

	private void rebuildFlows() {
		if (skipNotify)
			return;
		//		flow.clear();
		production.clear();
		mines.clear();
		externalInput.clear();
		consumption.clear();
		for (Recipe r : recipes.keySet()) {
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
			if (r instanceof ExtractableResource)
				mines.add(r.getResource(), r.getYield());
		}
		if (generatorMatrixRule != InclusionPattern.EXCLUDE) {
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
	public CountMap<Building> getBuildings() {
		CountMap<Building> map = new CountMap();
		for (Recipe r : recipeList)
			map.increment(r.productionBuilding, (int)Math.ceil(this.getCount(r)));
		for (FuelChoices f : generators.values())
			map.increment(f.generator, f.getTotal());
		for (ResourceSupply res : resourceSources.allValues(false)) {
			Building b = res.getBuilding();
			if (b != null)
				map.increment(b, res.getBuildingCount());
			if (res instanceof FrackingCluster) {
				map.increment(Database.lookupBuilding("Desc_FrackingExtractor_C"), ((FrackingCluster)res).getNodeCount());
			}
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

	public void computeNetPowerProduction(float[] avgMinMax) {
		avgMinMax[0] = 0;
		avgMinMax[1] = 0;
		avgMinMax[2] = 0;
		for (Generator g : generators.keySet()) {
			avgMinMax[0] += g.powerGenerationMW*this.getCount(g);
		}
		avgMinMax[1] = avgMinMax[0];
		avgMinMax[2] = avgMinMax[0];
		for (Recipe r : recipeList) {
			avgMinMax[0] -= r.getPowerCost()*this.getCount(r);
			avgMinMax[1] -= r.getMinPowerCost()*this.getCount(r);
			avgMinMax[2] -= r.getMaxPowerCost()*this.getCount(r);
		}
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
		return this.getTotalProduction(c)+this.getExternalInput(c, false) > this.getTotalConsumption(c);
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
			float has = this.getExternalInput(c, false)+this.getTotalProduction(c);
			float need = this.getTotalConsumption(c);
			if (has < need) {
				call.accept(new InsufficientResourceWarning(c, need, has));
			}
		}
		for (Consumable c : this.getAllProducedItems()) {
			float prod = this.getTotalProduction(c);
			float sup = this.getExternalInput(c, false);
			float has = prod+sup;
			float need = this.getTotalConsumption(c);
			boolean wanted = desiredProducts.contains(c);
			if (has > need && !wanted) {
				call.accept(new ExcessResourceWarning(c, need, has));
			}
			else if (wanted && Math.abs(has-need) < 0.1 && has > 0 && need > 0) {
				call.accept(new NoSurplusResourceWarning(c));
			}
			RateLimitedSupplyLine lim = c instanceof Fluid ? PipeTier.TWO : BeltTier.SIX;
			if (has > lim.getMaxThroughput())
				call.accept(new MultipleBeltsWarning(c, has, lim));
		}
		for (RecipeProductLoop p : recipeLoops) {
			String msg = "A production loop exists between "+p.recipe1.displayName+" and "+p.recipe2.displayName;
			if (this.getExternalInput(p.item1, false) > 0)
				call.accept(new Warning(p.item1 instanceof Fluid ? WarningSeverity.SEVERE : WarningSeverity.MINOR, msg+", with "+p.item1.displayName+" also being supplied externally. This risks a deadlock", new ResourceIconName(p.item1)));
			if (this.getExternalInput(p.item2, false) > 0)
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
			GuiUtil.queueTask("Saving factory", (id) -> this.save(currentFile));
	}

	public void save(File f) {
		GuiUtil.queueTask("Saving "+f.getAbsolutePath(), (id) -> {
			JSONObject root = new JSONObject();
			JSONArray rec = new JSONArray();
			JSONArray gens = new JSONArray();
			JSONArray resources = new JSONArray();
			JSONArray products = new JSONArray();
			JSONArray toggs = new JSONArray();
			root.put("name", name);

			for (Recipe r : recipeList) {
				JSONObject block = new JSONObject();
				block.put("id", r.id);
				block.put("count", String.format("%.3f", recipes.get(r)));
				rec.put(block);
			}
			root.put("recipes", rec);

			for (Generator g : generators.keySet()) {
				JSONObject block = new JSONObject();
				block.put("id", g.id);
				for (Fuel ff : g.getFuels()) {
					block.put("count_"+ff.item.id, generators.get(g).getCount(ff));
				}
				gens.put(block);
			}
			root.put("generators", gens);

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

			for (ToggleableVisiblityGroup c : toggles) {
				toggs.put(c.name());
			}
			root.put("toggles", toggs);

			JSONUtil.saveFile(f, root);
			this.setCurrentFile(f);
		});
	}

	public void reload() {
		GuiUtil.queueTask("Reloading factory from disk", (id) -> this.load(currentFile, id));
	}

	private void load(File f, UUID taskID) throws Exception {
		skipNotify = true;
		loading = true;
		this.clear();

		this.setCurrentFile(f);

		WaitDialogManager.instance.setTaskProgress(taskID, 5);

		JSONObject root = JSONUtil.readFile(f);
		WaitDialogManager.instance.setTaskProgress(taskID, 15);
		name = root.getString("name");

		JSONArray rec = root.getJSONArray("recipes");
		JSONArray gens = root.getJSONArray("generators");
		JSONArray resources = root.getJSONArray("resources");
		JSONArray products = root.getJSONArray("products");
		JSONArray toggs = root.has("toggles") ? root.getJSONArray("toggles") : null;
		WaitDialogManager.instance.setTaskProgress(taskID, 20);
		for (Object o : rec) {
			JSONObject block = (JSONObject)o;
			Recipe r = Database.lookupRecipe(block.getString("id"));
			/*
			recipeList.add(r);
			this.recipes.set(r, block.getInt("count"));
			 */
			this.addRecipe(r);
			this.setCount(r, block.getFloat("count"));
		}
		WaitDialogManager.instance.setTaskProgress(taskID, 50);
		for (Object o : gens) {
			JSONObject block = (JSONObject)o;
			String id = block.getString("id");
			if (id.equalsIgnoreCase("Build_GeneratorBiomass_C"))
				id = "Build_GeneratorBiomass_Automated_C";
			Generator r = (Generator)Database.lookupBuilding(id);
			for (Fuel ff : r.getFuels()) {
				String key = "count_"+ff.item.id;
				if (block.has(key))
					generators.get(r).setCount(ff, block.getInt(key));
			}
		}
		WaitDialogManager.instance.setTaskProgress(taskID, 60);
		for (Object o : resources) {
			JSONObject block = (JSONObject)o;
			ResourceSupplyType type = ResourceSupplyType.valueOf(block.getString("type"));
			ResourceSupply res = type.construct(block);
			resourceSources.addValue(res.getResource(), res);
		}
		WaitDialogManager.instance.setTaskProgress(taskID, 75);
		for (Object o : products) {
			desiredProducts.add(Database.lookupItem((String)o));
		}
		WaitDialogManager.instance.setTaskProgress(taskID, 80);
		if (toggs == null) {
			toggles.addAll(EnumSet.allOf(ToggleableVisiblityGroup.class));
		}
		else {
			for (Object o : toggs) {
				String s = (String)o;
				if (s.equalsIgnoreCase("POST10")) //ignore removed group
					continue;
				toggles.add(ToggleableVisiblityGroup.valueOf(s));
			}
		}
		WaitDialogManager.instance.setTaskProgress(taskID, 90);

		this.init(90, taskID);
	}

	public void init(double pct, UUID taskID) throws Exception {
		skipNotify = false;
		this.rebuildFlows();
		pct += 5;
		skipNotify = true;
		WaitDialogManager.instance.setTaskProgress(taskID, pct);
		ArrayList<Future<Void>> waits = new ArrayList();
		for (FactoryListener rr : changeCallback)
			waits.add(rr.onLoaded());
		double each = (98-pct)/waits.size();
		while (!waits.isEmpty()) {
			while (!waits.get(0).isDone()) {
				Thread.sleep(20);
			}
			waits.remove(0);
			pct += each;
			WaitDialogManager.instance.setTaskProgress(taskID, pct);
		}
		WaitDialogManager.instance.setTaskProgress(taskID, 98);
		this.queueMatrixAlign();
		skipNotify = false;
		loading = false;
	}

	private void setCurrentFile(File f) {
		currentFile = f;
		this.notifyListeners(c -> c.onSetFile(f));
		Main.addRecentFile(f);
		GuiSystem.getMainGUI().controller.buildRecentList();
	}

	public static Future<Factory> loadFactory(File f, FactoryListener... l) {
		return doLoadFactory(f, () -> new Factory(false), l);
	}

	public static Future<Factory> loadFactoryData(File f, FactoryListener... l) {
		return doLoadFactory(f, () -> new Factory(true), l);
	}

	private static Future<Factory> doLoadFactory(File f, Callable<Factory> call, FactoryListener... l) {
		String msg = "Loading factory from "+f.getAbsolutePath();
		CompletableFuture<Factory> fut = new CompletableFuture();
		Logging.instance.log(msg);
		GuiUtil.queueTask(msg, (id) -> {
			Factory ret = call.call();
			for (FactoryListener fl : l) {
				fl.setFactory(ret);
			}
			ret.load(f, id);
			fut.complete(ret);
		});
		return fut;
	}

	public void setUI(RecipeMatrixContainer gui) {
		this.gui = gui;
		matrix.setUI(gui);
		scaleMatrix.setUI(gui);
	}

	public void prepareDisposal() {
		skipNotify = true;
	}

	public int getMaxTier() {
		int ret = 0;
		for (Recipe r : recipeList) {
			ret = Math.max(ret, r.getTier());
		}
		for (FuelChoices g : generators.values()) {
			if (g.getTotal() > 0 && g.generator.getRecipe() != null)
				ret = Math.max(ret, g.generator.getRecipe().getTier());
		}
		return ret;
	}

	public void setLargeMatrixSpinnerStep(boolean large) {
		scaleMatrix.setSpinnerStep(large ? 10 : 1);
	}

}
