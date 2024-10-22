package Reika.SatisfactoryPlanner.GUI;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.controlsfx.control.SearchableComboBox;

import com.nativejavafx.taskbar.TaskbarProgressbar;

import Reika.SatisfactoryPlanner.ConfirmationOptions;
import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.NamedIcon;
import Reika.SatisfactoryPlanner.Data.Objects.Consumable;
import Reika.SatisfactoryPlanner.Data.Objects.Resource;
import Reika.SatisfactoryPlanner.GUI.Components.ItemCountController;
import Reika.SatisfactoryPlanner.GUI.Components.ItemRateController;
import Reika.SatisfactoryPlanner.GUI.Components.ListCells.DecoratedListCell;
import Reika.SatisfactoryPlanner.Util.ColorUtil;
import Reika.SatisfactoryPlanner.Util.Errorable;
import Reika.SatisfactoryPlanner.Util.Errorable.ErrorableWithArgument;
import Reika.SatisfactoryPlanner.Util.JavaUtil;
import Reika.SatisfactoryPlanner.Util.Logging;

import fxexpansions.ControllerBase;
import fxexpansions.ExpandingTilePane;
import fxexpansions.GuiInstance;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.PopupWindow.AnchorLocation;
import javafx.stage.Screen;
import javafx.util.Duration;

public class GuiUtil {

	public static void doWithDelay(int millis, Runnable call) {
		new Timeline(new KeyFrame(Duration.millis(millis), e -> {call.run();})).play();
	}

	public static Tooltip setTooltip(Node n, String msg) {
		return setTooltip(n, msg, 250);
	}

	public static Tooltip setTooltip(Node n, String msg, int delay) {
		Tooltip t = new Tooltip(msg);
		t.setStyle("-fx-font-size: 12; "+GuiSystem.getFontStyle());
		t.setShowDelay(Duration.millis(delay));
		if (n instanceof Control) {
			((Control)n).setTooltip(t);
		}
		else {
			Tooltip.install(n, t);
			t.getProperties().put("tooltip", t);
		}
		return t;
	}

	public static HBox createSpacedHBox(Node left, Node ctr, Node right) {
		HBox p = new HBox();
		p.setMaxWidth(Double.MAX_VALUE);
		p.setMaxHeight(Double.MAX_VALUE);
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
		p.setMaxWidth(Double.MAX_VALUE);
		p.setMaxHeight(Double.MAX_VALUE);
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

	public static String splitToWidth(String s, double maxWidth, String regex, Font f) {
		double currentWidth = 0;
		String[] parts = s.split(regex);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			double w = getWidth(parts[i], f);
			if (currentWidth+w > maxWidth) {
				sb.append("\n");
				currentWidth = 0;
			}
			sb.append(parts[i]);
			currentWidth += w;
		}
		return sb.toString();
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

	public static void setWidth(ColumnConstraints r, double width) {
		r.setPrefWidth(width);
		r.setMinWidth(Region.USE_PREF_SIZE);
		r.setMaxWidth(Region.USE_PREF_SIZE);
		r.setHgrow(Priority.NEVER);
	}

	public static void setHeight(RowConstraints r, double height) {
		r.setPrefHeight(height);
		r.setMinHeight(Region.USE_PREF_SIZE);
		r.setMaxHeight(Region.USE_PREF_SIZE);
		r.setVgrow(Priority.NEVER);
	}

	public static void setWidth(Region r, double width) {
		r.setPrefWidth(width);
		r.setMinWidth(Region.USE_PREF_SIZE);
		r.setMaxWidth(Region.USE_PREF_SIZE);
	}

	public static void setHeight(Region r, double height) {
		r.setPrefHeight(height);
		r.setMinHeight(Region.USE_PREF_SIZE);
		r.setMaxHeight(Region.USE_PREF_SIZE);
	}

	public static void setupCounter(Spinner<Integer> spinner, int min, int max, int init, boolean allowEdit) {
		SpinnerValueFactory.IntegerSpinnerValueFactory vf = new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, init);
		spinner.setValueFactory(vf);
		spinner.setEditable(allowEdit);

		int maxChars = 1+(int)Math.log10(max);
		int w = 56+8*maxChars;
		spinner.setPrefWidth(w);
		spinner.setMinWidth(Region.USE_PREF_SIZE);
		if (spinner.getMaxWidth() < 99999)
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
		vf.setValue(init);

	}

