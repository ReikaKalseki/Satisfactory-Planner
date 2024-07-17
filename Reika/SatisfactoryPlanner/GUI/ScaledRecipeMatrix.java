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
import javafx.scene.control.TableColumn;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ScaledRecipeMatrix extends RecipeMatrixBase {

	private final RecipeMatrix parent;

	protected final GapColumn countGapColumn = new GapColumn(0);
	protected final TableColumn countColumn = new TableColumn();

	protected final MatrixRow sumGapRow = new DividerRow(1);
	protected final MatrixRow sumsRow = new FixedContentRow();

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
	protected void addInitialRows() {
		super.addInitialRows();
		grid.getItems().add(sumGapRow);
		grid.getItems().add(sumsRow);
	}

	@Override
	protected void addInitialColumns() {
		super.addInitialColumns();

		grid.getColumns().add(countGapColumn);
		grid.getColumns().add(countColumn);
	}

	@Override
	public void createGrid() throws IOException {
		buildingGrid = true;
		recipeEntries.clear();

		sumEntriesIn.clear();
		sumEntriesOut.clear();

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
	protected RecipeRow addRecipeRow(Recipe r) throws IOException {
		RecipeRow rowIndex = super.addRecipeRow(r);

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
