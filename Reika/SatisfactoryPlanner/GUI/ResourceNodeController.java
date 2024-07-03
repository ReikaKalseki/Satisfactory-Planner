package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;

import com.google.common.base.Strings;

import Reika.SatisfactoryPlanner.Data.Constants.MinerTier;
import Reika.SatisfactoryPlanner.Data.Constants.Purity;
import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.ExtractableResource;
import Reika.SatisfactoryPlanner.Data.Fluid;
import Reika.SatisfactoryPlanner.Data.FrackingCluster;
import Reika.SatisfactoryPlanner.Data.Item;
import Reika.SatisfactoryPlanner.Data.OilNode;
import Reika.SatisfactoryPlanner.Data.SolidResourceNode;
import Reika.SatisfactoryPlanner.Data.WaterExtractor;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.GuiInstance;

import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.StringConverter;

public class ResourceNodeController extends ControllerBase {

	@FXML
	private Button addButton;

	@FXML
	private GridPane extraGrid;

	@FXML
	private ComboBox<Fluid> frackingDropdown;

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
	private HBox shardDisplay;

	@FXML
	private HBox yieldDisplay;

	@FXML
	private ComboBox<Item> solidDropdown;

	@FXML
	private RadioButton solidRadio;

	@FXML
	private RadioButton waterRadio;

	@FXML
	private RadioButton oilRadio;

	@FXML
	private RadioButton frackingRadio;

	private ToggleGroup radioButtons = new ToggleGroup();

	private int clockSpeed;

	@Override
	public void init(HostServices services) throws IOException {
		solidDropdown.setItems(FXCollections.observableArrayList(Database.getMineables()));
		frackingDropdown.setItems(FXCollections.observableArrayList(Database.getFrackables()));
		purity.setItems(FXCollections.observableArrayList(Purity.values()));
		solidMinerTier.setItems(FXCollections.observableArrayList(MinerTier.values()));

		StringConverter cv = new StringConverter<Consumable>() {
			@Override
			public String toString(Consumable mt) {
				return mt == null ? "" : mt.displayName;
			}

			@Override
			public Consumable fromString(String s) {
				return Strings.isNullOrEmpty(s) ? null : Database.lookupItem(s);
			}
		};
		solidDropdown.setConverter(cv);
		frackingDropdown.setConverter(cv);

		solidDropdown.setButtonCell(new ItemListCell<Item>("Choose Item...", true));
		solidDropdown.setCellFactory(c -> new ItemListCell<Item>("", false));
		frackingDropdown.setButtonCell(new ItemListCell<Fluid>("Choose Fluid...", true));
		frackingDropdown.setCellFactory(c -> new ItemListCell<Fluid>("", false));
		solidMinerTier.setButtonCell(new MinerListCell("Choose Miner...", true));
		solidMinerTier.setCellFactory(c -> new MinerListCell("", false));
		purity.setButtonCell(new PurityListCell("Choose Purity...", true));
		purity.setCellFactory(c -> new PurityListCell("", false));

		solidMinerTier.setConverter(MinerListCell.converter);

		solidRadio.setToggleGroup(radioButtons);
		waterRadio.setToggleGroup(radioButtons);
		oilRadio.setToggleGroup(radioButtons);
		frackingRadio.setToggleGroup(radioButtons);

		radioButtons.selectedToggleProperty().addListener((val, old, nnew) -> {
			solidDropdown.setDisable(nnew != solidRadio);
			solidMinerTier.setDisable(nnew != solidRadio);
			purity.setDisable(nnew == waterRadio || nnew == frackingRadio);
			frackingImpure.setDisable(nnew != frackingRadio);
			frackingNormal.setDisable(nnew != frackingRadio);
			frackingPure.setDisable(nnew != frackingRadio);
			frackingDropdown.setDisable(nnew != frackingRadio);
			this.updateStats();
		});

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

		radioButtons.selectToggle(solidRadio);

		this.setupFrackingSpinner(frackingImpure, Purity.IMPURE);
		this.setupFrackingSpinner(frackingNormal, Purity.NORMAL);
		this.setupFrackingSpinner(frackingPure, Purity.PURE);

		addButton.setOnAction(e -> {
			if (!purity.isDisabled() && purity.getSelectionModel().getSelectedItem() == null) {
				//TODO error
				return;
			}
			if (radioButtons.getSelectedToggle() == solidRadio && solidDropdown.getSelectionModel().getSelectedItem() == null) {
				//TODO error
				return;
			}
			if (radioButtons.getSelectedToggle() == solidRadio && solidMinerTier.getSelectionModel().getSelectedItem() == null) {
				//TODO error
				return;
			}
			if (radioButtons.getSelectedToggle() == frackingRadio && frackingDropdown.getSelectionModel().getSelectedItem() == null) {
				//TODO error
				return;
			}
			ExtractableResource res = this.createResource();
			res.setClockSpeed(clockSpeed/100F);
			((MainGuiController)owner).getFactory().addExternalSupply(res);
			this.close();
		});
	}

	private void updateStats() {
		ExtractableResource res = this.createResource();
		shardDisplay.getChildren().clear();
		yieldDisplay.getChildren().clear();
		if (clockSpeed > 100) {
			for (int i = 0; i < Math.ceil((clockSpeed-100)/50D); i++) {
				shardDisplay.getChildren().add(Database.lookupItem("Desc_CrystalShard_C").createImageView());
			}
		}
		if (res != null && res.getResource() != null && res.getYield() > 0) {
			res.setClockSpeed(clockSpeed/100F);
			GuiUtil.addIconCount(yieldDisplay, res.getResource(), res.getYield());
		}
	}

	private void setupFrackingSpinner(Spinner<Integer> counter, Purity p) {
		counter.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 99, 0));
		counter.setEditable(false);
		counter.setPrefWidth(64);
		counter.setMinWidth(Region.USE_PREF_SIZE);
		counter.setMaxWidth(Region.USE_PREF_SIZE);
		counter.getValueFactory().setValue(0);
		counter.getValueFactory().valueProperty().addListener((val, old, nnew) -> {
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

		GuiInstance gui = this.loadNestedFXML("ClockspeedSlider", extraGrid, 1, 1);
		((ClockspeedSliderController)gui.controller).setCallback(v -> {clockSpeed = v; this.updateStats();});
		this.setFont(this.getRootNode(), GuiSystem.getDefaultFont());
	}

}

