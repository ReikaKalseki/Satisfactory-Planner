package Reika.SatisfactoryPlanner.GUI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.Fraction;
import org.controlsfx.control.SearchableComboBox;

import com.google.common.base.Strings;
import com.google.common.collect.HashBiMap;

import Reika.SatisfactoryPlanner.ConfirmationOptions;
import Reika.SatisfactoryPlanner.FactoryListener;
import Reika.SatisfactoryPlanner.InclusionPattern;
import Reika.SatisfactoryPlanner.InternalIcons;
import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Setting;
import Reika.SatisfactoryPlanner.Data.Constants.ToggleableVisiblityGroup;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.Objects.Consumable;
import Reika.SatisfactoryPlanner.Data.Objects.Fuel;
import Reika.SatisfactoryPlanner.Data.Objects.Milestone;
import Reika.SatisfactoryPlanner.Data.Objects.Recipe;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.Generator;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.SimpleProductionBuilding;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.ExtractableResource;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.FromFactorySupply;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.LogisticSupply;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.ResourceSupply;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.SimpleProductionSupply;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.SolidResourceNode;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.TieredLogisticSupply;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.WaterExtractor;
import Reika.SatisfactoryPlanner.GUI.GuiUtil.SearchableSelector;
import Reika.SatisfactoryPlanner.GUI.Components.FactoryStatisticsContainer;
import Reika.SatisfactoryPlanner.GUI.Components.GeneratorRowController;
import Reika.SatisfactoryPlanner.GUI.Components.ItemCountController;
import Reika.SatisfactoryPlanner.GUI.Components.ItemOutputController;
import Reika.SatisfactoryPlanner.GUI.Components.ListCells.BuildingListCell;
import Reika.SatisfactoryPlanner.GUI.Components.ListCells.DecoratedListCell;
import Reika.SatisfactoryPlanner.GUI.Components.ListCells.ItemListCell;
import Reika.SatisfactoryPlanner.GUI.Components.ListCells.RecipeListCell;
import Reika.SatisfactoryPlanner.GUI.Supplies.ResourceSupplyEntryController;
import Reika.SatisfactoryPlanner.GUI.Windows.SummaryViewController;
import Reika.SatisfactoryPlanner.Util.Logging;

