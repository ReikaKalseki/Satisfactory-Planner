package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;

import Reika.SatisfactoryPlanner.Data.Constants.Purity;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.ExtractableResource;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.FrackingCluster;
import Reika.SatisfactoryPlanner.Data.SolidResourceNode;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class ResourceMineEntryController extends ControllerBase {

	@FXML
	private Button deleteButton;

	@FXML
	private HBox shardDisplay;

	@FXML
	private Label speedValue;

	@FXML
	private HBox topBar;

	@FXML
	private HBox yieldDisplay;

	@Override
	public void init(HostServices services) throws IOException {

	}

	@Override
	protected void postInit(WindowBase w) throws IOException {
		super.postInit(w);
		this.setFont(this.getRootNode(), GuiSystem.getDefaultFont());
	}

	public void setMine(Factory f, ExtractableResource res) {
		Node right = null;
		if (res instanceof SolidResourceNode) {
			Purity pp = ((SolidResourceNode)res).purityLevel;
			right = new ImageView(pp.image);
			GuiUtil.setTooltip(right, pp.name());
		}
		Node n = GuiUtil.createSpacedHBox(res.getResource().createImageView(), res.getBuilding().createImageView(), right);
		if (res instanceof FrackingCluster) {
			FrackingCluster fc = (FrackingCluster)res;
			VBox vb = new VBox();
			vb.getChildren().add(n);
			HBox hb = new HBox();
			for (int i = 0; i < fc.impureCount; i++)
				hb.getChildren().add(new ImageView(Purity.IMPURE.image));
			for (int i = 0; i < fc.normalCount; i++)
				hb.getChildren().add(new ImageView(Purity.NORMAL.image));
			for (int i = 0; i < fc.pureCount; i++)
				hb.getChildren().add(new ImageView(Purity.PURE.image));
			vb.getChildren().add(hb);
			n = vb;
		}
		if (n != null)
			topBar.getChildren().add(n);
		int pct = (int)(res.getClockSpeed()*100);
		speedValue.setText(pct+"%");
		if (pct > 100) {
			for (int i = 0; i < Math.ceil((pct-100)/50D); i++) {
				shardDisplay.getChildren().add(Database.lookupItem("Desc_CrystalShard_C").createImageView());
			}
		}
		else {
			((Pane)shardDisplay.getParent()).getChildren().remove(shardDisplay);
		}
		GuiUtil.addIconCount(yieldDisplay, res.getResource(), res.getYield());
		deleteButton.setOnAction(e -> {
			f.removeExternalSupply(res);
		});
	}

}

