package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;

import org.controlsfx.control.SearchableComboBox;

import Reika.SatisfactoryPlanner.Data.Building;
import Reika.SatisfactoryPlanner.Data.CraftingBuilding;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.FunctionalBuilding;
import Reika.SatisfactoryPlanner.Data.Milestone;
import Reika.SatisfactoryPlanner.GUI.GuiUtil.SearchableSelector;
import Reika.SatisfactoryPlanner.Util.Logging;

import fxexpansions.FXMLControllerBase;
import fxexpansions.GuiInstance;
import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CustomRecipeDefinitionController extends FXMLControllerBase {

	@FXML
	private Button addButton;

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
	private TextField modField;

	@FXML
	private TextField nameField;

	@FXML
	private VBox productList;

	@FXML
	private Spinner<Double> timeSpinner;

	private final ArrayList<GuiInstance<IngredientDefinitionRowController>> ingredients = new ArrayList();
	private final ArrayList<GuiInstance<IngredientDefinitionRowController>> products = new ArrayList();

	@Override
	public void init(HostServices services) throws IOException {
		GuiUtil.setButtonEvent(addButton, () -> {

		});

		machineDropdown.setButtonCell(new BuildingListCell("Choose Building...", true));
		machineDropdown.setCellFactory(c -> new BuildingListCell("", false));

		milestoneDropdown.setItems(FXCollections.observableArrayList(Database.getAllMilestones()));
		GuiUtil.setupAddSelector(milestoneDropdown, new SearchableSelector<Milestone>(){
			@Override
			public void accept(Milestone t) {
				milestoneList.getChildren().add(new Label(t.displayName));
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

	private void createIOSlots(FunctionalBuilding b) throws IOException {
		ingredientList.getChildren().clear();
		productList.getChildren().clear();
		ingredients.clear();
		products.clear();

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
				this.createIOSlots(2, 2, 2, 2);
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

		this.getWindow().sizeToScene();
	}

	private void createIOSlots(int itemsIn, int itemsOut) throws IOException {
		this.createIOSlots(itemsIn, 0, itemsOut, 0);
	}

	private void createIOSlots(int itemsIn, int fluidsIn, int itemsOut, int fluidsOut) throws IOException {
		int in = itemsIn+fluidsIn;
		for (int i = 0; i < in; i++) {
			GuiInstance<IngredientDefinitionRowController> gui = this.createIORow(in == 1 ? "Ingredient" : "Ingredient "+(i+1), i >= itemsIn);
			ingredients.add(gui);
			ingredientList.getChildren().add(gui.rootNode);
		}
		int out = itemsOut+fluidsOut;
		for (int i = 0; i < out; i++) {
			GuiInstance<IngredientDefinitionRowController> gui = this.createIORow(out == 1 ? "Product" : "Product "+(i+1), i >= itemsOut);
			products.add(gui);
			productList.getChildren().add(gui.rootNode);
		}
	}

	private GuiInstance<IngredientDefinitionRowController> createIORow(String name, boolean fluid) throws IOException {
		GuiInstance<IngredientDefinitionRowController> gui = this.loadNestedFXML("IngredientDefinitionRow", (Consumer<GuiInstance>)null);
		gui.controller.setName(name);
		gui.controller.setFluid(fluid);
		return gui;
	}

	@Override
	protected void postInit(Stage w) throws IOException {
		super.postInit(w);

		GuiUtil.initWidgets(this);
	}

}

