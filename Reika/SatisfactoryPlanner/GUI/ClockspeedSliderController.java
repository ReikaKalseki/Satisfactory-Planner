package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.function.Consumer;

import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Util.MathUtil;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

public class ClockspeedSliderController extends ControllerBase {

	@FXML
	private Slider slider;

	@FXML
	private Spinner<Integer> spinner;

	@FXML
	private Pane spinnerOverlay;

	@FXML
	private Label percentSign;

	@FXML
	private HBox shardDisplay;

	private Consumer<Integer> callback;

	@Override
	public void init(HostServices services) throws IOException {
		slider.setMin(0);
		slider.setMax(250);
		slider.setValue(100);
		slider.setBlockIncrement(50);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		slider.setMajorTickUnit(50);
		slider.setMinorTickCount(0);
		slider.setSnapToTicks(true);
		StringConverter<Double> cv = new StringConverter<Double>() {
			@Override
			public String toString(Double val) {
				return String.format("%.0f", val);
			}

			@Override
			public Double fromString(String s) {
				return Double.parseDouble(s.replace("%", ""));
			}
		};
		slider.setLabelFormatter(cv);/*
		s.setOnMouseClicked(e -> {
			if (e.getButton() == MouseButton.SECONDARY || (e.getButton() == MouseButton.PRIMARY && e.isShiftDown())) {
				s.setValue(100);
			}
			else if (e.getButton() == MouseButton.MIDDLE) {
				s.setValue(100);
			}
		});*/
		slider.setOnKeyPressed(e -> {
			if (e.isShiftDown()) {
				slider.setBlockIncrement(1);
				slider.setSnapToTicks(false);
				slider.setMinorTickCount(5);
			}
		});
		slider.setOnKeyReleased(e -> {
			boolean snap = !e.isShiftDown();
			double prev = slider.getValue();
			slider.setBlockIncrement(snap ? 50 : 1);
			slider.setSnapToTicks(snap);
			slider.setMinorTickCount(snap ? 0 : 10);
			slider.setValue(prev); //prevent snap enable from causing it to move
		});

		slider.valueProperty().addListener((val, old, nnew) -> {
			int real = (int)Math.round(nnew.doubleValue());
			this.setValue(real, false, true);
			if (Math.abs(nnew.doubleValue()-real) >= 0.1)
				slider.setValue(real);
		});

		slider.setOnMouseReleased(e -> {
			double val = slider.getValue();
			if (slider.isSnapToTicks() && ((int)(val))%50 != 0) {
				slider.setValue(MathUtil.roundToNearestX(50, (int)slider.getValue()));
			}
		});

		spinner.valueProperty().addListener((val, old, nnew) -> {
			if (nnew != null)
				this.setValue(nnew.intValue(), true, false);
		});
		TextField txt = spinner.getEditor();
		txt.textProperty().addListener((val, old, nnew) -> {
			this.setPercentSignPos(nnew, txt);
		});
		GuiUtil.setupCounter(spinner, 0, 250, 100, true);
		spinner.setPrefWidth(Region.USE_COMPUTED_SIZE);
		spinner.setMaxWidth(Double.POSITIVE_INFINITY);
		Platform.runLater(() -> this.setPercentSignPos("100", txt)); //small delay necessary to give it time to size things

		percentSign.layoutYProperty().bind(spinnerOverlay.heightProperty().subtract(percentSign.heightProperty()).divide(2));
		//percentSign.layoutXProperty().bind(slider.valueProperty().divide(10));

		//L to R
		//shardDisplay.layoutXProperty().bind(percentSign.layoutXProperty().add(12));

		//Center
		//shardDisplay.layoutXProperty().bind(spinner.widthProperty().subtract(shardDisplay.widthProperty()).divide(2));

		//Right
		shardDisplay.layoutXProperty().bind(txt.widthProperty().subtract(shardDisplay.widthProperty()).subtract(8));
		shardDisplay.layoutYProperty().bind(percentSign.layoutYProperty());
	}

	private void setPercentSignPos(String nnew, TextField txt) {
		txt.setText(nnew);
		Text text = new Text(nnew);
		text.setFont(txt.getFont());
		//Logging.instance.log(nnew+">"+text.getLayoutBounds().getWidth());
		percentSign.setLayoutX(2+text.getLayoutBounds().getWidth()+txt.getPadding().getLeft());
	}

	@Override
	protected void postInit(WindowBase w) throws IOException {
		super.postInit(w);
	}

	public void setValue(int real) {
		this.setValue(real, true, true);
	}

	private void setValue(int real, boolean updateSlider, boolean updateSpinner) {
		if (updateSlider)
			slider.setValue(real);
		if (updateSpinner)
			spinner.getValueFactory().setValue(real);

		shardDisplay.getChildren().clear();
		if (real > 100) {
			for (int i = 0; i < Math.ceil((real-100)/50D); i++) {
				shardDisplay.getChildren().add(Database.lookupItem("Desc_CrystalShard_C").createImageView(16));
			}
		}

		if (callback != null)
			callback.accept(real);
	}

	public void setCallback(Consumer<Integer> call) {
		callback = call;
	}

}

