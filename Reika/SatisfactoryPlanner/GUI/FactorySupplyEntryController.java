package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;

import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.FromFactorySupply;

import javafx.stage.Stage;

public class FactorySupplyEntryController extends ResourceSupplyEntryController<FromFactorySupply> {

	@Override
	protected void postInit(Stage w) throws IOException {
		super.postInit(w);
	}

	@Override
	protected void updateStats(boolean fullUpdate) {
		super.updateStats(fullUpdate);
	}

	@Override
	protected void onSetSupply(Factory f, FromFactorySupply res) {
		f.updateMatrixStatus(res.getResource());
	}

}

