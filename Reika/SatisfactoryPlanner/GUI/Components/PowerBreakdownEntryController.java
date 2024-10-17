package Reika.SatisfactoryPlanner.GUI.Components;

import Reika.SatisfactoryPlanner.Data.Objects.Buildables.FunctionalBuilding;
import Reika.SatisfactoryPlanner.GUI.GuiSystem;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.FontModifier;
import Reika.SatisfactoryPlanner.GUI.UIConstants;
import Reika.SatisfactoryPlanner.Util.ColorUtil;

import fxexpansions.SizedControllerBase;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;


public class PowerBreakdownEntryController extends SizedControllerBase {

	public final FunctionalBuilding building;
	public final float powerCost;

	private final GridPane root = new GridPane();
	private final ImageView icon;
	private final Label powerLabel = new Label();

	public PowerBreakdownEntryController(FunctionalBuilding b, float pwr) {
		building = b;
		powerCost = pwr;

		root.setHgap(4);
		RowConstraints rc = new RowConstraints();
		rc.setMinHeight(32);
		rc.setMaxHeight(32);
		root.getRowConstraints().add(rc);
		ColumnConstraints cc = new ColumnConstraints();
		cc.setMinWidth(32);
		cc.setMaxWidth(32);
		root.getColumnConstraints().add(cc);
		cc = new ColumnConstraints();
		cc.setMinWidth(96);
		cc.setMaxWidth(96);
		root.getColumnConstraints().add(cc);
		icon = building.createImageView();
		root.add(icon, 0, 0);
		root.add(powerLabel, 1, 0);
		powerLabel.setText(String.format("%s%.3fMW", powerCost > 0 ? "+" : "", powerCost));
		if (powerCost > 0)
			powerLabel.setStyle(GuiSystem.getFontStyle(FontModifier.BOLD)+" -fx-text-fill: "+ColorUtil.getCSSHex(UIConstants.OKAY_COLOR)+";");
		else
			powerLabel.setStyle(GuiSystem.getFontStyle(FontModifier.BOLD)+" -fx-text-fill: "+ColorUtil.getCSSHex(UIConstants.WARN_COLOR)+";");
	}

	@Override
	public Parent getRootNode() {
		return root;
	}

	@Override
	public double getWidth() {
		return 32+96;
	}

	@Override
	public double getHeight() {
		return 32;
	}

	@Override
	public String toString() {
		return "Power breakdown for "+building+": "+powerCost;
	}

}
