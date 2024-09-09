package Reika.SatisfactoryPlanner.GUI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.SearchableComboBox;

import com.google.common.base.Strings;

import Reika.SatisfactoryPlanner.FactoryListener;
import Reika.SatisfactoryPlanner.InclusionPattern;
import Reika.SatisfactoryPlanner.InternalIcons;
import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Data.Constants.ToggleableVisiblityGroup;
import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.ExtractableResource;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.Fuel;
import Reika.SatisfactoryPlanner.Data.FunctionalBuilding;
import Reika.SatisfactoryPlanner.Data.Generator;
import Reika.SatisfactoryPlanner.Data.Item;
import Reika.SatisfactoryPlanner.Data.LogisticSupply;
import Reika.SatisfactoryPlanner.Data.Milestone;
import Reika.SatisfactoryPlanner.Data.Recipe;
import Reika.SatisfactoryPlanner.Data.ResourceSupply;
import Reika.SatisfactoryPlanner.Data.SolidResourceNode;
import Reika.SatisfactoryPlanner.Data.Warning;
import Reika.SatisfactoryPlanner.Data.WaterExtractor;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.FontModifier;
import Reika.SatisfactoryPlanner.GUI.GuiUtil.SearchableSelector;
import Reika.SatisfactoryPlanner.Util.ColorUtil;
import Reika.SatisfactoryPlanner.Util.CountMap;
import Reika.SatisfactoryPlanner.Util.Logging;

