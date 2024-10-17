package Reika.SatisfactoryPlanner.GUI.Windows;

import java.io.IOException;

import org.controlsfx.control.SearchableComboBox;

import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.Objects.Consumable;
import Reika.SatisfactoryPlanner.Data.Objects.Fluid;
import Reika.SatisfactoryPlanner.Data.Objects.Item;
import Reika.SatisfactoryPlanner.GUI.GuiUtil;
import Reika.SatisfactoryPlanner.GUI.UIConstants;
import Reika.SatisfactoryPlanner.GUI.Components.ListCells.ItemListCell;
import Reika.SatisfactoryPlanner.Util.ColorUtil;

import fxexpansions.FXMLControllerBase;
import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ItemCatalogController extends FXMLControllerBase {

	@FXML
	private SearchableComboBox<Consumable> itemDropdown;

	@FXML
	private Label alien;

	@FXML
	private Label biomass;

	@FXML
	private Label energyValue;

	@FXML
	private Label equipment;

	@FXML
	private Label ficsmas;

	@FXML
	private Label findable;

	@FXML
	private Label itemDesc;

	@FXML
	private Label itemID;

	@FXML
	private Label nativeClass;

	@FXML
	private Button copyID;

	@FXML
	private Button copyClass;

	@FXML
	private Button copySink;

	@FXML
	private Label raw;

	@FXML
	private Label fluid;

	@FXML
	private Label sinkPoints;

	@FXML
	private Label radioactivity;

	@Override
	public void init(HostServices services) throws IOException {
		GuiUtil.setButtonEvent(copyID, () -> GuiUtil.setClipboard(itemID));
		GuiUtil.setButtonEvent(copyClass, () -> GuiUtil.setClipboard(nativeClass));
		GuiUtil.setButtonEvent(copySink, () -> GuiUtil.setClipboard(sinkPoints));

		itemDropdown.setButtonCell(new ItemListCell("Choose Item...", true));
		itemDropdown.setCellFactory(c -> new ItemListCell("", false));
		itemDropdown.setItems(FXCollections.observableArrayList(Database.getAllItems()));
		itemDropdown.getSelectionModel().selectedItemProperty().addListener((val, old, nnew) -> {
			this.setItem(nnew);
		});
		this.setItem(null);
	}

	private void setItem(Consumable nnew) {
		itemID.setText(nnew == null ? "" : nnew.id);
		itemDesc.setText(nnew == null ? "" : nnew.description);
		nativeClass.setText(nnew == null ? "" : nnew.nativeClass);
		energyValue.setText(nnew == null ? "0" : String.format("%.2f MJ", nnew.energyValue));
		sinkPoints.setText(nnew instanceof Item && ((Item)nnew).sinkValue > 0 ? String.valueOf(((Item)nnew).sinkValue) : "N/A");
		if (nnew instanceof Item) {
			Item ii = (Item)nnew;
			radioactivity.setText(String.format("%.0f", ii.radioactivity));
			radioactivity.setStyle("-fx-text-fill: "+ColorUtil.getCSSHex(ii.radioactivity > 0 ? UIConstants.SEVERE_COLOR : UIConstants.OKAY_COLOR)+"; -fx-font-weight: "+(ii.radioactivity > 0 ? "bold" : "normal")+";");
		}
		else {
			radioactivity.setText("N/A");
			radioactivity.setStyle("-fx-text-fill: #000; -fx-font-weight: normal;");
		}
		if (nnew instanceof Fluid) {
			boolean gas = ((Fluid)nnew).isGas;
			fluid.setText(gas ? "Gas" : "Liquid");
			fluid.setStyle("-fx-background-color: "+ColorUtil.getCSSHex(gas ? UIConstants.OKAY_COLOR : UIConstants.WARN_COLOR)+";");
		}
		else {
			fluid.setText("No");
			fluid.setStyle("-fx-background-color: #ccc;");
		}
		if (nnew != null) {
			this.setFlag(raw, nnew.isRawResource);
			this.setFlag(biomass, nnew.isBiomass);
			this.setFlag(equipment, nnew.isEquipment);
			this.setFlag(ficsmas, nnew.isFicsmas);
			this.setFlag(findable, nnew.isFindable);
			this.setFlag(alien, nnew.isAlien);
			this.getWindow().sizeToScene();
		}
	}

	private void setFlag(Label lb, boolean val) {
		lb.setText(val ? "Yes" : "No");
		lb.setStyle("-fx-background-color: "+ColorUtil.getCSSHex(val ? UIConstants.OKAY_COLOR : UIConstants.WARN_COLOR)+";");
	}

	@Override
	protected void postInit(Stage w) throws IOException {
		super.postInit(w);

		GuiUtil.initWidgets(this);
	}

}