	public static void setupCounter(Spinner<Double> spinner, double min, double max, double init, boolean allowEdit) {
		SpinnerValueFactory.DoubleSpinnerValueFactory vf = new SpinnerValueFactory.DoubleSpinnerValueFactory(min, max, init);
		spinner.setValueFactory(vf);
		spinner.setEditable(allowEdit);

		int maxChars = 1+(int)Math.log10(max);
		int w = 56+8*maxChars+4;
		spinner.setPrefWidth(w);
		spinner.setMinWidth(Region.USE_PREF_SIZE);
		if (spinner.getMaxWidth() < 99999)
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
		vf.setValue(init);
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
			Logging.instance.log(ex);
			showException(ex);
		}
	}

	public static void raiseUserErrorDialog(String title, String text) {
		raiseDialog(AlertType.ERROR, title, text, ButtonType.OK);
	}

	public static boolean getConfirmation(String text) {
		return raiseDialog(AlertType.CONFIRMATION, "Confirm Action", text, ButtonType.YES, ButtonType.NO) == ButtonType.YES;
	}

	public static void doWithConfirmation(String text, Errorable e) {
		if (getConfirmation(text)) {
			tryWithErrorHandling(e);
		}
	}

	public static void doWithToggleableConfirmation(ConfirmationOptions key, Errorable e, Object... args) {
		if (getToggleableConfirmation(key, args)) {
			tryWithErrorHandling(e);
		}
	}

	public static boolean getToggleableConfirmation(ConfirmationOptions key, Object... args) {
		return !key.isEnabled() || getConfirmation(key.getMessage(args));
	}

	public static ButtonType raiseDialog(AlertType type, String title, String text, ButtonType... buttons) {
		return raiseDialog(type, title, text, null, 400, buttons);
	}

	public static ButtonType raiseDialog(AlertType type, String title, String text, Consumer<Alert> modifier, int width, ButtonType... buttons) {
		Alert a = createDialog(type, title, text, width, buttons);
		if (modifier != null)
			modifier.accept(a);
		boolean flag = false;
		if (type == AlertType.ERROR && TaskbarProgressbar.isSupported()) {
			flag = true;
			TaskbarProgressbar.showFullErrorProgress(GuiSystem.getMainStage());
		}
		Optional<ButtonType> b = a.showAndWait();
		if (flag)
			TaskbarProgressbar.stopProgress(GuiSystem.getMainStage());
		return b.isPresent() ? b.get() : null;
	}

	private static Alert createDialog(AlertType type, String title, String text, int width, ButtonType... buttons) {
		Alert a = new Alert(type, text, buttons);
		Text tt = new Text(text);
		ScrollPane panel = new ScrollPane();
		panel.setContent(tt);
		tt.setWrappingWidth(width);
		panel.setPadding(new Insets(8, 8, 8, 8));
		a.getDialogPane().setContent(panel);
		a.setTitle(title);
		GuiInstance<MainGuiController> main = GuiSystem.getMainGUI();
		if (main != null)
			a.initOwner(main.controller.getWindow());
		a.initModality(Modality.APPLICATION_MODAL);
		Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
		a.getDialogPane().setMaxHeight(bounds.getHeight()*0.8);
		a.setX((bounds.getWidth()-a.getWidth())/2);
		a.setX((bounds.getHeight()-a.getHeight())/2);
		a.getDialogPane().getScene().getStylesheets().add(Main.class.getResource("Resources/CSS/style.css").toString());
		a.getDialogPane().getStyleClass().add("widget");
		return a;
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
		}, 1000, ButtonType.OK);
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
		/*
		//FIXME temporary workaround to stop space from counting as a select
		sb.skinProperty().addListener((val, old, nnew) -> {
			if (nnew instanceof SkinBase) {
				((ComboBox<E>)((SkinBase<ComboBox<E>>)nnew).getChildren().get(0)).skinProperty().addListener((obs, oldVal, newVal) -> {
					if (newVal instanceof ComboBoxListViewSkin) {
						ComboBoxListViewSkin cblwSkin = (ComboBoxListViewSkin)newVal;
						cblwSkin.getPopupContent().setOnKeyPressed(e -> {if (e.getCode() == KeyCode.SPACE) {Logging.instance.log("Space"); cblwSkin.getSkinnable().requestFocus(); e.consume();}});
					}
				});
			}
		});
		 */
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

	public static final GuiInstance<ItemRateController> createItemView(Consumable c, float baseAmount, ExpandingTilePane container) {
		return createItemView(c, baseAmount, true, inner -> container.addEntry(inner));
	}

	public static final GuiInstance<ItemRateController> createItemView(Consumable c, float baseAmount, Pane container) {
		return createItemView(c, baseAmount, false, inner -> container.getChildren().add(inner.rootNode));
	}

	public static final GuiInstance<ItemRateController> createItemView(Consumable c, float baseAmount, TabPane container) {
		return createItemView(c, baseAmount, false, inner -> {Tab t = new Tab(); t.setContent(inner.rootNode); container.getTabs().add(t);});
	}

	public static final GuiInstance<ItemRateController> createItemView(Consumable c, float baseAmount, Tab container) {
		return createItemView(c, baseAmount, false, inner -> {container.setContent(inner.rootNode);});
	}

	public static final GuiInstance<ItemRateController> createItemView(Consumable c, float baseAmount, GridPane container, int col, int row) {
		return createItemView(c, baseAmount, false, inner -> container.add(inner.rootNode, col, row));
	}

	public static GuiInstance<ItemRateController> createItemView(Consumable c, float baseAmount, boolean table, Consumer<GuiInstance<ItemRateController>> acceptor) {
		ItemRateController view = new ItemRateController(c, baseAmount, table);
		GuiInstance<ItemRateController> gui = new GuiInstance<ItemRateController>(view.getRootNode(), view);
		acceptor.accept(gui);
		return gui;
	}

	public static final GuiInstance<ItemCountController> addIconCount(Resource c, float amt, int digits, boolean compress, ExpandingTilePane container) {
		return addIconCount(c, amt, digits, compress, inner -> container.addEntry(inner));
	}

	public static final GuiInstance<ItemCountController> addIconCount(Resource c, float amt, int digits, boolean compress, Pane container) {
		return addIconCount(c, amt, digits, compress, inner -> container.getChildren().add(inner.rootNode));
	}

	public static final GuiInstance<ItemCountController> addIconCount(Resource c, float amt, int digits, boolean compress, TabPane container) {
		return addIconCount(c, amt, digits, compress, inner -> {Tab t = new Tab(); t.setContent(inner.rootNode); container.getTabs().add(t);});
	}

	public static final GuiInstance<ItemCountController> addIconCount(Resource c, float amt, int digits, boolean compress, Tab container) {
		return addIconCount(c, amt, digits, compress, inner -> {container.setContent(inner.rootNode);});
	}

	public static final GuiInstance<ItemCountController> addIconCount(Resource c, float amt, int digits, boolean compress, GridPane container, int col, int row) {
		return addIconCount(c, amt, digits, compress, inner -> container.add(inner.rootNode, col, row));
	}

	public static GuiInstance<ItemCountController> addIconCount(Resource r, float amt, int digits, boolean compress, Consumer<GuiInstance<ItemCountController>> acceptor) {
		ItemCountController c = new ItemCountController(r, amt, compress);
		GuiInstance<ItemCountController> gui = new GuiInstance<ItemCountController>(c.getRootNode(), c);
		gui.controller.setMaxLength(digits);
		acceptor.accept(gui);
		return gui;
	}

	public static void setTitledPaneGraphicRight(TitledPane tp, double extraSpace) {
		Label lb = new Label(tp.getText());
		lb.setFont(tp.getFont());
		HBox box = new HBox();
		box.getStyleClass().add("titled-pane-graphic-title");
		//box.setPadding(new Insets(0.01, 0.01, 0.01, 0.01));
		box.setAlignment(Pos.CENTER);
		box.setMaxWidth(Double.MAX_VALUE);
		box.getChildren().add(lb);
		addSpacer(box);
		box.getChildren().add(tp.getGraphic());
		tp.setText(null);
		tp.setGraphic(box);
		Insets tpad = tp.getPadding();
		Insets pad = tp.getLabelPadding();
		box.setPadding(new Insets(0, 14+extraSpace, 0, (tp.isCollapsible() ? 14 : 4)+extraSpace));
		box.minWidthProperty().bind(tp.widthProperty().subtract(pad.getLeft()).subtract(pad.getRight()).subtract(tpad.getLeft()).subtract(tpad.getRight()));
		box.maxWidthProperty().bind(box.minWidthProperty());
	}

	public static void runOnJFXThread(Errorable r) {
		if (Platform.isFxApplicationThread()) {
			tryWithErrorHandling(r);
		}
		else {
			Platform.runLater(() -> tryWithErrorHandling(r));
		}
	}

	public static UUID queueTask(String desc, ErrorableWithArgument<UUID> e) {
		return queueTask(desc, e, null);
	}

	public static UUID queueTask(String desc, ErrorableWithArgument<UUID> e, ErrorableWithArgument<UUID> jfxActionWhenDone) {
		UUID id = GuiSystem.isSplashShowing() ? null : WaitDialogManager.instance.registerTask(desc);
		Logging.instance.log("Queuing long task '"+desc+"' ["+id+"] "+e+" with JFX post-action "+jfxActionWhenDone);
		JavaUtil.queueTask(() -> {
			try {
				e.run(id);
			}
			catch (Exception ex) {
				Logging.instance.log("Task '"+desc+"' ["+id+"] threw exception:");
				Logging.instance.log(ex);
				Platform.runLater(() -> showException(ex));
			}
			Logging.instance.log("Task '"+desc+"' ["+id+"] complete, queuing JFX post-action if any");
			Platform.runLater(() -> {
				if (jfxActionWhenDone != null) {
					try {
						jfxActionWhenDone.run(id);
					}
					catch (Exception ex) {
						Logging.instance.log(ex);
					}
				}
				if (id != null)
					WaitDialogManager.instance.completeTask(id);
			});
		});
		return id;
	}
	/*
	public static void pauseUIUntil(Future f, Errorable whenComplete) {
		AnimationTimer a = new AnimationTimer() {
			@Override
			public void handle(long now) {
				if (f.isDone()) {
					try {
						whenComplete.run();
					}
					catch (Exception e) {
						Logging.instance.log(e);
					}
				}
			}
		};
		a.start();
	}
	 */
	public static void putInto(Node parent, Node n) {
		if (parent instanceof ScrollPane) {
			((ScrollPane)parent).setContent(n);
		}
		else if (parent instanceof TitledPane) {
			((TitledPane)parent).setContent(n);
		}
		else if (parent instanceof Pane) {
			((Pane)parent).getChildren().add(n);
		}
	}

	public static Parent reparent(Node n, Parent p) {
		Parent from = n.getParent();
		//if (from instanceof Tab) {

		//}
		if (from instanceof ScrollPane) {
			((ScrollPane)from).setContent(p);
			putInto(p, n);
		}
		else if (from instanceof TitledPane) {
			((TitledPane)from).setContent(p);
			putInto(p, n);
		}
		else if (from instanceof Pane) {
			Pane pp = (Pane)from;
			Priority pr = null;
			if (pp instanceof VBox)
				pr = ((VBox)pp).getVgrow(n);
			else if (pp instanceof HBox)
				pr = ((HBox)pp).getHgrow(n);
			int idx = pp.getChildren().indexOf(n);
			putInto(p, n);
			pp.getChildren().add(idx, p);
			if (pp instanceof VBox)
				((VBox)pp).setVgrow(n, pr);
			else if (pp instanceof HBox)
				((HBox)pp).setHgrow(n, pr);
		}
		else {
			putInto(p, n); //n might not have a parent, put it into p directly
		}
		return from;
	}

	public static Node unwrapDecorativeWrappers(Node n) {
		if (n instanceof Parent && n.getStyleClass().contains("bolt-wrapper"))
			n = ((Parent)n).getChildrenUnmodifiable().get(0);
		return n;
	}

	public static void initWidgets(ControllerBase c) {
		initWidgets(c.getRootNode());
	}

	public static void initWidgets(Parent root) {
		applyToAllNodes(root, n -> {
			if (n != null) {
				if (n.getStyleClass().contains("panel") && !n.getStyleClass().contains("bolt-wrapped")) {
					applyPanelBolts(n, 3);
				}/*
				else if (n.getStyleClass().contains("dark-pane")) {
					applyPanelBolts(n, 6);
				}*/
			}
		});
	}

	public static StackPane applyPanelBolts(Node n, double inset) {
		StackPane p = new StackPane();
		//if (n instanceof TitledPane)
		//	((TitledPane)n).setPadding(new Insets(4, 4, 4, 4));
		p.getStyleClass().add("bolt-wrapper");
		n.getStyleClass().add("bolt-wrapped");
		p.setMaxWidth(Double.MAX_VALUE);
		p.setMinWidth(Region.USE_COMPUTED_SIZE);
		p.setMaxHeight(Double.MAX_VALUE);
		p.setMinHeight(Region.USE_COMPUTED_SIZE);
		p.managedProperty().bind(n.managedProperty());
		p.visibleProperty().bind(n.visibleProperty());
		reparent(n, p);
		GridPane bolts = new GridPane();
		bolts.setMouseTransparent(true);
		bolts.setMaxWidth(Double.MAX_VALUE);
		bolts.setMaxHeight(Double.MAX_VALUE);
		p.getChildren().add(bolts);
		if (n.getStyleClass().contains("single-bolt-row")) {
			bolts.getRowConstraints().add(new RowConstraints());
			bolts.getColumnConstraints().add(new ColumnConstraints());
			bolts.getColumnConstraints().add(new ColumnConstraints());
			bolts.getColumnConstraints().add(new ColumnConstraints());
			bolts.add(createBoltGraphic(), 0, 0);
			bolts.add(createBoltGraphic(), 2, 0);
			bolts.getRowConstraints().get(0).setVgrow(Priority.ALWAYS);
			inset -= 2;
		}
		else {
			for (int i = 0; i < 3; i++) {
				bolts.getRowConstraints().add(new RowConstraints());
				bolts.getColumnConstraints().add(new ColumnConstraints());
			}
			bolts.add(createBoltGraphic(), 0, 0);
			bolts.add(createBoltGraphic(), 2, 0);
			bolts.add(createBoltGraphic(), 0, 2);
			bolts.add(createBoltGraphic(), 2, 2);
			bolts.getRowConstraints().get(0).setVgrow(Priority.NEVER);
			bolts.getRowConstraints().get(1).setVgrow(Priority.ALWAYS);
			bolts.getRowConstraints().get(2).setVgrow(Priority.NEVER);
		}
		bolts.getColumnConstraints().get(0).setHgrow(Priority.NEVER);
		bolts.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);
		bolts.getColumnConstraints().get(2).setHgrow(Priority.NEVER);
		bolts.setPadding(new Insets(inset, inset, inset, inset));
		return p;
	}

	public static Node createBoltGraphic() {
		StackPane sp = new StackPane();

		Circle c = new Circle();
		c.setStroke(Color.TRANSPARENT);
		c.setFill(new LinearGradient(0.5, 0, 0.5, 1, true, CycleMethod.NO_CYCLE, new Stop(0, ColorUtil.getColor(0xFFBDBDBD)), new Stop(1, ColorUtil.getColor(0xFF767676))));
		c.setRadius(6);

		Circle c2 = new Circle();
		c2.setStroke(Color.TRANSPARENT);
		c2.setFill(new LinearGradient(0.5, 0, 0.5, 1, true, CycleMethod.NO_CYCLE, new Stop(0, ColorUtil.getColor(0xFF4E4E4E)), new Stop(1, ColorUtil.getColor(0xFF646464))));
		c2.setRadius(3);

		sp.getChildren().add(c);
		sp.getChildren().add(c2);
		return sp;
	}

	public static void addPaneNodeAt(Pane p, Node n, double x, double y) {
		p.getChildren().add(n);
		n.setLayoutX(x);
		n.setLayoutY(y);
	}

	public static void applyToAllNodes(Node n, Consumer<Node> call) {
		call.accept(n);
		if (n instanceof TabPane) {
			for (Tab t : ((TabPane)n).getTabs()) {
				applyToAllNodes(t.getContent(), call);
			}
		}
		else if (n instanceof ScrollPane) {
			applyToAllNodes(((ScrollPane)n).getContent(), call);
		}
		else if (n instanceof TitledPane) {
			applyToAllNodes(((TitledPane)n).getContent(), call);
		}
		else if (n instanceof Parent) {
			for (Node n2 : ((Parent)n).getChildrenUnmodifiable()) {
				applyToAllNodes(n2, call);
			}
		}
	}

	public static StackPane createItemDisplay(NamedIcon c, int size, boolean compress) {
		return createItemDisplay(c.createImageView(), c.getDisplayName(), size, compress);
	}

	public static StackPane createItemDisplay(ImageView icon, String tooltip, int size, boolean compress) {
		StackPane sp = new StackPane();
		ImageView bcg = createItemBCG(size+8);
		sp.getChildren().add(bcg);
		if (compress)
			sp.setMargin(bcg, new Insets(-4, -4, -4, -4));
		sp.getChildren().add(icon);
		GuiUtil.setTooltip(icon, tooltip);
		return sp;
	}

	public static ImageView createItemBCG(int size) {
		return new ImageView(new Image(Main.class.getResourceAsStream("Resources/Graphics/Icons/item-background.png"), size, size, true, true));
	}

	public static void addSortedNode(Pane p, GuiInstance g, Comparator<Node> compare) {
		addSortedNode(p, g.rootNode, compare);
	}

	public static void addSortedNode(Pane p, Node n, Comparator<Node> compare) {
		ObservableList<Node> li = p.getChildren();
		for (int i = li.size()-1; i >= 0; i--) {
			if (compare.compare(n, li.get(i)) > 0) { //comes after i, add after i
				li.add(i+1, n);
				return;
			}
		}
		li.add(0, n); //smaller than every entry, add to beginning
	}

	public static void setClipboard(Labeled lb) {
		setClipboard(lb.getText(), lb);
	}

	public static void setClipboard(String text, Node ref) {
		Clipboard clipboard = Clipboard.getSystemClipboard();
		ClipboardContent content = new ClipboardContent();
		content.putString(text);
		clipboard.setContent(content);
		if (ref != null) {
			//Tooltip prev = ref instanceof Control ? ((Control)ref).getTooltip() : (Tooltip)ref.getProperties().get("tooltip");
			Bounds pos = ref.localToScreen(ref.getBoundsInLocal());
			//setTooltip(ref, "'"+text+"' copied to clipboard", 0).show(ref, pos.getCenterX(), pos.getMinY()-32);
			Tooltip popup = new Tooltip("'"+text+"' copied to clipboard");
			popup.setShowDelay(Duration.ZERO);
			//Tooltip.install(ref, popup);
			popup.setAnchorLocation(AnchorLocation.WINDOW_TOP_RIGHT);
			double rootX = pos.getCenterX()-popup.getWidth()/2D;
			double rootY = pos.getMinY();
			popup.show(ref, rootX, rootY);
			int dur = 1500;
			int fadeStart = 750;
			int fadeLen = dur-fadeStart;
			long start = System.nanoTime();
			long end = start+dur*1000000;

			AnimationTimer timer = new AnimationTimer() {
				@Override
				public void handle(long now) {
					long millis = (now-start)/1000000;
					popup.setX(rootX);
					double offset = 0;
					if (millis > fadeStart) {
						double alpha = 1D-((millis-fadeStart)/(double)fadeLen);
						//Logging.instance.log(now +" > "+millis+" > "+alpha);
						popup.setOpacity(Math.max(0, Math.min(1, alpha)));
						offset = 0.000000075*(now-start-fadeStart*1000000);
					}
					popup.setY(rootY-offset);
				}
			};
			doWithDelay(dur, () -> {
				popup.hide();
				timer.stop();
				//Tooltip.uninstall(ref, popup);
			});
			timer.start();
			//if (prev != null)
			//	setTooltip(ref, prev.getText());
		}
	}

}
