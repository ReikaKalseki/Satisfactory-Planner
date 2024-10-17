package Reika.SatisfactoryPlanner.GUI.Supplies;

import java.io.IOException;

import Reika.SatisfactoryPlanner.Data.Constants.BuildingTier;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.TieredLogisticSupply;
import Reika.SatisfactoryPlanner.GUI.Components.ListCells.TierListCell;

import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

public class TieredLogisticSupplyEntryController<S extends TieredLogisticSupply> extends LogisticSupplyEntryController<S> {

	@FXML
	private ComboBox<BuildingTier> tier;

	@Override
	public void init(HostServices services) throws IOException {
		tier.getSelectionModel().selectedItemProperty().addListener((val, old, nnew) -> {
			supply.tier = nnew;
			this.updateStats();
		});
	}

	@Override
	protected void onSetSupply(Factory f, TieredLogisticSupply res) throws IOException {
		super.onSetSupply(f, supply);
		tier.setItems(FXCollections.observableArrayList(res.getTierValues()));
		tier.setButtonCell(new TierListCell("Choose Tier...", true));
		tier.setCellFactory(c -> new TierListCell("", false));
		tier.getSelectionModel().select(supply.tier);
	}

}

