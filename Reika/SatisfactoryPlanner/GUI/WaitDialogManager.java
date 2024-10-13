package Reika.SatisfactoryPlanner.GUI;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.nativejavafx.taskbar.TaskbarProgressbar;
import com.nativejavafx.taskbar.TaskbarProgressbarFactory;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Util.Logging;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class WaitDialogManager {

	public static final WaitDialogManager instance = new WaitDialogManager();

	private final ConcurrentHashMap<UUID, WaitTask> currentTasks = new ConcurrentHashMap(4);
	private UUID enclosingTask;

	private Stage dialog;
	private ProgressBar dialogBar;
	private Label taskList;
	private TaskbarProgressbar taskbarLink;

	private WaitDialogManager() {

	}

	public UUID registerTask(String desc) {
		UUID uid = UUID.randomUUID();
		while (currentTasks.containsKey(uid)) //reroll if conflict
			uid = UUID.randomUUID();

		currentTasks.put(uid, new WaitTask(desc));

		if (enclosingTask == null)
			enclosingTask = uid;

		Logging.instance.log("Adding queued task '"+desc+"' "+uid+" to wait UI, task list = "+currentTasks);

		if (dialog == null)
			this.showUI();
		if (taskList != null && dialog != null && dialog.isShowing())
			GuiUtil.runOnJFXThread(() -> taskList.setText(this.computeTaskText()));

		return uid;
	}

	public void completeTask(UUID id) {
		this.setTaskProgress(id, 100);
		currentTasks.remove(id);
		Logging.instance.log("Removing queued task "+id+" from wait UI, task list = "+currentTasks);
		if (currentTasks.isEmpty()) {
			this.closeUI();
		}
		else {
			taskList.setText(this.computeTaskText());
		}
	}

	private String computeTaskText() {
		StringBuilder sb = new StringBuilder();
		sb.append("The following operations are underway:\n");
		for (WaitTask s : currentTasks.values()) {
			sb.append("\n");
			sb.append(s.description);
		}
		return sb.toString();
	}

	private void showUI() {
		GuiUtil.runOnJFXThread(() -> {
			if (currentTasks.isEmpty()) {
				Logging.instance.log("All tasks completed before wait UI shown. Skipping.");
				return;
			}

			Logging.instance.log("Loading wait UI");
			VBox box = new VBox();
			box.setSpacing(12);
			box.setPadding(new Insets(8, 8, 8, 8));
			taskList = new Label(this.computeTaskText());
			taskList.setFont(Font.font(GuiSystem.getDefaultFont().getFamily(), 12));
			taskList.setWrapText(true);
			box.getChildren().add(taskList);
			dialogBar = new ProgressBar();
			dialogBar.setMinHeight(32);
			dialogBar.setProgress(0);
			box.getChildren().add(dialogBar);
			for (Node n : box.getChildren()) {
				if (n instanceof Region) {
					Region r = (Region)n;
					box.setVgrow(r, Priority.ALWAYS);
					r.setMaxWidth(Double.MAX_VALUE);
				}
			}

			dialog = new Stage();
			dialog.setTitle("Loading");
			dialog.setScene(new Scene(box, 500, 192));
			dialog.getScene().getStylesheets().add(Main.class.getResource("Resources/CSS/style.css").toString());
			box.layout();
			taskbarLink = TaskbarProgressbarFactory.getTaskbarProgressbar(dialog);
			if (taskbarLink == null)
				Logging.instance.log("Could not set up Win7+ taskbar progress link");
			dialog.show();
			dialog.setAlwaysOnTop(true);
			Logging.instance.log("Wait UI shown");
		});
	}

	private void closeUI() {
		GuiUtil.runOnJFXThread(() -> {
			if (dialog == null) {
				Logging.instance.log("Wait UI already closed. Skipping.");
				return;
			}

			Logging.instance.log("Closing wait UI");
			PauseTransition timer = new PauseTransition(Duration.millis(50)); //let screen stick for just long enough to show more progress
			timer.setOnFinished(e -> {
				dialogBar = null;
				if (taskbarLink != null)
					taskbarLink.stopProgress();
				dialog.close();
				dialog = null;
				enclosingTask = null;
			});
			timer.play();
		});
	}

	public void setTaskProgress(UUID id, double pct) {
		if (id == null || !currentTasks.containsKey(id))
			return;
		WaitTask w = currentTasks.get(id);
		w.percentageComplete = pct;
		Logging.instance.log("Stepping task "+w.description+" to "+pct+"%");
		if (id.equals(enclosingTask)) {
			GuiUtil.runOnJFXThread(() -> {
				dialogBar.setProgress(pct/100D);
				//TaskbarProgressbar.showCustomProgress(GuiSystem.getMainStage(), pct/100D, TaskbarProgressbar.Type.NORMAL);
				TaskbarProgressbar.showCustomProgress(dialog, pct/100D, TaskbarProgressbar.Type.NORMAL);
			});
		}
	}

	private class WaitTask {

		private final String description;

		private double percentageComplete;

		private WaitTask(String desc) {
			description = desc;
		}

		@Override
		public String toString() {
			return description;
		}

	}

}
