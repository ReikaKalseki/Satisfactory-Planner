package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.base.Strings;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.Recipe;
import Reika.SatisfactoryPlanner.Util.FactoryListener;
import Reika.SatisfactoryPlanner.Util.GuiUtil;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.StringConverter;

public class MainGuiController extends ControllerBase implements FactoryListener {

	private boolean hasLoaded;

	@FXML
	private Button addProductButton;

	@FXML
	private MenuItem clearMenu;

	@FXML
	private Menu controlMenu;

	@FXML
	private Menu currentMenu;

	@FXML
	private Menu factoryMenu;

	@FXML
	private TitledPane gridContainer;

	@FXML
	private TitledPane inputsPanel;

	@FXML
	private TitledPane inputsPanel1;

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
	private TitledPane productsPanel;

	@FXML
	private TilePane productGrid;

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
		recipeDropdown.setButtonCell(new RecipeListCell(true));
		recipeDropdown.setCellFactory(c -> new RecipeListCell(false));

		((ImageView)addProductButton.getGraphic()).setImage(new Image(Main.class.getResourceAsStream("Resources/Graphics/Icons/add.png")));
		addProductButton.setOnAction(e -> {

		});
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
		}
		catch (IOException e) {
			e.printStackTrace();
		}
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
		productGrid.getChildren().add(this.createProductButton(c));
	}

	@Override
	public void onRemoveProduct(Consumable c) {
		productGrid.getChildren().remove(productButtons.get(c));
	}

	private static class RecipeListCell extends ListCell<Recipe> {

		private static final String PROMPT = "Click To Add Recipe...";

		public RecipeListCell(boolean isButton) {
			super();

			//this.setGraphicTextGap(24);
			this.setContentDisplay(ContentDisplay.RIGHT);
			this.setMinHeight(Region.USE_PREF_SIZE);
			if (!isButton) {
				this.setPadding(new Insets(2, 12, 2, 6));
			}
			Insets in = this.getInsets();
			this.setPrefHeight(32+in.getTop()+in.getBottom());
		}

		@Override
		protected void updateItem(Recipe r, boolean empty) {
			super.updateItem(r, empty);
			if (empty || r == null) {
				this.setText(PROMPT);
				this.setGraphic(null);
			}
			else {
				//this.setText(r.name);
				this.setText("");
				this.setGraphic(this.createRecipePreview(r));
			}
		}

		private Node createRecipePreview(Recipe r) {/*
			HBox hb = new HBox();
			hb.setAlignment(Pos.CENTER_RIGHT);
			GuiUtil.addSpacer(hb);
			hb.setSpacing(8);
			for (Consumable c : r.getCost().keySet()) {
				ImageView img = new ImageView(c.createIcon());
				hb.getChildren().add(img);
			}
			hb.getChildren().add(new ImageView(new Image(Main.class.getResourceAsStream("Resources/Graphics/Icons/arrow-right-small.png"))));
			for (Consumable c : r.getProducts().keySet())
				hb.getChildren().add(new ImageView(c.createIcon()));
			graphicTextGapProperty().bind(widthProperty().subtract(this.labelPaddingProperty().);*/

			/*
			for (Consumable c : r.getCost().keySet())
				hb1.getChildren().add(new ImageView(c.createIcon()));
			for (Consumable c : r.getProducts().keySet())
				hb2.getChildren().add(new ImageView(c.createIcon()));
			HBox hb0 = GuiUtil.createSpacedHBox(hb1, new ImageView(new Image(Main.class.getResourceAsStream("Resources/Graphics/Icons/arrow-right-small.png"))), hb2);
			hb0.setMaxWidth(Double.POSITIVE_INFINITY);
			HBox.setHgrow(hb0, Priority.ALWAYS);
			return GuiUtil.createSpacedHBox(new Label(r.name), hb0, null);*/

			HBox ingredients = new HBox();
			HBox products = new HBox();
			ingredients.setSpacing(8);
			products.setSpacing(8);
			ingredients.setMinWidth(Region.USE_PREF_SIZE);
			ingredients.setMaxWidth(Region.USE_PREF_SIZE);
			products.setMinWidth(Region.USE_PREF_SIZE);
			products.setMaxWidth(Region.USE_PREF_SIZE);
			ingredients.prefWidthProperty().bind(ingredients.spacingProperty().multiply(Recipe.getMaxIngredients()-1).add(Recipe.getMaxIngredients()*32));
			products.prefWidthProperty().bind(products.spacingProperty().multiply(Recipe.getMaxProducts()-1).add(Recipe.getMaxProducts()*32));

			for (Consumable c : r.getCost().keySet())
				ingredients.getChildren().add(new ImageView(c.createIcon()));
			for (Consumable c : r.getProducts().keySet())
				products.getChildren().add(new ImageView(c.createIcon()));
			HBox itemBar = new HBox();
			itemBar.getChildren().add(ingredients);
			ImageView img = new ImageView(new Image(Main.class.getResourceAsStream("Resources/Graphics/Icons/arrow-right-small.png")));
			itemBar.getChildren().add(img);
			HBox.setMargin(img, new Insets(0, 4, 0, 4));
			itemBar.getChildren().add(products);
			HBox buildingBar = new HBox();
			buildingBar.setAlignment(Pos.CENTER);
			buildingBar.setSpacing(12);
			buildingBar.getChildren().add(new ImageView(r.productionBuilding.createIcon()));
			buildingBar.getChildren().add(new Label(r.productionBuilding.name));
			buildingBar.setPrefWidth(96);
			buildingBar.setMinWidth(Region.USE_PREF_SIZE);
			buildingBar.setMaxWidth(Region.USE_PREF_SIZE);
			HBox graphicBar = new HBox();
			graphicBar.getChildren().add(itemBar);
			Rectangle rect = new Rectangle();
			rect.setFill(UIConstants.FADE_COLOR);
			rect.setWidth(4);
			rect.setHeight(32);
			graphicBar.getChildren().add(rect);
			graphicBar.getChildren().add(buildingBar);
			graphicBar.setSpacing(24);
			Label lb = new Label(r.name);
			lb.setStyle("-fx-font-weight: bold;");
			return GuiUtil.createSpacedHBox(lb, graphicBar, null);

		}

	}

}

