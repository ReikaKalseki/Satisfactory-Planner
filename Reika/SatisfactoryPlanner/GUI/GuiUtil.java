package Reika.SatisfactoryPlanner.GUI;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

import org.controlsfx.control.SearchableComboBox;

import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Resource;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.GuiInstance;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.MainWindow;
import Reika.SatisfactoryPlanner.Util.Errorable;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.util.Duration;

public class GuiUtil {

	public static void doWithDelay(int millis, Runnable call) {
		new Timeline(new KeyFrame(Duration.millis(millis), e -> {call.run();})).play();
	}

	public static void setTooltip(Node n, String msg) {
		setTooltip(n, msg, 250);
	}

	public static void setTooltip(Node n, String msg, int delay) {
		Tooltip t = new Tooltip(msg);
		t.setStyle("-fx-font-size: 12; "+GuiSystem.getFontStyle());
		t.setShowDelay(Duration.millis(delay));
		if (n instanceof Control)
			((Control)n).setTooltip(t);
		else
			Tooltip.install(n, t);
	}

	public static HBox createSpacedHBox(Node left, Node ctr, Node right) {
		HBox p = new HBox();
		p.setAlignment(Pos.CENTER);
		p.getChildren().add(left);
		addSpacer(p);
		p.getChildren().add(ctr);
		if (right != null) {
			addSpacer(p);
			p.getChildren().add(right);
		}
		return p;
	}

	public static VBox createSpacedVBox(Node left, Node ctr, Node right) {
		VBox p = new VBox();
		p.setAlignment(Pos.CENTER);
		p.getChildren().add(left);
		addSpacer(p);
		if (right != null) {
			p.getChildren().add(ctr);
			addSpacer(p);
			p.getChildren().add(right);
		}
		return p;
	}

	public static void addSpacer(HBox p) {
		Region add = new HBox();
		add.setMaxWidth(Double.POSITIVE_INFINITY);
		p.getChildren().add(add);
		HBox.setHgrow(add, Priority.ALWAYS);
	}

	public static void addSpacer(VBox p) {
		Region add = new HBox();
		add.setMaxHeight(Double.POSITIVE_INFINITY);
		p.getChildren().add(add);
		VBox.setVgrow(add, Priority.ALWAYS);
	}

	public static double getWidth(String s, Font f) {
		Text text = new Text(s);
		text.setFont(f);
		return text.getLayoutBounds().getWidth();
	}

	public static double getWidth(Labeled node) {
		return getWidth(node.getText(), node.getFont());
	}

	public static void sizeToContent(Labeled node) {
		node.setMinWidth(getWidth(node));
	}

	public static ColumnConstraints addColumnToGridPane(GridPane gp, int at) {
		ColumnConstraints rc = new ColumnConstraints();
		gp.getColumnConstraints().add(at, rc);
		for (Node n : gp.getChildren()) {
			int idx = gp.getColumnIndex(n);
			if (idx >= at) {
				gp.setColumnIndex(n, idx+1);
			}
		}
		return rc;
	}

	public static RowConstraints addRowToGridPane(GridPane gp, int at) {
		RowConstraints rc = new RowConstraints();
		gp.getRowConstraints().add(at, rc);
		for (Node n : gp.getChildren()) {
			int idx = gp.getRowIndex(n);
			if (idx >= at) {
				gp.setRowIndex(n, idx+1);
			}
		}
		return rc;
	}

