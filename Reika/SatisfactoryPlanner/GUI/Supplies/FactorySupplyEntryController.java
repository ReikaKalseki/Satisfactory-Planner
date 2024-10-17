package Reika.SatisfactoryPlanner.GUI.Supplies;

import java.io.IOException;

import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.FromFactorySupply;

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
	protected void onSetSupply(Factory f, FromFactorySupply res) {
		f.updateMatrixStatus(res.getResource());
		nameText.setText(res.sourceFactory);
		reloadButton.setOnAction(e -> {
			f.updateFactorySupply(res.sourceFactoryFile, res, () -> this.updateStats(false));
		});
	}

}

