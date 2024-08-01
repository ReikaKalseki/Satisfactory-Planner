package fxexpansions;

import java.io.IOException;

import javafx.stage.Modality;

public class DialogWindow extends WindowBase {

	protected DialogWindow(String title, String fxmlName, WindowBase from) throws IOException {
		super(title, fxmlName, from.icon);

		window.initModality(Modality.APPLICATION_MODAL);
		window.initOwner(from.window);
	}

	@Override
	public void show() throws IOException {
		window.toFront();
		super.show();
	}

}