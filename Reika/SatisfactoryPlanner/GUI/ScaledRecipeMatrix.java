package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Recipe;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.GuiInstance;
import Reika.SatisfactoryPlanner.GUI.ItemViewController.WarningState;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.Region;

public class ScaledRecipeMatrix extends RecipeMatrixBase {

	private final RecipeMatrix parent;

	protected final GapColumn countGapColumn = new GapColumn(0);
	protected final MatrixColumn countColumn = new MatrixColumn();

	protected final MatrixRow sumGapRow = new DividerRow(1);
	protected final MatrixRow sumsRow = new SumsRow();

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
	protected RecipeRow addRecipeRow(Recipe r) throws IOException {
		RecipeRow rr = super.addRecipeRow(r);

		Spinner<Double> counter = new Spinner();
		GuiUtil.setupCounter(counter, 0, 9999, parent.owner.getCount(r), true);
		counter.setPrefHeight(32);
		counter.setMinHeight(Region.USE_PREF_SIZE);
		counter.setMaxHeight(Region.USE_PREF_SIZE);
		counter.valueProperty().addListener((val, old, nnew) -> {
			if (nnew != null)
				parent.owner.setCount(r, nnew.floatValue());
		});
		rr.addNode(countColumn, counter);
		return rr;
	}

	@Override
	protected void addTitles() {
		super.addTitles();

		titlesRow.addTitle("Counts", countColumn);
	}

	@Override
	protected void onAddItem(Consumable c, boolean isInput) {
		try {
			GuiInstance gui = this.gui.loadNestedFXML("ItemView", p -> {});
			((ItemViewController)gui.controller).setItem(c, isInput ? owner.getTotalConsumption(c) : owner.getTotalProduction(c));
			(isInput ? sumEntriesIn : sumEntriesOut).put(c, gui);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onRemoveItem(Consumable c, boolean isInput) {
		(isInput ? sumEntriesIn : sumEntriesOut).remove(c);
	}

	@Override
	public void onSetCount(Recipe r, float count) {
		recipeEntries.get(r).setScale(count);
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

	@Override
	public void onLoaded() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCleared() {
		// TODO Auto-generated method stub

	}

	private class SumsRow extends MatrixRow {

		@Override
		protected Node getNode(MatrixColumn c) {
			if (c instanceof ItemColumn) {
				int idx = ScaledRecipeMatrix.this.grid.getColumns().indexOf(c);
				ItemColumn ic = (ItemColumn)c;
				boolean isOutput = !ic.isInput;
				//Logging.instance.log("Sum fetch for column "+ic+" = "+ic.item+" ("+isOutput+"): "+(isOutput ? sumEntriesOut : sumEntriesIn).get(ic.item));
				GuiInstance gui = (isOutput ? sumEntriesOut : sumEntriesIn).get(ic.item);
				return gui == null ? null : gui.rootNode;
			}
			return null;
		}

	}

}
