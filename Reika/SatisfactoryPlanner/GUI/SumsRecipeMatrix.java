package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import Reika.SatisfactoryPlanner.InclusionPattern;
import Reika.SatisfactoryPlanner.Setting;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.ItemConsumerProducer;
import Reika.SatisfactoryPlanner.Data.Objects.Consumable;
import Reika.SatisfactoryPlanner.Data.Objects.Fuel;
import Reika.SatisfactoryPlanner.Data.Objects.Recipe;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.Generator;
import Reika.SatisfactoryPlanner.GUI.RecipeMatrixContainer.MatrixType;
import Reika.SatisfactoryPlanner.GUI.Components.ItemRateController;
import Reika.SatisfactoryPlanner.GUI.Components.ItemRateController.WarningState;

import fxexpansions.GuiInstance;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class SumsRecipeMatrix extends RecipeMatrixBase {

	protected int sumRowIn;
	protected int sumRowOut;

	protected int inOutGapRow;

	protected Label sumsLabelIn;
	protected Label sumsLabelOut;

	private final HashMap<Consumable, GuiInstance<ItemRateController>> sumEntriesIn = new HashMap();
	private final HashMap<Consumable, GuiInstance<ItemRateController>> sumEntriesOut = new HashMap();

	private boolean buildingGrid;
	private boolean settingValue;

	public SumsRecipeMatrix(Factory f) {
		super(f, MatrixType.SUM);
	}

	@Override
	protected double getMultiplier(ItemConsumerProducer r) {
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
		/*this.computeIO();*/
		this.getItemSet();
		titlesRow = this.addRow();
		titleGapRow = this.addRow();
		minorRowGaps.clear();

		recipeStartRow = -1;

		nameColumn = this.addColumn(); //name
		mainGapColumn = this.addColumn(); //separator
		minorColumnGaps.clear();

		this.addItemColumns();

		buildingGapColumn = -1;
		buildingColumn = -1;

		itemsStartColumn = /*2*/mainGapColumn+1;

		for (int i = 0; i < recipes.size(); i++) {
			this.addRecipeRow(recipes.get(i), i);
		}

		this.createDivider(mainGapColumn, titlesRow, 0);

		sumRowOut = this.addRow();
		inOutGapRow = this.addRow();
		sumRowIn = this.addRow();

		this.createRowDivider(titleGapRow, 0);
		this.createRowDivider(inOutGapRow, 1);
		for (int row : minorRowGaps)
			this.createRowDivider(row, 2);

		sumEntriesIn.clear();
		sumEntriesOut.clear();

		this.createDivider(mainGapColumn, sumRowIn, 0);
		this.createDivider(mainGapColumn, sumRowOut, 0);

		buildingGrid = false;

		GridPane gp = this.getGrid();
		for (int i = 0; i < items.size(); i++) {
			Consumable c = items.get(i);
			int idx = itemsStartColumn+items.indexOf(c)*2;
			GuiInstance<ItemRateController> gui = GuiUtil.createItemView(c, owner.getTotalConsumption(c), gp, idx, sumRowIn);
			if (Setting.FIXEDMATRIX.getCurrentValue())
				gui.controller.setMinWidth("9999.9999");
			gui.rootNode.getStyleClass().add("matrix-item-cell");
			sumEntriesIn.put(c, gui);

			gui = GuiUtil.createItemView(c, this.getAvailable(c), gp, idx, sumRowOut);
			if (Setting.FIXEDMATRIX.getCurrentValue())
				gui.controller.setMinWidth("9999.9999");
			gui.rootNode.getStyleClass().add("matrix-item-cell");
			sumEntriesOut.put(c, gui);

			if (i < items.size()-1)
				this.createDivider(idx+1, titleGapRow+1, 2);
		}

		for (ItemConsumerProducer r : recipes) {
			if (r instanceof Recipe)
				this.onSetCount((Recipe)r, owner.getCount((Recipe)r));
			if (r instanceof Fuel) {
				Fuel f = (Fuel)r;
				double amt = owner.getCount(f.generator, f);
				this.onSetCount(f.generator, f, amt, amt);
			}
		}

		this.addTitles();
	}


	protected final double getAvailable(Consumable c) {
		return owner.getTotalProduction(c)+(owner.resourceMatrixRule == InclusionPattern.EXCLUDE ? 0 : owner.getExternalInput(c, false));
	}

	@Override
	protected RecipeRow addRecipeRow(ItemConsumerProducer r, int i) throws IOException {
		return null;
	}

	@Override
	protected void addTitles() {
		super.addTitles();

		sumsLabelIn = new Label("Total Consumption");
		sumsLabelIn.getStyleClass().add("table-header");
		this.getGrid().add(sumsLabelIn, nameColumn, sumRowIn);

		sumsLabelOut = new Label("Total Available");
		sumsLabelOut.getStyleClass().add("table-header");
		this.getGrid().add(sumsLabelOut, nameColumn, sumRowOut);
	}

	@Override
	public void onSetCount(Recipe r, double amt) {
		this.updateStatuses(r);
	}

	@Override
	public final void onSetCount(Generator g, Fuel fuel, double old, double count) {
		super.onSetCount(g, fuel, old, count);
		this.updateStatuses(fuel);
	}

	public void onUpdateIO() {
		for (Consumable c : items) {
			this.updateStatuses(c);
		}
	}

	@Deprecated
	public final void setSpinnerStep(double amount) {/*
		for (RecipeRow rr : recipeEntries.values()) {
			((DoubleSpinnerValueFactory)((Spinner<Double>)rr.getChildNode("counter")).getValueFactory()).setAmountToStepBy(amount);
		}*/
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
			double total = owner.getTotalConsumption(c);
			gui.controller.setAmount(total);
			gui.controller.setState(total > owner.getTotalProduction(c)+owner.getExternalInput(c, false)+0.0001 ? WarningState.INSUFFICIENT : WarningState.NONE);
		}

		gui = sumEntriesOut.get(c);
		if (gui != null) {
			gui.controller.setAmount(this.getAvailable(c));
			gui.controller.setState(owner.isExcess(c) ? WarningState.LEFTOVER : WarningState.NONE);
		}
	}

	@Override
	protected Set<Entry<Consumable, Double>> getItemRates(ItemConsumerProducer r) {
		return null;
	}

	@Override
	protected String getItemHeader() {
		return "Totals";
	}

}
