package Reika.SatisfactoryPlanner.GUI;

import Reika.SatisfactoryPlanner.Data.Resource;

import fxexpansions.SizedControllerBase;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;


public class ItemCountController extends SizedControllerBase {

	private final HBox root = new HBox();

	private final Label countLabel = new Label();

	public final Resource item;

	public ItemCountController(Resource c, float amt) {
		item = c;

		root.setSpacing(4);
		root.setAlignment(Pos.CENTER);
		root.getChildren().add(c.createImageView());
		root.getChildren().add(countLabel);
		this.setAmount(amt);
	}

	public void setAmount(float amt) {
		countLabel.setText("x"+GuiUtil.formatProductionDecimal(amt));
	}

	@Override
	public Parent getRootNode() {
		return root;
	}

	@Override
	public double getWidth() {
		return 32+GuiUtil.getWidth(countLabel)+root.getSpacing();
	}

	@Override
	public double getHeight() {
		return 32;
	}

	@Override
	public String toString() {
		return item.displayName+" "+countLabel.getText();
	}

}
