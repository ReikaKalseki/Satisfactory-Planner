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
import Reika.SatisfactoryPlanner.GUI.RecipeMatrixBase.GridLine;
import Reika.SatisfactoryPlanner.Util.FactoryListener;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public abstract class RecipeMatrixBase implements FactoryListener {

	protected final TableView<MatrixRow> grid = new TableView();

	protected final TitleRow titlesRow = new TitleRow();
	protected final DividerRow titleGapRow = new DividerRow(0);
	protected final HashSet<DividerRow> minorRowGaps = new HashSet();

	protected final MatrixColumn nameColumn = new MatrixColumn();
	protected final GapColumn mainGapColumn = new GapColumn(0);
	protected final GapColumn buildingGapColumn = new GapColumn(1);
	protected final MatrixColumn buildingColumn = new MatrixColumn();
	protected final HashSet<GapColumn> minorColumnGaps = new HashSet();

	protected final GapColumn inoutGapColumn = new GapColumn(1);

	protected Label nameLabel;
	protected Label consumptionLabel;
	protected Label productionLabel;
	protected Label buildingLabel;

	protected ArrayList<Consumable> inputs;
	protected ArrayList<Consumable> outputs;

	protected final HashMap<Consumable, ItemColumn> inputColumns = new HashMap();
	protected final HashMap<Consumable, ItemColumn> outputColumns = new HashMap();
	protected final HashMap<Recipe, RecipeRow> recipeEntries = new HashMap();

	public final Factory owner;
	private ControllerBase gui;

	protected RecipeMatrixBase(Factory f) {
		owner = f;
		grid.setMaxWidth(Double.POSITIVE_INFINITY);
		grid.setMaxHeight(Double.POSITIVE_INFINITY);
	}

	protected void addInitialRows() {
		grid.getItems().add(titlesRow);
		grid.getItems().add(titleGapRow);
	}

	protected void addInitialColumns() {
		grid.getColumns().add(nameColumn);
		grid.getColumns().add(mainGapColumn);

		grid.getColumns().add(inoutGapColumn);

		grid.getColumns().add(buildingGapColumn);
		grid.getColumns().add(buildingColumn);
	}

	public final void setUI(ControllerBase gui) {
		this.gui = gui;
	}

	public final Node getGrid() {
		return grid;
	}

	public abstract List<Recipe> getRecipes();

	protected float getMultiplier(Recipe r) {
		return 1;
	}

	protected final void computeIO() {
		inputs = new ArrayList(owner.getAllIngredients());
		outputs = new ArrayList(owner.getAllProducedItems());
		Collections.sort(inputs);
		Collections.sort(outputs);
	}

	protected final void addInputColumns() {
		for (int i = 0; i < inputs.size(); i++) {
			ItemColumn c = new ItemColumn(inputs.get(i));
			inputColumns.put(c.item, c);
			if (i < inputs.size()-1)
				minorColumnGaps.add(new GapColumn(2)); //separator
		}
		//if (inputs.isEmpty())
		//	this.addColumn(); //space for "consuming" title
	}

	protected final void addOutputColumns() {
		for (int i = 0; i < outputs.size(); i++) {
			ItemColumn c = new ItemColumn(outputs.get(i));
			outputColumns.put(c.item, c);
			if (i < outputs.size()-1)
				minorColumnGaps.add(new GapColumn(2)); //separator
		}
		//if (outputs.isEmpty())
		//	this.addColumn(); //space for "producing" title
	}

	protected RecipeRow addRecipeRow(Recipe r) throws IOException {
		RecipeRow row = new RecipeRow(r);
		recipeEntries.put(r, row);
		for (Entry<Consumable, Float> e : r.getIngredientsPerMinute().entrySet()) {
			Consumable c = e.getKey();
			GuiInstance gui = this.gui.loadNestedFXML("ItemView", grid, ingredientsStartColumn+inputs.indexOf(c)*2, rowIndex);
			((ItemViewController)gui.controller).setItem(c, e.getValue()*this.getMultiplier(r));
			row.inputSlots.put(c, gui);
		}
		for (Entry<Consumable, Float> e : r.getProductsPerMinute().entrySet()) {
			Consumable c = e.getKey();
			GuiInstance gui = gui.loadNestedFXML("ItemView", grid, productsStartColumn+outputs.indexOf(c)*2, rowIndex);
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
		titlesRow.addTitle("Item Name", nameColumn);
		//titlesRow.addTitle("Consuming", ingredientsStartColumn);
		//titlesRow.addTitle("Producing", productsStartColumn);
		titlesRow.addTitle("In", buildingColumn);
	}

	@Override
	public final int getSortIndex() {
		return Integer.MIN_VALUE;
	}

	@Override
	public final void setFactory(Factory f) {
		throw new UnsupportedOperationException();
	}

	public final void onAddRecipe(Recipe r) {
		try {
			this.addRecipeRow(r);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	protected class RecipeRow extends FixedContentRow {

		private final Recipe recipe;
		private final HashMap<Consumable, GuiInstance> inputSlots = new HashMap();
		private final HashMap<Consumable, GuiInstance> outputSlots = new HashMap();

		private RecipeRow(Recipe r) {
			recipe = r;

			Label lb = new Label(r.displayName);
			lb.setFont(Font.font(lb.getFont().getFamily(), FontWeight.BOLD, FontPosture.REGULAR, 14));
			GuiUtil.sizeToContent(lb);
			this.addNode(nameColumn, lb);
		}

		public void setScale(float scale) {
			for (GuiInstance gui : inputSlots.values())
				((ItemViewController)gui.controller).setScale(scale);
			for (GuiInstance gui : outputSlots.values())
				((ItemViewController)gui.controller).setScale(scale);
		}

	}

	protected class TitleRow extends FixedContentRow {

		protected TitleRow() {

		}

		protected TitleRow addTitle(String s, MatrixColumn c) {
			Label l = new Label(s);
			l.setFont(Font.font(l.getFont().getFamily(), FontWeight.BOLD, 16));
			this.addNode(c, l);
			return this;
		}

	}

	protected class FixedContentRow extends MatrixRow {

		private final HashMap<MatrixColumn, Node> nodes = new HashMap();

		public FixedContentRow() {

		}

		public FixedContentRow addNode(MatrixColumn c, Node n) {
			nodes.put(c, n);
			return this;
		}

		@Override
		protected final Node getNode(MatrixColumn c) {
			return nodes.get(c);
		}

	}

	protected class DividerRow extends MatrixRow {

		private final HBox hb = new HBox();

		public DividerRow(int tier) {
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
			hb.setMaxWidth(Double.POSITIVE_INFINITY);
			hb.setMaxHeight(w);
			//hb.setMinWidth(1);
			hb.setMinHeight(w);
			hb.setStyle("-fx-background-color: #"+c+";");
		}

		@Override
		protected Node getNode(MatrixColumn c) {
			return hb;
		}

	}

	protected abstract class MatrixRow {

		public MatrixRow() {

		}

		protected abstract Node getNode(MatrixColumn c);

	}

	protected class ItemColumn extends MatrixColumn {

		public final Consumable item;

		public ItemColumn(Consumable c) {
			item = c;
		}

	}

	protected class GapColumn extends MatrixColumn {

		private final Rectangle rect = new Rectangle();

		public GapColumn(int tier) {
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
		}

	}

	protected class MatrixColumn extends TableColumn<MatrixRow, Node> {

	}
}
