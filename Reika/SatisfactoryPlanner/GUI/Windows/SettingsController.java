package Reika.SatisfactoryPlanner.GUI.Windows;

import java.io.File;
import java.io.IOException;

import org.controlsfx.control.textfield.CustomTextField;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Setting;
import Reika.SatisfactoryPlanner.Setting.InputInOutputOptions;
import Reika.SatisfactoryPlanner.Setting.SettingRef;
import Reika.SatisfactoryPlanner.GUI.GuiUtil;
import Reika.SatisfactoryPlanner.Util.Logging;
import Reika.SatisfactoryPlanner.Util.StringUtil;

import fxexpansions.FXMLControllerBase;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SettingsController extends FXMLControllerBase {


	@FXML
	private Tab appTab;

	@FXML
	private Button chooseGameDir;

	@FXML
	private Button closeWindow;

	@FXML
	private Button cancelButton;

	@FXML
	private CheckBox allowFractional;

	@FXML
	private CustomTextField gameDirPath;

	@FXML
	private Button revertAll;

	@FXML
	private VBox root;

	@FXML
	private TabPane tabs;

	@FXML
	private Tab visualTab;
	/*
	@FXML
	private RadioButton runLog;

	@FXML
	private RadioButton sharedLog;

	@FXML
	private RadioButton noLog;

	private final ToggleGroup loggingOptions = new ToggleGroup();*/


	@FXML
	private RadioButton excludeInRadio;

	@FXML
	private RadioButton mineOnlyRadio;

	@FXML
	private RadioButton allInRadio;

	private final ToggleGroup includeInputOptions = new ToggleGroup();

	@FXML
	private CheckBox autoRecompute;

	@FXML
	private CheckBox noSignificanceThreshold;

	@FXML
	private Spinner<Double> displayThreshold;

	@Override
	public void init(HostServices services) throws IOException {/*
		noLog.setToggleGroup(loggingOptions);
		sharedLog.setToggleGroup(loggingOptions);
		runLog.setToggleGroup(loggingOptions); //order must match enum

		loggingOptions.selectedToggleProperty().addListener((val, old, nnew) -> Setting.LOG.changeValue(LogOptions.values()[loggingOptions.getToggles().indexOf(nnew)]));
	 */

		excludeInRadio.setToggleGroup(includeInputOptions);
		mineOnlyRadio.setToggleGroup(includeInputOptions);
		allInRadio.setToggleGroup(includeInputOptions); //order must match enum

		includeInputOptions.selectToggle(includeInputOptions.getToggles().get(Setting.INOUT.getCurrentValue().ordinal()));

		includeInputOptions.selectedToggleProperty().addListener((val, old, nnew) -> Setting.INOUT.changeValue(InputInOutputOptions.values()[includeInputOptions.getToggles().indexOf(nnew)]));

		GuiUtil.setupCounter(displayThreshold, 0, 999, Setting.IOTHRESH.getCurrentValue().doubleValue(), true);
		((DoubleSpinnerValueFactory)displayThreshold.getValueFactory()).setAmountToStepBy(0.125);
		displayThreshold.getValueFactory().valueProperty().addListener((val, old, nnew) -> {Setting.IOTHRESH.changeValue(nnew.floatValue());});
		boolean any = Setting.IOTHRESH.getCurrentValue() <= 0;
		noSignificanceThreshold.setSelected(any);
		displayThreshold.setDisable(any);
		noSignificanceThreshold.selectedProperty().addListener((val, old, nnew) -> {displayThreshold.setDisable(nnew); Setting.IOTHRESH.changeValue(nnew ? 0F : displayThreshold.getValue().floatValue());});
	}

	@Override
	protected void postInit(Stage w) throws IOException {
		super.postInit(w);
		this.getWindow().setOnCloseRequest(e -> {
			cancelButton.fire();
		});
		GuiUtil.setButtonEvent(chooseGameDir, () -> {
			File f = this.openDirDialog("Satisfactory Install", Setting.GAMEDIR.getCurrentValue());
			if (f != null) {
				Setting.GAMEDIR.changeValue(f);
				gameDirPath.setText(Setting.GAMEDIR.getString());
			}
		});
		GuiUtil.setButtonEvent(closeWindow, () -> {
			GuiUtil.queueTask("Applying Changes", (id) -> Setting.applyChanges(id), (id) -> this.close());
		});
		GuiUtil.setButtonEvent(cancelButton, () -> {
			if (GuiUtil.getConfirmation("Are you sure you want to discard all changes?"))
				this.close();
		});
		GuiUtil.setButtonEvent(revertAll, () -> {
			if (GuiUtil.getConfirmation("Are you sure you want to revert all settings? This will save over their original values.")) {
				for (SettingRef s : Setting.getSettings())
					s.revert();
				this.setFields();
			}
		});

		gameDirPath.textProperty().addListener((val, old, nnew) -> {
			boolean flag = false;
			if (StringUtil.isValidPath(nnew)) {
				File f = new File(nnew);
				if (f.exists() && f.isDirectory()) {
					Setting.GAMEDIR.changeValue(f);
					flag = true;
				}
			}
			if (flag)
				gameDirPath.setRight(null);
			else
				gameDirPath.setRight(new ImageView(new Image(Main.class.getResourceAsStream("Resources/Graphics/Icons/invalid.png"))));
		});

		allowFractional.selectedProperty().addListener((val, old, nnew) -> {
			Setting.ALLOWDECIMAL.changeValue(nnew);
		});

		GuiUtil.initWidgets(root);

		this.setFields();
	}

	public void setFields() {
		try {
			gameDirPath.setText(Setting.GAMEDIR.getString());
			allowFractional.setSelected(Setting.ALLOWDECIMAL.getCurrentValue());
		}
		catch (Exception e) {
			Logging.instance.log(e);
			GuiUtil.showException(e, "Error setting game directory field");
		}
	}

}
