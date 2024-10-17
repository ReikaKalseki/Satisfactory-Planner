package Reika.SatisfactoryPlanner.GUI.Components;

import org.apache.commons.lang3.StringUtils;

import Reika.SatisfactoryPlanner.Data.Objects.Resource;
import Reika.SatisfactoryPlanner.GUI.GuiSystem;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.FontModifier;
import Reika.SatisfactoryPlanner.GUI.GuiUtil;
import Reika.SatisfactoryPlanner.GUI.UIConstants;
import Reika.SatisfactoryPlanner.Util.ColorUtil;

import fxexpansions.SizedControllerBase;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;


public class ItemCountController extends SizedControllerBase {

	private final HBox root = new HBox();

	private final Label countLabel = new Label();

	private final StackPane itemBox;

	public final Resource item;

	public ItemCountController(Resource c, float amt) {
		item = c;

		root.setSpacing(4);
		root.setAlignment(Pos.CENTER);
		itemBox = GuiUtil.createItemDisplay(c, 32, false);
		root.getChildren().add(itemBox);
		root.getChildren().add(countLabel);
		this.setAmount(amt);
		//countLabel.setStyle("-fx-text-fill: "+ColorUtil.getCSSHex(UIConstants.FICSIT_COLOR)+"; "+GuiSystem.getFontStyle(FontModifier.BOLD));
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
		return 40+GuiUtil.getWidth(countLabel)+root.getSpacing();
	}

	@Override
	public double getHeight() {
		return 40;
	}

	@Override
	public String toString() {
		return item.displayName+" "+countLabel.getText();
	}

	public void setMaxLength(int digits) {
		countLabel.setMinWidth(GuiUtil.getWidth(StringUtils.repeat("0", digits)+".00", GuiSystem.getFont()));
	}

	public void setWarning() {
		countLabel.setStyle("-fx-text-fill: "+ColorUtil.getCSSHex(UIConstants.WARN_COLOR)+"; "+GuiSystem.getFontStyle(FontModifier.BOLD));
	}

}
