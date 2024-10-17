package Reika.SatisfactoryPlanner.GUI.Supplies;

import java.io.IOException;

import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.WaterExtractor;
import Reika.SatisfactoryPlanner.GUI.GuiUtil;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

public class WaterEntryController extends ResourceMineEntryController<WaterExtractor> {

	@FXML
	private Spinner<Integer> count;

	@Override
	public void init(HostServices services) throws IOException {
		count.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9999, 0));
		GuiUtil.setupCounter(count, 0, 9999, 0, true);
		count.valueProperty().addListener((val, old, nnew) -> {
			supply.numberExtractors = nnew;
			this.updateStats();
		});
	}

	@Override
	protected void onSetSupply(Factory f, WaterExtractor res) throws IOException {
		super.onSetSupply(f, supply);
		count.getValueFactory().setValue(supply.numberExtractors);
	}

}
