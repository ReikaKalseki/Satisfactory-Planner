package Reika.SatisfactoryPlanner.GUI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import Reika.SatisfactoryPlanner.Data.Constants.ToggleableVisiblityGroup;
import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.Fuel;
import Reika.SatisfactoryPlanner.Data.Generator;
import Reika.SatisfactoryPlanner.Data.ItemConsumerProducer;
import Reika.SatisfactoryPlanner.Data.Recipe;
import Reika.SatisfactoryPlanner.Data.ResourceSupply;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.GuiInstance;
import Reika.SatisfactoryPlanner.Util.FactoryListener;

import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public abstract class RecipeMatrixBase<R extends ItemConsumerProducer> implements FactoryListener {

	protected int titlesRow;
	protected int titleGapRow;
	protected final HashSet<Integer> minorRowGaps = new HashSet();

	protected int nameColumn;
	protected int mainGapColumn;
	protected int buildingGapColumn;
	protected int buildingColumn;
	protected final HashSet<Integer> minorColumnGaps = new HashSet();

	protected int inoutGapColumn;
	protected int ingredientsStartColumn;
	protected int productsStartColumn;

	protected Label nameLabel;
	protected Label consumptionLabel;
	protected Label productionLabel;
	protected Label buildingLabel;

	protected ArrayList<Consumable> inputs;
	protected ArrayList<Consumable> outputs;

	protected final HashMap<R, RecipeRow> recipeEntries = new HashMap();

	public final Factory owner;
	protected final GridPane grid;

	protected ControllerBase gui;

	protected RecipeMatrixBase(Factory f) {
		owner = f;
		grid = new GridPane();
		owner.addCallback(this);

		grid.setHgap(4);
		grid.setVgap(4);
		grid.setMaxHeight(Double.POSITIVE_INFINITY);
		grid.setMaxWidth(Double.POSITIVE_INFINITY);
	}

	public final void setUI(ControllerBase gui) {
		this.gui = gui;
	}

	protected float getMultiplier(R r) {
		return 1;
	}

	public final GridPane getGrid() {
		return grid;
	}

	protected final List<Recipe> getRecipes() {
		return owner.getRecipes();
	}

	protected final void computeIO() {
		inputs = new ArrayList(owner.getAllIngredients());
		outputs = new ArrayList(owner.getAllProducedItems());
		Collections.sort(inputs);
		Collections.sort(outputs);
	}

	protected abstract void rebuildGrid() throws IOException;

	protected final void addInputColumns() {
		for (int i = 0; i < inputs.size(); i++) {
			this.addColumn();
			if (i < inputs.size()-1)
				minorColumnGaps.add(this.addColumn()); //separator
		}
		if (inputs.isEmpty())
			this.addColumn(); //space for "consuming" title
		if (inputs.size() <= 1)
			grid.getColumnConstraints().get(grid.getColumnCount()-1).setMinWidth(96);
	}

	protected final void addOutputColumns() {
		for (int i = 0; i < outputs.size(); i++) {
			this.addColumn();
			if (i < outputs.size()-1)
				minorColumnGaps.add(this.addColumn()); //separator
		}
		if (outputs.isEmpty())
			this.addColumn(); //space for "producing" title
		if (outputs.size() <= 1)
			grid.getColumnConstraints().get(grid.getColumnCount()-1).setMinWidth(96);
	}

	protected RecipeRow addRecipeRow(R r, int i) throws IOException {
		Label lb = new Label(r.getDisplayName());
		lb.setFont(Font.font(lb.getFont().getFamily(), FontWeight.BOLD, FontPosture.REGULAR, 14));
		GuiUtil.sizeToContent(lb);
		int rowIndex = titleGapRow+1+i*2;
		RecipeRow row = new RecipeRow(r, i, rowIndex);
		recipeEntries.put(r, row);
		grid.add(lb, nameColumn, rowIndex);
		for (Entry<Consumable, Float> e : r.getIngredientsPerMinute().entrySet()) {
			Consumable c = e.getKey();
			GuiInstance<ItemViewController> gui = this.gui.loadNestedFXML("ItemView", grid, ingredientsStartColumn+inputs.indexOf(c)*2, rowIndex);
			gui.controller.setItem(c, e.getValue()*this.getMultiplier(r));
			row.inputSlots.put(c, gui);
		}
		for (Entry<Consumable, Float> e : r.getProductsPerMinute().entrySet()) {
			Consumable c = e.getKey();
			GuiInstance<ItemViewController> gui = this.gui.loadNestedFXML("ItemView", grid, productsStartColumn+outputs.indexOf(c)*2, rowIndex);
			gui.controller.setItem(c, e.getValue()*this.getMultiplier(r));
			row.outputSlots.put(c, gui);
		}
		grid.add(r.getBuilding().createImageView(), buildingColumn, rowIndex);

		this.createDivider(mainGapColumn, rowIndex, 0);
		this.createDivider(inoutGapColumn, rowIndex, 1);
		for (int col : minorColumnGaps)
			this.createDivider(col, rowIndex, 2);
		this.createDivider(buildingGapColumn, rowIndex, 1);
		return row;
	}

	protected void addTitles() {
		nameLabel = new Label("Item Name");
		nameLabel.setFont(Font.font(nameLabel.getFont().getFamily(), FontWeight.BOLD, 16));
		grid.add(nameLabel, nameColumn, titlesRow);
		consumptionLabel = new Label("Consuming");
		consumptionLabel.setFont(Font.font(consumptionLabel.getFont().getFamily(), FontWeight.BOLD, 16));
		grid.add(consumptionLabel, ingredientsStartColumn, titlesRow);
		productionLabel = new Label("Producing");
		productionLabel.setFont(Font.font(productionLabel.getFont().getFamily(), FontWeight.BOLD, 16));
		grid.add(productionLabel, productsStartColumn, titlesRow);
		buildingLabel = new Label("In");
		buildingLabel.setFont(Font.font(productionLabel.getFont().getFamily(), FontWeight.BOLD, 16));
		grid.add(buildingLabel, buildingColumn, titlesRow);
		grid.setColumnSpan(consumptionLabel, productsStartColumn-ingredientsStartColumn);
		grid.setColumnSpan(productionLabel, buildingGapColumn-productsStartColumn);
		grid.setColumnSpan(buildingLabel, 1);
	}

	protected final int addRow() {
		RowConstraints cc = new RowConstraints();
		cc.setPrefHeight(Region.USE_COMPUTED_SIZE);
		cc.setMaxHeight(Region.USE_COMPUTED_SIZE);
		cc.setMinHeight(Region.USE_COMPUTED_SIZE);
		grid.getRowConstraints().add(cc);
		return grid.getRowConstraints().size()-1;
	}

	protected final int addColumn() {
		ColumnConstraints cs = new ColumnConstraints();
		grid.getColumnConstraints().add(cs);
		cs.setPrefWidth(Region.USE_COMPUTED_SIZE);
		cs.setMaxWidth(Region.USE_COMPUTED_SIZE);
		cs.setMinWidth(Region.USE_COMPUTED_SIZE);
		return grid.getColumnConstraints().size()-1;
	}

	protected final void createRowDivider(int row, int tier) {
		String c = "000000ff";
		int w = 8;
		switch(tier) {
			case 0:
				c = "7f7f7fff";
				break;
			case 1:
				c = "b2b2b2ff";
				w = 4;
				break;
			case 2:
				c = "d9d9d9ff";
				w = 1;
				break;
		}
		ObservableList<ColumnConstraints> li = grid.getColumnConstraints();
		for (int i = 0; i < li.size(); i++) {/*
			Rectangle rect = new Rectangle();
			rect.setFill(c);
			li.get(i).setFillWidth(true);
			//rect.widthProperty().bind(li.get(i).maxWidthProperty().subtract(2));
			//rect.widthProperty().bind(gp.widthProperty().subtract(4+8+4+minorColumnGaps.size()).divide(li.size()-minorColumnGaps.size()+2));
			rect.setHeight(w);
			//rect.setWidth(gp.getCellBounds(i, row).getWidth());
			gp.add(rect, i, row);/*
			AnchorPane ap = new AnchorPane();
			ap.getChildren().add(rect);
			ap.setBottomAnchor(rect, 0D);
			ap.setTopAnchor(rect, 0D);
			ap.setLeftAnchor(rect, 0D);
			ap.setRightAnchor(rect, 0D);
			gp.add(ap, i, row);*/
			HBox hb = new HBox();
			hb.setMaxWidth(Double.POSITIVE_INFINITY);
			hb.setMaxHeight(w);
			//hb.setMinWidth(1);
			hb.setMinHeight(w);
			hb.setStyle("-fx-background-color: #"+c+";");
			grid.add(hb, i, row);
		}
	}

	protected final void createDivider(int col, int row, int tier) {
		Rectangle rect = new Rectangle();
		Color c = Color.BLACK;
		int w = 8;
		switch(tier) {
			case 0:
				c = Color.gray(0.5);
				break;
			case 1:
				c = Color.gray(0.7);
				w = 4;
				break;
			case 2:
				c = Color.gray(0.85);
				w = 1;
				break;
		}
		rect.setFill(c);
		//rect.widthProperty().bind(gp.getColumnConstraints().get(split).maxWidthProperty().subtract(2));
		//rect.heightProperty().bind(gp.getRowConstraints().get(i).maxHeightProperty().subtract(2));
		rect.setWidth(w);
		rect.setHeight(32);
		grid.add(rect, col, row);
	}

	@Override
	public final void setFactory(Factory f) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final int getSortIndex() {
		return Integer.MIN_VALUE;
	}

	protected final void rebuild() {
		try {
			grid.getChildren().clear();
			grid.getColumnConstraints().clear();
			grid.getRowConstraints().clear();
			recipeEntries.clear();
			this.rebuildGrid();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public final void onAddRecipe(Recipe r) {
		this.rebuild();
	}

	@Override
	public final void onRemoveRecipe(Recipe r) {
		this.rebuild();
	}

	@Override
	public final void onCleared() {
		this.rebuild();
	}

	@Override
	public final void onLoaded() {
		this.rebuild();
	}

	@Override
	public void onSetCount(Generator g, Fuel fuel, int old, int count) {}

	@Override
	public final void onAddProduct(Consumable c) {}

	@Override
	public final void onRemoveProduct(Consumable c) {}

	@Override
	public final void onAddSupply(ResourceSupply s) {}

	@Override
	public final void onRemoveSupply(ResourceSupply s) {}

	@Override
	public final void onSetToggle(ToggleableVisiblityGroup grp, boolean active) {}

	@Override
	public final void onSetFile(File f) {}

	public void updateStatuses(Consumable c) {

	}

	protected class RecipeRow {

		public final R recipe;
		public final int recipeIndex;
		public final int rowIndex;
		private final HashMap<Consumable, GuiInstance<ItemViewController>> inputSlots = new HashMap();
		private final HashMap<Consumable, GuiInstance<ItemViewController>> outputSlots = new HashMap();

		private final Label label;

		private RecipeRow(R r, int index, int row) throws IOException {
			super();
			recipe = r;
			rowIndex = row;
			recipeIndex = index;
			label = new Label(r.getDisplayName());
			label.setFont(Font.font(label.getFont().getFamily(), FontWeight.BOLD, FontPosture.REGULAR, 14));
			GuiUtil.sizeToContent(label);

			ControllerBase con = gui;
			for (Entry<Consumable, Float> e : r.getIngredientsPerMinute().entrySet()) {
				Consumable c = e.getKey();
				GuiInstance<ItemViewController> gui = con.loadNestedFXML("ItemView", p -> {});
				gui.controller.setItem(c, e.getValue());
				inputSlots.put(c, gui);
			}
			for (Entry<Consumable, Float> e : r.getProductsPerMinute().entrySet()) {
				Consumable c = e.getKey();
				GuiInstance<ItemViewController> gui = con.loadNestedFXML("ItemView", p -> {});
				gui.controller.setItem(c, e.getValue()); //*this.getMultiplier(r)
				outputSlots.put(c, gui);
			}
		}

		public void setScale(float scale) {
			for (GuiInstance<ItemViewController> gui : inputSlots.values())
				gui.controller.setScale(scale);
			for (GuiInstance<ItemViewController> gui : outputSlots.values())
				gui.controller.setScale(scale);
		}
	}
}
