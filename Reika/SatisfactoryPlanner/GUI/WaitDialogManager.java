package Reika.SatisfactoryPlanner.GUI;

import java.util.Collections;
import java.util.Set;
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

	private final Set<UUID> currentTasks = Collections.newSetFromMap(new ConcurrentHashMap(4));

	private Stage dialog;

	private WaitDialogManager() {

	}

	public UUID registerTask() {
		UUID uid = UUID.randomUUID();
		while (!currentTasks.add(uid)) //reroll if conflict
			uid = UUID.randomUUID();

		Logging.instance.log("Adding queued task "+uid+" to wait UI");

		if (dialog == null)
			this.showUI();

		return uid;
	}

	public void completeTask(UUID id) {
		Logging.instance.log("Removing queued task "+id+" from wait UI");
		currentTasks.remove(id);
		if (currentTasks.isEmpty()) {
			this.closeUI();
		}
	}

	private void showUI() {
		GuiUtil.queueIfNecessary(() -> {
			if (currentTasks.isEmpty()) {
				Logging.instance.log("All tasks completed before wait UI shown. Skipping.");
				return;
			}

			Logging.instance.log("Loading wait UI");
			VBox box = new VBox();
			box.setSpacing(12);
			box.setPadding(new Insets(8, 8, 8, 8));
			Label lb = new Label("The selected operation is underway.");
			lb.setFont(Font.font(GuiSystem.getDefaultFont().getFamily(), 14));
			box.getChildren().add(lb);
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
			dialog.setScene(new Scene(box, 300, 65));
			dialog.getScene().getStylesheets().add(Main.class.getResource("Resources/CSS/style.css").toString());
			box.layout();
			dialog.show();
			Logging.instance.log("Wait UI shown");
		});
	}

	private void closeUI() {
		GuiUtil.queueIfNecessary(() -> {
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
