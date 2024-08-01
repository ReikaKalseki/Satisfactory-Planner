package Reika.SatisfactoryPlanner.GUI;

import java.util.function.Consumer;

import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.FontModifier;
import Reika.SatisfactoryPlanner.Util.ColorUtil;

import fxexpansions.ControllerBase;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.TextAlignment;

public class ItemRateController extends ControllerBase {

	private final HBox mainPanel = new HBox();

	private final Label amount = new Label();

	private final Label minLabel = new Label();

	private final VBox countContainer = new VBox();

	private final Line divider = new Line();

	private final ImageView icon = new ImageView();

	public final Consumable item;

	private float baseAmount;

	private String minimumWidth = "min";

	public ItemRateController(Consumable c, float amt) {
		item = c;
		baseAmount = amt;

		mainPanel.setSpacing(4);
		mainPanel.setMaxHeight(Double.MAX_VALUE);
		mainPanel.setMaxWidth(Double.MAX_VALUE);

		icon.setFitHeight(32);
		icon.setFitWidth(32);
		icon.setSmooth(true);
		countContainer.setAlignment(Pos.CENTER);
		amount.setPadding(new Insets(-2, 0, -3, 0));
		amount.setAlignment(Pos.CENTER);
		amount.setTextAlignment(TextAlignment.CENTER);
		minLabel.setPadding(new Insets(-4, 0, -2, 0));
		minLabel.setAlignment(Pos.CENTER);
		minLabel.setTextAlignment(TextAlignment.CENTER);
		minLabel.setText("min");
		divider.setStroke(UIConstants.FADE_COLOR);
		divider.setStrokeWidth(1.5);
		divider.setStrokeType(StrokeType.CENTERED);
		divider.setStrokeLineCap(StrokeLineCap.ROUND);

		mainPanel.getChildren().add(icon);
		mainPanel.getChildren().add(countContainer);
		countContainer.getChildren().add(amount);
		countContainer.getChildren().add(divider);
		countContainer.getChildren().add(minLabel);

		icon.setImage(c.createIcon());
		this.setAmountText(amt);
		GuiUtil.setTooltip(icon, c.displayName);

		countContainer.minWidthProperty().bind(amount.minWidthProperty().add(2));

		divider.endXProperty().bind(countContainer.widthProperty().subtract(divider.strokeWidthProperty()));
		this.setTextStyle(GuiSystem.getFontStyle(FontModifier.SEMIBOLD));

		mainPanel.minWidthProperty().bind(countContainer.widthProperty().add(mainPanel.spacingProperty()).add(icon.fitWidthProperty()));
		GuiUtil.sizeToContent(minLabel);
	}

	public void setAmount(float amt) {
		baseAmount = amt;
		this.setAmountText(amt);
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
		amount.setMinWidth(GuiUtil.getWidth(minimumWidth, GuiSystem.getFont(FontModifier.BOLD)));
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

		private final Consumer<ItemRateController> applyStyles;

		private WarningState(Consumer<ItemRateController> style) {
			applyStyles = style;
		}
	}

	@Override
	public Parent getRootNode() {
		return mainPanel;
	}

}

