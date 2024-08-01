package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;

import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.LogisticSupply;

import fxexpansions.WindowBase;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.image.ImageView;

public class LogisticSupplyEntryController extends ResourceSupplyEntryController<LogisticSupply> {

	@FXML
	private Spinner<Integer> amount;

	@FXML
	private ImageView icon;

	@Override
	protected void postInit(WindowBase w) throws IOException {
		super.postInit(w);

		GuiUtil.setupCounter(amount, 0, 9999, 0, true);

		amount.valueProperty().addListener((val, old, nnew) -> {
			supply.setAmount(nnew);
			this.updateStats();
		});
		//amount.getEditor().setVisible(false);
	}

	@Override
	protected void updateStats() {
		yieldDisplay.getChildren().clear();
		icon.setImage(supply.resource.createIcon());
	}

	@Override
	protected void onSetSupply(Factory f, LogisticSupply res) {
		amount.getValueFactory().setValue(res.getYield());
		f.updateMatrixStatus(res.getResource());
	}

}

