package Reika.SatisfactoryPlanner.GUI;

import Reika.SatisfactoryPlanner.Data.Constants.Purity;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.ExtractableResource;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.FrackingCluster;
import Reika.SatisfactoryPlanner.Data.OilNode;
import Reika.SatisfactoryPlanner.Data.SolidResourceNode;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class ResourceMineEntryController extends ResourceSupplyEntryController<ExtractableResource> {

	@FXML
	private HBox shardDisplay;

	@FXML
	private Label speedValue;

	@Override
	protected Node getTopBarRightContent() {
		if (this.getSupply() instanceof SolidResourceNode) {
			Purity pp = ((SolidResourceNode)this.getSupply()).purityLevel;
			ImageView right = new ImageView(pp.image);
			GuiUtil.setTooltip(right, pp.name());
			return right;
		}
		else if (this.getSupply() instanceof OilNode) {
			Purity pp = ((OilNode)this.getSupply()).purityLevel;
			ImageView right = new ImageView(pp.image);
			GuiUtil.setTooltip(right, pp.name());
			return right;
		}
		return null;
	}

	@Override
	protected Node getTopBarContent(HBox orig) {
		if (this.getSupply() instanceof FrackingCluster) {
			FrackingCluster fc = (FrackingCluster)this.getSupply();
			VBox vb = new VBox();
			vb.getChildren().add(orig);
			HBox hb = new HBox();
			for (int i = 0; i < fc.impureCount; i++)
				hb.getChildren().add(new ImageView(Purity.IMPURE.image));
			for (int i = 0; i < fc.normalCount; i++)
				hb.getChildren().add(new ImageView(Purity.NORMAL.image));
			for (int i = 0; i < fc.pureCount; i++)
				hb.getChildren().add(new ImageView(Purity.PURE.image));
			vb.getChildren().add(hb);
			return vb;
		}
		return orig;
	}

	@Override
	protected void onSetSupply(Factory f, ExtractableResource res) {
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
	}

}

