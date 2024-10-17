package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;

import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.SimpleProductionSupply;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;

public class SimpleProductionEntryController extends ResourceSupplyEntryController<SimpleProductionSupply> {

	@FXML
	private Spinner<Integer> count;

	@Override
	protected void postInit(Stage w) throws IOException {
		super.postInit(w);

		GuiUtil.initWidgets(this);
	}

	@Override
	public void init(HostServices services) throws IOException {
		count.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9999, 0));
		GuiUtil.setupCounter(count, 0, 9999, 0, true);
		count.valueProperty().addListener((val, old, nnew) -> {
			supply.count = nnew;
			this.updateStats();
		});
	}

	@Override
	protected void onSetSupply(Factory f, SimpleProductionSupply res) throws IOException {
		super.onSetSupply(f, supply);
		count.getValueFactory().setValue(supply.count);
	}

}
