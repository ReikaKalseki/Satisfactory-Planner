package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;

import Reika.SatisfactoryPlanner.Main;

import fxexpansions.FXMLControllerBase;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class AboutPageController extends FXMLControllerBase {

	//@FXML
	//private Button contactButton;

	@FXML
	private Button folderButton;

	@FXML
	private Button webButton;

	@Override
	public void init(HostServices services) throws IOException {
		//GuiUtil.setButtonEvent(contactButton, () -> GuiUtil.raiseDialog(AlertType.in, null, null, null));
		GuiUtil.setButtonEvent(folderButton, () -> GuiSystem.getHSVC().showDocument(Main.getRelativeFile("").toURI().toString()));
		GuiUtil.setButtonEvent(webButton, () -> GuiSystem.getHSVC().showDocument("https://reikakalseki.github.io/projects/sfcalc.html"));
	}

	@Override
	protected void postInit(Stage w) throws IOException {
		super.postInit(w);
	}
}

