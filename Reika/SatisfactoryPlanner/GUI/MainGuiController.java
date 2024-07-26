package Reika.SatisfactoryPlanner.GUI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map.Entry;

import org.controlsfx.control.SearchableComboBox;

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
import Reika.SatisfactoryPlanner.Data.Recipe;
import Reika.SatisfactoryPlanner.Data.ResourceSupply;
import Reika.SatisfactoryPlanner.Data.Warning;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.FontModifier;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.GuiInstance;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.SettingsWindow;
import Reika.SatisfactoryPlanner.GUI.GuiUtil.SearchableSelector;
import Reika.SatisfactoryPlanner.Util.ColorUtil;
import Reika.SatisfactoryPlanner.Util.CountMap;
import Reika.SatisfactoryPlanner.Util.FactoryListener;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class MainGuiController extends FXMLControllerBase implements FactoryListener {

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
	private TilePane localSupplyTotals;

	@FXML
	private TilePane buildingBar;

	@FXML
	private TilePane netProductBar;

	@FXML
	private MenuItem clearMenu;

	@FXML
	private MenuItem clearProductMenu;

	@FXML
	private Menu controlMenu;

	@FXML
	private TilePane buildCostBar;

	@FXML
	private TilePane netConsumptionBar;

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

	//@FXML
	//private Button refreshButton;

	private Factory factory;

	private final EnumMap<ToggleableVisiblityGroup, CheckBox> toggleFilters = new EnumMap(ToggleableVisiblityGroup.class);
	private final HashMap<Generator, GuiInstance<GeneratorRowController>> generators = new HashMap();
	private final HashMap<Consumable, ProductButton> productButtons = new HashMap();
	private final HashMap<ResourceSupply, GuiInstance<? extends ResourceSupplyEntryController>> supplyEntries = new HashMap();

	@Override
	public void init(HostServices services) throws IOException {
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

		GuiUtil.setButtonEvent(addMineButton, () -> this.openFXMLDialog("Add Resource Node", "ResourceNodeDialog"));
		GuiUtil.setButtonEvent(addInputButton, () -> this.openFXMLDialog("Add Logistic Supply", "LogisticSupplyDialog"));

		for (ToggleableVisiblityGroup tv : ToggleableVisiblityGroup.values()) {
			CheckBox cb = new CheckBox(tv.name());
			cb.setSelected(true);
			cb.selectedProperty().addListener((val, old, nnew) -> {
				if (old != nnew)
					factory.setToggle(tv, nnew);
			});
			toggleFilters.put(tv, cb);
			toggleFilterBox.getChildren().add(cb);
		}

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

		factoryName.textProperty().addListener((val, old, nnew) -> {
			factory.name = nnew;
		});
		/*
		GuiUtil.setButtonEvent(refreshButton, () -> {
			Logging.instance.log("Refresh @ "+System.currentTimeMillis());
			factory.refreshMatrices();
		});
		 */
		GuiUtil.setMenuEvent(settingsMenu, () -> this.openChildWindow(new SettingsWindow()));
		GuiUtil.setMenuEvent(quitMenu, () -> this.close());
		GuiUtil.setMenuEvent(newMenu, () -> this.setFactory(new Factory()));
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
	}

	public Factory getFactory() {
		return factory;
	}

	public void setFactory(Factory f) {
		this.setFactory(f, true);
	}

	private void setFactory(Factory f, boolean update) {
		factory = f;
		factory.setUI(this);

		for (GuiInstance<GeneratorRowController> gui : generators.values()) {
			gui.controller.setFactory(f);
		}

		Node gp = factory.createRawMatrix();
		gridContainer.setContent(gp);

		gp = factory.createNetMatrix();
		netGridContainer.setContent(gp);

		if (update)
			this.rebuildEntireUI();
	}

	@Override
	protected void postInit(WindowBase w) throws IOException {
		super.postInit(w);

		for (Generator g : Database.getAllGenerators()) {
			GuiInstance<GeneratorRowController> gui = this.loadNestedFXML("GeneratorRow", generatorList);
			gui.controller.setGenerator(g);
			generators.put(g, gui);
		}

		this.setFont(gridContainer, GuiSystem.getDefaultFont());
		this.setFont(netGridContainer, GuiSystem.getDefaultFont());

		this.buildRecentList();

		this.setFactory(new Factory(), false);
		factory.addCallback(this);

		this.rebuildEntireUI();
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
			li2.removeIf(r -> !this.isRecipeValid(r) || factory.getRecipes().contains(r));

			recipeDropdown.setItems(FXCollections.observableList(li2));
			recipeDropdown.setDisable(li2.isEmpty());
		}
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
		for (ToggleableVisiblityGroup tv : ToggleableVisiblityGroup.values()) {
			if (tv.isRecipeInGroup.test(r) && !factory.getToggle(tv))
				return false;
		}
		return true;
	}

	private void rebuildEntireUI() {
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

	private void updateWarnings() {
		ArrayList<Warning> li = new ArrayList();
		factory.getWarnings(w -> li.add(w));
		boolean any = !li.isEmpty();
		Collections.sort(li);
		warningList.getChildren().clear();
		for (Warning w : li) {
			warningList.getChildren().add(w.createUI());
		}
		warningPanel.setDisable(!any);
		warningPanel.setExpanded(any);
		warningPanel.setCollapsible(any);
		warningPanel.setVisible(any);
		warningPanel.layout();
	}

	public void updateStats(boolean all) {
		this.updateStats(all, all, all, all, all, all);
	}

	public void updateStats(boolean warnings, boolean buildings, boolean production, boolean consuming, boolean local, boolean power) {
		if (warnings)
			this.updateWarnings();

		if (buildings) {
			buildCostBar.getChildren().clear();
			buildingBar.getChildren().clear();

			CountMap<Item> cost = new CountMap();
			CountMap<FunctionalBuilding> bc = factory.getBuildings();
			for (FunctionalBuilding b : bc.keySet()) {

				int amt = bc.get(b);
				GuiUtil.addIconCount(buildingBar, b, amt);

				for (Entry<Item, Integer> e : b.getConstructionCost().entrySet()) {
					cost.increment(e.getKey(), e.getValue()*amt);
				}
			}
			for (Item i : cost.keySet()) {
				GuiUtil.addIconCount(buildCostBar, i, cost.get(i));
			}
		}

		if (power) {
			float prod = factory.getNetPowerProduction();
			powerProduction.setText(String.format("%.2fMW", prod));
			if (prod > 0) {
				powerProduction.setStyle(GuiSystem.getFontStyle(FontModifier.BOLD)+" -fx-text-fill: "+ColorUtil.getCSSHex(UIConstants.OKAY_COLOR)+";");
			}
			else if (prod < 0) {
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
				float amt = factory.getTotalConsumption(c)-factory.getTotalProduction(c);
				if (amt > 0)
					GuiUtil.addIconCount(netConsumptionBar, c, amt);
			}
		}
		if (production) {
			netProductBar.getChildren().clear();
			for (Consumable c : factory.getAllProducedItems()) {
				float amt = factory.getTotalProduction(c)-factory.getTotalConsumption(c);
				if (amt > 0)
					GuiUtil.addIconCount(netProductBar, c, amt);
			}
		}

		if (local) {
			localSupplyTotals.getChildren().clear();
			CountMap<Consumable> totalSupply = new CountMap();
			for (ResourceSupply res : factory.getSupplies()) {
				totalSupply.increment(res.getResource(), res.getYield());
			}
			for (Consumable c : totalSupply.keySet()) {
				GuiUtil.addIconCount(localSupplyTotals, c, totalSupply.get(c));
			}
		}
	}

	@Override
	public void onAddRecipe(Recipe r) {
		this.rebuildLists(true, false);
		this.updateStats(false);
	}

	@Override
	public void onRemoveRecipe(Recipe r) {
		this.rebuildLists(true, false);
		this.updateStats(false);
	}

	@Override
	public void onSetCount(Recipe r, float count) {
		this.updateStats(true, true, true, true, false, true);
	}

	@Override
	public void onSetCount(Generator g, Fuel fuel, int old, int count) {
		generators.get(g).controller.setCount(fuel, count);
		this.updateStats(true, true, true, true, false, true);
	}

	@Override
	public void onAddProduct(Consumable c) {
		productGrid.getChildren().add(new ProductButton(c));
		this.updateStats(true, false, false, false, false, false);
	}

	@Override
	public void onRemoveProduct(Consumable c) {
		productGrid.getChildren().remove(productButtons.get(c));
		this.updateStats(true, false, false, false, false, false);
	}

	@Override
	public void onAddSupply(ResourceSupply res) {
		try {
			if (res instanceof ExtractableResource) {
				GuiInstance<ResourceMineEntryController> gui = this.loadNestedFXML("ResourceMineEntry", inputGrid);
				gui.controller.setSupply(factory, (ExtractableResource)res);
				supplyEntries.put(res, gui);
			}
			else if (res instanceof LogisticSupply) {
				GuiInstance<LogisticSupplyEntryController> gui = this.loadNestedFXML("LogisticSupplyEntry", inputGrid);
				gui.controller.setSupply(factory, (LogisticSupply)res);
				supplyEntries.put(res, gui);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		this.updateStats(true, false, true, false, true, true);
	}

	@Override
	public void onRemoveSupply(ResourceSupply s) {
		inputGrid.getChildren().remove(supplyEntries.get(s).rootNode);
		this.updateStats(true, false, true, false, true, true);
	}

	@Override
	public void onSetToggle(ToggleableVisiblityGroup tv, boolean active) {
		toggleFilters.get(tv).setSelected(active);
	}

	@Override
	public void onLoaded() {
		this.rebuildEntireUI();
	}

	@Override
	public void onCleared() {
		this.rebuildEntireUI();
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
}
