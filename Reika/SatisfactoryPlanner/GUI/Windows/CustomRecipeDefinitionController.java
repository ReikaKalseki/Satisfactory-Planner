package Reika.SatisfactoryPlanner.GUI.Windows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.controlsfx.control.SearchableComboBox;

import com.google.common.base.Strings;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.Objects.Consumable;
import Reika.SatisfactoryPlanner.Data.Objects.Milestone;
import Reika.SatisfactoryPlanner.Data.Objects.Recipe;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.Building;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.CraftingBuilding;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.FunctionalBuilding;
import Reika.SatisfactoryPlanner.GUI.GuiSystem;
import Reika.SatisfactoryPlanner.GUI.GuiUtil;
import Reika.SatisfactoryPlanner.GUI.GuiUtil.SearchableSelector;
import Reika.SatisfactoryPlanner.GUI.Components.IngredientDefinitionRowController;
import Reika.SatisfactoryPlanner.GUI.Components.ListCells.BuildingListCell;
import Reika.SatisfactoryPlanner.GUI.Components.ListCells.DecoratedListCell;
import Reika.SatisfactoryPlanner.GUI.Components.ListCells.MilestoneListCell;
import Reika.SatisfactoryPlanner.Util.Logging;

import fxexpansions.FXMLControllerBase;
import fxexpansions.GuiInstance;
import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CustomRecipeDefinitionController extends FXMLControllerBase {

	@FXML
	private Button loadButton;

	@FXML
	private Button addButton;

	@FXML
	private Button clearButton;

	@FXML
	private TextField idField;

	@FXML
	private VBox ingredientList;

	@FXML
	private ComboBox<CraftingBuilding> machineDropdown;

	@FXML
	private SearchableComboBox<Milestone> milestoneDropdown;

	@FXML
	private VBox milestoneList;

	@FXML
	private SearchableComboBox<String> modDropdown;

	@FXML
	private TextField nameField;

	@FXML
	private VBox productList;

	@FXML
	private Spinner<Double> timeSpinner;

	private final ArrayList<GuiInstance<IngredientDefinitionRowController>> ingredients = new ArrayList();
	private final ArrayList<GuiInstance<IngredientDefinitionRowController>> products = new ArrayList();
	private final ArrayList<Milestone> milestones = new ArrayList();

	private File forcedFile;

	@Override
	public void init(HostServices services) throws IOException {
		GuiUtil.setButtonEvent(addButton, () -> {
			String id = idField.getText();
			if (Strings.isNullOrEmpty(id)) {
				GuiUtil.raiseUserErrorDialog("No ID specified", "Recipes must have a valid ID.");
				return;
			}
			String name = nameField.getText();
			if (Strings.isNullOrEmpty(name)) {
				GuiUtil.raiseUserErrorDialog("No display name specified", "Recipes must have a valid display name.");
				return;
			}
			CraftingBuilding bld = machineDropdown.getSelectionModel().getSelectedItem();
			if (bld == null) {
				GuiUtil.raiseUserErrorDialog("No machine specified", "Recipes must have a valid crafting machine.");
				return;
			}
			Recipe r = new Recipe(id, name, bld, timeSpinner.getValue().floatValue(), false);
			for (GuiInstance<IngredientDefinitionRowController> gui : ingredients) {
				Consumable c = gui.controller.getItem();
				int amt = gui.controller.getAmount();
				if (c != null && amt > 0) {
					r.addIngredient(c, amt);
				}
			}
			if (r.getIngredientsPerMinute().isEmpty()) {
				GuiUtil.raiseUserErrorDialog("No ingredients specified", "Recipes must have at least one ingredient.");
				return;
			}
			for (GuiInstance<IngredientDefinitionRowController> gui : products) {
				Consumable c = gui.controller.getItem();
				int amt = gui.controller.getAmount();
				if (c != null && amt > 0) {
					r.addProduct(c, amt);
				}
			}
			if (r.getProductsPerMinute().isEmpty()) {
				GuiUtil.raiseUserErrorDialog("No products specified", "Recipes must have at least one product.");
				return;
			}
			for (Milestone m : milestones) {
				r.addMilestone(m);
			}
			File f;
			if (forcedFile == null)
				f = Database.exportCustomRecipe(r, modDropdown.getSelectionModel().getSelectedItem(), this::verifyFileOverwrite);
			else
				f = Database.exportCustomRecipe(r, forcedFile, this::verifyFileOverwrite);
			if (f != null)
				GuiUtil.raiseDialog(AlertType.INFORMATION, "Recipe Exported", "Recipe saved to\n"+GuiUtil.splitToWidth(f.getCanonicalPath(), 400, "(?=[\\s\\\\]+)", GuiSystem.getDefaultFont()), ButtonType.OK);
			//this.close();
		});

		GuiUtil.setButtonEvent(loadButton, () -> {
			forcedFile = GuiUtil.openFileDialog(this.getWindow(), "Recipe", Main.getModsFolder());
			if (forcedFile != null) {
				Path p1 = Main.getModsFolder().toPath();
				Path p2 = forcedFile.toPath();
				String mod = p2.startsWith(p1) ? p1.relativize(p2).toString() : null;
				if (!Strings.isNullOrEmpty(mod))
					mod = mod.substring(0, mod.indexOf('\\'));
				String modInner = mod;
				try {
					AtomicReference<Recipe> ref = new AtomicReference();
					GuiUtil.queueTask("Loading recipe from "+forcedFile.getName(), id -> {ref.set(Database.parseCustomRecipeFile(forcedFile, modInner, false));}, id -> this.setData(ref.get()));
				}
				catch (Exception e) {
					GuiUtil.showException(e, "Recipe could not be loaded.");
				}
			}
		});

		GuiUtil.setButtonEvent(clearButton, () -> {
			this.setData(null);
		});

		machineDropdown.setButtonCell(new BuildingListCell("Choose Building...", true));
		machineDropdown.setCellFactory(c -> new BuildingListCell("", false));

		milestoneDropdown.setItems(FXCollections.observableArrayList(Database.getAllMilestones()));
		GuiUtil.setupAddSelector(milestoneDropdown, new SearchableSelector<Milestone>(){
			@Override
			public void accept(Milestone t) {
				CustomRecipeDefinitionController.this.addMilestone(t);
			}

			@Override
			public DecoratedListCell<Milestone> createListCell(String text, boolean button) {
				return new MilestoneListCell(text, button);
			}

			@Override
			public String getEntryTypeName() {
				return "Milestone";
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

		ObservableList<CraftingBuilding> li = FXCollections.observableArrayList();
		for (Building b : Database.getAllBuildings()) {
			if (b instanceof CraftingBuilding)
				li.add((CraftingBuilding)b);
		}
		Collections.sort(li);
		machineDropdown.setItems(li);
		machineDropdown.getSelectionModel().selectedItemProperty().addListener((val, old, nnew) -> {
			try {
				this.createIOSlots(nnew);
			}
			catch (IOException e) {
				Logging.instance.log(e);
				GuiUtil.showException(e);
			}
		});

		ObservableList<String> li2 = FXCollections.observableArrayList(Database.getModList());
		li2.add(0, "<None>");
		modDropdown.setItems(li2);
		modDropdown.getSelectionModel().select(0);

		timeSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 999, 1));
		GuiUtil.setupCounter(timeSpinner, 0.1, 999, 1, true);
		timeSpinner.valueProperty().addListener((val, old, nnew) -> {
			for (GuiInstance<IngredientDefinitionRowController> gui : ingredients) {
				gui.controller.setTimeCoefficient(nnew.floatValue());
			}
			for (GuiInstance<IngredientDefinitionRowController> gui : products) {
				gui.controller.setTimeCoefficient(nnew.floatValue());
			}
		});
	}

	private boolean verifyFileOverwrite(File f) {
		return GuiUtil.getConfirmation("Recipe file '"+f.getName()+"' already exists\n(modified "+Main.timeStampFormat.format(new Date(f.lastModified()))+").\n\nDo you want to overwrite it?");
	}

	private void setData(Recipe r) throws IOException {
		if (r == null)
			forcedFile = null;
		milestoneList.getChildren().removeIf(n -> !(n instanceof ComboBox));
		milestones.clear();
		idField.setText(r == null ? "" : r.id);
		nameField.setText(r == null ? "" : r.displayName);
		if (r == null || Strings.isNullOrEmpty(r.getMod()))
			modDropdown.getSelectionModel().clearSelection();
		else
			modDropdown.getSelectionModel().select(r.getMod());
		if (r == null)
			machineDropdown.getSelectionModel().select(null);
		else
			machineDropdown.getSelectionModel().select(r.productionBuilding);
		if (r == null) {
			this.createIOSlots(null);
		}
		else {
			for (Milestone t : r.getMilestones()) {
				this.addMilestone(t);
			}
			this.createIOSlots(r.productionBuilding);
			int i = 0;
			for (Entry<Consumable, Integer> e : r.getDirectCost().entrySet()) {
				ingredients.get(i).controller.setItem(e.getKey());
				ingredients.get(i).controller.setAmount(e.getValue());
				i++;
			}
			i = 0;
			for (Entry<Consumable, Integer> e : r.getDirectProducts().entrySet()) {
				products.get(i).controller.setItem(e.getKey());
				products.get(i).controller.setAmount(e.getValue());
				i++;
			}
		}
		timeSpinner.getValueFactory().setValue(r == null ? 1 : (double)r.craftingTime);
	}

	private void addMilestone(Milestone t) {
		milestoneList.getChildren().add(this.createMilestoneRow(t));
		milestones.add(t);
		this.getWindow().sizeToScene();
	}

	private Node createMilestoneRow(Milestone t) {
		HBox hb = new HBox();
		hb.setSpacing(12);
		hb.setAlignment(Pos.CENTER_LEFT);
		Button b = new Button();
		b.setGraphic(new ImageView(new Image(Main.class.getResourceAsStream("Resources/Graphics/Icons/delete.png"))));
		b.setPrefWidth(32);
		b.setPrefHeight(32);
		b.setMinHeight(Region.USE_PREF_SIZE);
		b.setMaxHeight(Region.USE_PREF_SIZE);
		b.setMinWidth(Region.USE_PREF_SIZE);
		b.setMaxWidth(Region.USE_PREF_SIZE);
		b.setOnAction(e -> {
			milestones.remove(t);
			milestoneList.getChildren().remove(hb);
			this.getWindow().sizeToScene();
		});
		hb.getChildren().add(b);
		hb.getChildren().add(new Label(t.displayName));
		if (milestones.size()%2 == 1) {
			hb.getStyleClass().add("table-row-darken");
		}
		return hb;
	}

	private void createIngredientDefinitionRow(ArrayList<GuiInstance<IngredientDefinitionRowController>> li, VBox container) throws IOException {
		GuiInstance<IngredientDefinitionRowController> gui = this.loadNestedFXML("IngredientDefinitionRow", (Consumer<GuiInstance>)null);
		li.add(gui);
		gui.rootNode.setVisible(false);
		gui.rootNode.setManaged(false);
		container.getChildren().add(gui.rootNode);
	}

	@Override
	protected void postInit(Stage w) throws IOException {
		super.postInit(w);

		for (int i = 0; i < 4; i++) {
			this.createIngredientDefinitionRow(ingredients, ingredientList);
			this.createIngredientDefinitionRow(products, productList);
		}

		GuiUtil.initWidgets(this);
	}

	private void createIOSlots(FunctionalBuilding b) throws IOException {
		if (b == null) {
			for (int i = 0; i < 4; i++) {
				GuiInstance<IngredientDefinitionRowController> guiIn = ingredients.get(i);
				GuiInstance<IngredientDefinitionRowController> guiOut = products.get(i);
				guiIn.rootNode.setVisible(false);
				guiIn.rootNode.setManaged(false);
				guiOut.rootNode.setVisible(false);
				guiOut.rootNode.setManaged(false);
			}
		}
		else {
			switch (b.id) { //TODO make automatic, defined in the building
				case "Build_OilRefinery_C":
					this.createIOSlots(1, 1, 1, 1);
					break;
				case "Build_FoundryMk1_C":
					this.createIOSlots(2, 1);
					break;
				case "Build_Packager_C":
					this.createIOSlots(1, 1, 1, 1);
					break;
				case "Build_ManufacturerMk1_C":
					this.createIOSlots(4, 1);
					break;
				case "Build_AssemblerMk1_C":
					this.createIOSlots(2, 1);
					break;
				case "Build_Blender_C":
					this.createIOSlots(2, 2, 1, 1);
					break;
				case "Build_SmelterMk1_C":
					this.createIOSlots(1, 1);
					break;
				case "Build_ConstructorMk1_C":
					this.createIOSlots(1, 1);
					break;
				case "Build_QuantumEncoder_C":
					this.createIOSlots(1, 0, 1, 1);
					break;
				case "Build_Converter_C":
					this.createIOSlots(2, 1);
					break;
				case "Build_HadronCollider_C":
					this.createIOSlots(2, 1);
					break;
			}
		}
		this.getWindow().sizeToScene();
	}

	private void createIOSlots(int itemsIn, int itemsOut) throws IOException {
		this.createIOSlots(itemsIn, 0, itemsOut, 0);
	}

	private void createIOSlots(int itemsIn, int fluidsIn, int itemsOut, int fluidsOut) throws IOException {
		int in = itemsIn+fluidsIn;
		int out = itemsOut+fluidsOut;
		for (int i = 0; i < 4; i++) {
			GuiInstance<IngredientDefinitionRowController> guiIn = ingredients.get(i);
			GuiInstance<IngredientDefinitionRowController> guiOut = products.get(i);
			if (i < in) {
				guiIn.rootNode.setVisible(true);
				guiIn.rootNode.setManaged(true);
				guiIn.controller.setName(in == 1 ? "Ingredient" : "Ingredient "+(i+1));
				guiIn.controller.setFluid(i >= itemsIn);
			}
			else {
				guiIn.rootNode.setVisible(false);
				guiIn.rootNode.setManaged(false);
			}

			if (i < out) {
				guiOut.rootNode.setVisible(true);
				guiOut.rootNode.setManaged(true);
				guiOut.controller.setName(out == 1 ? "Product" : "Product "+(i+1));
				guiOut.controller.setFluid(i >= itemsOut);
			}
			else {
				guiOut.rootNode.setVisible(false);
				guiOut.rootNode.setManaged(false);
			}
		}
	}

}

