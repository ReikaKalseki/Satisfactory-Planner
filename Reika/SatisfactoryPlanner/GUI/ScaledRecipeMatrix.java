package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import Reika.SatisfactoryPlanner.InclusionPattern;
import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Fuel;
import Reika.SatisfactoryPlanner.Data.Generator;
import Reika.SatisfactoryPlanner.Data.ItemConsumerProducer;
import Reika.SatisfactoryPlanner.Data.Recipe;
import Reika.SatisfactoryPlanner.GUI.ItemRateController.WarningState;
import Reika.SatisfactoryPlanner.GUI.RecipeMatrixContainer.MatrixType;

import fxexpansions.GuiInstance;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

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
	private boolean settingValue;

	public ScaledRecipeMatrix(RecipeMatrix r) {
		super(r.owner, MatrixType.SCALE);
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

		GridPane gp = this.getGrid();
		for (int i = 0; i < inputs.size(); i++) {
			Consumable c = inputs.get(i);
			int idx = ingredientsStartColumn+inputs.indexOf(c)*2;
			GuiInstance<ItemRateController> gui = GuiUtil.createItemView(c, owner.getTotalConsumption(c), gp, idx, sumsRow);
			sumEntriesIn.put(c, gui);
			if (i < inputs.size()-1)
				this.createDivider(idx+1, sumsRow, 2);
		}
		for (int i = 0; i < outputs.size(); i++) {
			Consumable c = outputs.get(i);
			int idx = productsStartColumn+outputs.indexOf(c)*2;
			//Logging.instance.log(c+" @ "+idx+" in "+outputs.indexOf(c)+":"+outputs);
			GuiInstance<ItemRateController> gui = GuiUtil.createItemView(c, this.getAvailable(c), gp, idx, sumsRow);
			sumEntriesOut.put(c, gui);
			if (i < outputs.size()-1)
				this.createDivider(idx+1, sumsRow, 2);
		}

		for (ItemConsumerProducer r : recipes) {
			if (r instanceof Recipe)
				this.onSetCount((Recipe)r, owner.getCount((Recipe)r));
			if (r instanceof Fuel) {
				Fuel f = (Fuel)r;
				int amt = owner.getCount(f.generator, f);
				this.onSetCount(f.generator, f, amt, amt);
			}
		}

		this.addTitles();

		gp.getColumnConstraints().get(countColumn).setMinWidth(92);
	}

	private float getAvailable(Consumable c) {
		return owner.getTotalProduction(c)+(owner.resourceMatrixRule == InclusionPattern.EXCLUDE ? 0 : owner.getExternalInput(c, false));
	}

	@Override
	protected RecipeRow addRecipeRow(ItemConsumerProducer r, int i) throws IOException {
		RecipeRow rowIndex = super.addRecipeRow(r, i);

		this.createDivider(countGapColumn, rowIndex.rowIndex, 0);
		if (r instanceof Recipe) {
			Spinner<Double> counter = new Spinner();
			GuiUtil.setupCounter(counter, 0, 9999, owner.getCount((Recipe)r), true);
			counter.setPrefHeight(32);
			counter.setMinHeight(Region.USE_PREF_SIZE);
			counter.setMaxHeight(Region.USE_PREF_SIZE);
			//counter.getValueFactory().setValue(owner.get);
			counter.valueProperty().addListener((val, old, nnew) -> {
				if (nnew != null) {
					settingValue = true;
					parent.owner.setCount((Recipe)r, nnew.floatValue());
					settingValue = false;
				}
			});
			this.getGrid().add(counter, countColumn, rowIndex.rowIndex);
			recipeEntries.get(r).addChildNode(counter, "counter");
		}
		else if (r instanceof Fuel) {
			Fuel f = (Fuel)r;
			Label lb = new Label(String.valueOf(owner.getCount(f.generator, f)));
			lb.setPadding(new Insets(2, 2, 2, 8));
			this.getGrid().add(lb, countColumn, rowIndex.rowIndex);
		}
		return rowIndex;
	}

	@Override
	protected void onClickPrefixButton(Recipe r) {
		//owner.setCount(r, 0);
		this.zeroRecipeRow(recipeEntries.get(r)); //will trigger the listener above to update
	}

	private void zeroRecipeRow(RecipeRow rr) {
		((Spinner<Double>)rr.getChildNode("counter")).getValueFactory().setValue(0D);
	}

	@Override
	protected String getPrefixButtonIcon() {
		return "reset";
	}

	@Override
	protected void addTitles() {
		super.addTitles();

		countLabel = new Label("Counts");
		countLabel.getStyleClass().add("table-header");
		this.getGrid().add(countLabel, countColumn, titlesRow);
		this.getGrid().setColumnSpan(countLabel, GridPane.REMAINING);
	}

	@Override
	public void onSetCount(Recipe r, float amt) {
		RecipeRow rr = recipeEntries.get(r);
		rr.setScale(amt);
		if (!settingValue && !buildingGrid && this.isGridBuilt())
			this.zeroRecipeRow(rr);
		this.updateStatuses(r);
	}

	@Override
	public void onSetCount(Generator g, Fuel fuel, int old, int count) {
		super.onSetCount(g, fuel, old, count);
		if (count > 0 && recipeEntries.containsKey(fuel))
			recipeEntries.get(fuel).setScale(count);
		this.updateStatuses(fuel);
	}

	public void onUpdateIO() {
		for (ItemConsumerProducer i : recipeEntries.keySet()) {
			this.updateStatuses(i);
		}
	}

	private void updateStatuses(ItemConsumerProducer i) {
		for (Consumable c : i.getIngredientsPerMinute().keySet())
			this.updateStatuses(c);
		for (Consumable c : i.getProductsPerMinute().keySet())
			this.updateStatuses(c);
	}

	@Override
	public void updateStatuses(Consumable c) {
		GuiInstance<ItemRateController> gui = sumEntriesIn.get(c);
		if (gui != null) {
			float total = owner.getTotalConsumption(c);
			gui.controller.setAmount(total);
			gui.controller.setState(total > owner.getTotalProduction(c)+owner.getExternalInput(c, false) ? WarningState.INSUFFICIENT : WarningState.NONE);
		}

		gui = sumEntriesOut.get(c);
		if (gui != null) {
			gui.controller.setAmount(this.getAvailable(c));
			gui.controller.setState(owner.isExcess(c) ? WarningState.LEFTOVER : WarningState.NONE);
		}
	}

	public void setSpinnerStep(double amount) {
		for (RecipeRow rr : recipeEntries.values()) {
			((DoubleSpinnerValueFactory)((Spinner<Double>)rr.getChildNode("counter")).getValueFactory()).setAmountToStepBy(amount);
		}
	}

}
