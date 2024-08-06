package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;

import Reika.SatisfactoryPlanner.Data.Constants.Purity;
import Reika.SatisfactoryPlanner.Data.ExtractableResource;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.FrackingCluster;
import Reika.SatisfactoryPlanner.Data.OilNode;
import Reika.SatisfactoryPlanner.Data.SolidResourceNode;

import fxexpansions.GuiInstance;
import fxexpansions.WindowBase;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ResourceMineEntryController<R extends ExtractableResource> extends ResourceSupplyEntryController<R> {

	@Override
	protected void postInit(WindowBase w) throws IOException {
		super.postInit(w);

		this.setFont(this.getRootNode(), GuiSystem.getDefaultFont());
	}

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
	protected void onSetSupply(Factory f, R res) throws IOException {
		GuiInstance<ClockspeedSliderController> gui = this.loadNestedFXML("ClockspeedSlider", root);
		gui.rootNode.toBack();
		topBar.toBack();
		Platform.runLater(() -> gui.controller.setValue((int)(res.getClockSpeed()*100)));
		gui.controller.setCallback(v -> {
			supply.setClockSpeed(v/100F);
			f.updateMatrixStatus(supply.getResource());
			this.updateStats();
		});
	}

}

