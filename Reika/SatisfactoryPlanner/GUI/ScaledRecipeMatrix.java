package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Recipe;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.GuiInstance;
import Reika.SatisfactoryPlanner.GUI.ItemViewController.WarningState;
import Reika.SatisfactoryPlanner.Util.CountMap;
import Reika.SatisfactoryPlanner.Util.FactoryListener;

import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ScaledRecipeMatrix extends RecipeMatrixBase implements FactoryListener {

	private final RecipeMatrix parent;

	private final CountMap<Recipe> scales = new CountMap();

	protected int countGapColumn;
	protected int countColumn;

	protected int sumGapRow;
	protected int sumsRow;

	protected Label countLabel;

	private final HashMap<GuiInstance, Consumable> sumEntriesIn = new HashMap();
	private final HashMap<GuiInstance, Consumable> sumEntriesOut = new HashMap();

	private boolean buildingGrid;

	public ScaledRecipeMatrix(RecipeMatrix r) {
		parent = r;
		parent.owner.addCallback(this);
	}

	@Override
	protected int getMultiplier(Recipe r) {
		return buildingGrid ? 1 : scales.get(r);
	}

	@Override
	public GridPane createGrid(ControllerBase con) throws IOException {
		buildingGrid = true;
		GridPane gp = new GridPane();
		List<Recipe> recipes = this.getRecipes();
		this.computeIO();
		titlesRow = this.addRow(gp);
		titleGapRow = this.addRow(gp);
		minorRowGaps.clear();
		for (int i = 0; i < recipes.size(); i++) {
			this.addRow(gp);
			if (i < recipes.size()-1)
				minorRowGaps.add(this.addRow(gp)); //separator
		}
		nameColumn = this.addColumn(gp); //name
		mainGapColumn = this.addColumn(gp); //separator
		minorColumnGaps.clear();

		this.addInputColumns(gp);

		inoutGapColumn = this.addColumn(gp); //separator

		this.addOutputColumns(gp);

		buildingGapColumn = this.addColumn(gp);
		buildingColumn = this.addColumn(gp);

		countGapColumn = this.addColumn(gp);
		countColumn = this.addColumn(gp);

		ingredientsStartColumn = /*2*/mainGapColumn+1;
		productsStartColumn = /*2+in.size()+1*/inoutGapColumn+1;

		recipeEntries.clear();

		for (int i = 0; i < recipes.size(); i++) {
			this.addRecipeRow(con, gp, recipes.get(i), i);
		}
		this.createDivider(gp, mainGapColumn, titlesRow, 0);
		this.createDivider(gp, inoutGapColumn, titlesRow, 1);
		this.createDivider(gp, buildingGapColumn, titlesRow, 1);
		this.createDivider(gp, countGapColumn, titlesRow, 0);
		sumGapRow = this.addRow(gp);
		sumsRow = this.addRow(gp);
		this.createRowDivider(gp, titleGapRow, 0);
		this.createRowDivider(gp, sumGapRow, 1);
		for (int row : minorRowGaps)
			this.createRowDivider(gp, row, 2);

		sumEntriesIn.clear();
		sumEntriesOut.clear();

		this.createDivider(gp, mainGapColumn, sumsRow, 0);
		this.createDivider(gp, countGapColumn, sumsRow, 0);
		this.createDivider(gp, inoutGapColumn, sumsRow, 1);
		this.createDivider(gp, buildingGapColumn, sumsRow, 1);

		buildingGrid = false;

		for (int i = 0; i < inputs.size(); i++) {
			Consumable c = inputs.get(i);
			int idx = ingredientsStartColumn+inputs.indexOf(c)*2;
			GuiInstance gui = con.loadNestedFXML("ItemView", gp, idx, sumsRow);
			((ItemViewController)gui.controller).setItem(c, this.getTotalConsumption(c));
			sumEntriesIn.put(gui, c);
			if (i < inputs.size()-1)
				this.createDivider(gp, idx+1, sumsRow, 2);
		}
		for (int i = 0; i < outputs.size(); i++) {
			Consumable c = outputs.get(i);
			int idx = productsStartColumn+outputs.indexOf(c)*2;
			GuiInstance gui = con.loadNestedFXML("ItemView", gp, idx, sumsRow);
			((ItemViewController)gui.controller).setItem(c, this.getTotalProduction(c));
			sumEntriesOut.put(gui, c);
			if (i < outputs.size()-1)
				this.createDivider(gp, idx+1, sumsRow, 2);
		}

		this.addTitles(gp);

		for (Recipe r : recipes)
			this.setScale(r, scales.get(r));

		gp.setHgap(4);
		gp.setVgap(4);
		//gp.setGridLinesVisible(true);
		return gp;
	}

	@Override
	protected int addRecipeRow(ControllerBase con, GridPane gp, Recipe r, int i) throws IOException {
		int rowIndex = super.addRecipeRow(con, gp, r, i);

		this.createDivider(gp, countGapColumn, rowIndex, 0);
		Spinner<Integer> counter = new Spinner();
		counter.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9999, 0));
		counter.setEditable(true);
		counter.setPrefWidth(72);
		counter.setMinWidth(Region.USE_PREF_SIZE);
		counter.setMaxWidth(Region.USE_PREF_SIZE);
		counter.setPrefHeight(32);
		counter.setMinHeight(Region.USE_PREF_SIZE);
		counter.setMaxHeight(Region.USE_PREF_SIZE);
		counter.valueProperty().addListener((val, old, nnew) -> {
			if (nnew != null)
				this.setScale(r, nnew);
		});
		TextField txt = counter.getEditor();
		txt.textProperty().addListener((val, old, nnew) -> {
			if (nnew.length() > 4)
				txt.setText(nnew.substring(0, 4));
			nnew = nnew.replaceAll("[^\\d.]", "");
			txt.setText(nnew);
		});
		counter.getValueFactory().setValue(scales.get(r));
		gp.add(counter, countColumn, rowIndex);
		return rowIndex;
	}

	@Override
	protected void addTitles(GridPane gp) {
		super.addTitles(gp);

		countLabel = new Label("Counts");
		countLabel.setFont(Font.font(countLabel.getFont().getFamily(), FontWeight.BOLD, 16));
		gp.add(countLabel, countColumn, titlesRow);
		gp.setColumnSpan(countLabel, GridPane.REMAINING);
	}

	private void setScale(Recipe r, int amt) {
		scales.set(r, amt);
		for (GuiInstance gui : recipeEntries.get(r)) {
			((ItemViewController)gui.controller).setScale(amt);
		}
		for (Entry<GuiInstance, Consumable> gui : sumEntriesIn.entrySet()) {
			Consumable c = gui.getValue();
			int total = this.getTotalConsumption(c);
			ItemViewController cc = (ItemViewController)gui.getKey().controller;
			cc.setItem(c, total);
			cc.setState(total > this.getTotalProduction(c)+parent.owner.getExternalSupply(c) ? WarningState.INSUFFICIENT : WarningState.NONE);
		}
		for (Entry<GuiInstance, Consumable> gui : sumEntriesOut.entrySet()) {
			Consumable c = gui.getValue();
			int total = this.getTotalProduction(c);
			ItemViewController cc = (ItemViewController)gui.getKey().controller;
			cc.setItem(c, total);
			cc.setState(!parent.owner.isDesiredFinalProduct(c) && total > this.getTotalConsumption(c) ? WarningState.LEFTOVER : WarningState.NONE);
		}
	}

	@Override
	public List<Recipe> getRecipes() {
		return parent.getRecipes();
	}

	@Override
	public void onAddRecipe(Recipe r) {
		this.setScale(r, 0);

	}

	@Override
	public void onRemoveRecipe(Recipe r) {

	}

	@Override
	public void onAddProduct(Consumable c) {

	}

	@Override
	public void onRemoveProduct(Consumable c) {

	}

}