	public static void setupCounter(Spinner<Integer> spinner, int min, int max, int init, boolean allowEdit) {
		spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, init));
		spinner.setEditable(allowEdit);

		int maxChars = 1+(int)Math.log10(max);
		int w = 56+8*maxChars;
		spinner.setPrefWidth(w);
		spinner.setMinWidth(Region.USE_PREF_SIZE);
		spinner.setMaxWidth(Region.USE_PREF_SIZE);

		if (allowEdit) {
			TextField txt = spinner.getEditor();
			txt.textProperty().addListener((val, old, nnew) -> {
				nnew = nnew.replaceAll("[^\\d]", "");
				if (!nnew.isEmpty() && Integer.parseInt(nnew) > max)
					nnew = String.valueOf(max);
				//if (nnew.length() > maxChars)
				//	txt.setText(nnew.substring(0, maxChars));
				txt.setText(nnew);
			});
		}
		spinner.getValueFactory().setValue(init);
	}

	public static void setupCounter(Spinner<Double> spinner, double min, double max, double init, boolean allowEdit) {
		spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(min, max, init));
		spinner.setEditable(allowEdit);

		int maxChars = 1+(int)Math.log10(max);
		int w = 56+8*maxChars+4;
		spinner.setPrefWidth(w);
		spinner.setMinWidth(Region.USE_PREF_SIZE);
		spinner.setMaxWidth(Region.USE_PREF_SIZE);

		if (allowEdit) {
			TextField txt = spinner.getEditor();
			txt.textProperty().addListener((val, old, nnew) -> {
				nnew = nnew.replaceAll("[^\\d\\.]", "");
				if (!nnew.isEmpty() && Double.parseDouble(nnew) > max)
					nnew = String.format("%.3f", max);
				//if (nnew.length() > maxChars)
				//	txt.setText(nnew.substring(0, maxChars));
				txt.setText(nnew);
			});
		}
		spinner.getValueFactory().setValue(init);
	}

	public static void setButtonEvent(ButtonBase b, Errorable e) {
		b.setOnAction(ev -> {tryWithErrorHandling(e);});
	}

	public static void setMenuEvent(MenuItem b, Errorable e) {
		b.setOnAction(ev -> {tryWithErrorHandling(e);});
	}

	public static void tryWithErrorHandling(Errorable e) {
		try {
			e.run();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			showException(ex);
		}
	}

	public static void raiseUserErrorDialog(String title, String text) {
		raiseDialog(AlertType.INFORMATION, title, text, ButtonType.OK);
	}

	public static boolean getConfirmation(String text) {
		return raiseDialog(AlertType.CONFIRMATION, "Confirm Action", text, ButtonType.YES, ButtonType.NO) == ButtonType.YES;
	}

	public static void doWithConfirmation(String text, Errorable e) {
		if (getConfirmation(text)) {
			tryWithErrorHandling(e);
		}
	}

	public static ButtonType raiseDialog(AlertType type, String title, String text, ButtonType... buttons) {
		return raiseDialog(type, title, text, null, buttons);
	}

	public static ButtonType raiseDialog(AlertType type, String title, String text, Consumer<Alert> modifier, ButtonType... buttons) {
		Alert a = new Alert(type, text, buttons);
		a.setTitle(title);
		MainWindow main = GuiSystem.MainWindow.getGUI();
		if (main != null)
			a.initOwner(main.window);
		a.initModality(Modality.APPLICATION_MODAL);
		if (modifier != null)
			modifier.accept(a);
		Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
		a.getDialogPane().setMaxHeight(bounds.getHeight()*0.8);
		a.setX((bounds.getWidth()-a.getWidth())/2);
		a.setX((bounds.getHeight()-a.getHeight())/2);
		Optional<ButtonType> b = a.showAndWait();
		return b.isPresent() ? b.get() : null;
	}

	public static void showException(Throwable t) {
		showException(t, null);
	}

	public static void showException(Throwable t, String msg) {
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		raiseDialog(AlertType.ERROR, "Error", "", a -> {
			int w = 1000;
			TextArea area = new TextArea(sw.toString());
			area.setWrapText(false);
			area.setEditable(false);
			area.setPrefWidth(w);
			area.setMinWidth(w);
			area.setMaxWidth(w);
			area.setPrefHeight(600);
			a.getDialogPane().setContent(area);
			if (msg != null) {
				a.getDialogPane().setHeaderText(msg);
			}
		}, ButtonType.OK);
	}

	public static <E> void setupAddSelector(SearchableComboBox<E> sb, Consumer<E> onSelect, boolean clearOnSelect) {
		sb.getSelectionModel().selectedItemProperty().addListener((val, old, nnew) -> {
			if (nnew != null)
				Platform.runLater(() -> { //need to delay since this often updates the selection and contents, which cannot be done inside a selection change
					onSelect.accept(nnew);
					if (clearOnSelect)
						sb.getSelectionModel().clearSelection();
				});
		});
	}

	public static <E> void setupAddSelector(SearchableComboBox<E> sb, SearchableSelector<E> sel) {
		setupAddSelector(sb, (Consumer<E>)sel, sel.clearOnSelect());
		sb.setButtonCell(sel.createListCell("Click to "+sel.getActionName()+" "+sel.getEntryTypeName()+"...", true));
		sb.setCellFactory(c -> sel.createListCell("", false));
	}

	public static interface SearchableSelector<E> extends Consumer<E> {

		public DecoratedListCell<E> createListCell(String text, boolean button);
		public boolean clearOnSelect();
		public String getEntryTypeName();
		public String getActionName();

	}

	public static String formatProductionDecimal(float amt) {
		DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		df.setMaximumFractionDigits(4);
		return df.format(amt);
	}

	public static final GuiInstance<ItemRateController> createItemView(Consumable c, float baseAmount, Pane container) {
		return createItemView(c, baseAmount, inner -> container.getChildren().add(inner));
	}

	public static final GuiInstance<ItemRateController> createItemView(Consumable c, float baseAmount, TabPane container) {
		return createItemView(c, baseAmount, inner -> {Tab t = new Tab(); t.setContent(inner); container.getTabs().add(t);});
	}

	public static final GuiInstance<ItemRateController> createItemView(Consumable c, float baseAmount, Tab container) {
		return createItemView(c, baseAmount, inner -> {container.setContent(inner);});
	}

	public static final GuiInstance<ItemRateController> createItemView(Consumable c, float baseAmount, GridPane container, int col, int row) {
		return createItemView(c, baseAmount, inner -> container.add(inner, col, row));
	}

	public static GuiInstance<ItemRateController> createItemView(Consumable c, float baseAmount, Consumer<Parent> acceptor) {
		ItemRateController view = new ItemRateController(c, baseAmount);
		acceptor.accept(view.getRootNode());
		return new GuiInstance<ItemRateController>(view.getRootNode(), view);
	}

	public static final GuiInstance<ItemCountController> addIconCount(Resource c, float amt, Pane container) {
		return addIconCount(c, amt, inner -> container.getChildren().add(inner));
	}

	public static final GuiInstance<ItemCountController> addIconCount(Resource c, float amt, TabPane container) {
		return addIconCount(c, amt, inner -> {Tab t = new Tab(); t.setContent(inner); container.getTabs().add(t);});
	}

	public static final GuiInstance<ItemCountController> addIconCount(Resource c, float amt, Tab container) {
		return addIconCount(c, amt, inner -> {container.setContent(inner);});
	}

	public static final GuiInstance<ItemCountController> addIconCount(Resource c, float amt, GridPane container, int col, int row) {
		return addIconCount(c, amt, inner -> container.add(inner, col, row));
	}

	public static GuiInstance<ItemCountController> addIconCount(Resource r, float amt, Consumer<Parent> acceptor) {
		ItemCountController c = new ItemCountController(r, amt);
		acceptor.accept(c.getRootNode());
		return new GuiInstance<ItemCountController>(c.getRootNode(), c);
	}

}
