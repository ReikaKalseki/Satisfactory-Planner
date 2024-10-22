package Reika.SatisfactoryPlanner.GUI.Supplies;

import java.io.IOException;

import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.LogisticSupply;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.OverclockableResource;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.ResourceSupply;
import Reika.SatisfactoryPlanner.GUI.GuiUtil;

import fxexpansions.FXMLControllerBase;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public abstract class ResourceSupplyEntryController<R extends ResourceSupply> extends FXMLControllerBase implements Comparable<ResourceSupplyEntryController> {

	@FXML
	protected VBox root;

	@FXML
	private Button deleteButton;

	@FXML
	private Button duplicateButton;

	@FXML
	protected HBox topBar;

	@FXML
	protected HBox yieldDisplay;

	protected R supply;
	protected Factory factory;

	@Override
	public void init(HostServices services) throws IOException {

	}

	@Override
	protected void postInit(Stage w) throws IOException {
		super.postInit(w);

		GuiUtil.initWidgets(this);
	}

	protected void updateStats() {
		this.updateStats(true);
	}

	protected void updateStats(boolean updateFactory) {
		//Logging.instance.log("Updating supply "+supply.getResource().displayName+"x"+supply.getYield()+": "+fullUpdate);
		if (yieldDisplay != null) {
			yieldDisplay.getChildren().clear();
			GuiUtil.addIconCount(supply.getResource(), supply.getYield(), 4, false, yieldDisplay);
		}
		if (updateFactory)
			factory.updateResourceSupply(supply);
	}

	protected final R getSupply() {
		return supply;
	}

	protected Node getTopBarRightContent() {
		return null;
	}

	protected Node getTopBarContent(HBox orig) {
		return orig;
	}

	protected void onSetSupply(Factory f, R res) throws IOException {

	}

	public final void setSupply(Factory f, R res) throws IOException {
		supply = res;
		factory = f;
		StackPane itemIco = GuiUtil.createItemDisplay(res.getResource(), 32, false);
		HBox hb = GuiUtil.createSpacedHBox(itemIco, GuiUtil.createItemDisplay(res, 32, false), this.getTopBarRightContent());
		hb.setSpacing(4);
		Node n = this.getTopBarContent(hb);
		if (n != null)
			topBar.getChildren().add(n);
		this.updateStats();
		deleteButton.setOnAction(e -> {
			f.removeExternalSupply(res);
		});
		if (duplicateButton != null) {
			duplicateButton.setOnAction(e -> {
				ResourceSupply r = res.duplicate();
				if (r instanceof OverclockableResource)
					((OverclockableResource)r).setClockSpeed(((OverclockableResource)res).getClockSpeed());
				if (r instanceof LogisticSupply)
					((LogisticSupply)r).setAmount((int)((LogisticSupply)res).getYield());
				f.addExternalSupply(r);
			});
		}
		this.onSetSupply(f, res);
	}

	public int compareTo(ResourceSupplyEntryController rc) {
		return ResourceSupply.globalSupplySorter.compare(supply, rc.supply);
	}

}

