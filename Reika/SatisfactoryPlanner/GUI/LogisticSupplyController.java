package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.ArrayList;

import org.controlsfx.control.SearchableComboBox;

import Reika.SatisfactoryPlanner.Data.Constants.BeltTier;
import Reika.SatisfactoryPlanner.Data.Constants.PipeTier;
import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.DroneStation;
import Reika.SatisfactoryPlanner.Data.Fluid;
import Reika.SatisfactoryPlanner.Data.InputBelt;
import Reika.SatisfactoryPlanner.Data.InputPipe;
import Reika.SatisfactoryPlanner.Data.Item;
import Reika.SatisfactoryPlanner.Data.LogisticSupply;
import Reika.SatisfactoryPlanner.Data.TrainStation;
import Reika.SatisfactoryPlanner.Data.TruckStation;
import Reika.SatisfactoryPlanner.GUI.GuiUtil.SearchableSelector;

import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.Toggle;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class LogisticSupplyController extends RadioTitledPaneSection {

	@FXML
	private Button addButton;

	@FXML
	private ComboBox<BeltTier> beltTier;

	@FXML
	private ComboBox<PipeTier> pipeTier;

	@FXML
	private GridPane extraGrid;

	@FXML
	private Spinner<Integer> itemAmount;

	@FXML
	private SearchableComboBox<Consumable> itemDropdown;

	@FXML
	private Spinner<Integer> trainStationCount;

	@FXML
	private RadioButton beltRadio;

	@FXML
	private RadioButton droneRadio;

	@FXML
	private RadioButton truckRadio;

	@FXML
	private RadioButton trainRadio;

	@FXML
	private RadioButton pipeRadio;

	@Override
	public void init(HostServices services) throws IOException {

	}

	private LogisticSupply createResource() {
		Consumable c = itemDropdown.getSelectionModel().getSelectedItem();
		return switch (radioButtons.getSelectedToggle()) {
			case Toggle t when t == beltRadio -> new InputBelt((Item)c, beltTier.getSelectionModel().getSelectedItem());
			case Toggle t when t == pipeRadio -> new InputPipe((Fluid)c, pipeTier.getSelectionModel().getSelectedItem());
			case Toggle t when t == truckRadio -> new TruckStation((Item)c);
			case Toggle t when t == trainRadio -> new TrainStation(c, trainStationCount.getValue());
			case Toggle t when t == droneRadio -> new DroneStation((Item)c);
			default -> null;
		};
	}

	@Override
	protected void postInit(Stage w) throws IOException {
		super.postInit(w);

		beltTier.setItems(FXCollections.observableArrayList(BeltTier.values()));
		pipeTier.setItems(FXCollections.observableArrayList(PipeTier.values()));

		GuiUtil.setupAddSelector(itemDropdown, new SearchableSelector<Consumable>(){
			@Override
			public void accept(Consumable t) {

			}

			@Override
			public DecoratedListCell<Consumable> createListCell(String text, boolean button) {
				return new ItemListCell<Consumable>(text, button);
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

		beltTier.setButtonCell(new TierListCell<BeltTier>("Choose Tier...", true));
		beltTier.setCellFactory(c -> new TierListCell<BeltTier>("", false));

		pipeTier.setButtonCell(new TierListCell<PipeTier>("Choose Pipe...", true));
		pipeTier.setCellFactory(c -> new TierListCell<PipeTier>("", false));

		itemDropdown.getSelectionModel().selectedItemProperty().addListener((val, old, nnew) -> {
			itemAmount.setDisable(nnew == null);
		});

		//radioButtons.selectToggle(beltRadio); do not initial select because need to finish initializing to access owner for item filters

		GuiUtil.setupCounter(itemAmount, 0, 9999, 0, true);
		GuiUtil.setupCounter(trainStationCount, 0, 99, 0, false);

		addButton.setOnAction(e -> {
			Toggle b = radioButtons.getSelectedToggle();
			Consumable c = itemDropdown.getSelectionModel().getSelectedItem();
			if (c == null) {
				GuiUtil.raiseUserErrorDialog("Resource Supply Invalid", "No item selected.");
				return;
			}
			if (c instanceof Fluid && b != pipeRadio && b != trainRadio) {
				GuiUtil.raiseUserErrorDialog("Resource Supply Invalid", "Invalid item for logistics type.");
				return;
			}
			if (c instanceof Item && b == pipeRadio) {
				GuiUtil.raiseUserErrorDialog("Resource Supply Invalid", "Invalid item for logistics type.");
				return;
			}
			if (b == beltRadio && beltTier.getSelectionModel().getSelectedItem() == null) {
				GuiUtil.raiseUserErrorDialog("Resource Supply Invalid", "No belt selected.");
				return;
			}
			if (b == pipeRadio && pipeTier.getSelectionModel().getSelectedItem() == null) {
				GuiUtil.raiseUserErrorDialog("Resource Supply Invalid", "No pipe selected.");
				return;
			}
			LogisticSupply res = this.createResource();
			res.setAmount(itemAmount.getValue());
			((MainGuiController)owner).getFactory().addExternalSupply(res);
			this.close();
		});

		GuiUtil.initWidgets(this);
	}

	@Override
	protected void onToggleSelected(RadioButton rb) {
		super.onToggleSelected(rb);
		itemDropdown.setDisable(rb == null);
		itemAmount.setDisable(rb == null);

		if (rb == null) {
			itemDropdown.getItems().clear();
		}
		else {
			ArrayList<Consumable> li = new ArrayList(Database.getAllItems());
			Toggle b = radioButtons.getSelectedToggle();
			if (b == pipeRadio)
				li.removeIf(c -> c instanceof Item);
			else if (b == beltRadio || b == truckRadio || b == droneRadio)
				li.removeIf(c -> c instanceof Fluid);
			MainGuiController main = (MainGuiController)owner;
			li.removeIf(c -> !main.isItemValid(c));
			itemDropdown.setItems(FXCollections.observableArrayList(li)); //automatically clears if the old selection is no longer valid
		}
	}

}

