package Reika.SatisfactoryPlanner.GUI.Supplies;

import java.io.IOException;

import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.LogisticSupply;
import Reika.SatisfactoryPlanner.GUI.GuiUtil;

import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class LogisticSupplyEntryController<S extends LogisticSupply> extends ResourceSupplyEntryController<S> {

	@FXML
	private Spinner<Double> amount;

	@FXML
	private HBox countRow;

	@Override
	protected void postInit(Stage w) throws IOException {
		super.postInit(w);

		GuiUtil.setupCounter(amount, 0, 9999, 0, true, true);

		amount.valueProperty().addListener((val, old, nnew) -> {
			supply.setAmount(nnew);
			this.updateStats();
		});
		//amount.getEditor().setVisible(false);
	}

	@Override
	protected void updateStats() {
		super.updateStats();
		yieldDisplay.getChildren().clear();
		//icon.setImage(supply.resource.createIcon());
	}

	@Override
	protected void onSetSupply(Factory f, LogisticSupply res) {
		amount.getValueFactory().setValue(res.getYield());
		f.updateMatrixStatus(res.getResource());
		countRow.getChildren().add(0, GuiUtil.createItemDisplay(supply.getResource(), 32, false));
	}

}

