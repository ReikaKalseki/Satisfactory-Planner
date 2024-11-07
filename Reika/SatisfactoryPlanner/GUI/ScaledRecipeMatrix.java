package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import Reika.SatisfactoryPlanner.InclusionPattern;
import Reika.SatisfactoryPlanner.Setting;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.ItemConsumerProducer;
import Reika.SatisfactoryPlanner.Data.Objects.Consumable;
import Reika.SatisfactoryPlanner.Data.Objects.Fuel;
import Reika.SatisfactoryPlanner.Data.Objects.Recipe;
import Reika.SatisfactoryPlanner.GUI.RecipeMatrixContainer.MatrixType;
import Reika.SatisfactoryPlanner.GUI.Components.ItemRateController;

import fxexpansions.GuiInstance;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public abstract class ScaledRecipeMatrix extends RecipeMatrixBase {

	protected int countGapColumn;
	protected int countColumn;

	protected int sumGapRow;
	protected int sumsRow;

	protected Label sumsLabel;
	protected Label countLabel;

	protected final HashMap<Consumable, GuiInstance<ItemRateController>> sumEntries = new HashMap();

	private boolean buildingGrid;
	private boolean settingValue;

	public ScaledRecipeMatrix(Factory f, MatrixType type) {
		super(f, type);
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

		if (this.sumAtTop()) {
			sumsRow = this.addRow();
			sumGapRow = this.addRow();
			recipeStartRow = sumGapRow+1;
		}
		else {
			recipeStartRow = titleGapRow+1;
		}
		for (int i = 0; i < recipes.size(); i++) {
			this.addRow();
			if (i < recipes.size()-1)
				minorRowGaps.add(this.addRow()); //separator
		}
		nameColumn = this.addColumn(); //name
		mainGapColumn = this.addColumn(); //separator
		minorColumnGaps.clear();

		this.addItemColumns();

		buildingGapColumn = this.addColumn();
		buildingColumn = this.addColumn();

		countGapColumn = this.addColumn();
		countColumn = this.addColumn();
		//countFracGapColumn = this.addColumn();
		//fractionColumn = this.addColumn();

		itemsStartColumn = /*2*/mainGapColumn+1;

		for (int i = 0; i < recipes.size(); i++) {
			this.addRecipeRow(recipes.get(i), i);
		}

		this.createDivider(mainGapColumn, titlesRow, 0);
		this.createDivider(buildingGapColumn, titlesRow, 1);
		this.createDivider(countGapColumn, titlesRow, 0);

		if (!this.sumAtTop()) {
			sumGapRow = this.addRow();
			sumsRow = this.addRow();
		}

		this.createRowDivider(titleGapRow, 0);
		if (!owner.getRecipes().isEmpty())
			this.createRowDivider(sumGapRow, 1);
		for (int row : minorRowGaps)
			this.createRowDivider(row, 2);

		sumEntries.clear();

		this.createDivider(mainGapColumn, sumsRow, 0);
		this.createDivider(countGapColumn, sumsRow, 0);
		this.createDivider(buildingGapColumn, sumsRow, 1);

		buildingGrid = false;

		GridPane gp = this.getGrid();
		for (int i = 0; i < items.size(); i++) {
			Consumable c = items.get(i);
			int idx = itemsStartColumn+items.indexOf(c)*2;
			GuiInstance<ItemRateController> gui = GuiUtil.createItemView(c, this.getAmount(c), gp, idx, sumsRow);
			if (Setting.FIXEDMATRIX.getCurrentValue())
				gui.controller.setMinWidth("9999.9999");
			gui.rootNode.getStyleClass().add("matrix-item-cell");
			sumEntries.put(c, gui);
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

		gp.getColumnConstraints().get(countColumn).setMinWidth(92);
	}

	protected final double getAvailable(Consumable c) {
		return owner.getTotalProduction(c)+(owner.resourceMatrixRule == InclusionPattern.EXCLUDE ? 0 : owner.getExternalInput(c, false));
	}

	@Override
	protected RecipeRow addRecipeRow(ItemConsumerProducer r, int i) throws IOException {
		RecipeRow rowIndex = super.addRecipeRow(r, i);

		//this.createDivider(countGapColumn, rowIndex.rowIndex, 0);
		double amt = 0;
		if (r instanceof Recipe) {
			Recipe f = (Recipe)r;
			amt = owner.getCount(f);
		}
		else if (r instanceof Fuel) {
			Fuel f = (Fuel)r;
			amt = owner.getCount(f.generator, f);
		}
		else {
			amt = -1;
		}
		if (amt >= 0) {
			Region lb = Setting.FRACTION.getCurrentValue().format(amt, false, false);
			if (lb instanceof HBox) {
				((HBox)lb).setAlignment(Pos.CENTER_LEFT);
				lb.setPadding(new Insets(0, 0, 0, 8));
			}
			else {
				lb.setPadding(new Insets(2, 2, 2, 8));
			}
			lb.setMaxHeight(40);
			this.getGrid().add(lb, countColumn, rowIndex.rowIndex);
		}
		return rowIndex;
	}

	@Override
	protected void addTitles() {
		super.addTitles();

		sumsLabel = new Label("Total");
		sumsLabel.getStyleClass().add("table-header");
		this.getGrid().add(sumsLabel, nameColumn, sumsRow);
		countLabel = new Label("Counts");
		countLabel.getStyleClass().add("table-header");
		this.getGrid().add(countLabel, countColumn, titlesRow);
		this.getGrid().setColumnSpan(countLabel, GridPane.REMAINING);
	}

	@Override
	public void onSetCount(Recipe r, double amt) {
		RecipeRow rr = recipeEntries.get(r);
		rr.setScale(amt);
		if (!settingValue && !buildingGrid && this.isGridBuilt())
			;//this.zeroRecipeRow(rr);
		this.updateStatuses(r);
	}

	public void onUpdateIO() {
		for (ItemConsumerProducer i : recipeEntries.keySet()) {
			this.updateStatuses(i);
		}
	}

	protected final void updateStatuses(ItemConsumerProducer i) {
		for (Entry<Consumable, Double> c : this.getItemRates(i))
			this.updateStatuses(c.getKey());
	}

	@Deprecated
	public final void setSpinnerStep(double amount) {/*
		for (RecipeRow rr : recipeEntries.values()) {
			((DoubleSpinnerValueFactory)((Spinner<Double>)rr.getChildNode("counter")).getValueFactory()).setAmountToStepBy(amount);
		}*/
	}

	protected abstract boolean sumAtTop();

	public abstract double getAmount(Consumable c);

}
