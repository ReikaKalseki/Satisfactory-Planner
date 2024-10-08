package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;

import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.FromFactorySupply;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class FactorySupplyEntryController extends ResourceSupplyEntryController<FromFactorySupply> {

	@FXML
	private Label nameText;

	@FXML
	private Button reloadButton;

	@Override
	protected void postInit(Stage w) throws IOException {
		super.postInit(w);
	}

	@Override
	protected void updateStats() {
		super.updateStats();
	}

	@Override
	protected void onSetSupply(Factory f, FromFactorySupply res) {
		f.updateMatrixStatus(res.getResource());
		nameText.setText(res.sourceFactory);
		reloadButton.setOnAction(e -> {
			f.updateFactorySupply(res.sourceFactoryFile, res);
		});
	}

}

