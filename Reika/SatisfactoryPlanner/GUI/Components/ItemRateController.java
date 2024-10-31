package Reika.SatisfactoryPlanner.GUI.Components;

import java.util.function.Consumer;

import Reika.SatisfactoryPlanner.Data.Objects.Consumable;
import Reika.SatisfactoryPlanner.GUI.GuiSystem;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.FontModifier;
import Reika.SatisfactoryPlanner.GUI.GuiUtil;
import Reika.SatisfactoryPlanner.GUI.UIConstants;
import Reika.SatisfactoryPlanner.Util.ColorUtil;

import fxexpansions.SizedControllerBase;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.TextAlignment;

public class ItemRateController extends SizedControllerBase {

	private final HBox mainPanel = new HBox();

	private final Label amount = new Label();

	private final Label minLabel = new Label();

	private final VBox countContainer = new VBox();

	private final Line divider = new Line();

	public final Consumable item;

	private double baseAmount;

	private String minimumWidth = "min";

	private final StackPane itemBox;

	public ItemRateController(Consumable c, double amt, boolean table) {
		item = c;
		baseAmount = amt;

		mainPanel.setSpacing(6);
		mainPanel.setMaxHeight(Double.MAX_VALUE);
		mainPanel.setMaxWidth(Double.MAX_VALUE);
		mainPanel.getStyleClass().add("item-rate-box");

		countContainer.setAlignment(Pos.CENTER);
		amount.setPadding(new Insets(-2, 0, -3, 0));
		amount.setAlignment(Pos.CENTER);
		amount.setTextAlignment(TextAlignment.CENTER);
		//amount.setStyle("-fx-text-fill: "+ColorUtil.getCSSHex(UIConstants.FICSIT_COLOR)+";");
		minLabel.setPadding(new Insets(-4, 0, -2, 0));
		minLabel.setAlignment(Pos.CENTER);
		minLabel.setTextAlignment(TextAlignment.CENTER);
		minLabel.setText("min");
		divider.setStroke(UIConstants.FADE_COLOR);
		divider.setStrokeWidth(1.5);
		divider.setStrokeType(StrokeType.CENTERED);
		divider.setStrokeLineCap(StrokeLineCap.ROUND);

		itemBox = GuiUtil.createItemDisplay(c, 32, !table);
		mainPanel.getChildren().add(itemBox);
		mainPanel.getChildren().add(countContainer);
		countContainer.getChildren().add(amount);
		countContainer.getChildren().add(divider);
		countContainer.getChildren().add(minLabel);

		if (table) {
			mainPanel.setPadding(new Insets(-4, -4, -4, -4));
			mainPanel.setMargin(itemBox, new Insets(0, -4, 0, 0));
		}

		this.setAmountText(amt);

		countContainer.minWidthProperty().bind(amount.minWidthProperty().add(2));

		divider.endXProperty().bind(countContainer.widthProperty().subtract(divider.strokeWidthProperty()));
		this.setTextStyle(GuiSystem.getFontStyle(FontModifier.SEMIBOLD));

		mainPanel.minWidthProperty().bind(countContainer.widthProperty().add(mainPanel.spacingProperty()).add(32));
		mainPanel.setPrefHeight(32);
		mainPanel.setMinHeight(mainPanel.getPrefHeight());
		mainPanel.setMaxHeight(mainPanel.getPrefHeight());
		GuiUtil.sizeToContent(minLabel);
	}

	public ItemRateController setAmount(double amt) {
		baseAmount = amt;
		this.setAmountText(amt);
		return this;
	}

	public ItemRateController setScale(double scale) {
		return this.setAmountText(baseAmount*scale);
	}

	private ItemRateController setAmountText(double amt) {
		String txt = GuiUtil.formatProductionDecimal(amt);
		amount.setText(txt);
		this.setMinWidth(txt);
		return this;
	}

	public ItemRateController setMinWidth(String txt) {
		if (GuiUtil.getWidth(txt, amount.getFont()) > GuiUtil.getWidth(minimumWidth, amount.getFont()))
			minimumWidth = txt;
		//GuiUtil.sizeToContent(amount);
		amount.setMinWidth(GuiUtil.getWidth(minimumWidth, GuiSystem.getFont(FontModifier.BOLD)));
		return this;
	}

	@Override
	public double getHeight() {
		return Math.max(40, mainPanel.getHeight());
	}

	@Override
	public double getWidth() {
		return amount.getMinWidth()+mainPanel.getSpacing()+40;
	}

	public ItemRateController setState(WarningState st) {
		st.applyStyles.accept(this);
		return this;
	}

	private ItemRateController setTextStyle(String style) {
		amount.setStyle(style);
		minLabel.setStyle(style);
		return this;
	}

	private ItemRateController setLineStyle(Color c, double thick) {
		divider.setStroke(c);
		divider.setStrokeWidth(thick);
		return this;
	}

	public static enum WarningState {
		NONE(v -> {v.setTextStyle(/*"-fx-text-fill: "+ColorUtil.getCSSHex(UIConstants.FICSIT_COLOR)+"; "+*/GuiSystem.getFontStyle(FontModifier.SEMIBOLD)); v.mainPanel.setStyle(""); v.setLineStyle(UIConstants.FADE_COLOR, 1.5);}),
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

