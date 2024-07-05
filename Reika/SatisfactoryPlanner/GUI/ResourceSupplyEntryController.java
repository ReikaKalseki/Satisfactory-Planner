package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;

import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.ResourceSupply;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public abstract class ResourceSupplyEntryController<R extends ResourceSupply> extends ControllerBase {

	@FXML
	protected VBox root;

	@FXML
	private Button deleteButton;

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
	protected void postInit(WindowBase w) throws IOException {
		super.postInit(w);

		this.setFont(this.getRootNode(), GuiSystem.getDefaultFont());
	}

	protected void updateStats() {
		yieldDisplay.getChildren().clear();
		GuiUtil.addIconCount(yieldDisplay, supply.getResource(), supply.getYield());
		//TODO update color/warn styles in recipe matrix
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
		Node n = this.getTopBarContent(GuiUtil.createSpacedHBox(res.getResource().createImageView(), res.getIcon().createImageView(), this.getTopBarRightContent()));
		if (n != null)
			topBar.getChildren().add(n);
		this.updateStats();
		deleteButton.setOnAction(e -> {
			f.removeExternalSupply(res);
		});
		this.onSetSupply(f, res);
	}

}

