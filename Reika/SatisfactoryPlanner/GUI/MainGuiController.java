package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;

import org.controlsfx.control.SearchableComboBox;

import com.google.common.base.Strings;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.ExtractableResource;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.FunctionalBuilding;
import Reika.SatisfactoryPlanner.Data.Item;
import Reika.SatisfactoryPlanner.Data.LogisticSupply;
import Reika.SatisfactoryPlanner.Data.Recipe;
import Reika.SatisfactoryPlanner.Data.ResourceSupply;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.FontModifier;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.GuiInstance;
import Reika.SatisfactoryPlanner.Util.ColorUtil;
import Reika.SatisfactoryPlanner.Util.CountMap;
import Reika.SatisfactoryPlanner.Util.FactoryListener;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class MainGuiController extends ControllerBase implements FactoryListener {

	private boolean hasLoaded;

	@FXML
	private Button addInputButton;

	@FXML
	private Button addProductButton;

	@FXML
	private HBox buildingBar;

	@FXML
	private MenuItem clearMenu;

	@FXML
	private MenuItem clearProductMenu;

	@FXML
	private Menu controlMenu;

	@FXML
	private HBox costBar;

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

	private Factory factory = new Factory();

	@Override
	public void init(HostServices services) throws IOException {
		factory.addCallback(this);

		recipeDropdown.setConverter(new StringConverter<Recipe>() {
			@Override
			public String toString(Recipe r) {
				return r == null ? "" : r.displayName;
			}

			@Override
			public Recipe fromString(String id) {
				return Strings.isNullOrEmpty(id) ? null : Database.lookupRecipe(id);
			}
		});
		recipeDropdown.getSelectionModel().selectedItemProperty().addListener((val, old, nnew) -> {
			if (nnew != null)
				Platform.runLater(() -> factory.addRecipe(nnew)); //need to delay since this updates the selection and contents, which cannot be done inside a selection change
		});
		recipeDropdown.setButtonCell(new RecipeListCell("Click To Add Recipe...", true));
		recipeDropdown.setCellFactory(c -> new RecipeListCell("", false));


		((ImageView)addInputButton.getGraphic()).setImage(new Image(Main.class.getResourceAsStream("Resources/Graphics/Icons/add.png")));
		GuiUtil.setButtonEvent(addInputButton, () -> this.openFXMLDialog("Add Resource Node", "ResourceNodeDialog"));

		((ImageView)addProductButton.getGraphic()).setImage(new Image(Main.class.getResourceAsStream("Resources/Graphics/Icons/add.png")));
		GuiUtil.setButtonEvent(addProductButton, () -> this.openFXMLDialog("Choose Item", "ItemChoiceDialog", ct -> ((ItemChoiceController)ct).setCallback(c -> factory.addProduct(c))));

		factoryName.textProperty().addListener((val, old, nnew) -> {
			factory.name = nnew;
		});

		GuiUtil.setMenuEvent(saveMenu, () -> factory.save());
		GuiUtil.setMenuEvent(reloadMenu, () -> factory.reload());
		GuiUtil.setMenuEvent(openMenu, () -> this.openFXMLDialog("Open Factory", "ResourceNodeDialog"));
		openMenu.setOnAction(e -> {

		});
	}

	private void loadFactory(Factory f) {

	}

	public Factory getFactory() {
		return factory;
	}

	@Override
	protected void postInit(WindowBase w) throws IOException {
		super.postInit(w);

		this.updateUI();
	}

	private void updateUI() {
		try {
			recipeDropdown.getSelectionModel().clearSelection();
			ArrayList<Recipe> li = new ArrayList(Database.getAllAutoRecipes());
			li.removeAll(factory.getRecipes());
			recipeDropdown.setItems(FXCollections.observableList(li));
			recipeDropdown.setDisable(li.isEmpty());

			GridPane gp = factory.createRawMatrix(this);
			gp.setMaxWidth(Double.POSITIVE_INFINITY);
			gp.setMaxHeight(Double.POSITIVE_INFINITY);
			gridContainer.setContent(gp);

			gp = factory.createNetMatrix(this);
			gp.setMaxWidth(Double.POSITIVE_INFINITY);
			gp.setMaxHeight(Double.POSITIVE_INFINITY);
			netGridContainer.setContent(gp);

			this.setFont(gridContainer, GuiSystem.getDefaultFont());
			this.setFont(netGridContainer, GuiSystem.getDefaultFont());

			productGrid.getChildren().removeIf(n -> n instanceof ProductButton);
			for (Consumable c : factory.getProducts())
				productGrid.getChildren().add(productGrid.getChildren().size()-1, new ProductButton(c));

			this.updateStats();
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
			});
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void updateWarnings() {
		boolean any = false;
		warningPanel.setDisable(!any);
		if (!any)
			warningPanel.setExpanded(false);
		warningPanel.setCollapsible(any);
		warningPanel.setVisible(any);
	}

	@Override
	public void onContentsChange() {
		this.updateUI();
	}

	private void updateStats() {
		inputGrid.getChildren().removeIf(n -> !(n instanceof Button));
		for (ResourceSupply res : factory.getSupplies()) {
			try {
				if (res instanceof ExtractableResource) {
					GuiInstance gui = this.loadNestedFXML("ResourceMineEntry", inputGrid);
					((ResourceMineEntryController)gui.controller).setSupply(factory, (ExtractableResource)res);
				}
				else if (res instanceof LogisticSupply) {
					GuiInstance gui = this.loadNestedFXML("ResourceSupplyEntry", inputGrid);
					((LogisticSupplyEntryController)gui.controller).setSupply(factory, (LogisticSupply)res);
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		costBar.getChildren().clear();
		buildingBar.getChildren().clear();

		CountMap<Item> cost = new CountMap();
		CountMap<FunctionalBuilding> c = factory.getBuildings();
		for (FunctionalBuilding b : c.keySet()) {

			int amt = c.get(b);
			GuiUtil.addIconCount(buildingBar, b, amt);

			for (Entry<Item, Integer> e : b.getConstructionCost().entrySet()) {
				cost.increment(e.getKey(), e.getValue()*amt);
			}
		}
		for (Item i : cost.keySet()) {
			GuiUtil.addIconCount(costBar, i, cost.get(i));
		}

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

	private class ProductButton extends Button {

		public final Consumable item;

		private ProductButton(Consumable c) {
			item = c;
			int size = 64;//32;
			this.setGraphic(new ImageView(c.createIcon(size)));
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
		}

	}

}

