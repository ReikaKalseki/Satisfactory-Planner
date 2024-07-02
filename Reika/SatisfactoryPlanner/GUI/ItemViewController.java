package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.function.Consumer;

import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Util.ColorUtil;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class ItemViewController extends ControllerBase {

	@FXML
	private HBox mainPanel;

	@FXML
	private Label amount;

	@FXML
	private Label minLabel;

	@FXML
	private VBox countContainer;

	@FXML
	private Line divider;

	@FXML
	private ImageView icon;

	private float baseAmount;

	@Override
	public void init(HostServices services) throws IOException {
		divider.endXProperty().bind(countContainer.widthProperty().subtract(divider.strokeWidthProperty()));
	}

	@Override
	protected void postInit(WindowBase w) throws IOException {
		super.postInit(w);
	}

	public void setItem(Consumable c, float amt) {
		baseAmount = amt;
		icon.setImage(c.createIcon());
		amount.setText(String.format("%.3f", amt));
		GuiUtil.setTooltip(icon, c.displayName);
	}

	public void setScale(int scale) {
		amount.setText(String.valueOf(baseAmount*scale));
	}

	public void setState(WarningState st) {
		st.applyStyles.accept(this);
	}

	private void setTextStyle(String style) {
		amount.setStyle(style);
		minLabel.setStyle(style);
	}

	private void setLineStyle(Color c, double thick) {
		divider.setStroke(c);
		divider.setStrokeWidth(thick);
	}

	public static enum WarningState {
		NONE(v -> {v.setTextStyle(""); v.mainPanel.setStyle(""); v.setLineStyle(UIConstants.FADE_COLOR, 1.5);}),
		LEFTOVER(v -> {v.setTextStyle("-fx-text-fill: "+ColorUtil.getCSSHex(UIConstants.WARN_COLOR)+"; -fx-font-weight: bold;"); v.setLineStyle(UIConstants.WARN_COLOR, 2); v.mainPanel.setStyle("");}),
		INSUFFICIENT(v -> {v.setTextStyle("-fx-text-fill: #fff; -fx-font-weight: bold;"); v.setLineStyle(Color.WHITE, 3); v.mainPanel.setStyle("-fx-background-color: "+ColorUtil.getCSSHex(UIConstants.SEVERE_COLOR)+";");});

		private final Consumer<ItemViewController> applyStyles;

		private WarningState(Consumer<ItemViewController> style) {
			applyStyles = style;
		}
	}

}

