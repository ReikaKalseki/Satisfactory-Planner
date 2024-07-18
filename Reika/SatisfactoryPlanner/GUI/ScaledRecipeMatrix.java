package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Recipe;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.GuiInstance;
import Reika.SatisfactoryPlanner.GUI.ItemViewController.WarningState;

import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ScaledRecipeMatrix extends RecipeMatrixBase {

	private final RecipeMatrix parent;

	protected int countGapColumn;
	protected int countColumn;

	protected int sumGapRow;
	protected int sumsRow;

	protected Label countLabel;

	private final HashMap<Consumable, GuiInstance> sumEntriesIn = new HashMap();
	private final HashMap<Consumable, GuiInstance> sumEntriesOut = new HashMap();

	private boolean buildingGrid;

	public ScaledRecipeMatrix(RecipeMatrix r) {
		super(r.owner);
		parent = r;
	}

	@Override
	protected float getMultiplier(Recipe r) {
		return buildingGrid ? 1 : owner.getCount(r);
	}

	@Override
	public void rebuildGrid() throws IOException {
		buildingGrid = true;
		List<Recipe> recipes = this.getRecipes();
		this.computeIO();
		titlesRow = this.addRow();
		titleGapRow = this.addRow();
		minorRowGaps.clear();
		for (int i = 0; i < recipes.size(); i++) {
			this.addRow();
			if (i < recipes.size()-1)
				minorRowGaps.add(this.addRow()); //separator
		}
		nameColumn = this.addColumn(); //name
		mainGapColumn = this.addColumn(); //separator
		minorColumnGaps.clear();

		this.addInputColumns();

		inoutGapColumn = this.addColumn(); //separator

		this.addOutputColumns();

		buildingGapColumn = this.addColumn();
		buildingColumn = this.addColumn();

		countGapColumn = this.addColumn();
		countColumn = this.addColumn();

		ingredientsStartColumn = /*2*/mainGapColumn+1;
		productsStartColumn = /*2+in.size()+1*/inoutGapColumn+1;

		for (int i = 0; i < recipes.size(); i++) {
			this.addRecipeRow(recipes.get(i), i);
		}
		this.createDivider(mainGapColumn, titlesRow, 0);
		this.createDivider(inoutGapColumn, titlesRow, 1);
		this.createDivider(buildingGapColumn, titlesRow, 1);
		this.createDivider(countGapColumn, titlesRow, 0);
		sumGapRow = this.addRow();
		sumsRow = this.addRow();
		this.createRowDivider(titleGapRow, 0);
		this.createRowDivider(sumGapRow, 1);
		for (int row : minorRowGaps)
			this.createRowDivider(row, 2);

		sumEntriesIn.clear();
		sumEntriesOut.clear();

		this.createDivider(mainGapColumn, sumsRow, 0);
		this.createDivider(countGapColumn, sumsRow, 0);
		this.createDivider(inoutGapColumn, sumsRow, 1);
		this.createDivider(buildingGapColumn, sumsRow, 1);

		buildingGrid = false;

		for (int i = 0; i < inputs.size(); i++) {
			Consumable c = inputs.get(i);
			int idx = ingredientsStartColumn+inputs.indexOf(c)*2;
			GuiInstance gui = this.gui.loadNestedFXML("ItemView", grid, idx, sumsRow);
			((ItemViewController)gui.controller).setItem(c, owner.getTotalConsumption(c));
			sumEntriesIn.put(c, gui);
			if (i < inputs.size()-1)
				this.createDivider(idx+1, sumsRow, 2);
		}
		for (int i = 0; i < outputs.size(); i++) {
			Consumable c = outputs.get(i);
			int idx = productsStartColumn+outputs.indexOf(c)*2;
			GuiInstance gui = this.gui.loadNestedFXML("ItemView", grid, idx, sumsRow);
			((ItemViewController)gui.controller).setItem(c, owner.getTotalProduction(c));
			sumEntriesOut.put(c, gui);
			if (i < outputs.size()-1)
				this.createDivider(idx+1, sumsRow, 2);
		}

		for (Recipe r : this.getRecipes())
			this.onSetCount(r, owner.getCount(r));

		this.addTitles();
	}

	@Override
	protected RecipeRow addRecipeRow(Recipe r, int i) throws IOException {
		RecipeRow rowIndex = super.addRecipeRow(r, i);

		this.createDivider(countGapColumn, rowIndex.rowIndex, 0);
		Spinner<Double> counter = new Spinner();
		GuiUtil.setupCounter(counter, 0, 9999, parent.owner.getCount(r), true);
		counter.setPrefHeight(32);
		counter.setMinHeight(Region.USE_PREF_SIZE);
		counter.setMaxHeight(Region.USE_PREF_SIZE);
		counter.valueProperty().addListener((val, old, nnew) -> {
			if (nnew != null)
				parent.owner.setCount(r, nnew.floatValue());
		});
		grid.add(counter, countColumn, rowIndex.rowIndex);
		return rowIndex;
	}

	@Override
	protected void addTitles() {
		super.addTitles();

		countLabel = new Label("Counts");
		countLabel.setFont(Font.font(countLabel.getFont().getFamily(), FontWeight.BOLD, 16));
		grid.add(countLabel, countColumn, titlesRow);
		grid.setColumnSpan(countLabel, GridPane.REMAINING);
	}

	@Override
	public void onSetCount(Recipe r, float amt) {
		recipeEntries.get(r).setScale(amt);
		for (Entry<Consumable, GuiInstance> gui : sumEntriesIn.entrySet()) {
			Consumable c = gui.getKey();
			float total = owner.getTotalConsumption(c);
			ItemViewController cc = (ItemViewController)gui.getValue().controller;
			cc.setItem(c, total);
			cc.setState(total > owner.getTotalAvailable(c) ? WarningState.INSUFFICIENT : WarningState.NONE);
		}
		for (Entry<Consumable, GuiInstance> gui : sumEntriesOut.entrySet()) {
			Consumable c = gui.getKey();
			ItemViewController cc = (ItemViewController)gui.getValue().controller;
			cc.setItem(c, owner.getTotalProduction(c));
			cc.setState(owner.isExcess(c) ? WarningState.LEFTOVER : WarningState.NONE);
		}
	}

}
