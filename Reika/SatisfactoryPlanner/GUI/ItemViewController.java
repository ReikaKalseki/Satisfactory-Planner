package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
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

	private Consumable item;

	private float baseAmount;

	private String minimumWidth = "";

	@Override
	public void init(HostServices services) throws IOException {
		divider.endXProperty().bind(countContainer.widthProperty().subtract(divider.strokeWidthProperty()));
	}

	@Override
	protected void postInit(WindowBase w) throws IOException {
		super.postInit(w);
		this.setTextStyle(GuiSystem.getFontStyle(FontModifier.SEMIBOLD));

		mainPanel.minWidthProperty().bind(countContainer.widthProperty().add(mainPanel.spacingProperty()).add(icon.fitWidthProperty()));
		GuiUtil.sizeToContent(minLabel);
	}

	public Consumable getItem() {
		return item;
	}

	public void setItem(Consumable c, float amt) {
		item = c;
		baseAmount = amt;
		icon.setImage(c.createIcon());
		this.setAmountText(amt);

		GuiUtil.setTooltip(icon, c.displayName);
	}

	public void setScale(float scale) {
		this.setAmountText(baseAmount*scale);
	}

	private void setAmountText(float amt) {
		String txt = GuiUtil.formatProductionDecimal(amt);
		amount.setText(txt);

		if (GuiUtil.getWidth(txt, amount.getFont()) > GuiUtil.getWidth(minimumWidth, amount.getFont()))
			minimumWidth = txt;
		//GuiUtil.sizeToContent(amount);
		amount.setMinWidth(GuiUtil.getWidth(minimumWidth, amount.getFont()));
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

