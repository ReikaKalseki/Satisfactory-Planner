package Reika.SatisfactoryPlanner.GUI;

import Reika.SatisfactoryPlanner.Data.Resource;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class GuiUtil {

	public static void doWithDelay(int millis, Runnable call) {
		new Timeline(new KeyFrame(Duration.millis(millis), e -> {call.run();})).play();
	}

	public static void setTooltip(Node n, String msg) {
		setTooltip(n, msg, 250);
	}

	public static void setTooltip(Node n, String msg, int delay) {
		Tooltip t = new Tooltip(msg);
		t.setStyle("-fx-font-size: 12; "+GuiSystem.getFontStyle());
		t.setShowDelay(Duration.millis(delay));
		if (n instanceof Control)
			((Control)n).setTooltip(t);
		else
			Tooltip.install(n, t);
	}

	public static HBox createSpacedHBox(Node left, Node ctr, Node right) {
		HBox p = new HBox();
		p.setAlignment(Pos.CENTER);
		p.getChildren().add(left);
		addSpacer(p);
		p.getChildren().add(ctr);
		if (right != null) {
			addSpacer(p);
			p.getChildren().add(right);
		}
		return p;
	}

	public static VBox createSpacedVBox(Node left, Node ctr, Node right) {
		VBox p = new VBox();
		p.setAlignment(Pos.CENTER);
		p.getChildren().add(left);
		addSpacer(p);
		if (right != null) {
			p.getChildren().add(ctr);
			addSpacer(p);
			p.getChildren().add(right);
		}
		return p;
	}

	public static void addSpacer(HBox p) {
		Region add = new HBox();
		add.setMaxWidth(Double.POSITIVE_INFINITY);
		p.getChildren().add(add);
		HBox.setHgrow(add, Priority.ALWAYS);
	}

	public static void addSpacer(VBox p) {
		Region add = new HBox();
		add.setMaxHeight(Double.POSITIVE_INFINITY);
		p.getChildren().add(add);
		VBox.setVgrow(add, Priority.ALWAYS);
	}

	public static void addIconCount(Pane p, Resource r, int amt) {
		HBox n = new HBox();
		n.setSpacing(4);
		n.setAlignment(Pos.CENTER);
		n.getChildren().add(r.createImageView());
		n.getChildren().add(new Label("x"+amt));
		p.getChildren().add(n);
	}

	public static void addIconCount(GridPane p, int col, int row, Resource r, int amt) {
		HBox n = new HBox();
		n.setSpacing(4);
		n.setAlignment(Pos.CENTER);
		n.getChildren().add(r.createImageView());
		n.getChildren().add(new Label("x"+amt));
		p.add(n, col, row);
	}

	public static double getWidth(Labeled node) {
		Text text = new Text(node.getText());
		text.setFont(node.getFont());
		return text.getLayoutBounds().getWidth();
	}

	public static void sizeToContent(Labeled node) {
		node.setMinWidth(getWidth(node));
	}

}
