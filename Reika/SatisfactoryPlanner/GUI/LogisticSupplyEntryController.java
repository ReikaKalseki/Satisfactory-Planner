package Reika.SatisfactoryPlanner.GUI;

import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.LogisticSupply;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class LogisticSupplyEntryController extends ResourceSupplyEntryController<LogisticSupply> {

	@FXML
	private Label amountValue;

	@Override
	protected void onSetSupply(Factory f, LogisticSupply res) {
		amountValue.setText(res.getYield()+"/min");
	}

}

