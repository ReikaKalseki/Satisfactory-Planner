package Reika.SatisfactoryPlanner.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import Reika.SatisfactoryPlanner.GUI.ControllerBase;
import Reika.SatisfactoryPlanner.GUI.RecipeMatrix;
import Reika.SatisfactoryPlanner.GUI.ScaledRecipeMatrix;
import Reika.SatisfactoryPlanner.Util.FactoryListener;
import Reika.SatisfactoryPlanner.Util.MultiMap;

import javafx.scene.layout.GridPane;

public class Factory {

	private final ArrayList<Recipe> recipes = new ArrayList();

	private final ArrayList<FactoryListener> changeCallback = new ArrayList();

	private final RecipeMatrix matrix = new RecipeMatrix(this);
	private final ScaledRecipeMatrix scaleMatrix = new ScaledRecipeMatrix(matrix);

	private final MultiMap<Consumable, ExtractableResource> resourceSources = new MultiMap();
	private final MultiMap<Consumable, LogisticSupply> externalSupplies = new MultiMap();
	private final ArrayList<Consumable> desiredProducts = new ArrayList();

	public Factory() {

	}

	public Factory addCallback(FactoryListener r) {
		changeCallback.add(r);
		return this;
	}

	public void addRecipe(Recipe r) {
		if (r == null || recipes.contains(r))
			return;
		recipes.add(r);
		Collections.sort(recipes);
		for (FactoryListener rr : changeCallback)
			rr.onAddRecipe(r);
	}

	public void removeRecipe(Recipe r) {
		if (recipes.remove(r)) {
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

	public int getExternalSupply(Consumable c) {
		int ret = 0;
		for (ExtractableResource res : resourceSources.get(c))
			ret += res.getYield();
		for (LogisticSupply res : externalSupplies.get(c))
			ret += res.getYield();
		return ret;
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
		return Collections.unmodifiableList(recipes);
	}

	public Map<Item, Integer> getConstructionCost() {

	}

	public int getNetPowerProduction() {

	}

}
