package Reika.SatisfactoryPlanner.GUI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.apache.commons.lang3.function.TriConsumer;

import com.google.common.base.Strings;

import Reika.SatisfactoryPlanner.FactoryListener;
import Reika.SatisfactoryPlanner.InclusionPattern;
import Reika.SatisfactoryPlanner.InternalIcons;
import Reika.SatisfactoryPlanner.NamedIcon;
import Reika.SatisfactoryPlanner.Setting;
import Reika.SatisfactoryPlanner.Data.Constants.ToggleableVisiblityGroup;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.ItemConsumerProducer;
import Reika.SatisfactoryPlanner.Data.Objects.Consumable;
import Reika.SatisfactoryPlanner.Data.Objects.Fuel;
import Reika.SatisfactoryPlanner.Data.Objects.Recipe;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.Generator;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.ResourceSupply;
import Reika.SatisfactoryPlanner.GUI.RecipeMatrixContainer.MatrixType;
import Reika.SatisfactoryPlanner.GUI.Components.ItemRateController;
import Reika.SatisfactoryPlanner.Util.CountMap;
import Reika.SatisfactoryPlanner.Util.Errorable.ErrorableWithArgument;
import Reika.SatisfactoryPlanner.Util.Logging;

import fxexpansions.GuiInstance;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public abstract class RecipeMatrixBase implements FactoryListener {

	protected int titlesRow;
	protected int titleGapRow;
	protected final HashSet<Integer> minorRowGaps = new HashSet();
	protected int recipeStartRow;

	protected int nameColumn;
	protected int mainGapColumn;
	protected int buildingGapColumn;
	protected int buildingColumn;
	protected final HashSet<Integer> minorColumnGaps = new HashSet();

	protected int itemsStartColumn;

	protected Label nameLabel;
	protected Label itemLabel;
	protected Label buildingLabel;

	protected ArrayList<Consumable> items;

	protected final HashMap<ItemConsumerProducer, RecipeRow> recipeEntries = new HashMap();

	protected GroupedProducer<ResourceSupply> supplyGroup;
	protected GroupedProducer<Fuel> generatorGroup;

	public final Factory owner;

	protected RecipeMatrixContainer container;

	private double[] contentWidths;

	public final MatrixType type;

	private GridPane grid;

	protected RecipeMatrixBase(Factory f, MatrixType t) {
		owner = f;
		type = t;
		owner.addCallback(this);
	}

	public final void setUI(RecipeMatrixContainer gui) {
		container = gui;
	}

	@Deprecated
	protected double getMultiplier(ItemConsumerProducer r) {
		return 1;
	}

	public final GridPane getGrid() {
		return grid;
	}

	public boolean isGridBuilt() {
		return contentWidths != null;
	}

	protected final List<ItemConsumerProducer> getRecipes() {
		List<ItemConsumerProducer> ret = new ArrayList();
		if (type == MatrixType.OUT) {
			switch(owner.resourceMatrixRule) {
				case EXCLUDE:
					break;
				case MERGE:
					if (owner.getSupplies().size() > 0) {
						supplyGroup = new GroupedProducer<ResourceSupply>("External Supplies", InternalIcons.SUPPLY, owner.getSupplies(), r -> 1F);
						ret.add(supplyGroup);
					}
					break;
				case INDIVIDUAL:
					ret.addAll(owner.getSupplies());
					break;
			}
		}
		ret.addAll(owner.getRecipes());
		if (type == MatrixType.IN) {
			switch(owner.generatorMatrixRule) {
				case EXCLUDE:
					break;
				case MERGE:
					if (owner.getTotalGeneratorCount() > 0) {
						generatorGroup = new GroupedProducer<Fuel>("Generators", InternalIcons.POWER, Fuel.getFuels(), f -> (float)owner.getCount(f.generator, f));
						ret.add(generatorGroup);
					}
					break;
				case INDIVIDUAL:
					for (Generator g : Database.getAllGenerators()) {
						for (Fuel f : g.getFuels()) {
							double amt = owner.getCount(g, f);
							if (amt > 0)
								ret.add(f);
						}
					}
					break;
			}
		}
		return ret;
	}

	protected final void getItemSet() {
		HashSet<Consumable> set = new HashSet(owner.getAllIngredients());
		set.addAll(owner.getAllProducedItems());
		if (owner.resourceMatrixRule != InclusionPattern.EXCLUDE)
			set.addAll(owner.getAllSuppliedItems());
		items = new ArrayList(set);
		Collections.sort(items);
	}

	protected abstract void rebuildGrid() throws IOException;

	protected final void addItemColumns() {
		for (int i = 0; i < items.size(); i++) {
			this.addColumn();
			if (i < items.size()-1)
				minorColumnGaps.add(this.addColumn()); //separator
		}
		if (items.isEmpty())
			this.addColumn(); //space for "consuming"/"producing" title
		if (items.size() <= 1)
			grid.getColumnConstraints().get(grid.getColumnCount()-1).setMinWidth(96);
	}

	protected final void iterateIO(TriConsumer<Consumable, Integer, Boolean> action) {
		GridPane gp = this.getGrid();
		for (int i = 0; i < items.size(); i++) {
			Consumable c = items.get(i);
			int idx = itemsStartColumn+items.indexOf(c)*2;
			action.accept(c, idx, true);
		}
	}

	protected abstract Set<Entry<Consumable, Double>> getItemRates(ItemConsumerProducer r);

	protected RecipeRow addRecipeRow(ItemConsumerProducer r, int i) throws IOException {
		int rowIndex = recipeStartRow+i*2;
		RecipeRow row = new RecipeRow(r, i, rowIndex);
		recipeEntries.put(r, row);
		grid.add(row.label, nameColumn, rowIndex);
		for (Entry<Consumable, Double> e : this.getItemRates(r)) {
			Consumable c = e.getKey();
			int col = itemsStartColumn+items.indexOf(c)*2;
			GuiInstance<ItemRateController> gui = GuiUtil.createItemView(c, e.getValue()*this.getMultiplier(r), grid, col, rowIndex);
			if (Setting.FIXEDMATRIX.getCurrentValue())
				gui.controller.setMinWidth("9999.9999");
			gui.rootNode.getStyleClass().add("matrix-item-cell");
			row.slots.put(c, new RateSlot(gui, col));
		}
		NamedIcon loc = r.getLocationIcon();
		if (loc != null)
			grid.add(loc.createImageView(), buildingColumn, rowIndex);
		/*
		if (r instanceof Fuel || (r instanceof GroupedProducer && ((GroupedProducer)r).getType() instanceof Fuel)) {
			grid.add(InternalIcons.POWER.createImageView(), buttonColumn, row.rowIndex);
		}
		else if (r instanceof ResourceSupply || (r instanceof GroupedProducer && ((GroupedProducer)r).getType() instanceof ResourceSupply)) {
			grid.add(InternalIcons.SUPPLY.createImageView(), buttonColumn, row.rowIndex);
		}
		 */
		return row;
	}

	protected abstract String getItemHeader();
	/*
	public final void debugGridPositioning() {
		for (RecipeRow r : recipeEntries.values()) {
			int idx = r.rowIndex;
			Logging.instance.log(r.recipe+" @ "+r.rowIndex);
			for (GuiInstance<ItemViewController> gui : r.inputSlots.values()) {
				GridPane.setRowIndex(gui.rootNode, idx);
				int col = ingredientsStartColumn+inputs.indexOf(gui.controller.item)*2;
				Logging.instance.log(gui.controller.item+" in "+GridPane.getColumnIndex(gui.rootNode)+"/"+col);
			}
			for (GuiInstance<ItemViewController> gui : r.outputSlots.values()) {
				GridPane.setRowIndex(gui.rootNode, idx);
			}
		}
	}
	 */
	protected void addTitles() {
		nameLabel = new Label("Item Name");
		nameLabel.getStyleClass().add("table-header");
		grid.add(nameLabel, nameColumn, titlesRow);
		itemLabel = new Label(this.getItemHeader());
		itemLabel.getStyleClass().add("table-header");
		grid.add(itemLabel, itemsStartColumn, titlesRow);
		buildingLabel = new Label("In");
		buildingLabel.getStyleClass().add("table-header");
		grid.add(buildingLabel, buildingColumn, titlesRow);
		grid.setColumnSpan(itemLabel, buildingGapColumn-itemsStartColumn);
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
		}/*
		ObservableList<ColumnConstraints> li = grid.getColumnConstraints();
		for (int i = 0; i < li.size(); i++) {
			if (i >= mainGapColumn && i <= buildingGapColumn && (i == inoutGapColumn || (i-mainGapColumn)%2 == 0) && (w == 1 || (i > mainGapColumn && i < buildingGapColumn && i != inoutGapColumn)))
				continue;
			HBox hb = new HBox();
			hb.setMaxWidth(Double.POSITIVE_INFINITY);
			hb.setMaxHeight(w);
			//hb.setMinWidth(1);
			hb.setMinHeight(w);
			hb.setStyle("-fx-background-color: #"+c+";");
			grid.add(hb, i, row);
		}*/
		HBox hb = new HBox();
		hb.setMaxWidth(Double.POSITIVE_INFINITY);
		hb.setMaxHeight(w);
		//hb.setMinWidth(1);
		hb.setMinHeight(w);
		hb.setStyle("-fx-background-color: #"+c+";");
		hb.setViewOrder(-w);
		grid.add(hb, 0, row);
		GridPane.setColumnSpan(hb, GridPane.REMAINING);
	}

	protected final void createDivider(int col, int startRow, int tier) {
		Rectangle rect = new Rectangle();
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
		//rect.setFill(c);
		//rect.widthProperty().bind(gp.getColumnConstraints().get(split).maxWidthProperty().subtract(2));
		//rect.heightProperty().bind(gp.getRowConstraints().get(i).maxHeightProperty().subtract(2));

		/*
		rect.setWidth(w);
		grid.getColumnConstraints().get(col).setMinWidth(w);
		rect.setHeight(32);
		grid.add(rect, col, row);
		 */

		HBox hb = new HBox();
		hb.setMaxHeight(Double.POSITIVE_INFINITY);
		hb.setMaxWidth(w);
		//hb.minHeightProperty().bind(grid.heightProperty());
		hb.setMinWidth(w);
		hb.setStyle("-fx-background-color: #"+c+";");
		hb.setViewOrder(-w);
		grid.add(hb, col, startRow);
		GridPane.setRowSpan(hb, GridPane.REMAINING);
	}

	@Override
	public final void setFactory(Factory f) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final int getSortIndex() {
		return Integer.MIN_VALUE;
	}

	public final Future<Void> rebuild(boolean updateIO) {
		CompletableFuture<Void> f = new CompletableFuture();
		Logging.instance.log("Rebuilding grid "+type);
		//Thread.dumpStack();
		owner.setGridBuilt(type, false);
		contentWidths = null;
		ErrorableWithArgument<UUID> rebuild = (id) -> {
			Logging.instance.log("Matrix "+type+" rebuild task started");
			//Thread.sleep(5000);
			grid.setHgap(2);
			grid.setVgap(2);
			grid.setMaxHeight(Double.POSITIVE_INFINITY);
			grid.setMaxWidth(Double.POSITIVE_INFINITY);
			grid.setPrefHeight(Region.USE_COMPUTED_SIZE);
			grid.setPrefWidth(Region.USE_COMPUTED_SIZE);
			GridPane current = container.getMatrix(type);
			long gid = System.identityHashCode(grid);
			if (grid == current)
				throw new IllegalStateException("New "+type+" grid "+gid+" socketed too early!");
			Logging.instance.log("Rebuilding "+type+" matrix with pane "+gid+" ["+System.identityHashCode(current)+"]");
			//grid.getChildren().clear();
			//grid.getColumnConstraints().clear();
			//grid.getRowConstraints().clear();
			recipeEntries.clear();
			supplyGroup = null;
			generatorGroup = null;
			WaitDialogManager.instance.setTaskProgress(id, 5);
			this.rebuildGrid();
			WaitDialogManager.instance.setTaskProgress(id, 90);
			this.initializeWidths();
			Logging.instance.log("Finished rebuilding "+type+" matrix");
			if (updateIO)
				this.onUpdateIO();
			//Logging.instance.log(sum+" of "+Arrays.toString(w));
			//Platform.runLater(() -> grid.layout());
		};

		//thread jumping:
		//cancelled JFX: remove grid from scenetree
		//other thread: rebuild grid
		//JFX: put grid back in scenetree
		GuiUtil.runOnJFXThread(() -> {
			long old = System.identityHashCode(grid);
			grid = new GridPane();
			grid.getStyleClass().add("recipe-matrix");
			grid.getStyleClass().add(this.getClass().getName().toLowerCase(Locale.ENGLISH));
			Logging.instance.log("Beginning rebuild of "+type+" matrix: "+old+" > "+System.identityHashCode(grid)+" ["+System.identityHashCode(container.getMatrix(type))+"]");
			//gui.setMatrix(type, null); //leave old grid in place
			GuiUtil.queueTask("Rebuilding "+type+" matrix UI", rebuild, (id) -> {
				Logging.instance.log("Socketing new "+type+" matrix "+System.identityHashCode(grid));
				WaitDialogManager.instance.setTaskProgress(id, 95);
				container.setMatrix(type, grid);
				owner.setGridBuilt(type, true);
				f.complete(null);
			});
		});
		return f;
	}

	private void initializeWidths() {
		grid.getColumnConstraints().get(buildingColumn).setMinWidth(32);
		grid.layout();
		contentWidths = new double[grid.getColumnCount()];
		this.computeWidths(contentWidths, true);
	}

	public void alignWith(RecipeMatrixBase other) {
		this.computeWidths(other.contentWidths, false);
	}

	private void computeWidths(double[] minValues, boolean computeChildWidth) {
		if (computeChildWidth) {
			for (Node n : grid.getChildren()) {
				if (n instanceof Region) {
					int col = grid.getColumnIndex(n);
					Region r = (Region)n;
					contentWidths[col] = Math.max(minValues[col], Math.max(r.getMinWidth(), grid.getColumnConstraints().get(col).getMinWidth()));
				}

			}
		}
		else {
			for (int i = 0; i < contentWidths.length; i++) {
				if (i < minValues.length) {
					//Logging.instance.log("Column "+i+" = "+contentWidths[i]+" <> "+minValues[i]);
					this.updateColumnWidth(i, minValues[i]);
				}
			}
		}
		this.resizeGrid();
	}

	private void resizeGrid() {
		double sum = 0;
		for (double wd : contentWidths)
			sum += wd+grid.getHgap();
		grid.setMinWidth(sum+grid.getHgap()+8);
	}

	private void updateColumnWidth(int col, double atLeast) {/*
		if (col >= contentWidths.length) {
			Logging.instance.log(this+": Tried to set width of col "+col+" / "+contentWidths.length+"&"+grid.getColumnConstraints().size());
			return;
		}*/
		//Logging.instance.log("Updating width @ "+col+" >= "+atLeast+" from "+contentWidths[col]);
		contentWidths[col] = Math.max(atLeast, contentWidths[col]);
		grid.getColumnConstraints().get(col).setMinWidth(contentWidths[col]);
	}

	@Override
	public final void onAddRecipe(Recipe r) {
		this.rebuild(true);
	}

	@Override
	public final void onAddRecipes(Collection<Recipe> c) {
		this.rebuild(true);
	}

	@Override
	public final void onRemoveRecipes(Collection<Recipe> c) {
		this.rebuild(true);
	}

	@Override
	public final void onRemoveRecipe(Recipe r) {
		this.rebuild(true);
	}

	@Override
	public final void onCleared() {
		this.rebuild(false);
	}

	@Override
	public final Future<Void> onLoaded() {
		return this.rebuild(false);
	}

	@Override
	public void onSetCount(Generator g, Fuel fuel, double old, double count) {
		if ((old <= 0 && count > 0) || (count <= 0 && old > 0)) {
			this.rebuild(true);
		}
	}

	@Override
	public final void onAddProduct(Consumable c) {}

	@Override
	public final void onRemoveProduct(Consumable c) {}

	@Override
	public final void onRemoveProducts(Collection<Consumable> c) {}

	@Override
	public final void onToggleProductSink(Consumable c) {}

	@Override
	public final void onAddSupply(ResourceSupply s) {
		if (owner.resourceMatrixRule != InclusionPattern.EXCLUDE) {
			this.rebuild(true);
		}
	}

	@Override
	public final void onAddSupplies(Collection<? extends ResourceSupply> s) {
		if (owner.resourceMatrixRule != InclusionPattern.EXCLUDE) {
			this.rebuild(true);
		}
	}

	@Override
	public final void onRemoveSupply(ResourceSupply s) {
		if (owner.resourceMatrixRule != InclusionPattern.EXCLUDE) {
			this.rebuild(true);
		}
	}

	@Override
	public final void onRemoveSupplies(Collection<? extends ResourceSupply> c) {
		if (owner.resourceMatrixRule != InclusionPattern.EXCLUDE) {
			this.rebuild(true);
		}
	}

	@Override
	public void onUpdateSupply(ResourceSupply r) {

	}

	@Override
	public final void onSetToggle(ToggleableVisiblityGroup grp, boolean active) {}

	@Override
	public final void onSetFile(File f) {}

	public void updateStatuses(Consumable c) {

	}

	public double getWidth(int col) {
		return contentWidths[col];
	}

	private class RateSlot {

		private final GuiInstance<ItemRateController> gui;
		private final int columnIndex;

		private RateSlot(GuiInstance<ItemRateController> g, int idx) {
			gui = g;
			columnIndex = idx;
		}

	}

	protected class RecipeRow {

		public final ItemConsumerProducer recipe;
		public final int recipeIndex;
		public final int rowIndex;
		private final HashMap<Consumable, RateSlot> slots = new HashMap();

		private final Label label;

		private final HashMap<String, Node> auxNodes = new HashMap();

		private RecipeRow(ItemConsumerProducer r, int index, int row) throws IOException {
			super();
			recipe = r;
			rowIndex = row;
			recipeIndex = index;
			label = new Label(r.getDisplayName());
			String mod = r instanceof Recipe ? ((Recipe)r).getMod() : null;
			if (!Strings.isNullOrEmpty(mod)) {
				//label.setStyle("-fx-text-fill: "+ColorUtil.getCSSHex(UIConstants.HIGHLIGHT_COLOR)+";");
				label.setText(label.getText()+" ("+mod+")");
			}
			else if (r instanceof Fuel || (r instanceof GroupedProducer && ((GroupedProducer)r).getType() instanceof Fuel)) {
				//label.setStyle("-fx-text-fill: #ea5;");
			}
			else if (r instanceof ResourceSupply || (r instanceof GroupedProducer && ((GroupedProducer)r).getType() instanceof ResourceSupply)) {
				//label.setStyle("-fx-text-fill: #5ea;");
				if (r instanceof ResourceSupply)
					label.setText(label.getText()+" ("+((ResourceSupply)r).getResource().displayName+")");
			}
			label.setFont(Font.font(GuiSystem.getDefaultFont().getFamily(), FontWeight.NORMAL, FontPosture.REGULAR, 12));
			GuiUtil.sizeToContent(label);
			label.setPrefWidth(label.getMinWidth());
			label.setMaxWidth(label.getMinWidth());
			/*
			if (r instanceof Recipe) {
				this.setScale(owner.getCount((Recipe)r));
			}
			else if (r instanceof Fuel) {
				Fuel f = (Fuel)r;
				this.setScale(owner.getCount(f.generator, f));
			}*/
		}

		public void addChildNode(Node n, String id) {
			auxNodes.put(id, n);
		}

		public Node getChildNode(String id) {
			return auxNodes.get(id);
		}

		public void setScale(double scale) {
			for (RateSlot gui : slots.values()) {
				gui.gui.controller.setScale(scale);
				if (RecipeMatrixBase.this.isGridBuilt())
					RecipeMatrixBase.this.updateColumnWidth(gui.columnIndex, gui.gui.controller.getWidth());
			}
			if (RecipeMatrixBase.this.isGridBuilt())
				RecipeMatrixBase.this.resizeGrid();
		}

		public void setAmount(Consumable c, double amt) {
			RateSlot gui = slots.get(c);
			if (gui != null) {
				gui.gui.controller.setAmount(amt);
				if (RecipeMatrixBase.this.isGridBuilt())
					RecipeMatrixBase.this.updateColumnWidth(gui.columnIndex, gui.gui.controller.getWidth());
			}
			if (RecipeMatrixBase.this.isGridBuilt())
				RecipeMatrixBase.this.resizeGrid();
		}
		/*
		public double getInputWidth(Consumable c) {
			return inputSlots.get(c).gui.controller.getWidth();
		}

		public double getOutputWidth(Consumable c) {
			return outputSlots.get(c).gui.controller.getWidth();
		}*/
	}

	private class GroupedProducer<P extends ItemConsumerProducer> implements ItemConsumerProducer {

		public final String displayName;
		public final NamedIcon icon;

		private final Collection<P> producers;
		private final Function<P, Float> countFetch;

		public GroupedProducer(String name, NamedIcon ico, Collection<P> li, Function<P, Float> f) {
			displayName = name;
			icon = ico;
			producers = li;
			countFetch = f;
		}

		@Override
		public String getDisplayName() {
			return displayName;
		}

		@Override
		public NamedIcon getLocationIcon() {
			return icon;
		}

		public ItemConsumerProducer getType() {
			return producers.isEmpty() ? null : producers.iterator().next();
		}

		@Override
		public Map<Consumable, Double> getIngredientsPerMinute() {
			HashMap<Consumable, Double> map = new HashMap();
			for (P r : producers) {
				float scale = countFetch.apply(r);
				if (scale > 0)
					CountMap.incrementMapByMap(map, r.getIngredientsPerMinute(), scale);
			}
			return map;
		}

		@Override
		public Map<Consumable, Double> getProductsPerMinute() {
			HashMap<Consumable, Double> map = new HashMap();
			for (P r : producers) {
				float scale = countFetch.apply(r);
				if (scale > 0)
					CountMap.incrementMapByMap(map, r.getProductsPerMinute(), scale);
			}
			return map;
		}
	}
	/*
	private class GroupedSupply implements ItemConsumerProducer {

		@Override
		public String getDisplayName() {
			return "External Supplies";
		}

		@Override
		public Resource getLocationIcon() {
			return InternalIcons.SUPPLY;
		}

		@Override
		public Map<Consumable, Float> getIngredientsPerMinute() {
			return Map.of();
		}

		@Override
		public Map<Consumable, Float> getProductsPerMinute() {
			HashMap<Consumable, Float> map = new HashMap();
			for (ResourceSupply r : owner.getSupplies()) {
				Consumable c = r.getResource();
				Float has = map.get(c);
				map.put(c, (has == null ? 0 : has.floatValue())+r.getYield());
			}
			return map;
		}

	}

	private class GroupedGenerators implements ItemConsumerProducer {

		private static final ArrayList<Fuel> allFuels = new ArrayList();

		static {
			for (Generator g : Database.getAllGenerators()) {
				allFuels.addAll(g.getFuels());
			}
		}

		@Override
		public String getDisplayName() {
			return "Generators";
		}

		@Override
		public Resource getLocationIcon() {
			return InternalIcons.POWER;
		}

		@Override
		public Map<Consumable, Float> getIngredientsPerMinute() {
			HashMap<Consumable, Float> map = new HashMap();
			for (Fuel f : allFuels) {
				Consumable c = r.getResource();
				Float has = map.get(c);
				map.put(c, (has == null ? 0 : has.floatValue())+r.getYield());
			}
			return map;
		}

		@Override
		public Map<Consumable, Float> getProductsPerMinute() {
			HashMap<Consumable, Float> map = new HashMap();
			for (Fuel f : allFuels) {
				Consumable c = r.getResource();
				Float has = map.get(c);
				map.put(c, (has == null ? 0 : has.floatValue())+r.getYield());
			}
			return map;
		}

	}*/
}
