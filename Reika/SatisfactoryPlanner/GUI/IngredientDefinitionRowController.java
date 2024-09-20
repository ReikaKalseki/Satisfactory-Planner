package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;

import org.controlsfx.control.SearchableComboBox;

import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.Fluid;

import fxexpansions.FXMLControllerBase;
import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class IngredientDefinitionRowController extends FXMLControllerBase {

	@FXML
	private Spinner<Integer> countSpinner;

	@FXML
	private SearchableComboBox<Consumable> itemDropdown;

	@FXML
	private Label label;

	@FXML
	private GridPane root;

	private float timeCoefficient = 1;

	private ItemRateController rateDisplay;

	@Override
	public void init(HostServices services) throws IOException {
		this.setupCountSpinner(countSpinner, 999);

		itemDropdown.getSelectionModel().selectedItemProperty().addListener((val, old, nnew) -> {
			if (rateDisplay != null)
				root.getChildren().remove(rateDisplay.getRootNode());
			rateDisplay = null;
			if (nnew != null) {
				rateDisplay = new ItemRateController(nnew, countSpinner.getValue());
				root.add(rateDisplay.getRootNode(), 5, 0);
			}
		});

		itemDropdown.setButtonCell(new ItemListCell("Choose Item...", true));
		itemDropdown.setCellFactory(c -> new ItemListCell("", false));
	}

	private void setupCountSpinner(Spinner<Integer> counter, int max) {
		counter.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, max, 0));
		GuiUtil.setupCounter(counter, 0, max, 0, true);
		counter.valueProperty().addListener((val, old, nnew) -> {
			if (rateDisplay != null)
				rateDisplay.setAmount(nnew.floatValue());
			this.updateStats();
		});
	}

	public void setTimeCoefficient(float time) {
		timeCoefficient = time;
		this.updateStats();
	}

	public void setName(String name) {
		label.setText(name);
	}

	private void updateStats() {
		if (rateDisplay != null) {
			rateDisplay.setScale(1F/timeCoefficient);
		}
	}

	@Override
	protected void postInit(Stage w) throws IOException {
		super.postInit(w);
	}

	public void setFluid(boolean fluid) {
		itemDropdown.getSelectionModel().clearSelection();
		itemDropdown.setItems(FXCollections.observableArrayList(Database.getAllItems().stream().filter(c -> (c instanceof Fluid) == fluid).toList()));
	}

}

