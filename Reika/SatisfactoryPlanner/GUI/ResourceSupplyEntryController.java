package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;

import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.ResourceSupply;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public abstract class ResourceSupplyEntryController<R extends ResourceSupply> extends ControllerBase {

	@FXML
	private Button deleteButton;

	@FXML
	private HBox topBar;

	@FXML
	private HBox yieldDisplay;

	private R supply;

	@Override
	public void init(HostServices services) throws IOException {

	}

	@Override
	protected void postInit(WindowBase w) throws IOException {
		super.postInit(w);
		this.setFont(this.getRootNode(), GuiSystem.getDefaultFont());
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

	protected void onSetSupply(Factory f, R res) {

	}

	public final void setSupply(Factory f, R res) {
		supply = res;
		Node n = this.getTopBarContent(GuiUtil.createSpacedHBox(res.getResource().createImageView(), res.getIcon().createImageView(), this.getTopBarRightContent()));
		if (n != null)
			topBar.getChildren().add(n);
		GuiUtil.addIconCount(yieldDisplay, res.getResource(), res.getYield());
		deleteButton.setOnAction(e -> {
			f.removeExternalSupply(res);
		});
		this.onSetSupply(f, res);
	}

}

