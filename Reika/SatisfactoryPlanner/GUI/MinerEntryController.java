package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;

import Reika.SatisfactoryPlanner.Data.Constants.MinerTier;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.SolidResourceNode;

import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

public class MinerEntryController extends ResourceMineEntryController<SolidResourceNode> {

	@FXML
	private ComboBox<MinerTier> miner;

	@Override
	public void init(HostServices services) throws IOException {
		miner.setItems(FXCollections.observableArrayList(MinerTier.values()));
		miner.setButtonCell(new TierListCell<MinerTier>("Choose Miner...", true));
		miner.setCellFactory(c -> new TierListCell<MinerTier>("", false));
		miner.getSelectionModel().selectedItemProperty().addListener((val, old, nnew) -> {
			supply.minerLevel = nnew;
			this.updateStats();
		});
	}

	@Override
	protected void onSetSupply(Factory f, SolidResourceNode res) throws IOException {
		super.onSetSupply(f, supply);
		miner.getSelectionModel().select(supply.minerLevel);
	}

}