import fxexpansions.ExpandingTilePane;
import fxexpansions.FXMLControllerBase;
import fxexpansions.GuiInstance;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class MainGuiController extends FXMLControllerBase implements FactoryListener, RecipeMatrixContainer {

	private boolean hasLoaded;

	@FXML
	private ScrollPane overviewScroll;

	@FXML
	private ScrollPane ioScroll;

	@FXML
	private ScrollPane craftingScroll;

	@FXML
	private ScrollPane powerScroll;

	@FXML
	private TilePane toggleFilterBox;

	@FXML
	private Button addInputButton;

	@FXML
	private Button addMineButton;

	@FXML
	private SearchableComboBox<Consumable> addProductButton;

	@FXML
	private ExpandingTilePane<ItemCountController> localSupplyTotals;

	@FXML
	private ExpandingTilePane<ItemCountController> buildingBar;

	@FXML
	private ExpandingTilePane<ItemCountController> netProductBar;

	@FXML
	private MenuItem clearMenu;

	@FXML
	private MenuItem clearProductMenu;

	@FXML
	private Menu controlMenu;

	@FXML
	private ExpandingTilePane<ItemCountController> buildCostBar;

	@FXML
	private ExpandingTilePane<ItemCountController> netConsumptionBar;

	@FXML
	private ExpandingTilePane<TierLampController> tierBar;

	@FXML
	private Tab craftingTab;

	@FXML
	private Menu currentMenu;

	@FXML
	private Menu factoryMenu;

	@FXML
	private VBox generatorList;

	@FXML
	private TitledPane gridContainer;

	@FXML
	private GridPane infoGrid;

	@FXML
	private TitledPane infoPanel;

	@FXML
	private TilePane inputGrid;

	@FXML
	private TitledPane inputPanel;

	@FXML
	private Tab ioTab;

	@FXML
	private MenuItem isolateMenu;

	@FXML
	private MenuBar menu;

	@FXML
	private TitledPane netGridContainer;

	@FXML
	private MenuItem newMenu;

	@FXML
	private MenuItem openMenu;

	@FXML
	private TitledPane outputPanel;

	@FXML
	private Tab overviewTab;

	@FXML
	private Label powerProduction;

	@FXML
	private Tab powerTab;

	@FXML
	private TilePane productGrid;

	@FXML
	private MenuItem quitMenu;

	@FXML
	private Menu recentMenu;

	@FXML
	private SearchableComboBox<Recipe> recipeDropdown;

	@FXML
	private VBox root;

	@FXML
	private MenuItem saveMenu;

	@FXML
	private MenuItem saveAsMenu;

	@FXML
	private MenuItem reloadMenu;

	@FXML
	private MenuItem settingsMenu;

	@FXML
	private GridPane statisticsGrid;

	@FXML
	private TitledPane statsPanel;

	@FXML
	private TabPane tabs;

	@FXML
	private VBox warningList;

	@FXML
	private TitledPane warningPanel;

	@FXML
	private MenuItem zeroMenu;

	@FXML
	private TextField factoryName;

	@FXML
	private GridPane matrixOptionGrid;

	@FXML
	private ChoiceBox<InclusionPattern> generatorMatrixOptions;

	@FXML
	private ChoiceBox<InclusionPattern> resourceMatrixOptions;

	@FXML
	private Slider tierFilter;

	//@FXML
	//private Button refreshButton;

	private Factory factory;

	private final EnumMap<ToggleableVisiblityGroup, CheckBox> toggleFilters = new EnumMap(ToggleableVisiblityGroup.class);
	private final HashMap<Generator, GuiInstance<GeneratorRowController>> generators = new HashMap();
	private final HashMap<Consumable, ProductButton> productButtons = new HashMap();
	private final HashMap<ResourceSupply, GuiInstance<? extends ResourceSupplyEntryController>> supplyEntries = new HashMap();
	private final HashMap<Node, GuiInstance<? extends ResourceSupplyEntryController>> supplyEntryNodes = new HashMap();
	private GuiInstance<TierLampController>[] tierLamps = null;
	private int maxAllowedTier = 999;

	private final Comparator<Node> supplySorter = new Comparator<Node>() {
		@Override
		public int compare(Node o1, Node o2) {
			return supplyEntryNodes.get(o1).controller.compareTo(supplyEntryNodes.get(o2).controller);
		}
	};

	@Override
	public void init(HostServices services) throws IOException {
		GuiSystem.setSplashProgress(85);
		Logging.instance.log("Initializing main UI");
		GuiUtil.setupAddSelector(recipeDropdown, new SearchableSelector<Recipe>(){
			@Override
			public void accept(Recipe t) {
				factory.addRecipe(t);
			}

			@Override
			public DecoratedListCell<Recipe> createListCell(String text, boolean button) {
				return new RecipeListCell(text, button);
			}

			@Override
			public String getEntryTypeName() {
				return "Recipe";
			}

			@Override
			public String getActionName() {
				return "Add";
			}

			@Override
			public boolean clearOnSelect() {
				return true;
			}
		});
		Logging.instance.log("Recipe list compiled");

		GuiUtil.setupAddSelector(addProductButton, new SearchableSelector<Consumable>(){
			@Override
			public void accept(Consumable t) {
				factory.addProduct(t);
			}

			@Override
			public DecoratedListCell<Consumable> createListCell(String text, boolean button) {
				return new ItemListCell(text, button);
			}

			@Override
			public String getEntryTypeName() {
				return "Product";
			}

			@Override
			public String getActionName() {
				return "Add";
			}

			@Override
			public boolean clearOnSelect() {
				return true;
			}
		});

		Logging.instance.log("Product list compiled");

		factoryName.textProperty().addListener((val, old, nnew) -> {
			factory.name = nnew;
		});

		tierFilter.valueProperty().addListener((val, old, nnew) -> {
			int old2 = maxAllowedTier;
			maxAllowedTier = ((Double)nnew).intValue();
			if (old2 != maxAllowedTier) {
				this.rebuildLists(true, false);
			}
		});

		for (ToggleableVisiblityGroup tv : ToggleableVisiblityGroup.values()) {
			CheckBox cb = new CheckBox(tv.displayName);
			cb.setSelected(true);
			cb.selectedProperty().addListener((val, old, nnew) -> {
				if (old != nnew)
					factory.setToggle(tv, nnew);
			});
			toggleFilters.put(tv, cb);
			cb.getStyleClass().add("widget");
			toggleFilterBox.getChildren().add(cb);
		}

		Logging.instance.log("Overview hooks initialized");

		GuiUtil.setButtonEvent(addMineButton, () -> this.openChildWindow("Add Resource Node", "ResourceNodeDialog"));
		GuiUtil.setButtonEvent(addInputButton, () -> this.openChildWindow("Add Logistic Supply", "LogisticSupplyDialog"));
		/*
		GuiUtil.setButtonEvent(refreshButton, () -> {
			Logging.instance.log("Refresh @ "+System.currentTimeMillis());
			factory.refreshMatrices();
		});
		 */
		GuiUtil.setMenuEvent(settingsMenu, () -> this.openChildWindow("Application Settings", "Settings"));
		GuiUtil.setMenuEvent(quitMenu, () -> this.close());
		GuiUtil.setMenuEvent(newMenu, () -> {
			GuiUtil.queueTask("Loading new factory", (id) -> {
				this.setFactory(new Factory());
				double pct = 20;
				WaitDialogManager.instance.setTaskProgress(id, pct);
				factory.init(pct, id);
			}, (id) -> this.rebuildEntireUI());
		});
		GuiUtil.setMenuEvent(saveMenu, () -> factory.save());
		GuiUtil.setMenuEvent(saveAsMenu, () -> {
			File f = this.openSaveAsDialog(factory.name+".factory", Main.getRelativeFile("Factories"));
			if (f != null) {
				factory.save(f);
			}
		});
		GuiUtil.setMenuEvent(reloadMenu, () -> factory.reload());
		//GuiUtil.setMenuEvent(openMenu, () -> this.openFXMLDialog("Open Factory", "OpenMenuDialog"));
		GuiUtil.setMenuEvent(openMenu, () -> {
			File f = this.openFileDialog("Factory", Main.getRelativeFile("Factories"), new FileChooser.ExtensionFilter("Factory files (*.factory)", "*.factory"));
			if (f != null && f.exists()) {
				Factory.loadFactory(f, this);
			}
		});
		GuiUtil.setMenuEvent(clearMenu, () -> factory.clearRecipes());
		GuiUtil.setMenuEvent(zeroMenu, () -> {
			for (Recipe r : new ArrayList<Recipe>(factory.getRecipes())) {
				factory.setCount(r, 0);
			}
		});
		GuiUtil.setMenuEvent(clearProductMenu, () -> factory.removeProducts(new ArrayList<Consumable>(factory.getDesiredProducts())));
		GuiUtil.setMenuEvent(isolateMenu, () -> factory.removeExternalSupplies(new ArrayList<ResourceSupply>(factory.getSupplies())));
		Logging.instance.log("Menu hooks initialized");

		statisticsGrid.getRowConstraints().get(0).minHeightProperty().bind(buildingBar.minHeightProperty());
		statisticsGrid.getRowConstraints().get(1).minHeightProperty().bind(buildCostBar.minHeightProperty());
		statisticsGrid.getRowConstraints().get(2).minHeightProperty().bind(netConsumptionBar.minHeightProperty());
		statisticsGrid.getRowConstraints().get(3).minHeightProperty().bind(netProductBar.minHeightProperty());

		generatorMatrixOptions.setItems(FXCollections.observableArrayList(InclusionPattern.values()));
		resourceMatrixOptions.setItems(FXCollections.observableArrayList(InclusionPattern.values()));

		StringConverter<InclusionPattern> conv = new StringConverter<InclusionPattern>() {
			@Override
			public String toString(InclusionPattern p) {
				return p == null ? "" : StringUtils.capitalize(p.name().toLowerCase(Locale.ENGLISH));
			}

			@Override
			public InclusionPattern fromString(String s) {
				return Strings.isNullOrEmpty(s) ? null : InclusionPattern.valueOf(s.toUpperCase(Locale.ENGLISH));
			}
		};
		resourceMatrixOptions.setConverter(conv);
		generatorMatrixOptions.setConverter(conv);

		generatorMatrixOptions.getSelectionModel().selectedItemProperty().addListener((val, old, nnew) -> {
			factory.generatorMatrixRule = nnew;
			if (matrixOptionsActive) {
				factory.updateIO();
				factory.rebuildMatrices(true);
			}
		});
		resourceMatrixOptions.getSelectionModel().selectedItemProperty().addListener((val, old, nnew) -> {
			factory.resourceMatrixRule = nnew;
			if (matrixOptionsActive) {
				factory.updateIO();
				factory.rebuildMatrices(true);
			}
		});
		Logging.instance.log("Matrix options set up");

		buildingBar.minRowHeight = 32;
		buildCostBar.minRowHeight = 32;
		netConsumptionBar.minRowHeight = 32;
		netProductBar.minRowHeight = 32;

		this.setupTierBar();
		Logging.instance.log("Initialization complete");
	}

	private void setupTierBar() {
		tierBar.getChildren().clear();
		tierLamps = new GuiInstance[Milestone.getMaxTier()+1];
		for (int i = 0; i < tierLamps.length; i++) {
			TierLampController c = new TierLampController(i);
			GuiInstance<TierLampController> gui = new GuiInstance<TierLampController>(c.getRootNode(), c);
			tierBar.addEntry(gui);
			tierLamps[i] = gui;
		}

		tierFilter.setMax(Milestone.getMaxTier());
		tierFilter.setValue(tierFilter.getMax());
	}

	public Factory getFactory() {
		return factory;
	}

	private boolean matrixOptionsActive = true;

	public void setFactory(Factory f) {
		if (factory != null)
			factory.prepareDisposal();
		factory = f;
		factory.addCallback(this);

		GuiUtil.runOnJFXThread(() -> factory.setUI(this));

		matrixOptionsActive = false;
		generatorMatrixOptions.getSelectionModel().select(factory.generatorMatrixRule);
		resourceMatrixOptions.getSelectionModel().select(factory.resourceMatrixRule);
		matrixOptionsActive = true;

		for (GuiInstance<GeneratorRowController> gui : generators.values()) {
			gui.controller.setFactory(f);
		}
	}

	@Override
	protected void postInit(Stage w) throws IOException {
		super.postInit(w);
		Logging.instance.log("Postinit main UI start");
		GuiSystem.setSplashProgress(90);

		GuiUtil.queueTask("Building recipe menu", (id) -> RecipeListCell.init());

		overviewTab.setGraphic(new ImageView(InternalIcons.OVERVIEW.createIcon(16)));
		ioTab.setGraphic(new ImageView(InternalIcons.INPUTOUTPUT2.createIcon(16)));
		powerTab.setGraphic(new ImageView(InternalIcons.POWERTAB.createIcon(16)));
		craftingTab.setGraphic(new ImageView(InternalIcons.MATRICES.createIcon(16)));

		for (Tab t : tabs.getTabs()) {
			ScrollPane p = (ScrollPane)t.getContent();
			//p.prefHeightProperty().bind(tabs.heightProperty().subtract(32*0));
			//p.setMinHeight(Region.USE_PREF_SIZE);
			//p.minHeightProperty().bind(p.prefHeightProperty());
			//p.maxHeightProperty().bind(p.prefHeightProperty());
			p.prefWidthProperty().bind(this.getWindow().widthProperty().subtract(32));
			p.setMaxWidth(Region.USE_PREF_SIZE);
			p.setFitToHeight(true);
			p.setFitToWidth(true);

			HBox hb = GuiUtil.createSpacedHBox(new Label(t.getText()), t.getGraphic(), null);
			hb.setSpacing(11);
			t.setGraphic(hb);
			t.setText(null);
		}
		Logging.instance.log("Tabs set up");

		this.getWindow().getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN), () -> {root.layout(); tabs.requestLayout();});

		for (Generator g : Database.getAllGenerators()) {
			GuiInstance<GeneratorRowController> gui = this.loadNestedFXML("GeneratorRow", n -> {
				TitledPane tp = new TitledPane(g.displayName, n.rootNode);
				tp.setAnimated(false);
				tp.setExpanded(true);
				tp.setCollapsible(false);
				tp.getStyleClass().add("panel");
				generatorList.getChildren().add(GuiUtil.applyPanelBolts(tp, 3));
			});
			gui.controller.setGenerator(g);
			generators.put(g, gui);
		}
		Logging.instance.log("Generators loaded: "+generators.toString());

		GuiUtil.initWidgets(gridContainer);
		GuiUtil.initWidgets(netGridContainer);

		Logging.instance.log("Matrices set up");

		this.buildRecentList();

		Logging.instance.log("Setting new factory");
		this.setFactory(new Factory());
		factory.rebuildMatrices(false);

		Logging.instance.log("Refreshing UI");
		this.rebuildEntireUI();

		Platform.runLater(() -> this.layout());
		Logging.instance.log("Postinit complete");
	}

	public void rebuildLists(boolean recipe, boolean products) {
		if (products) {
			addProductButton.getSelectionModel().clearSelection();
			ArrayList<Consumable> li = new ArrayList(Database.getAllItems());
			li.removeIf(c -> !this.isItemValid(c));
			addProductButton.setItems(FXCollections.observableArrayList(li));
		}

		if (recipe) {
			recipeDropdown.getSelectionModel().clearSelection();
			ArrayList<Recipe> li2 = new ArrayList(Database.getAllAutoRecipes());
			if (factory != null)
				li2.removeIf(r -> !this.isRecipeValid(r) || factory.getRecipes().contains(r));

			recipeDropdown.setItems(FXCollections.observableList(li2));
			recipeDropdown.setDisable(li2.isEmpty());
		}

		this.setupTierBar();
	}

	public void buildRecentList() {
		recentMenu.getItems().clear();
		for (File f : Main.getRecentFiles()) {
			String n = f.getName();
			MenuItem mi = new MenuItem(n.substring(0, n.lastIndexOf('.')));
			GuiUtil.setMenuEvent(mi, () -> Factory.loadFactory(f, this));
			recentMenu.getItems().add(mi);
		}
	}

	public boolean isItemValid(Consumable r) {
		for (ToggleableVisiblityGroup tv : ToggleableVisiblityGroup.values()) {
			if (tv.isItemInGroup.test(r) && !factory.getToggle(tv))
				return false;
		}
		return true;
	}

	public boolean isRecipeValid(Recipe r) {
		if (r.getTier() > maxAllowedTier)
			return false;
		for (ToggleableVisiblityGroup tv : ToggleableVisiblityGroup.values()) {
			if (!factory.getToggle(tv) && tv.isRecipeInGroup.test(r))
				return false;
		}
		return true;
	}

	public void rebuildEntireUI() {
		this.rebuildLists(true, true);

		factoryName.setText(factory.name);
		for (ToggleableVisiblityGroup tv : ToggleableVisiblityGroup.values()) {
			toggleFilters.get(tv).setSelected(factory.getToggle(tv));
		}

		productGrid.getChildren().removeIf(n -> n instanceof ProductButton);
		for (Consumable c : factory.getDesiredProducts())
			this.onAddProduct(c);

		for (Entry<Generator, GuiInstance<GeneratorRowController>> e : generators.entrySet()) {
			Generator g = e.getKey();
			for (Fuel f : g.getFuels()) {
				e.getValue().controller.setCount(f, factory.getCount(g, f));
			}
		}

		inputGrid.getChildren().removeIf(n -> !(n instanceof Button));
		for (ResourceSupply res : factory.getSupplies()) {
			this.onAddSupply(res);
		}

		this.updateStats(true);

		this.layout();
	}

	private void updateWarnings() {
		ArrayList<Warning> li = new ArrayList();
		factory.getWarnings(w -> li.add(w));
		boolean any = !li.isEmpty();
		Collections.sort(li);
		warningList.getChildren().clear();
		for (Warning w : li) {
			warningList.getChildren().add(w.createUI());
		}
		if (!any) {
			Label lb = new Label("None - Your Factory Is Perfectly Efficient!");
			lb.setFont(GuiSystem.getFont());
			lb.setStyle("-fx-text-fill: "+ColorUtil.getCSSHex(UIConstants.OKAY_COLOR)+";");
			warningList.getChildren().add(lb);
		}
	}

	public void updateStats(boolean all) {
		this.updateStats(all, all, all, all, all, all, all);
	}

	public void updateStats(boolean warnings, boolean buildings, boolean production, boolean consuming, boolean local, boolean power, boolean tier) {
		if (warnings)
			this.updateWarnings();

		if (buildings) {
			buildCostBar.getChildren().clear();
			buildingBar.getChildren().clear();

			CountMap<Item> cost = new CountMap();
			CountMap<FunctionalBuilding> bc = factory.getBuildings();
			for (FunctionalBuilding b : bc.keySet()) {

				int amt = bc.get(b);
				GuiUtil.addIconCount(b, amt, 5, buildingBar);

				for (Entry<Item, Integer> e : b.getConstructionCost().entrySet()) {
					cost.increment(e.getKey(), e.getValue()*amt);
				}
			}

			for (Item i : cost.keySet()) {
				GuiUtil.addIconCount(i, cost.get(i), 5, buildCostBar);
			}
		}

		if (tier) {
			int max = factory.getMaxTier();
			for (int i = 0; i < tierLamps.length; i++) {
				tierLamps[i].controller.setState(i <= max);
			}
		}

		if (power) {
			float[] avgMinMax = new float[3];
			factory.computeNetPowerProduction(avgMinMax);
			String text = String.format("%.2fMW", avgMinMax[0]);
			if (Math.abs(avgMinMax[1]-avgMinMax[2]) > 0.1) {
				text = String.format("%s average (%.2fMW to %.2fMW range)", text, avgMinMax[1], avgMinMax[2]);
			}
			powerProduction.setText(text);
			if (avgMinMax[0] > 0) {
				powerProduction.setStyle(GuiSystem.getFontStyle(FontModifier.BOLD)+" -fx-text-fill: "+ColorUtil.getCSSHex(UIConstants.OKAY_COLOR)+";");
			}
			else if (avgMinMax[0] < 0) {
				powerProduction.setStyle(GuiSystem.getFontStyle(FontModifier.BOLD)+" -fx-text-fill: "+ColorUtil.getCSSHex(UIConstants.WARN_COLOR)+";");
			}
			else {
				powerProduction.setStyle("");
			}
		}

		/*
		if (consuming || production) {
			Collection<Consumable> all = factory.getAllRelevantItems();
			if (consuming)
				netConsumptionBar.getChildren().clear();
			if (production)
				netProductBar.getChildren().clear();
			for (Consumable c : all) {
				float amt = factory.getFlow(c).getNetYield();
				if (amt < 0 && consuming)
					GuiUtil.addIconCount(netConsumptionBar, c, -amt);
				if (amt > 0 && consuming)
					GuiUtil.addIconCount(netProductBar, c, amt);
			}
		}*/
		if (consuming) {
			netConsumptionBar.getChildren().clear();
			for (Consumable c : factory.getAllIngredients()) {
				float amt = factory.getTotalConsumption(c)-factory.getTotalProduction(c)-factory.getExternalInput(c, true);
				if (amt > 0)
					GuiUtil.addIconCount(c, amt, 5, netConsumptionBar);
			}
		}
		if (production) {
			netProductBar.getChildren().clear();
			HashSet<Consumable> set = new HashSet(factory.getAllProducedItems());
			set.addAll(factory.getAllMinedItems());
			for (Consumable c : set) {
				float amt = factory.getTotalProduction(c)+factory.getExternalInput(c, true)-factory.getTotalConsumption(c);
				if (amt > 0)
					GuiUtil.addIconCount(c, amt, 5, netProductBar);
			}
		}

		if (local) {
			localSupplyTotals.getChildren().clear();
			CountMap<Consumable> totalSupply = new CountMap();
			for (ResourceSupply res : factory.getSupplies()) {
				totalSupply.increment(res.getResource(), res.getYield());
			}
			for (Consumable c : totalSupply.keySet()) {
				GuiUtil.addIconCount(c, totalSupply.get(c), 5, localSupplyTotals);
			}
		}
		root.layout();
	}

	@Override
	public void onAddRecipe(Recipe r) {
		this.rebuildLists(true, false);
		this.updateStats(true, true, true, true, false, true, true);
	}

	@Override
	public void onRemoveRecipe(Recipe r) {
		this.rebuildLists(true, false);
		this.updateStats(true, true, true, true, false, true, true);
	}

	@Override
	public void onRemoveRecipes(Collection<Recipe> c) {
		this.rebuildLists(true, false);
		this.updateStats(true, true, true, true, false, true, true);
	}

	@Override
	public void onSetCount(Recipe r, float count) {
		this.updateStats(true, true, true, true, false, true, false);
	}

	@Override
	public void onSetCount(Generator g, Fuel fuel, int old, int count) {
		generators.get(g).controller.setCount(fuel, count);/*
		double maxCountW = -1;
		double maxPowerW = -1;/*
		for (Entry<Generator, GuiInstance<GeneratorRowController>> e : this.generators.entrySet()) {
			e.getValue().controller.setWidths(count, count);
		}*//*
		for (GuiInstance<GeneratorRowController> gui : generators.values()) {
			maxCountW = Math.max(maxCountW, gui.controller.getCountWidth());
			maxPowerW = Math.max(maxPowerW, gui.controller.getPowerWidth());
		}
		for (GuiInstance<GeneratorRowController> gui : generators.values()) {
			gui.controller.setWidths(count, count);
		}*/
		this.updateStats(true, true, true, true, false, true, count == 0 || old == 0);
	}

	@Override
	public void onAddProduct(Consumable c) {
		productGrid.getChildren().add(new ProductButton(c));
		this.updateStats(true, false, false, false, false, false, false);
	}

	@Override
	public void onRemoveProduct(Consumable c) {
		productGrid.getChildren().remove(productButtons.get(c));
		this.updateStats(true, false, false, false, false, false, false);
	}

	@Override
	public void onRemoveProducts(Collection<Consumable> c) {
		for (Consumable cc : c)
			productGrid.getChildren().remove(productButtons.get(cc));
		this.updateStats(true, false, false, false, false, false, false);
	}

	@Override
	public void onAddSupply(ResourceSupply res) {
		if (res instanceof WaterExtractor) {
			this.addResourceEntry("WaterEntry", res);
		}
		else if (res instanceof SolidResourceNode) {
			this.addResourceEntry("MinerEntry", res);
		}
		else if (res instanceof ExtractableResource) {
			this.addResourceEntry("ResourceMineEntry", res);
		}
		else if (res instanceof LogisticSupply) {
			this.addResourceEntry("LogisticSupplyEntry", res);
		}
		this.updateStats(true, false, true, true, true, true, false);
	}

	private <C extends ResourceSupplyEntryController, R extends ResourceSupply> void addResourceEntry(String fxml, R res) {
		try {
			this.loadNestedFXML(fxml, g -> {
				try {
					((ResourceSupplyEntryController)g.controller).setSupply(factory, res);
					supplyEntries.put(res, g);
					supplyEntryNodes.put(g.rootNode, g);
					GuiUtil.addSortedNode(inputGrid, g.rootNode, supplySorter);
				}
				catch (IOException e) {
					GuiUtil.showException(e);
				}
			});
		}
		catch (IOException e) {
			GuiUtil.showException(e);
		}
	}

	@Override
	public void onRemoveSupply(ResourceSupply s) {
		inputGrid.getChildren().remove(supplyEntries.get(s).rootNode);
		this.updateStats(true, false, true, true, true, true, false);
	}

	@Override
	public void onRemoveSupplies(Collection<ResourceSupply> c) {
		for (ResourceSupply s : c)
			inputGrid.getChildren().remove(supplyEntries.get(s).rootNode);
		this.updateStats(true, false, true, true, true, true, false);
	}

	@Override
	public void onUpdateSupply(ResourceSupply s) {
		//do not need anything since update IO accomplishes
	}

	@Override
	public void onSetToggle(ToggleableVisiblityGroup tv, boolean active) {
		toggleFilters.get(tv).setSelected(active);
		this.rebuildLists(true, true);
	}

	@Override
	public void onUpdateIO() {
		this.updateStats(true, false, true, true, true, true, false);
	}

	@Override
	public Future<Void> onLoaded() {
		this.onSetFile(factory.getFile());
		CompletableFuture<Void> f = new CompletableFuture();
		GuiUtil.runOnJFXThread(() -> {
			this.rebuildEntireUI();
			f.complete(null);
		});
		return f;
	}

	@Override
	public void onCleared() {
		GuiUtil.runOnJFXThread(() -> this.rebuildEntireUI());
	}

	@Override
	public void onSetFile(File f) {
		boolean dis = !factory.hasExistingFile();
		saveMenu.setDisable(dis);
		reloadMenu.setDisable(dis);
	}

	@Override
	public int getSortIndex() {
		return Integer.MAX_VALUE;
	}

	private class ProductButton extends Button {

		public final Consumable item;

		private ProductButton(Consumable c) {
			item = c;
			int size = 64;//32;
			Pane p = new Pane();
			ImageView ico = new ImageView(c.createIcon(size));
			p.getChildren().add(ico);
			GuiUtil.setTooltip(this, c.displayName);
			this.setPrefWidth(size);
			this.setPrefHeight(size);
			this.setMinHeight(Region.USE_PREF_SIZE);
			this.setMaxHeight(Region.USE_PREF_SIZE);
			this.setMinWidth(Region.USE_PREF_SIZE);
			this.setMaxWidth(Region.USE_PREF_SIZE);
			this.setOnAction(e -> {
				factory.removeProduct(c);
			});
			p.setPrefWidth(size);
			p.setPrefHeight(size);
			p.setMinHeight(Region.USE_PREF_SIZE);
			p.setMaxHeight(Region.USE_PREF_SIZE);
			p.setMinWidth(Region.USE_PREF_SIZE);
			p.setMaxWidth(Region.USE_PREF_SIZE);
			ImageView img = new ImageView(new Image(Main.class.getResourceAsStream("Resources/Graphics/Icons/delete.png"), 16, 16, true, true));
			img.layoutXProperty().bind(p.widthProperty().subtract(img.getImage().getWidth()));
			img.layoutYProperty().bind(p.heightProperty().subtract(img.getImage().getHeight()));
			p.getChildren().add(img);
			productButtons.put(c, this);
			this.setGraphic(p);
		}


	}

	@Override
	public void setMatrix(MatrixType mt, GridPane g) {
		TitledPane tp = null;
		switch(mt) {
			case MAIN:
				tp = gridContainer;
				break;
			case SCALE:
				tp = netGridContainer;
				break;
		}

		tp.setContent(g);
	}

	@Override
	public GridPane getMatrix(MatrixType mt) {
		TitledPane tp = null;
		switch(mt) {
			case MAIN:
				tp = gridContainer;
				break;
			case SCALE:
				tp = netGridContainer;
				break;
		}

		return (GridPane)tp.getContent();
	}

	@Override
	protected void onKeyPressed(KeyCode code) {
		if (code == KeyCode.SHIFT)
			factory.setLargeMatrixSpinnerStep(true);
	}

	@Override
	protected void onKeyReleased(KeyCode code) {
		if (code == KeyCode.SHIFT)
			factory.setLargeMatrixSpinnerStep(false);
	}

	public void layout() {
		if (this.getRootNode() != null)
			this.getRootNode().layout();
		Platform.runLater(() -> { //ugly hack but necessary to resize the tabpane, and later since it needs a layout pass to finish
			tabs.requestLayout();/*
				container.window.sizeToScene();
				container.window.centerOnScreen();
				Rectangle2D monitor = Screen.getPrimary().getVisualBounds();
				if (container.window.getWidth() > monitor.getWidth())
					container.window.setWidth(monitor.getWidth());
				if (container.window.getHeight() > monitor.getHeight()-48) //48 for title bar
					container.window.setHeight(monitor.getHeight()-48);*/
			this.getRootNode().layout();
		});
	}
}
