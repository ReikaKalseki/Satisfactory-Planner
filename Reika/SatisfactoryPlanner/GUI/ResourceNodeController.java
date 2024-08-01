package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;

import org.controlsfx.control.SearchableComboBox;

import Reika.SatisfactoryPlanner.Data.Constants.MinerTier;
import Reika.SatisfactoryPlanner.Data.Constants.Purity;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.ExtractableResource;
import Reika.SatisfactoryPlanner.Data.Fluid;
import Reika.SatisfactoryPlanner.Data.FrackingCluster;
import Reika.SatisfactoryPlanner.Data.Item;
import Reika.SatisfactoryPlanner.Data.OilNode;
import Reika.SatisfactoryPlanner.Data.SolidResourceNode;
import Reika.SatisfactoryPlanner.Data.WaterExtractor;
import Reika.SatisfactoryPlanner.GUI.GuiUtil.SearchableSelector;

import fxexpansions.GuiInstance;
import fxexpansions.WindowBase;
import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Toggle;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class ResourceNodeController extends RadioTitledPaneSection {

	@FXML
	private Button addButton;

	@FXML
	private GridPane extraGrid;

	@FXML
	private SearchableComboBox<Fluid> frackingDropdown;

	@FXML
	private Spinner<Integer> frackingImpure;

	@FXML
	private Spinner<Integer> frackingNormal;

	@FXML
	private Spinner<Integer> frackingPure;

	@FXML
	private ComboBox<Purity> purity;

	@FXML
	private ComboBox<MinerTier> solidMinerTier;

	@FXML
	private HBox yieldDisplay;

	@FXML
	private SearchableComboBox<Item> solidDropdown;

	@FXML
	private RadioButton solidRadio;

	@FXML
	private RadioButton waterRadio;

	@FXML
	private RadioButton oilRadio;

	@FXML
	private RadioButton frackingRadio;

	private int clockSpeed = 100;

	@Override
	public void init(HostServices services) throws IOException {
		solidDropdown.setItems(FXCollections.observableArrayList(Database.getMineables()));
		frackingDropdown.setItems(FXCollections.observableArrayList(Database.getFrackables()));
		purity.setItems(FXCollections.observableArrayList(Purity.values()));
		solidMinerTier.setItems(FXCollections.observableArrayList(MinerTier.values()));

		GuiUtil.setupAddSelector(solidDropdown, new SearchableSelector<Item>(){
			@Override
			public void accept(Item t) {

			}

			@Override
			public DecoratedListCell<Item> createListCell(String text, boolean button) {
				return new ItemListCell<Item>(text, button);
			}

			@Override
			public String getEntryTypeName() {
				return "Item";
			}

			@Override
			public String getActionName() {
				return "Choose";
			}

			@Override
			public boolean clearOnSelect() {
				return false;
			}
		});
		GuiUtil.setupAddSelector(frackingDropdown, new SearchableSelector<Fluid>(){
			@Override
			public void accept(Fluid t) {

			}

			@Override
			public DecoratedListCell<Fluid> createListCell(String text, boolean button) {
				return new ItemListCell<Fluid>(text, button);
			}

			@Override
			public String getEntryTypeName() {
				return "Fluid";
			}

			@Override
			public String getActionName() {
				return "Choose";
			}

			@Override
			public boolean clearOnSelect() {
				return false;
			}
		});

		solidMinerTier.setButtonCell(new TierListCell<MinerTier>("Choose Miner...", true));
		solidMinerTier.setCellFactory(c -> new TierListCell<MinerTier>("", false));
		purity.setButtonCell(new PurityListCell("Choose Purity...", true));
		purity.setCellFactory(c -> new PurityListCell("", false));

		purity.getSelectionModel().selectedItemProperty().addListener((val, old, nnew) -> {
			this.updateStats();
		});
		solidDropdown.getSelectionModel().selectedItemProperty().addListener((val, old, nnew) -> {
			this.updateStats();
		});
		solidMinerTier.getSelectionModel().selectedItemProperty().addListener((val, old, nnew) -> {
			this.updateStats();
		});
		frackingDropdown.getSelectionModel().selectedItemProperty().addListener((val, old, nnew) -> {
			this.updateStats();
		});

		this.setupFrackingSpinner(frackingImpure, Purity.IMPURE);
		this.setupFrackingSpinner(frackingNormal, Purity.NORMAL);
		this.setupFrackingSpinner(frackingPure, Purity.PURE);

		addButton.setOnAction(e -> {
			Toggle b = radioButtons.getSelectedToggle();
			if (!purity.isDisabled() && purity.getSelectionModel().getSelectedItem() == null) {
				GuiUtil.raiseUserErrorDialog("Resource Supply Invalid", "No purity selected.");
				return;
			}
			if (b == solidRadio && solidDropdown.getSelectionModel().getSelectedItem() == null) {
				GuiUtil.raiseUserErrorDialog("Resource Supply Invalid", "No item selected.");
				return;
			}
			if (b == solidRadio && solidMinerTier.getSelectionModel().getSelectedItem() == null) {
				GuiUtil.raiseUserErrorDialog("Resource Supply Invalid", "No miner selected.");
				return;
			}
			if (b == frackingRadio && frackingDropdown.getSelectionModel().getSelectedItem() == null) {
				GuiUtil.raiseUserErrorDialog("Resource Supply Invalid", "No fluid selected.");
				return;
			}
			ExtractableResource res = this.createResource();
			res.setClockSpeed(clockSpeed/100F);
			((MainGuiController)owner).getFactory().addExternalSupply(res);
			this.close();
		});
	}

	@Override
	protected void onToggleSelected(RadioButton rb) {
		super.onToggleSelected(rb);
		purity.setDisable(rb == waterRadio || rb == frackingRadio);
		this.updateStats();
	}

	private void updateStats() {
		ExtractableResource res = this.createResource();
		yieldDisplay.getChildren().clear();
		if (res != null && res.getResource() != null && res.getYield() > 0) {
			res.setClockSpeed(clockSpeed/100F);
			GuiUtil.addIconCount(res.getResource(), res.getYield(), yieldDisplay);
		}
	}

	private void setupFrackingSpinner(Spinner<Integer> counter, Purity p) {
		counter.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 99, 0));
		GuiUtil.setupCounter(counter, 0, 99, 0, false);
		counter.valueProperty().addListener((val, old, nnew) -> {
			this.updateStats();
		});
	}

	private ExtractableResource createResource() {
		return switch (radioButtons.getSelectedToggle()) {
			case Toggle t when t == solidRadio -> new SolidResourceNode(solidDropdown.getSelectionModel().getSelectedItem(), purity.getSelectionModel().getSelectedItem(), solidMinerTier.getSelectionModel().getSelectedItem());
			case Toggle t when t == waterRadio -> new WaterExtractor();
			case Toggle t when t == oilRadio -> new OilNode(purity.getSelectionModel().getSelectedItem());
			case Toggle t when t == frackingRadio -> new FrackingCluster(frackingDropdown.getSelectionModel().getSelectedItem(), frackingPure.getValue(), frackingNormal.getValue(), frackingImpure.getValue());
			default -> null;
		};
	}

	@Override
	protected void postInit(WindowBase w) throws IOException {
		super.postInit(w);

		GuiInstance<ClockspeedSliderController> gui = this.loadNestedFXML("ClockspeedSlider", extraGrid, 1, 1);
		gui.controller.setCallback(v -> {clockSpeed = v; this.updateStats();});
		this.setFont(this.getRootNode(), GuiSystem.getDefaultFont());
	}

}