import fxexpansions.ExpandingTilePane;
import fxexpansions.GuiInstance;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class MainGuiController extends FactoryStatisticsContainer implements FactoryListener, RecipeMatrixContainer {

	private boolean hasLoaded;

	@FXML
	protected VBox root;

	@FXML
	private TitledPane recipeListContainer;

	@FXML
	protected TitledPane inputGridContainer;

	@FXML
	protected TitledPane outputGridContainer;

	@FXML
	protected VBox recipeListView;

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
	private Button addFactoryInputButton;

	@FXML
	private SearchableComboBox<SimpleProductionBuilding> simpleProducerButton;

	@FXML
	private SearchableComboBox<Consumable> addProductButton;

	@FXML
	private ExpandingTilePane<ItemCountController> localSupplyTotals;

	@FXML
	private MenuItem clearMenu;

	@FXML
	private MenuItem clearProductMenu;

	@FXML
	private MenuItem summaryMenu;

	@FXML
	private Menu controlMenu;

	@FXML
	private Tab craftingTab;

	@FXML
	private Menu currentMenu;

	@FXML
	private Menu factoryMenu;

	@FXML
	private VBox generatorList;

	@FXML
	private VBox sinkList;

	@FXML
	private GridPane infoGrid;

	@FXML
	private TitledPane infoPanel;

	@FXML
	private ExpandingTilePane inputGrid;

	@FXML
	private TitledPane inputPanel;

	@FXML
	private Tab ioTab;

	@FXML
	private MenuItem isolateMenu;

	@FXML
	private MenuItem cleanupMenu;

	@FXML
	private MenuBar menu;

	@FXML
	private MenuItem newMenu;

	@FXML
	private MenuItem openMenu;

	@FXML
	private TitledPane outputPanel;

	@FXML
	private Tab overviewTab;

	@FXML
	private Tab powerTab;

	@FXML
	private ExpandingTilePane<ItemOutputController> productGrid;

	@FXML
	private MenuItem quitMenu;

	@FXML
	private Menu recentMenu;

	@FXML
	private SearchableComboBox<Recipe> recipeDropdown;

	@FXML
	private MenuItem saveMenu;

	@FXML
	private MenuItem saveAsMenu;

	@FXML
	private MenuItem reloadMenu;

	@FXML
	private MenuItem settingsMenu;

	@FXML
	private MenuItem neiMenu;

	@FXML
	private MenuItem itemViewerMenu;

	@FXML
	private MenuItem customRecipeMenu;

	@FXML
	private TitledPane statsPanel;

	@FXML
	private TabPane tabs;

	@FXML
	private MenuItem zeroMenu;

	@FXML
	private Menu helpMenu;

	@FXML
	private MenuItem aboutMenu;

	@FXML
	private MenuItem guideMenu;

	@FXML
	private MenuItem bugMenu;

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

	@FXML
	private MenuItem appFolderMenu;

	@FXML
	private MenuItem sfFolderMenu;

	@FXML
	private Menu modContentLibMenu;

	//@FXML
	//private Button refreshButton;

	private boolean matrixOptionsActive = true;
	private boolean rebuilding = false;
	private String lastNameSavedWith = null;

	private final EnumMap<ToggleableVisiblityGroup, CheckBox> toggleFilters = new EnumMap(ToggleableVisiblityGroup.class);
	private final HashMap<Generator, GuiInstance<GeneratorRowController>> generators = new HashMap();
	private final HashMap<Consumable, GuiInstance<ItemOutputController>> productButtons = new HashMap();
	private final HashMap<ResourceSupply, GuiInstance<? extends ResourceSupplyEntryController>> supplyEntries = new HashMap();
	private final HashMap<Node, GuiInstance<? extends ResourceSupplyEntryController>> supplyEntryNodes = new HashMap();
	private final HashBiMap<Recipe, Node> recipeListEntries = HashBiMap.create();
	private int maxAllowedTier = 999;

	private final Comparator<Node> supplySorter = new Comparator<Node>() {
		@Override
		public int compare(Node o1, Node o2) {
			return supplyEntryNodes.get(o1).controller.compareTo(supplyEntryNodes.get(o2).controller);
		}
	};

	private final Comparator<Node> recipeListSorter = new Comparator<Node>() {
		@Override
		public int compare(Node o1, Node o2) {
			Map<Node, Recipe> map = recipeListEntries.inverse();
			return map.get(o1).compareTo(map.get(o2));
		}
	};

	@Override
	public void init(HostServices services) throws IOException {
		GuiSystem.setSplashProgress(85);
		Logging.instance.log("Initializing main UI");

		GuiUtil.disableFocus(root);

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

		GuiUtil.setupAddSelector(simpleProducerButton, new SearchableSelector<SimpleProductionBuilding>(){
			@Override
			public void accept(SimpleProductionBuilding t) {
				//Logging.instance.log("Adding simple producer "+t);
				SimpleProductionSupply res = new SimpleProductionSupply(t);
				factory.addExternalSupply(res);
			}

			@Override
			public DecoratedListCell<SimpleProductionBuilding> createListCell(String text, boolean button) {
				return new BuildingListCell(text, button);
			}

			@Override
			public String getEntryTypeName() {
				return "Simple Producer";
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

		factoryName.textProperty().addListener((val, old, nnew) -> {
			factory.name = nnew;
		});

		tierFilter.valueProperty().addListener((val, old, nnew) -> {
			int old2 = maxAllowedTier;
			maxAllowedTier = (int)Math.round(((Double)nnew).doubleValue());
		});

		tierFilter.valueChangingProperty().addListener((val, old, nnew) -> {
			if (old && !nnew) {
				this.rebuildLists(true, false, true);
			}
		});

		for (ToggleableVisiblityGroup tv : ToggleableVisiblityGroup.values()) {
			CheckBox cb = new CheckBox(tv.displayName);
			cb.setSelected(true);
			cb.selectedProperty().addListener((val, old, nnew) -> {
				if (old != nnew && !rebuilding)
					factory.setToggle(tv, nnew);
			});
			toggleFilters.put(tv, cb);
			cb.getStyleClass().add("widget");
			toggleFilterBox.getChildren().add(cb);
		}

		Logging.instance.log("Overview hooks initialized");

		GuiUtil.setButtonEvent(addMineButton, () -> this.openChildWindow("Add Resource Node", "ResourceNodeDialog"));
		GuiUtil.setButtonEvent(addInputButton, () -> this.openChildWindow("Add Logistic Supply", "LogisticSupplyDialog"));
		GuiUtil.setButtonEvent(addFactoryInputButton, () -> {
			File f = this.openFactoryFile();
			factory.addFactorySupplies(f);
		});
		/*
		GuiUtil.setButtonEvent(refreshButton, () -> {
			Logging.instance.log("Refresh @ "+System.currentTimeMillis());
			factory.refreshMatrices();
		});
		 */
		GuiUtil.setMenuEvent(settingsMenu, () -> this.openChildWindow("Application Settings", "Settings"));
		GuiUtil.setMenuEvent(neiMenu, () -> this.openChildWindow("Recipe Catalogue", "RecipeCatalog"));
		GuiUtil.setMenuEvent(itemViewerMenu, () -> this.openChildWindow("Item Catalogue", "ItemCatalog"));
		GuiUtil.setMenuEvent(customRecipeMenu, () -> this.openChildWindow("Custom Recipe Definition", "CustomRecipeDefinitionDialog"));
		GuiUtil.setMenuEvent(quitMenu, () -> {
			if (!factory.hasUnsavedChanges() || GuiUtil.getToggleableConfirmation(ConfirmationOptions.CLOSE)) {
				Platform.exit();
			}
		});
		GuiUtil.setMenuEvent(newMenu, () -> {
			if (!factory.hasUnsavedChanges() || GuiUtil.getToggleableConfirmation(ConfirmationOptions.NEWOPEN)) {
				GuiUtil.queueTask("Loading new factory", (id) -> {
					this.setFactory(new Factory());
					double pct = 20;
					WaitDialogManager.instance.setTaskProgress(id, pct);
					factory.init(pct, id);
				}, (id) -> this.rebuildEntireUI());
			}
		});
		GuiUtil.setMenuEvent(saveMenu, () -> {
			if (!factory.name.equals(lastNameSavedWith) && !GuiUtil.getToggleableConfirmation(ConfirmationOptions.SAVEDIFFNAME, lastNameSavedWith))
				return;
			lastNameSavedWith = factory.name;
			factory.save();
		});
		GuiUtil.setMenuEvent(saveAsMenu, () -> {
			File f = GuiUtil.openSaveAsDialog(this.getWindow(), factory.name+".factory", Main.getRelativeFile("Factories"));
			if (f != null) {
				lastNameSavedWith = factory.name;
				factory.save(f);
			}
		});
		GuiUtil.setMenuEvent(reloadMenu, () -> {
			if (!factory.hasUnsavedChanges() || GuiUtil.getToggleableConfirmation(ConfirmationOptions.RELOAD))
				factory.reload();
		});
		//GuiUtil.setMenuEvent(openMenu, () -> this.openFXMLDialog("Open Factory", "OpenMenuDialog"));
		GuiUtil.setMenuEvent(openMenu, () -> {
			if (!factory.hasUnsavedChanges() || GuiUtil.getToggleableConfirmation(ConfirmationOptions.NEWOPEN)) {
				File f = this.openFactoryFile();
				if (f != null)
					Factory.loadFactory(f, this);
			}
		});
		GuiUtil.setMenuEvent(clearMenu, () -> GuiUtil.doWithToggleableConfirmation(ConfirmationOptions.CLEARCRAFT, () -> factory.clearRecipes()));
		GuiUtil.setMenuEvent(zeroMenu, () -> GuiUtil.doWithToggleableConfirmation(ConfirmationOptions.ZEROCRAFT, () -> {
			for (Recipe r : new ArrayList<Recipe>(factory.getRecipes())) {
				factory.setCount(r, Fraction.ZERO);
			}
		}));
		GuiUtil.setMenuEvent(clearProductMenu, () -> GuiUtil.doWithToggleableConfirmation(ConfirmationOptions.CLEARPROD, () -> factory.removeProducts(new ArrayList<Consumable>(factory.getDesiredProducts()))));
		GuiUtil.setMenuEvent(isolateMenu, () -> GuiUtil.doWithToggleableConfirmation(ConfirmationOptions.ISOLATE, () -> factory.removeExternalSupplies(new ArrayList<ResourceSupply>(factory.getSupplies()))));
		GuiUtil.setMenuEvent(cleanupMenu, () -> GuiUtil.doWithToggleableConfirmation(ConfirmationOptions.CLEANUP, () -> factory.cleanup()));
		GuiUtil.setMenuEvent(summaryMenu, () -> this.openChildWindow("Factory Summary", "SummaryView", g -> ((SummaryViewController)g.controller).setFactory(factory)));

		GuiUtil.setMenuEvent(aboutMenu, () -> this.openChildWindow("About This Application", "AboutPage"));
		GuiUtil.setMenuEvent(guideMenu, () -> {/*
			File f = File.createTempFile("sfcalc", "usage.pdf");
			FileUtils.copyInputStreamToFile(Main.class.getResourceAsStream("Resources/Docs/Usage.pdf"), f);
			GuiSystem.getHSVC().showDocument(f.toURI().toString());*/
			GuiSystem.getHSVC().showDocument("https://reikakalseki.github.io/projects/sfcalc.html#text-block-usage");
		});
		GuiUtil.setMenuEvent(bugMenu, () -> GuiSystem.getHSVC().showDocument("https://github.com/ReikaKalseki/Satisfactory-Planner/issues"));
		GuiUtil.setMenuEvent(appFolderMenu, () -> GuiSystem.getHSVC().showDocument(Main.getRelativeFile("").toPath().toString()));
		GuiUtil.setMenuEvent(sfFolderMenu, () -> GuiSystem.getHSVC().showDocument(Setting.GAMEDIR.getCurrentValue().toPath().toString()));
		Logging.instance.log("Menu hooks initialized");

		statisticsGrid.getRowConstraints().get(0).minHeightProperty().bind(buildingBar.minHeightProperty());
		statisticsGrid.getRowConstraints().get(1).minHeightProperty().bind(buildCostBar.minHeightProperty());
		statisticsGrid.getRowConstraints().get(2).minHeightProperty().bind(netConsumptionBar.minHeightProperty());
		statisticsGrid.getRowConstraints().get(3).minHeightProperty().bind(netProductBar.minHeightProperty());

		productGrid.minRowHeight = 64;
		inputGrid.minRowHeight = 100;

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

		super.init(services);
		this.rebuildTierSet(true);
		this.buildModMenu();
		Logging.instance.log("Initialization complete");
	}

	private File openFactoryFile() {
		File f = GuiUtil.openFileDialog(this.getWindow(), "Factory", Main.getRelativeFile("Factories"), new FileChooser.ExtensionFilter("Factory files (*.factory)", "*.factory"));
		return f != null && f.exists() ? f : null;
	}

	protected void rebuildTierSet(boolean resetValue) {
		this.setupTierBar();
		tierFilter.setMax(Milestone.getMaxTier());
		if (resetValue)
			tierFilter.setValue(tierFilter.getMax());
	}

	@Override
	public void setFactory(Factory f) {
		if (factory != null)
			factory.prepareDisposal();
		super.setFactory(f);
		lastNameSavedWith = factory == null ? null : factory.name;
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

		GuiUtil.initWidgets(inputGridContainer);
		GuiUtil.initWidgets(outputGridContainer);

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

	public void rebuildLists(boolean recipe, boolean products, boolean buildings) {
		if (products) {
			addProductButton.getSelectionModel().clearSelection();
			ArrayList<Consumable> li = new ArrayList(Database.getAllItems());
			li.removeIf(c -> !this.isItemValid(c));
			addProductButton.setItems(FXCollections.observableArrayList(li));
		}

		if (recipe) {
			recipeDropdown.getSelectionModel().clearSelection();
			ArrayList<Recipe> li = new ArrayList(Database.getAllAutoRecipes());
			if (factory != null)
				li.removeIf(r -> !this.isRecipeValid(r) || factory.getRecipes().contains(r));

			recipeDropdown.setItems(FXCollections.observableList(li));
			recipeDropdown.setDisable(li.isEmpty());
		}

		if (buildings) {
			simpleProducerButton.getSelectionModel().clearSelection();
			ArrayList<SimpleProductionBuilding> li = new ArrayList(Database.getAllBuildings().stream().filter(b -> b instanceof SimpleProductionBuilding).toList());
			simpleProducerButton.setItems(FXCollections.observableList(li));
			simpleProducerButton.setDisable(li.isEmpty());
		}

		this.rebuildTierSet(false);
		this.buildModMenu();
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

	private void buildModMenu() {
		modContentLibMenu.getItems().clear();
		for (String s : Database.getModList()) {
			File f = Database.getContentLibFolder(s);
			if (f != null) {
				String n = f.toPath().toString();
				MenuItem mi = new MenuItem(s);
				GuiUtil.setMenuEvent(mi, () -> GuiSystem.getHSVC().showDocument(n));
				modContentLibMenu.getItems().add(mi);
			}
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
		rebuilding = true;
		this.rebuildLists(true, true, true);

		for (GuiInstance<ItemOutputController> g : productButtons.values())
			productGrid.getChildren().remove(g.rootNode);

		recipeListView.getChildren().clear();

		recipeListEntries.clear();
		productButtons.clear();
		supplyEntries.clear();
		supplyEntryNodes.clear();

		factoryName.setText(factory.name);
		for (ToggleableVisiblityGroup tv : ToggleableVisiblityGroup.values()) {
			toggleFilters.get(tv).setSelected(factory.getToggle(tv));
		}

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

		this.updateStats();

		this.layout();
		rebuilding = false;
	}

	@Override
	public void updateStats(EnumSet<StatFlags> flags) {
		super.updateStats(flags);
		if (flags.contains(StatFlags.LOCALSUPPLY)) {
			localSupplyTotals.getChildren().clear();
			TreeMap<Consumable, Double> totalSupply = new TreeMap();
			for (ResourceSupply res : factory.getSupplies()) {
				Double has = totalSupply.get(res.getResource());
				double put = (has == null ? 0 : has.doubleValue())+res.getYield();
				totalSupply.put(res.getResource(), put);
			}
			for (Consumable c : totalSupply.keySet()) {
				GuiUtil.addIconCount(c, totalSupply.get(c), 5, false, localSupplyTotals);
			}
		}

		root.layout();
	}

	@Override
	public void onAddRecipe(Recipe r) {
		this.rebuildLists(true, false, false);
		this.updateStats(this.getAllExcept(StatFlags.LOCALSUPPLY));
		this.addRecipeToList(r);
	}

	@Override
	public void onAddRecipes(Collection<Recipe> c) {
		this.rebuildLists(true, false, false);
		for (Recipe r : c) {
			this.addRecipeToList(r);
		}
		this.updateStats(this.getAllExcept(StatFlags.LOCALSUPPLY));
	}

	private void addRecipeToList(Recipe r) {
		Node n = this.createActiveRecipeCell(r);
		recipeListEntries.put(r, n);
		GuiUtil.addSortedNode(recipeListView, n, recipeListSorter);
		this.updateRecipeListShading();
	}

	private void updateRecipeListShading() {
		int i = 0;
		for (Node n : recipeListView.getChildren()) {
			ObservableList<String> styles = n.getStyleClass();
			if (i%2 == 1) {
				if (!styles.contains("table-row-darken"))
					styles.add("table-row-darken");
			}
			else
				styles.removeIf(s -> s.equals("table-row-darken"));
			i++;
			n.applyCss();
		}
	}

	@Override
	public void onRemoveRecipe(Recipe r) {
		this.rebuildLists(true, false, false);
		recipeListView.getChildren().remove(recipeListEntries.get(r));
		recipeListEntries.remove(r);
		this.updateRecipeListShading();
		this.updateStats(this.getAllExcept(StatFlags.LOCALSUPPLY));
	}

	@Override
	public void onRemoveRecipes(Collection<Recipe> c) {
		this.rebuildLists(true, false, false);
		for (Recipe r : c) {
			recipeListView.getChildren().remove(recipeListEntries.get(r));
			recipeListEntries.remove(r);
		}
		this.updateRecipeListShading();
		this.updateStats(this.getAllExcept(StatFlags.LOCALSUPPLY));
	}

	@Override
	public void onSetCount(Recipe r, double count) {
		this.updateStats(this.getAllExcept(StatFlags.LOCALSUPPLY));
	}

	@Override
	public void onSetCount(Generator g, Fuel fuel, double old, double count) {
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
		EnumSet<StatFlags> set = this.getAllExcept(StatFlags.LOCALSUPPLY, StatFlags.TIER);
		if (count == 0 || old == 0)
			set.add(StatFlags.TIER);
		this.updateStats(set);
	}

	@Override
	public void onAddProduct(Consumable c) {
		ItemOutputController ic = new ItemOutputController(c, factory);
		GuiInstance<ItemOutputController> gui = new GuiInstance(ic.getRootNode(), ic);
		productButtons.put(c, gui);
		productGrid.addEntry(gui);
		this.updateStats(StatFlags.WARNINGS, StatFlags.PRODUCTION);
	}

	@Override
	public void onRemoveProduct(Consumable c) {
		productGrid.removeEntry(productButtons.get(c));
		this.updateStats(StatFlags.WARNINGS, StatFlags.PRODUCTION, StatFlags.SINK);
	}

	@Override
	public void onRemoveProducts(Collection<Consumable> c) {
		for (Consumable cc : c)
			productGrid.removeEntry(productButtons.get(cc));
		this.updateStats(StatFlags.WARNINGS, StatFlags.PRODUCTION, StatFlags.SINK);
	}

	@Override
	public void onAddSupply(ResourceSupply res) {
		this.addResourceEntry(res);
		this.rebuildLists(false, false, true);
		this.updateStats();
	}

	private void addResourceEntry(ResourceSupply res) {
		if (res instanceof WaterExtractor) {
			this.addResourceEntry("WaterEntry", res);
		}
		else if (res instanceof SolidResourceNode) {
			this.addResourceEntry("MinerEntry", res);
		}
		else if (res instanceof ExtractableResource) {
			this.addResourceEntry("ResourceMineEntry", res);
		}
		else if (res instanceof TieredLogisticSupply) {
			this.addResourceEntry("TieredLogisticSupplyEntry", res);
		}
		else if (res instanceof LogisticSupply) {
			this.addResourceEntry("LogisticSupplyEntry", res);
		}
		else if (res instanceof FromFactorySupply) {
			this.addResourceEntry("FactorySupplyEntry", res);
		}
		else if (res instanceof SimpleProductionSupply) {
			this.addResourceEntry("SimpleProductionEntry", res);
		}
	}

	@Override
	public void onAddSupplies(Collection<? extends ResourceSupply> c) {
		for (ResourceSupply res : c)
			this.addResourceEntry(res);
		this.updateStats();
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
		this.updateStats();
	}

	@Override
	public void onRemoveSupplies(Collection<? extends ResourceSupply> c) {
		for (ResourceSupply s : c)
			inputGrid.getChildren().remove(supplyEntries.get(s).rootNode);
		this.updateStats();
	}

	@Override
	public void onUpdateSupply(ResourceSupply s) {
		//do not need anything since update IO accomplishes
	}

	@Override
	public void onSetToggle(ToggleableVisiblityGroup tv, boolean active) {
		toggleFilters.get(tv).setSelected(active);
		this.rebuildLists(true, true, false);
	}

	@Override
	public void onUpdateIO() {
		this.updateStats();
	}

	@Override
	public void onToggleProductSink(Consumable c) {
		this.updateStats(StatFlags.SINK);
	}

	@Override
	public Future<Void> onLoaded() {
		this.onSetFile(factory.getFile());
		lastNameSavedWith = factory.name;
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

	@Override
	public void setMatrix(MatrixType mt, GridPane g) {
		TitledPane tp = null;
		switch(mt) {
			case IN:
				tp = inputGridContainer;
				break;
			case OUT:
				tp = outputGridContainer;
				break;
		}

		tp.setContent(g);
	}

	@Override
	public GridPane getMatrix(MatrixType mt) {
		TitledPane tp = null;
		switch(mt) {
			case IN:
				tp = inputGridContainer;
				break;
			case OUT:
				tp = outputGridContainer;
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

	private HBox createActiveRecipeCell(Recipe r) {
		Spinner<Double> counter = new Spinner();
		GuiUtil.setupCounter(counter, 0, 9999, factory.getCount(r), true, true);
		counter.setPrefHeight(32);
		counter.setMinHeight(Region.USE_PREF_SIZE);
		counter.setMaxHeight(Region.USE_PREF_SIZE);
		//counter.getValueFactory().setValue(owner.get);
		counter.valueProperty().addListener((val, old, nnew) -> {
			if (nnew != null) {
				factory.setCount(r, Fraction.getFraction(counter.getEditor().getText()));
			}
		});
		counter.setTooltip(new Tooltip(r.displayName));
		Label lb = new Label(r.displayName);
		lb.setMinWidth(400);
		HBox hb = new HBox();
		hb.setSpacing(6);
		hb.setAlignment(Pos.CENTER_LEFT);
		Button b = new Button();
		b.setGraphic(new ImageView(new Image(Main.class.getResourceAsStream("Resources/Graphics/Icons/button-cancel-icon.png"), 32, 32, true, true)));
		b.setPrefWidth(32);
		b.setPrefHeight(32);
		b.setMinHeight(Region.USE_PREF_SIZE);
		b.setMaxHeight(Region.USE_PREF_SIZE);
		b.setMinWidth(Region.USE_PREF_SIZE);
		b.setMaxWidth(Region.USE_PREF_SIZE);
		b.setOnAction(e -> {
			factory.removeRecipe(r);
		});
		hb.getChildren().add(b);
		b = new Button();
		b.setGraphic(new ImageView(new Image(Main.class.getResourceAsStream("Resources/Graphics/Icons/button-reset-icon.png"), 32, 32, true, true)));
		b.setPrefWidth(32);
		b.setPrefHeight(32);
		b.setMinHeight(Region.USE_PREF_SIZE);
		b.setMaxHeight(Region.USE_PREF_SIZE);
		b.setMinWidth(Region.USE_PREF_SIZE);
		b.setMaxWidth(Region.USE_PREF_SIZE);
		b.setOnAction(e -> {
			counter.getValueFactory().setValue(0D);
		});
		hb.getChildren().add(b);
		hb.getChildren().add(lb);
		hb.getChildren().add(RecipeListCell.buildIODisplay(r, false, 1));
		HBox ret = GuiUtil.createSpacedHBox(hb, counter, null);
		ret.setPrefHeight(40);
		ret.setMinHeight(Region.USE_PREF_SIZE);
		ret.setMaxHeight(Region.USE_PREF_SIZE);
		ret.setMaxWidth(Double.MAX_VALUE);
		ret.setMinWidth(Region.USE_COMPUTED_SIZE);
		ret.setPrefWidth(Region.USE_COMPUTED_SIZE);
		ret.setPadding(new Insets(4, 0, 0, 0));
		return ret;
	}
}
