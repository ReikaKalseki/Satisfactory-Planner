package Reika.SatisfactoryPlanner.GUI;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Util.Logging;

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

public class WaitDialogManager {

	public static final WaitDialogManager instance = new WaitDialogManager();

	private final ConcurrentHashMap<UUID, String> currentTasks = new ConcurrentHashMap(4);

	private Stage dialog;
	private Label taskList;

	private WaitDialogManager() {

	}

	public UUID registerTask(String desc) {
		UUID uid = UUID.randomUUID();
		while (currentTasks.containsKey(uid)) //reroll if conflict
			uid = UUID.randomUUID();

		currentTasks.put(uid, desc);

		Logging.instance.log("Adding queued task '"+desc+"' "+uid+" to wait UI, task list = "+currentTasks);

		if (dialog == null)
			this.showUI();
		if (taskList != null && dialog != null && dialog.isShowing())
			taskList.setText(this.computeTaskText());

		return uid;
	}

	public void completeTask(UUID id) {
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
		for (String s : currentTasks.values()) {
			sb.append("\n");
			sb.append(s);
		}
		return sb.toString();
	}

	private void showUI() {
		GuiUtil.runOnJFXThread(() -> {
			if (currentTasks.isEmpty()) {
				Logging.instance.log("All tasks completed before wait UI shown. Skipping.");
				return;
			}

			Logging.instance.log("All tasks complete. Loading wait UI");
			VBox box = new VBox();
			box.setSpacing(12);
			box.setPadding(new Insets(8, 8, 8, 8));
			taskList = new Label(this.computeTaskText());
			taskList.setFont(Font.font(GuiSystem.getDefaultFont().getFamily(), 12));
			taskList.setWrapText(true);
			box.getChildren().add(taskList);
			box.getChildren().add(new ProgressBar());
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
			dialog.show();
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
			dialog.close();
			dialog = null;
		});
	}

}
