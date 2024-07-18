package Reika.SatisfactoryPlanner.GUI;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map.Entry;

import Reika.SatisfactoryPlanner.Data.Constants.ToggleableVisiblityGroup;
import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.Generator;
import Reika.SatisfactoryPlanner.Data.Recipe;
import Reika.SatisfactoryPlanner.Data.ResourceSupply;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.GuiInstance;
import Reika.SatisfactoryPlanner.Util.FactoryListener;
import Reika.SatisfactoryPlanner.Util.Logging;
import Reika.SatisfactoryPlanner.Util.MultiMap;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public abstract class RecipeMatrixBase implements FactoryListener {

	protected final TableView<MatrixRow> grid = new TableView();

	protected final TitleRow titlesRow = new TitleRow();
	protected final DividerRow titleGapRow = new DividerRow(0);

	protected final MatrixColumn nameColumn = new MatrixColumn();
	protected final GapColumn mainGapColumn = new GapColumn(0);
	protected final GapColumn buildingGapColumn = new GapColumn(1);
	protected final MatrixColumn buildingColumn = new MatrixColumn();

	protected final GapColumn inoutGapColumn = new GapColumn(1);

	protected Label nameLabel;
	protected Label consumptionLabel;
	protected Label productionLabel;
	protected Label buildingLabel;

	//protected final ArrayList<Consumable> inputsList = new ArrayList();
	//protected final ArrayList<Consumable> outputsList = new ArrayList();
	protected final MultiMap<Consumable, Recipe> inputs = new MultiMap();
	protected final MultiMap<Consumable, Recipe> outputs = new MultiMap();

	protected final HashMap<Consumable, ItemColumn> inputColumns = new HashMap();
	protected final HashMap<Consumable, ItemColumn> outputColumns = new HashMap();
	protected final HashMap<Recipe, RecipeRow> recipeEntries = new HashMap();

	public final Factory owner;
	protected ControllerBase gui;

	protected RecipeMatrixBase(Factory f) {
		owner = f;
		owner.addCallback(this);
		grid.setMaxWidth(Double.POSITIVE_INFINITY);
		grid.setMaxHeight(Double.POSITIVE_INFINITY);
		grid.setPrefHeight(Region.USE_COMPUTED_SIZE);
		grid.setPrefWidth(Region.USE_COMPUTED_SIZE);
		grid.setMinWidth(Region.USE_COMPUTED_SIZE);
		grid.setMinHeight(Region.USE_COMPUTED_SIZE);

		grid.setEditable(false);
		grid.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

		//TableViewSkin sk = new TableViewSkin(grid);
		//sk.
		//grid.setSkin(sk);
	}

	public void buildGrid() {
		this.addInitialColumns();
		this.addInitialRows();
		this.addTitles();
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

	protected float getMultiplier(Recipe r) {
		return 1;
	}
	/*
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
	}*/

	protected RecipeRow addRecipeRow(Recipe r) throws IOException {
		RecipeRow row = new RecipeRow(r);
		int idx = grid.getItems().indexOf(titleGapRow)+1+recipeEntries.size();
		grid.getItems().add(idx, row);
		recipeEntries.put(r, row);
		if (recipeEntries.size() > 1) {
			DividerRow div = new DividerRow(2);
			row.precedingGap = div;
			grid.getItems().add(idx, div);
		}

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
			for (Consumable c : r.getDirectCost().keySet()) {
				if (!inputs.containsKey(c)) {
					//inputsList.add(c);
					this.addItemColumn(c, true);
					this.onAddItem(c, true);
				}
				inputs.addValue(c, r);
			}
			//Collections.sort(inputsList);
			for (Consumable c : r.getProductsPerMinute().keySet()) {
				if (!outputs.containsKey(c)) {
					//outputsList.add(c);
					this.addItemColumn(c, false);
					this.onAddItem(c, false);
				}
				outputs.addValue(c, r);
			}
			//Collections.sort(outputsList);
			this.addRecipeRow(r);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void onAddItem(Consumable c, boolean isInput) {

	}

	private void addItemColumn(Consumable c, boolean isInput) {
		ItemColumn ic = new ItemColumn(c, isInput);
		int idx = isInput ? grid.getColumns().indexOf(mainGapColumn)+1+inputColumns.size() : grid.getColumns().indexOf(inoutGapColumn)+1+outputColumns.size();
		grid.getColumns().add(idx, ic);
		HashMap<Consumable, ItemColumn> map = isInput ? inputColumns : outputColumns;
		map.put(c, ic);
		if (map.size() > 1) {
			GapColumn gap = new GapColumn(2);
			ic.precedingGap = gap;
			grid.getColumns().add(idx, gap);
		}
	}

	public final void onRemoveRecipe(Recipe r) {
		for (Consumable c : r.getDirectCost().keySet()) {
			inputs.remove(c, r);
			if (!inputs.containsKey(c)) {
				//inputsList.remove(c);
				inputColumns.get(c).removeFromGrid();
				this.onRemoveItem(c, true);
			}
		}
		for (Consumable c : r.getProductsPerMinute().keySet()) {
			outputs.remove(c, r);
			if (!outputs.containsKey(c)) {
				//outputsList.remove(c);
				outputColumns.get(c).removeFromGrid();
				this.onRemoveItem(c, false);
			}
		}
		recipeEntries.get(r).removeFromGrid();
	}

	protected void onRemoveItem(Consumable c, boolean isInput) {

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

		private DividerRow precedingGap;

		private final Label label;

		private RecipeRow(Recipe r) throws IOException {
			super();
			recipe = r;

			label = new Label(r.displayName);
			label.setFont(Font.font(label.getFont().getFamily(), FontWeight.BOLD, FontPosture.REGULAR, 14));
			GuiUtil.sizeToContent(label);

			ControllerBase con = gui;
			for (Entry<Consumable, Float> e : r.getIngredientsPerMinute().entrySet()) {
				Consumable c = e.getKey();
				GuiInstance gui = con.loadNestedFXML("ItemView", p -> {});
				((ItemViewController)gui.controller).setItem(c, e.getValue());
				inputSlots.put(c, gui);
			}
			for (Entry<Consumable, Float> e : r.getProductsPerMinute().entrySet()) {
				Consumable c = e.getKey();
				GuiInstance gui = con.loadNestedFXML("ItemView", p -> {});
				((ItemViewController)gui.controller).setItem(c, e.getValue()); //*this.getMultiplier(r)
				outputSlots.put(c, gui);
			}
		}

		public void setScale(float scale) {
			for (GuiInstance gui : inputSlots.values())
				((ItemViewController)gui.controller).setScale(scale);
			for (GuiInstance gui : outputSlots.values())
				((ItemViewController)gui.controller).setScale(scale);
		}

		public void removeFromGrid() {
			grid.getItems().remove(this);
			if (precedingGap != null)
				grid.getItems().remove(precedingGap);
		}

		@Override
		protected Node getNode(MatrixColumn c) {
			if (c == nameColumn) {
				return label;
			}
			else if (c instanceof ItemColumn) {
				ItemColumn ic = (ItemColumn)c;
				GuiInstance gui = (ic.isInput ? inputSlots : outputSlots).get(ic.item);
				return gui == null ? null : gui.rootNode;
			}
			return super.getNode(c);
		}

	}

	protected class TitleRow extends FixedContentRow {

		protected TitleRow() {
			super();
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
			super();
		}

		public FixedContentRow addNode(MatrixColumn c, Node n) {
			nodes.put(c, n);
			return this;
		}

		@Override
		protected Node getNode(MatrixColumn c) {
			return nodes.get(c);
		}

	}

	protected class DividerRow extends MatrixRow {

		private final HBox hb = new HBox();

		public DividerRow(int tier) {
			super();
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
		public final boolean isInput;

		private GapColumn precedingGap;

		public ItemColumn(Consumable c, boolean inp) {
			super();
			item = c;
			isInput = inp;
		}

		public void removeFromGrid() {
			grid.getColumns().remove(this);
			if (precedingGap != null)
				grid.getColumns().remove(precedingGap);
		}

	}

	protected class GapColumn extends MatrixColumn {

		private final Rectangle rect = new Rectangle();

		public GapColumn(int tier) {
			super();
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

		protected MatrixColumn() {
			this.setMaxWidth(Double.POSITIVE_INFINITY);
			this.setPrefWidth(Region.USE_COMPUTED_SIZE);
			this.setMinWidth(Region.USE_COMPUTED_SIZE);
			this.setCellValueFactory(d -> {
				return new ReadOnlyObjectWrapper<Node>(d.getValue().getNode((MatrixColumn)d.getTableColumn()));
			});
			this.setResizable(false);
			this.setEditable(false);
			this.setReorderable(false);
			this.setCellFactory(tc -> new MatrixCell(this));
		}

	}

	private static class MatrixCell extends TableCell<MatrixRow, Node> {

		private final MatrixColumn owner;

		private MatrixCell(MatrixColumn c) {
			owner = c;
			this.setMaxWidth(Double.POSITIVE_INFINITY);
			this.setMaxHeight(Double.POSITIVE_INFINITY);
			this.setPrefHeight(Region.USE_COMPUTED_SIZE);
			this.setPrefWidth(Region.USE_COMPUTED_SIZE);
			this.setMinWidth(Region.USE_COMPUTED_SIZE);
			this.setMinHeight(Region.USE_COMPUTED_SIZE);
			this.setEditable(false);
		}

		@Override
		protected void updateItem(Node item, boolean empty) {
			super.updateItem(item, empty);
			this.setText(null);
			this.setGraphic(item);
			//this.setPrefWidth(item != null ? item.getBoundsInParent().getWidth() : 0);
			//((ResizeableColumnSkin)owner.getStyleableNode()).fit();
			try {
				Method m = TableColumnHeader.class.getDeclaredMethod("resizeColumnToFitContent", int.class);
				m.setAccessible(true);
				Node n = owner.getStyleableNode();
				Logging.instance.log("Column "+owner+": "+n);
				if (n != null)
					m.invoke(n, -1);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			//((TableColumnHeader)owner.getStyleableNode()).fit();
		}

	}
	/*
	private static class ResizeableColumnSkin extends TableColumnHeader {

		public ResizeableColumnSkin(TableColumnBase tc) {
			super(tc);
		}

		public void fit() {
			this.resizeColumnToFitContent(-1);
		}

	}*/
}
