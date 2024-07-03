package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.function.Consumer;

import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.FontModifier;
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
		this.setTextStyle(GuiSystem.getFontStyle(FontModifier.SEMIBOLD));
	}

	public void setItem(Consumable c, float amt) {
		baseAmount = amt;
		icon.setImage(c.createIcon());
		this.setAmountText(amt);

		GuiUtil.setTooltip(icon, c.displayName);
	}

	public void setScale(int scale) {
		this.setAmountText(baseAmount*scale);
	}

	private void setAmountText(float amt) {
		DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		df.setMaximumFractionDigits(340); //340 = DecimalFormat.DOUBLE_FRACTION_DIGITS
		amount.setText(df.format(amt));
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
		NONE(v -> {v.setTextStyle(GuiSystem.getFontStyle(FontModifier.SEMIBOLD)); v.mainPanel.setStyle(""); v.setLineStyle(UIConstants.FADE_COLOR, 1.5);}),
		LEFTOVER(v -> {v.setTextStyle("-fx-text-fill: "+ColorUtil.getCSSHex(UIConstants.WARN_COLOR)+"; "+GuiSystem.getFontStyle(FontModifier.BOLD)); v.setLineStyle(UIConstants.WARN_COLOR, 2); v.mainPanel.setStyle("");}),
		INSUFFICIENT(v -> {v.setTextStyle("-fx-text-fill: #fff; "+GuiSystem.getFontStyle(FontModifier.BOLD)); v.setLineStyle(Color.WHITE, 3); v.mainPanel.setStyle("-fx-background-color: "+ColorUtil.getCSSHex(UIConstants.SEVERE_COLOR)+";");});

		private final Consumer<ItemViewController> applyStyles;

		private WarningState(Consumer<ItemViewController> style) {
			applyStyles = style;
		}
	}

}

