package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.common.base.Strings;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Data.Building;
import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.ExtractableResource;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.Generator;
import Reika.SatisfactoryPlanner.Data.Item;
import Reika.SatisfactoryPlanner.Data.Recipe;
import Reika.SatisfactoryPlanner.Data.ResourceSupply;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.GuiInstance;
import Reika.SatisfactoryPlanner.Util.ColorUtil;
import Reika.SatisfactoryPlanner.Util.CountMap;
import Reika.SatisfactoryPlanner.Util.FactoryListener;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
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
	private Button addMinerButton;

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
	private Menu currentMenu;

	@FXML
	private Menu factoryMenu;

	@FXML
	private TitledPane generatorsPanel;

	@FXML
	private TitledPane gridContainer;

	@FXML
	private HBox inputBar;

	@FXML
	private TitledPane inputsPanel;

	@FXML
	private MenuItem isolateMenu;

	@FXML
	private MenuBar menu;

	@FXML
	private HBox minerBar;

	@FXML
	private TitledPane minersPanel;

	@FXML
	private TitledPane netGridContainer;

	@FXML
	private MenuItem newMenu;

	@FXML
	private MenuItem openMenu;

	@FXML
	private Label powerProduction;

	@FXML
	private TilePane productGrid;

	@FXML
	private TitledPane productsPanel;

	@FXML
	private MenuItem quitMenu;

	@FXML
	private Menu recentMenu;

	@FXML
	private ComboBox<Recipe> recipeDropdown;

	@FXML
	private VBox root;

	@FXML
	private MenuItem saveMenu;

	@FXML
	private MenuItem settingsMenu;

	@FXML
	private GridPane statisticsGrid;

	@FXML
	private TitledPane statsPanel;

	@FXML
	private TitledPane warningPanel;

	@FXML
	private MenuItem zeroMenu;

	private Factory factory = new Factory();

	private final HashMap<Consumable, Button> productButtons = new HashMap();

	@Override
	public void init(HostServices services) throws IOException {
		factory.addCallback(this);

		recipeDropdown.setConverter(new StringConverter<Recipe>() {
			@Override
			public String toString(Recipe r) {
				return r == null ? "" : r.name;
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


		((ImageView)addMinerButton.getGraphic()).setImage(new Image(Main.class.getResourceAsStream("Resources/Graphics/Icons/add.png")));
		addMinerButton.setOnAction(e -> {
			try {
				this.openFXMLDialog("Add Resource Node", "ResourceNodeDialog");
			}
			catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		});

		((ImageView)addInputButton.getGraphic()).setImage(new Image(Main.class.getResourceAsStream("Resources/Graphics/Icons/add.png")));
		addInputButton.setOnAction(e -> {

		});

		((ImageView)addProductButton.getGraphic()).setImage(new Image(Main.class.getResourceAsStream("Resources/Graphics/Icons/add.png")));
		addProductButton.setOnAction(e -> {

		});
	}

	public Factory getFactory() {
		return factory;
	}

	private Button createProductButton(Consumable c) {
		Button b = new Button();
		b.setGraphic(new ImageView(c.createIcon()));
		GuiUtil.setTooltip(b, c.name);
		b.setPrefWidth(32);
		b.setPrefHeight(32);
		b.setMinHeight(Region.USE_PREF_SIZE);
		b.setMaxHeight(Region.USE_PREF_SIZE);
		b.setMinWidth(Region.USE_PREF_SIZE);
		b.setMaxWidth(Region.USE_PREF_SIZE);
		b.setOnAction(e -> {
			factory.removeProduct(c);
		});
		productButtons.put(c, b);
		return b;
	}

	@Override
	protected void postInit(WindowBase w) throws IOException {
		super.postInit(w);

		this.updateRecipes();
	}

	private void updateRecipes() {
		try {
			recipeDropdown.getSelectionModel().clearSelection();
			ArrayList<Recipe> li = new ArrayList(Database.getAllRecipes());
			li.removeAll(factory.getRecipes());
			recipeDropdown.setItems(FXCollections.observableList(li));
			recipeDropdown.setDisable(li.isEmpty());

			gridContainer.setContent(factory.createRawMatrix(this));
			netGridContainer.setContent(factory.createNetMatrix(this));

			this.setFont(gridContainer, GuiSystem.getDefaultFont());
			this.setFont(netGridContainer, GuiSystem.getDefaultFont());

			this.updateStats();
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
	public void onAddRecipe(Recipe r) {
		this.updateRecipes();
	}

	@Override
	public void onRemoveRecipe(Recipe r) {
		this.updateRecipes();
	}

	@Override
	public void onAddProduct(Consumable c) {
		productGrid.getChildren().add(productGrid.getChildren().size()-1, this.createProductButton(c));
		this.updateRecipes();
	}

	@Override
	public void onRemoveProduct(Consumable c) {
		productGrid.getChildren().remove(productButtons.get(c));
		this.updateRecipes();
	}

	@Override
	public void onSetCount(Recipe r, int amt) {
		this.updateStats();
	}

	@Override
	public void onSetCount(Generator g, int amt) {
		this.updateStats();
	}

	@Override
	public void onAddSupply(ResourceSupply res) {
		this.updateRecipes();
	}

	@Override
	public void onRemoveSupply(ResourceSupply res) {
		this.updateRecipes();
	}

	private void updateStats() {
		minerBar.getChildren().removeIf(n -> !(n instanceof Button));
		inputBar.getChildren().removeIf(n -> !(n instanceof Button));
		for (ExtractableResource res : factory.getMines()) {
			try {
				GuiInstance gui = this.loadNestedFXML("ResourceMineEntry", minerBar);
				((ResourceMineEntryController)gui.controller).setMine(factory, res);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		costBar.getChildren().clear();
		buildingBar.getChildren().clear();

		CountMap<Building> c = factory.getBuildings();
		for (Building b : c.keySet()) {

			int amt = c.get(b);
			GuiUtil.addIconCount(buildingBar, b, amt);

			for (Entry<Item, Integer> e : b.getConstructionCost().entrySet()) {
				GuiUtil.addIconCount(costBar, e.getKey(), e.getValue()*amt);
			}
		}

		int prod = factory.getNetPowerProduction();
		powerProduction.setText(prod+" MW");
		if (prod > 0) {
			powerProduction.setStyle("-fx-font-weight: bold; -fx-text-fill: "+ColorUtil.getCSSHex(UIConstants.OKAY_COLOR)+";");
		}
		else if (prod < 0) {
			powerProduction.setStyle("-fx-font-weight: bold; -fx-text-fill: "+ColorUtil.getCSSHex(UIConstants.WARN_COLOR)+";");
		}
		else {
			powerProduction.setStyle("");
		}
	}

}

