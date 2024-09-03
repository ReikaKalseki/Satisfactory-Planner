package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import fxexpansions.FXMLControllerBase;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;


public abstract class RadioTitledPaneSection extends FXMLControllerBase {

	private final HashMap<RadioButton, TitledPane> selections = new HashMap();

	protected final ToggleGroup radioButtons = new ToggleGroup();

	@Override
	protected void postInit(Stage w) throws IOException {
		super.postInit(w);

		radioButtons.selectedToggleProperty().addListener((val, old, nnew) -> this.onToggleSelected((RadioButton)nnew));

		for (Node n : this.getRootNode().getChildrenUnmodifiable()) {
			n = GuiUtil.unwrapDecorativeWrappers(n);
			if (n instanceof TitledPane) {
				TitledPane tp = (TitledPane)n;
				Node g = tp.getGraphic();
				if (g instanceof RadioButton) {
					RadioButton rb = (RadioButton)g;
					rb.setToggleGroup(radioButtons);
					GuiUtil.setTitledPaneGraphicRight(tp, 14+16); //+16 because of the 8 from bolt wrap pad, +24 from title pad
					selections.put(rb, tp);
				}
			}
		}

		this.onToggleSelected(null);
	}

	protected void onToggleSelected(RadioButton rb) {
		for (Entry<RadioButton, TitledPane> e : selections.entrySet()) {
			Node in = e.getValue().getContent();
			if (in != null)
				in.setDisable(rb != e.getKey());
		}
	}

	protected Parent getSectionContainer() {
		return this.getRootNode();
	}

}
