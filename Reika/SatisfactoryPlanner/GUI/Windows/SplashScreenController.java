package Reika.SatisfactoryPlanner.GUI.Windows;

import java.io.IOException;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.GUI.GuiUtil;

import fxexpansions.FXMLControllerBase;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

public class SplashScreenController extends FXMLControllerBase {

	@FXML
	private ProgressBar bar;

	@FXML
	private Label loadingText;

	@FXML
	private Label buildTime;

	@Override
	public void init(HostServices services) throws IOException {
		buildTime.setText(Main.buildTime);
	}

	@Override
	protected void postInit(Stage w) throws IOException {
		super.postInit(w);
	}

	public void setProgress(double frac) {
		GuiUtil.runOnJFXThread(() -> {
			bar.setProgress(frac/100D);
			loadingText.setText(String.format("Loading%s(%2.1f)%s...", frac > 0 ? " " : "", frac, "%"));
		});
	}

}

