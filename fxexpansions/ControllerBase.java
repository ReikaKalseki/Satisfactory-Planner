package fxexpansions;

import Reika.SatisfactoryPlanner.GUI.GuiSystem;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TitledPane;
import javafx.scene.text.Font;

public abstract class ControllerBase {

	public abstract Parent getRootNode();

	public final void setFont(Node n, Font f) {
		if (n instanceof TextInputControl) {
			Font fp = ((TextInputControl)n).getFont();
			double size = fp != null ? fp.getSize() : 12;
			((TextInputControl)n).setFont(f);
		}
		if (n instanceof Labeled) {
			Font fp = ((Labeled)n).getFont();
			double size = fp != null ? fp.getSize() : 12;
			if (n instanceof TitledPane) {
				size = 14;
			}
			((Labeled)n).setFont(f);
			this.setFont(((Labeled)n).getGraphic(), f);
		}
		if (n instanceof ComboBox) {
			n.setStyle(GuiSystem.getFontStyle());
		}
		if (n instanceof ChoiceBox) {
			n.setStyle(GuiSystem.getFontStyle());
		}
		if (n instanceof Parent) {
			for (Node n2 : ((Parent)n).getChildrenUnmodifiable()) {
				this.setFont(n2, f);
			}
		}
	}

}
