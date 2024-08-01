package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Fuel;
import Reika.SatisfactoryPlanner.Data.Generator;
import Reika.SatisfactoryPlanner.Data.ItemConsumerProducer;
import Reika.SatisfactoryPlanner.Data.Recipe;
import Reika.SatisfactoryPlanner.GUI.ItemRateController.WarningState;

import fxexpansions.GuiInstance;
import javafx.geometry.Insets;
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

	private final HashMap<Consumable, GuiInstance<ItemRateController>> sumEntriesIn = new HashMap();
	private final HashMap<Consumable, GuiInstance<ItemRateController>> sumEntriesOut = new HashMap();

	private boolean buildingGrid;

	public ScaledRecipeMatrix(RecipeMatrix r) {
		super(r.owner);
		parent = r;
	}

	@Override
	protected float getMultiplier(ItemConsumerProducer r) {
		if (buildingGrid)
			return 1;
		if (r instanceof Recipe)
			return owner.getCount((Recipe)r);
		if (r instanceof Fuel) {
			Fuel f = (Fuel)r;
			return owner.getCount(f.generator, f);
		}
		return 1;
	}

	@Override
	public void rebuildGrid() throws IOException {
		buildingGrid = true;
		List<ItemConsumerProducer> recipes = this.getRecipes();
		this.computeIO();
		titlesRow = this.addRow();
		titleGapRow = this.addRow();
		minorRowGaps.clear();
		for (int i = 0; i < recipes.size(); i++) {
			this.addRow();
			if (i < recipes.size()-1)
				minorRowGaps.add(this.addRow()); //separator
		}
		buttonColumn = this.addColumn(); //reset
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
			GuiInstance<ItemRateController> gui = GuiUtil.createItemView(c, owner.getTotalConsumption(c), grid, idx, sumsRow);
			sumEntriesIn.put(c, gui);
			if (i < inputs.size()-1)
				this.createDivider(idx+1, sumsRow, 2);
		}
		for (int i = 0; i < outputs.size(); i++) {
			Consumable c = outputs.get(i);
			int idx = productsStartColumn+outputs.indexOf(c)*2;
			GuiInstance<ItemRateController> gui = GuiUtil.createItemView(c, owner.getTotalProduction(c), grid, idx, sumsRow);
			sumEntriesOut.put(c, gui);
			if (i < outputs.size()-1)
				this.createDivider(idx+1, sumsRow, 2);
		}

		for (ItemConsumerProducer r : this.getRecipes())
			if (r instanceof Recipe)
				this.onSetCount((Recipe)r, owner.getCount((Recipe)r));

		this.addTitles();
	}

	@Override
	protected RecipeRow addRecipeRow(ItemConsumerProducer r, int i) throws IOException {
		RecipeRow rowIndex = super.addRecipeRow(r, i);

		this.createDivider(countGapColumn, rowIndex.rowIndex, 0);
		if (r instanceof Recipe) {
			Spinner<Double> counter = new Spinner();
			GuiUtil.setupCounter(counter, 0, 9999, parent.owner.getCount((Recipe)r), true);
			counter.setPrefHeight(32);
			counter.setMinHeight(Region.USE_PREF_SIZE);
			counter.setMaxHeight(Region.USE_PREF_SIZE);
			counter.valueProperty().addListener((val, old, nnew) -> {
				if (nnew != null)
					parent.owner.setCount((Recipe)r, nnew.floatValue());
			});
			grid.add(counter, countColumn, rowIndex.rowIndex);
			recipeEntries.get(r).addChildNode(counter, "counter");
		}
		else if (r instanceof Fuel) {
			Fuel f = (Fuel)r;
			Label lb = new Label(String.valueOf(owner.getCount(f.generator, f)));
			lb.setPadding(new Insets(2, 2, 2, 8));
			grid.add(lb, countColumn, rowIndex.rowIndex);
		}
		return rowIndex;
	}

	@Override
	protected void onClickPrefixButton(Recipe r) {
		//owner.setCount(r, 0);
		((Spinner<Double>)recipeEntries.get(r).getChildNode("counter")).getValueFactory().setValue(0D); //will trigger the listener above to update
	}

	@Override
	protected String getPrefixButtonIcon() {
		return "reset";
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
		this.updateStatuses(r);
	}

	@Override
	public void onSetCount(Generator g, Fuel fuel, int old, int count) {
		if ((old <= 0 && count > 0) || (count <= 0 && old > 0)) {
			this.rebuild();
			for (ItemConsumerProducer i : recipeEntries.keySet()) {
				if (i instanceof Recipe)
					this.updateStatuses((Recipe)i);
				if (i instanceof Fuel)
					this.updateStatuses((Fuel)i);
			}
		}
		if (count > 0)
			recipeEntries.get(fuel).setScale(count);
		this.updateStatuses(fuel);
	}

	private void updateStatuses(Recipe r) {
		for (Consumable c : r.getIngredientsPerMinute().keySet())
			this.updateStatuses(c);
		for (Consumable c : r.getProductsPerMinute().keySet())
			this.updateStatuses(c);
	}

	private void updateStatuses(Fuel fuel) {
		this.updateStatuses(fuel.item);
		if (fuel.secondaryItem != null)
			this.updateStatuses(fuel.secondaryItem);
		if (fuel.byproduct != null)
			this.updateStatuses(fuel.byproduct);
	}

	@Override
	public void updateStatuses(Consumable c) {
		GuiInstance<ItemRateController> gui = sumEntriesIn.get(c);
		if (gui != null) {
			float total = owner.getTotalConsumption(c);
			gui.controller.setAmount(total);
			gui.controller.setState(total > owner.getTotalProduction(c)+owner.getExternalInput(c) ? WarningState.INSUFFICIENT : WarningState.NONE);
		}

		gui = sumEntriesOut.get(c);
		if (gui != null) {
			gui.controller.setAmount(owner.getTotalProduction(c));
			gui.controller.setState(owner.isExcess(c) ? WarningState.LEFTOVER : WarningState.NONE);
		}
	}

}
