package Reika.SatisfactoryPlanner.Util;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
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
		t.setStyle("-fx-font-size: 12");
		t.setShowDelay(Duration.millis(delay));
		if (n instanceof Control)
			((Control)n).setTooltip(t);
		else
			Tooltip.install(n, t);
	}

}
