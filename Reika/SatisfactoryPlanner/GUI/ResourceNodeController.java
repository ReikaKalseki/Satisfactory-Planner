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
import Reika.SatisfactoryPlanner.Util.GuiUtil;

import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
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
	private Slider clockspeed;

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
	private ChoiceBox<Purity> purity;

	@FXML
	private ChoiceBox<MinerTier> solidMinerTier;

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

	@Override
	public void init(HostServices services) throws IOException {
		solidDropdown.setItems(FXCollections.observableArrayList(Database.getMineables()));
		frackingDropdown.setItems(FXCollections.observableArrayList(Database.getFrackables()));
		purity.setItems(FXCollections.observableArrayList(Purity.values()));
		solidMinerTier.setItems(FXCollections.observableArrayList(MinerTier.values()));

		StringConverter cv = new StringConverter<Consumable>() {
			@Override
			public String toString(Consumable mt) {
				return mt == null ? "" : mt.name;
			}

			@Override
			public Consumable fromString(String s) {
				return Strings.isNullOrEmpty(s) ? null : Database.lookupItem(s);
			}
		};
		solidDropdown.setConverter(cv);
		frackingDropdown.setConverter(cv);

		solidMinerTier.setConverter(new StringConverter<MinerTier>() {
			@Override
			public String toString(MinerTier mt) {
				return mt == null ? "" : "Miner Mk"+(mt.ordinal()+1);
			}

			@Override
			public MinerTier fromString(String s) {
				return Strings.isNullOrEmpty(s) ? null : MinerTier.values()[Integer.parseInt(s.substring(s.length()-1))-1];
			}
		});

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
			this.updateStats((int)clockspeed.getValue());
		});

		purity.getSelectionModel().selectedItemProperty().addListener((val, old, nnew) -> {
			this.updateStats((int)clockspeed.getValue());
		});
		solidDropdown.getSelectionModel().selectedItemProperty().addListener((val, old, nnew) -> {
			this.updateStats((int)clockspeed.getValue());
		});
		solidMinerTier.getSelectionModel().selectedItemProperty().addListener((val, old, nnew) -> {
			this.updateStats((int)clockspeed.getValue());
		});
		frackingDropdown.getSelectionModel().selectedItemProperty().addListener((val, old, nnew) -> {
			this.updateStats((int)clockspeed.getValue());
		});

		radioButtons.selectToggle(solidRadio);

		this.setupFrackingSpinner(frackingImpure, Purity.IMPURE);
		this.setupFrackingSpinner(frackingNormal, Purity.NORMAL);
		this.setupFrackingSpinner(frackingPure, Purity.PURE);

		clockspeed.valueProperty().addListener((val, old, nnew) -> {
			this.updateStats(nnew.intValue());
		});

		addButton.setOnAction(e -> {
			if (!purity.isDisabled() && purity.getSelectionModel().getSelectedItem() == null) {
				//error
				return;
			}
			if (radioButtons.getSelectedToggle() == solidRadio && solidDropdown.getSelectionModel().getSelectedItem() == null) {
				//error
				return;
			}
			if (radioButtons.getSelectedToggle() == solidRadio && solidMinerTier.getSelectionModel().getSelectedItem() == null) {
				//error
				return;
			}
			if (radioButtons.getSelectedToggle() == frackingRadio && frackingDropdown.getSelectionModel().getSelectedItem() == null) {
				//error
				return;
			}
			ExtractableResource res = this.createResource();
			res.setClockSpeed((int)clockspeed.getValue());
			((MainGuiController)owner).getFactory().addExternalSupply(res);
		});
	}

	private void updateStats(int clock) {
		ExtractableResource res = this.createResource();
		shardDisplay.getChildren().clear();
		yieldDisplay.getChildren().clear();
		for (int i = 0; i < Math.ceil((clock-100)/50D); i++) {
			shardDisplay.getChildren().add(Database.lookupItem("Power Shard").createImageView());
		}
		if (res != null && res.getResource() != null && res.getYield() > 0) {
			res.setClockSpeed(clock/100F);
			GuiUtil.addIconCount(yieldDisplay, res.getResource(), res.getYield());
		}
	}

	private void setupFrackingSpinner(Spinner<Integer> counter, Purity p) {
		counter.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 99, 0));
		counter.setEditable(true);
		counter.setPrefWidth(64);
		counter.setMinWidth(Region.USE_PREF_SIZE);
		counter.setMaxWidth(Region.USE_PREF_SIZE);
		counter.getValueFactory().setValue(0);
		counter.getValueFactory().valueProperty().addListener((val, old, nnew) -> {
			this.updateStats((int)clockspeed.getValue());
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
	}

}

