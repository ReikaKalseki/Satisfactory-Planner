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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ScaledRecipeMatrix extends RecipeMatrixBase {

	private final RecipeMatrix parent;

	protected GridLine<ColumnConstraints> countGapColumn;
	protected GridLine<ColumnConstraints> countColumn;

	protected GridLine<RowConstraints> sumGapRow;
	protected GridLine<RowConstraints> sumsRow;

	protected Label countLabel;

	private final HashMap<GuiInstance, Consumable> sumEntriesIn = new HashMap();
	private final HashMap<GuiInstance, Consumable> sumEntriesOut = new HashMap();

	private boolean buildingGrid;

	public ScaledRecipeMatrix(RecipeMatrix r) {
		super(r.owner);
		parent = r;
		parent.owner.addCallback(this);
	}

	@Override
	protected float getMultiplier(Recipe r) {
		return buildingGrid ? 1 : owner.getCount(r);
	}

	@Override
	public void createGrid(ControllerBase con) throws IOException {
		buildingGrid = true;
		List<Recipe> recipes = this.getRecipes();
		this.computeIO();
		titlesRow = this.addRow();
		titleGapRow = this.addRow();
		minorRowGaps.clear();
		for (int i = 0; i < recipes.size(); i++) {
			RecipeRow rr = this.addRecipeRow(con, recipes.get(i), i);
			if (i < recipes.size()-1)
				minorRowGaps.add(rr.createGap()); //separator
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

		ingredientsStartColumn = /*2*/mainGapColumn.index+1;
		productsStartColumn = /*2+in.size()+1*/inoutGapColumn.index+1;

		recipeEntries.clear();

		this.createDivider(mainGapColumn, titlesRow, 0);
		this.createDivider(inoutGapColumn, titlesRow, 1);
		this.createDivider(buildingGapColumn, titlesRow, 1);
		this.createDivider(countGapColumn, titlesRow, 0);
		sumGapRow = this.addRow();
		sumsRow = this.addRow();
		this.createRowDivider(titleGapRow, 0);
		this.createRowDivider(sumGapRow, 1);
		for (GridLine<RowConstraints> row : minorRowGaps)
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
			GuiInstance gui = con.loadNestedFXML("ItemView", grid, idx, sumsRow.index);
			((ItemViewController)gui.controller).setItem(c, owner.getTotalConsumption(c));
			sumEntriesIn.put(gui, c);
			if (i < inputs.size()-1)
				this.createDivider(idx+1, sumsRow.index, 2);
		}
		for (int i = 0; i < outputs.size(); i++) {
			Consumable c = outputs.get(i);
			int idx = productsStartColumn+outputs.indexOf(c)*2;
			GuiInstance gui = con.loadNestedFXML("ItemView", grid, idx, sumsRow.index);
			((ItemViewController)gui.controller).setItem(c, owner.getTotalProduction(c));
			sumEntriesOut.put(gui, c);
			if (i < outputs.size()-1)
				this.createDivider(idx+1, sumsRow.index, 2);
		}

		this.addTitles();
		//gp.setGridLinesVisible(true);
	}

	@Override
	protected RecipeRow addRecipeRow(ControllerBase con, Recipe r, int i) throws IOException {
		RecipeRow rowIndex = super.addRecipeRow(con, r, i);

		this.createDivider(countGapColumn, rowIndex.mainRow, 0);
		Spinner<Double> counter = new Spinner();
		GuiUtil.setupCounter(counter, 0, 9999, parent.owner.getCount(r), true);
		counter.setPrefHeight(32);
		counter.setMinHeight(Region.USE_PREF_SIZE);
		counter.setMaxHeight(Region.USE_PREF_SIZE);
		counter.valueProperty().addListener((val, old, nnew) -> {
			if (nnew != null)
				parent.owner.setCount(r, nnew.floatValue());
		});
		grid.add(counter, countColumn.index, rowIndex.getRowIndex());
		return rowIndex;
	}

	@Override
	protected void addTitles() {
		super.addTitles();

		countLabel = new Label("Counts");
		countLabel.setFont(Font.font(countLabel.getFont().getFamily(), FontWeight.BOLD, 16));
		grid.add(countLabel, countColumn.index, titlesRow.index);
		grid.setColumnSpan(countLabel, GridPane.REMAINING);
	}

	@Override
	public List<Recipe> getRecipes() {
		return parent.getRecipes();
	}

	@Override
	public void onSetCount(Recipe r, float count) {
		recipeEntries.get(r).setScale(count);
		for (Entry<GuiInstance, Consumable> gui : sumEntriesIn.entrySet()) {
			Consumable c = gui.getValue();
			float total = owner.getTotalConsumption(c);
			ItemViewController cc = (ItemViewController)gui.getKey().controller;
			cc.setItem(c, total);
			cc.setState(total > owner.getTotalAvailable(c) ? WarningState.INSUFFICIENT : WarningState.NONE);
		}
		for (Entry<GuiInstance, Consumable> gui : sumEntriesOut.entrySet()) {
			Consumable c = gui.getValue();
			ItemViewController cc = (ItemViewController)gui.getKey().controller;
			cc.setItem(c, owner.getTotalProduction(c));
			cc.setState(owner.isExcess(c) ? WarningState.LEFTOVER : WarningState.NONE);
		}
	}

	@Override
	public void onLoaded() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCleared() {
		// TODO Auto-generated method stub

	}

}
