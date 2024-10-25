package Reika.SatisfactoryPlanner.GUI.Windows;

import java.io.IOException;

import org.controlsfx.control.SearchableComboBox;

import com.google.common.base.Strings;

import Reika.SatisfactoryPlanner.InternalIcons;
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
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
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
	private Button copyColor;

	@FXML
	private Button copyHex;

	@FXML
	private Label raw;

	@FXML
	private Label fluid;

	@FXML
	private Label mod;

	@FXML
	private Label sinkPoints;

	@FXML
	private Label radioactivity;

	@FXML
	private Label colorText;

	@FXML
	private Label colorHex;

	@FXML
	private ImageView colorBox;

	@Override
	public void init(HostServices services) throws IOException {
		GuiUtil.setButtonEvent(copyID, () -> GuiUtil.setClipboard(itemID));
		GuiUtil.setButtonEvent(copyClass, () -> GuiUtil.setClipboard(nativeClass));
		GuiUtil.setButtonEvent(copySink, () -> GuiUtil.setClipboard(sinkPoints));
		GuiUtil.setButtonEvent(copyColor, () -> GuiUtil.setClipboard(colorText));
		GuiUtil.setButtonEvent(copyHex, () -> GuiUtil.setClipboard(colorHex));

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
			Fluid f = (Fluid)nnew;
			boolean gas = f.isGas;
			fluid.setText(gas ? "Gas" : "Liquid");
			fluid.setStyle("-fx-background-color: "+ColorUtil.getCSSHex(gas ? UIConstants.OKAY_COLOR : UIConstants.WARN_COLOR)+";");
			Color c = f.baseColor;
			colorHex.setText(ColorUtil.getCSSHex(c));
			colorText.setText(String.format("B=%.0f,G=%.0f,R=%.0f,A=%.0f", c.getBlue()*255, c.getGreen()*255, c.getRed()*255, c.getOpacity()*255));
			WritableImage img = new WritableImage(20, 20);
			double a = c.getOpacity() <= 0.05 ? 1 : c.getOpacity();
			Color c2 = Color.color(c.getRed(), c.getGreen(), c.getBlue(), a); //all the base fluids have alpha of 0
			PixelWriter pw = img.getPixelWriter();
			for (int i = 0; i < 20; i++) {
				for (int k = 0; k < 20; k++) {
					pw.setColor(i, k, i == 0 || k == 0 || i == 19 || k == 19 ? Color.BLACK : c2);
				}
			}
			colorBox.setImage(img);
		}
		else {
			fluid.setText("No");
			fluid.setStyle("-fx-background-color: #ccc;");
			colorHex.setText("N/A");
			colorText.setText("N/A");
			colorBox.setImage(InternalIcons.INVALID.createIcon());
		}
		if (nnew != null) {
			this.setFlag(raw, nnew.isRawResource);
			this.setFlag(biomass, nnew.isBiomass);
			this.setFlag(equipment, nnew.isEquipment);
			this.setFlag(ficsmas, nnew.isFicsmas);
			this.setFlag(findable, nnew.isFindable);
			this.setFlag(alien, nnew.isAlien);
			String mod = nnew.getMod();
			this.mod.setText(Strings.isNullOrEmpty(mod) ? "Vanilla" : mod);
			this.getWindow().sizeToScene();
		}
		else {
			mod.setText("N/A");
			this.resetFlag(raw);
			this.resetFlag(biomass);
			this.resetFlag(equipment);
			this.resetFlag(ficsmas);
			this.resetFlag(findable);
			this.resetFlag(alien);
			colorHex.setText("N/A");
			colorText.setText("N/A");
			colorBox.setImage(InternalIcons.INVALID.createIcon());
		}
	}

	private void resetFlag(Label lb) {
		lb.setText("N/A");
		lb.setStyle("-fx-background-color: #ccc;");
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

