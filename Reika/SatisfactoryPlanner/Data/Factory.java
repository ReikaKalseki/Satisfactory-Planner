package Reika.SatisfactoryPlanner.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import Reika.SatisfactoryPlanner.GUI.ControllerBase;
import Reika.SatisfactoryPlanner.GUI.RecipeMatrix;
import Reika.SatisfactoryPlanner.GUI.ScaledRecipeMatrix;
import Reika.SatisfactoryPlanner.Util.CountMap;
import Reika.SatisfactoryPlanner.Util.FactoryListener;
import Reika.SatisfactoryPlanner.Util.MultiMap;

import javafx.scene.layout.GridPane;

public class Factory {

	private final CountMap<Recipe> recipes = new CountMap();
	private final ArrayList<Recipe> recipeList = new ArrayList();

	private final CountMap<Generator> generators = new CountMap();

	private final ArrayList<FactoryListener> changeCallback = new ArrayList();

	private final RecipeMatrix matrix = new RecipeMatrix(this);
	private final ScaledRecipeMatrix scaleMatrix = new ScaledRecipeMatrix(matrix);

	private final MultiMap<Consumable, ExtractableResource> resourceSources = new MultiMap();
	private final MultiMap<Consumable, LogisticSupply> externalSupplies = new MultiMap();
	private final ArrayList<Consumable> desiredProducts = new ArrayList();

	public String name;

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
		for (FactoryListener rr : changeCallback)
			rr.onAddRecipe(r);
	}

	public void removeRecipe(Recipe r) {
		if (recipeList.remove(r)) {
			recipes.remove(r);
			for (FactoryListener rr : changeCallback)
				rr.onRemoveRecipe(r);
		}
	}

	public GridPane createRawMatrix(ControllerBase con) throws IOException {
		return matrix.createGrid(con);
	}

	public GridPane createNetMatrix(ControllerBase con) throws IOException {
		return scaleMatrix.createGrid(con);
	}

	public void addExternalSupply(ExtractableResource res) {
		resourceSources.addValue(res.getResource(), res);
		for (FactoryListener rr : changeCallback)
			rr.onAddSupply(res);
	}

	public void addExternalSupply(LogisticSupply res) {
		externalSupplies.addValue(res.getResource(), res);
		for (FactoryListener rr : changeCallback)
			rr.onAddSupply(res);
	}

	public void removeExternalSupply(ExtractableResource res) {
		resourceSources.remove(res.getResource(), res);
		for (FactoryListener rr : changeCallback)
			rr.onRemoveSupply(res);
	}

	public void removeExternalSupply(LogisticSupply res) {
		externalSupplies.remove(res.getResource(), res);
		for (FactoryListener rr : changeCallback)
			rr.onRemoveSupply(res);
	}

	public int getExternalSupply(Consumable c) {
		int ret = 0;
		for (ExtractableResource res : resourceSources.get(c))
			ret += res.getYield();
		for (LogisticSupply res : externalSupplies.get(c))
			ret += res.getYield();
		return ret;
	}

	public Collection<ExtractableResource> getMines() {
		return Collections.unmodifiableCollection(resourceSources.allValues(false));
	}

	public Collection<LogisticSupply> getSupplies() {
		return Collections.unmodifiableCollection(externalSupplies.allValues(false));
	}

	public boolean isDesiredFinalProduct(Consumable c) {
		return desiredProducts.contains(c);
	}

	public void addProduct(Consumable c) {
		if (desiredProducts.contains(c))
			return;
		desiredProducts.add(c);
		for (FactoryListener rr : changeCallback)
			rr.onAddProduct(c);
	}

	public void removeProduct(Consumable c) {
		if (desiredProducts.remove(c)) {
			for (FactoryListener rr : changeCallback)
				rr.onRemoveProduct(c);
		}
	}

	public List<Recipe> getRecipes() {
		return Collections.unmodifiableList(recipeList);
	}

	public int getCount(Recipe r) {
		return recipes.get(r);
	}

	public void setCount(Recipe r, int amt) {
		recipes.set(r, amt);
		for (FactoryListener rr : changeCallback)
			rr.onSetCount(r, amt);
	}

	public int getCount(Generator g) {
		return generators.get(g);
	}

	public void setCount(Generator g, int amt) {
		generators.set(g, amt);
		for (FactoryListener rr : changeCallback)
			rr.onSetCount(g, amt);
	}

	public CountMap<Building> getBuildings() {
		CountMap<Building> map = new CountMap();
		for (Recipe r : recipeList)
			map.increment(r.productionBuilding, this.getCount(r));
		for (Generator g : generators.keySet())
			map.increment(g, this.getCount(g));
		for (ExtractableResource res : resourceSources.allValues(false))
			map.increment(res.getBuilding(), 1);
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

	public void save() {

	}

	public void load() {

	}

}
