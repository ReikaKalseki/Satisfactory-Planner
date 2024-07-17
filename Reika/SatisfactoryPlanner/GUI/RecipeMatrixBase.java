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
import Reika.SatisfactoryPlanner.Data.Generator;
import Reika.SatisfactoryPlanner.Data.Recipe;
import Reika.SatisfactoryPlanner.Data.ResourceSupply;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.GuiInstance;
import Reika.SatisfactoryPlanner.Util.FactoryListener;

import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.ConstraintsBase;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public abstract class RecipeMatrixBase implements FactoryListener {

	protected final GridPane grid = new GridPane();

	protected GridLine<RowConstraints> titlesRow;
	protected GridLine<RowConstraints> titleGapRow;
	protected final HashSet<GridLine<RowConstraints>> minorRowGaps = new HashSet();

	protected GridLine<ColumnConstraints> nameColumn;
	protected GridLine<ColumnConstraints> mainGapColumn;
	protected GridLine<ColumnConstraints> buildingGapColumn;
	protected GridLine<ColumnConstraints> buildingColumn;
	protected final HashSet<GridLine<ColumnConstraints>> minorColumnGaps = new HashSet();

	protected GridLine<ColumnConstraints> inoutGapColumn;
	protected int ingredientsStartColumn;
	protected int productsStartColumn;

	protected Label nameLabel;
	protected Label consumptionLabel;
	protected Label productionLabel;
	protected Label buildingLabel;

	protected ArrayList<Consumable> inputs;
	protected ArrayList<Consumable> outputs;

	protected final HashMap<Recipe, RecipeRow> recipeEntries = new HashMap();

	public final Factory owner;

	protected RecipeMatrixBase(Factory f) {
		owner = f;
		grid.setMaxWidth(Double.POSITIVE_INFINITY);
		grid.setMaxHeight(Double.POSITIVE_INFINITY);
		grid.setHgap(4);
		grid.setVgap(4);
	}

	public final GridPane getGrid() {
		return grid;
	}

	public abstract List<Recipe> getRecipes();

	protected float getMultiplier(Recipe r) {
		return 1;
	}

	public abstract void createGrid(ControllerBase con) throws IOException;

	protected final void computeIO() {
		inputs = new ArrayList(owner.getAllIngredients());
		outputs = new ArrayList(owner.getAllProducedItems());
		Collections.sort(inputs);
		Collections.sort(outputs);
	}

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

	protected RecipeRow addRecipeRow(ControllerBase con, Recipe r, int i) throws IOException {
		RecipeRow row = new RecipeRow(r, this.addRow());
		recipeEntries.put(r, row);
		Label lb = new Label(r.displayName);
		lb.setFont(Font.font(lb.getFont().getFamily(), FontWeight.BOLD, FontPosture.REGULAR, 14));
		GuiUtil.sizeToContent(lb);
		int rowIndex = titleGapRow.index+1+i*2;
		grid.add(lb, nameColumn.index, rowIndex);
		for (Entry<Consumable, Float> e : r.getIngredientsPerMinute().entrySet()) {
			Consumable c = e.getKey();
			GuiInstance gui = con.loadNestedFXML("ItemView", grid, ingredientsStartColumn+inputs.indexOf(c)*2, rowIndex);
			((ItemViewController)gui.controller).setItem(c, e.getValue()*this.getMultiplier(r));
			row.inputSlots.put(c, gui);
		}
		for (Entry<Consumable, Float> e : r.getProductsPerMinute().entrySet()) {
			Consumable c = e.getKey();
			GuiInstance gui = con.loadNestedFXML("ItemView", grid, productsStartColumn+outputs.indexOf(c)*2, rowIndex);
			((ItemViewController)gui.controller).setItem(c, e.getValue()*this.getMultiplier(r));
			row.outputSlots.put(c, gui);
		}
		grid.add(r.productionBuilding.createImageView(), buildingColumn.index, rowIndex);

		this.createDivider(mainGapColumn, row.mainRow, 0);
		this.createDivider(inoutGapColumn, row.mainRow, 1);
		for (GridLine<ColumnConstraints> col : minorColumnGaps)
			this.createDivider(col, row.mainRow, 2);
		this.createDivider(buildingGapColumn, row.mainRow, 1);
		return row;
	}

	protected void addTitles() {
		nameLabel = new Label("Item Name");
		nameLabel.setFont(Font.font(nameLabel.getFont().getFamily(), FontWeight.BOLD, 16));
		grid.add(nameLabel, nameColumn.index, titlesRow.index);
		consumptionLabel = new Label("Consuming");
		consumptionLabel.setFont(Font.font(consumptionLabel.getFont().getFamily(), FontWeight.BOLD, 16));
		grid.add(consumptionLabel, ingredientsStartColumn, titlesRow.index);
		productionLabel = new Label("Producing");
		productionLabel.setFont(Font.font(productionLabel.getFont().getFamily(), FontWeight.BOLD, 16));
		grid.add(productionLabel, productsStartColumn, titlesRow.index);
		buildingLabel = new Label("In");
		buildingLabel.setFont(Font.font(productionLabel.getFont().getFamily(), FontWeight.BOLD, 16));
		grid.add(buildingLabel, buildingColumn.index, titlesRow.index);
		grid.setColumnSpan(consumptionLabel, productsStartColumn-ingredientsStartColumn);
		grid.setColumnSpan(productionLabel, buildingGapColumn.index-productsStartColumn);
		grid.setColumnSpan(buildingLabel, 1);
	}

	protected final GridLine<RowConstraints> addRow() {
		return this.addRow(-1);
	}

	protected final GridLine<RowConstraints> addRow(int at) {
		GridLine<RowConstraints> cc;
		if (at > 0) {
			cc = new GridLine(GuiUtil.addRowToGridPane(grid, at));
		}
		else {
			cc = new GridLine(new RowConstraints());
			grid.getRowConstraints().add(cc.line);
		}
		cc.line.setPrefHeight(Region.USE_COMPUTED_SIZE);
		cc.line.setMaxHeight(Region.USE_COMPUTED_SIZE);
		cc.line.setMinHeight(Region.USE_COMPUTED_SIZE);
		return cc;
	}

	protected final GridLine<ColumnConstraints> addColumn() {
		return this.addColumn(-1);
	}

	protected final GridLine<ColumnConstraints> addColumn(int at) {
		GridLine<ColumnConstraints> cc;
		if (at > 0) {
			cc = new GridLine(GuiUtil.addColumnToGridPane(grid, at));
		}
		else {
			cc = new GridLine(new ColumnConstraints());
			grid.getColumnConstraints().add(cc.line);
		}

		cc.line.setPrefWidth(Region.USE_COMPUTED_SIZE);
		cc.line.setMaxWidth(Region.USE_COMPUTED_SIZE);
		cc.line.setMinWidth(Region.USE_COMPUTED_SIZE);
		return cc;
	}

	protected final void createRowDivider(GridLine<RowConstraints> row, int tier) {
		this.createRowDivider(row.index, tier);
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

	protected final void createDivider(GridLine<ColumnConstraints> col, GridLine<RowConstraints> row, int tier) {
		this.createDivider(col.index, row.index, tier);
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
	/*
	private Node createItem(Consumable c, int amt) {
		HBox hb = new HBox();
		hb.setAlignment(Pos.CENTER_LEFT);
		ImageView img = new ImageView(new Image(c.getIcon(), 32, 32, true, true));
		img.setFitHeight(32);
		img.setFitWidth(32);
		img.setSmooth(true);
		img.setCache(true);
		hb.setSpacing(0);
		hb.getChildren().add(img);
		Label lb = new Label(amt+"/min");
		lb.setFont(Font.font(lb.getFont().getFamily(), 14));
		hb.getChildren().add(lb);
		GuiUtil.setTooltip(img, c.name);
		return hb;
	}*/
	/*
	protected final int getColumnWidth(GridPane gp, int col) {
		if (col == deleteColumn)
			return 32;
		else if (col == mainGapColumn)
			return 8;
		else if (col == inoutGapColumn)
			return 4;
		else if (minorColumnGaps.contains(col))
			return 1;
		else
			return -1;
	}
	 */

	@Override
	public final int getSortIndex() {
		return Integer.MIN_VALUE;
	}

	@Override
	public final void setFactory(Factory f) {
		throw new UnsupportedOperationException();
	}

	public final void onAddRecipe(Recipe r) {
		this.addRecipeRow(null, r, ingredientsStartColumn);
	}

	public final void onRemoveRecipe(Recipe r) {

	}

	@Override
	public final void onSetCount(Generator g, float count) {}

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

	protected class RecipeRow {

		private final Recipe recipe;
		final GridLine<RowConstraints> mainRow;
		private GridLine<RowConstraints> gapRow;
		private final HashMap<Consumable, GuiInstance> inputSlots = new HashMap();
		private final HashMap<Consumable, GuiInstance> outputSlots = new HashMap();

		private RecipeRow(Recipe r, GridLine<RowConstraints> g) {
			recipe = r;
			mainRow = g;
		}

		public GridLine<RowConstraints> createGap() {
			gapRow = RecipeMatrixBase.this.addRow();
			return gapRow;
		}

		public int getRowIndex() {
			return mainRow.index;
		}

		public void setScale(float scale) {
			for (GuiInstance gui : inputSlots.values())
				((ItemViewController)gui.controller).setScale(scale);
			for (GuiInstance gui : outputSlots.values())
				((ItemViewController)gui.controller).setScale(scale);
		}

	}

	protected static class GridLine<C extends ConstraintsBase> {

		private final C line;
		public int index;

		public GridLine(C l) {
			line = l;
		}

		public GridLine setIndex(int idx) {
			index = idx;
			return this;
		}

		public GridLine move(int offset) {
			index += offset;
			return this;
		}

		@Override
		public int hashCode() {
			return index;
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof GridLine && ((GridLine)o).index == index;
		}

		@Override
		public String toString() {
			return line+" @ "+index;
		}

	}
}
