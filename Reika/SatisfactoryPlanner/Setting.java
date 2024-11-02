package Reika.SatisfactoryPlanner;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.math.Fraction;

import Reika.SatisfactoryPlanner.GUI.UIConstants;
import Reika.SatisfactoryPlanner.GUI.WaitDialogManager;
import Reika.SatisfactoryPlanner.Util.ColorUtil;
import Reika.SatisfactoryPlanner.Util.Errorable;

import fxexpansions.FractionHandlingDoubleConverter;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;

public class Setting<S> {

	//public static final Setting<LogOptions> LOG = new Setting<LogOptions>(LogOptions.RUNTIME, new EnumConverter(LogOptions.class)).addChangeCallback(() -> Logging.instance.updateLogPath());
	public static final Setting<File> GAMEDIR = new Setting<File>(new File("C:/Program Files (x86)/Steam/steamapps/common/Satisfactory"), FileConverter.instance).addChangeCallback(() -> Main.parseGameData());
	public static final Setting<Boolean> ALLOWDECIMAL = new Setting<Boolean>(false, BoolConverter.instance);
	public static final Setting<InputInOutputOptions> INOUT = new Setting<InputInOutputOptions>(InputInOutputOptions.MINES, new EnumConverter(InputInOutputOptions.class)).addChangeCallback(() -> Main.updateMainUI(false));
	public static final Setting<FractionDisplayOptions> FRACTION = new Setting<FractionDisplayOptions>(FractionDisplayOptions.MIXED, new EnumConverter(FractionDisplayOptions.class)).addChangeCallback(() -> {Main.updateMainUI(true); Main.rebuildMatrices();});
	public static final Setting<Float> IOTHRESH = new Setting<Float>(0F, FloatConverter.instance).addChangeCallback(() -> Main.updateMainUI(false));

	public static final Setting<Boolean> OPENRECENT = new Setting<Boolean>(true, BoolConverter.instance);
	public static final Setting<Boolean> SAVERECENT = new Setting<Boolean>(true, BoolConverter.instance);
	public static final Setting<Boolean> INPUTRECENT = new Setting<Boolean>(false, BoolConverter.instance);

	public static final Setting<Boolean> FIXEDMATRIX = new Setting<Boolean>(false, BoolConverter.instance).addChangeCallback(() -> Main.rebuildMatrices());

	public final S defaultValue;
	private S currentValue;
	private S pendingValue;
	private final StringValueConverter<S> converter;

	private Errorable ifChanged;
	private boolean changed;

	public Setting(S def, StringValueConverter<S> c) {
		defaultValue = def;
		currentValue = def;
		converter = c;
	}

	private Setting<S> addChangeCallback(Errorable r) {
		ifChanged = r;
		return this;
	}

	public S getCurrentValue() {
		return currentValue;
	}

	public void changeValue(S val) {
		pendingValue = val;
		changed = !Objects.equals(val, currentValue);
	}

	public void commit() throws Exception {
		if (changed) {
			currentValue = pendingValue;
			if (ifChanged != null)
				ifChanged.run();
		}
		changed = false;
		pendingValue = null;
	}

	public String getString() throws Exception {
		return converter.getString(currentValue);
	}

	public void parse(String s) throws Exception {
		currentValue = converter.parseString(s);
	}

	public static List<SettingRef> getSettings() throws Exception {
		ArrayList<SettingRef> li = new ArrayList();
		for (Field f : Setting.class.getDeclaredFields()) {
			if (f.getType() == Setting.class) {
				li.add(new SettingRef(f));
			}
		}
		return li;
	}

	public static /*Future<Void>*/void applyChanges(UUID id) throws Exception {
		//CompletableFuture<Void> f = new CompletableFuture();
		List<SettingRef> li = getSettings();
		double each = 100/li.size();
		//GuiUtil.queueTask("Applying Changes", (id) -> {
		double pct = 0;
		for (SettingRef s : li) {
			s.setting.commit();
			pct += each;
			WaitDialogManager.instance.setTaskProgress(id, pct);
		}
		//f.complete(null);
		//});
		//return f;
	}

	public static interface StringValueConverter<S> {

		public String getString(S obj) throws Exception;

		public S parseString(String s) throws Exception;

	}

	public static final class BoolConverter implements StringValueConverter<Boolean> {

		public static final BoolConverter instance = new BoolConverter();

		@Override
		public String getString(Boolean obj) throws Exception {
			return obj.toString();
		}

		@Override
		public Boolean parseString(String s) throws Exception {
			return Boolean.parseBoolean(s);
		}

	}

	public static final class IntConverter implements StringValueConverter<Integer> {

		public static final IntConverter instance = new IntConverter();

		@Override
		public String getString(Integer obj) throws Exception {
			return obj.toString();
		}

		@Override
		public Integer parseString(String s) throws Exception {
			return Integer.parseInt(s);
		}

	}

	public static final class FloatConverter implements StringValueConverter<Float> {

		public static final FloatConverter instance = new FloatConverter();

		@Override
		public String getString(Float obj) throws Exception {
			return String.format("%.6f", obj.floatValue());
		}

		@Override
		public Float parseString(String s) throws Exception {
			return Float.parseFloat(s);
		}

	}

	public static final class FileConverter implements StringValueConverter<File> {

		public static final FileConverter instance = new FileConverter();

		@Override
		public String getString(File obj) throws Exception {
			return obj == null ? "" : obj.getCanonicalPath().replace('\\', '/').replace("\\\\", "/");
		}

		@Override
		public File parseString(String s) throws Exception {
			return new File(s);
		}

	}

	public static class SettingRef {

		public final Setting setting;
		public final String name;

		public SettingRef(Field f) throws Exception {
			setting = (Setting)f.get(null);
			name = f.getName();
		}
		/*
		public String generateFileString() throws Exception {
			return name+"="+setting.converter.getString(setting.currentValue);
		}

		public void parse(String val) throws Exception {
			setting.currentValue = setting.converter.parseString(val.substring(val.indexOf('=')+1));
		}
		 */

		public void revert() {
			setting.currentValue = setting.defaultValue;
		}
	}

	public static final class EnumConverter<E extends Enum> implements StringValueConverter<E> {

		private final Class<E> enumClass;

		public EnumConverter(Class<E> enu) {
			enumClass = enu;
		}

		@Override
		public String getString(E obj) throws Exception {
			return obj == null ? "" : obj.name();
		}

		@Override
		public E parseString(String s) throws Exception {
			return (E)Enum.valueOf(enumClass, s);
		}

	}

	public enum InputInOutputOptions {
		EXCLUDE,
		MINES,
		ALL;
	}

	public enum FractionDisplayOptions {
		DECIMAL,
		MIXED,
		IMPROPER;

		public Region format(double amt, boolean includeX, boolean color) {
			switch (Setting.FRACTION.getCurrentValue()) {
				case DECIMAL:
					return new Label(String.format("%s%.3f", includeX ? "x" : "", amt));
				case MIXED:
					String[] parts = FractionHandlingDoubleConverter.instance.toString(amt).split(" ");
					Label lb = new Label(/*String.format("x%.2f", amt)*/(includeX ? "x" : "")+parts[0]);
					if (parts.length == 2) {
						String[] parts2 = parts[1].split("/");
						VBox sp = new VBox();
						sp.setSpacing(-2);
						sp.setAlignment(Pos.CENTER);
						HBox hb = new HBox();
						hb.setAlignment(Pos.CENTER);
						hb.setSpacing(2);
						hb.getChildren().add(lb);
						Line l = new Line();
						l.setStrokeWidth(1);
						l.setStrokeType(StrokeType.CENTERED);
						l.setStrokeLineCap(StrokeLineCap.ROUND);
						Label lb1 = new Label(parts2[0]);
						Label lb2 = new Label(parts2[1]);
						if (color) {
							l.setStroke(UIConstants.WARN_COLOR);
							lb.setStyle("-fx-text-fill: "+ColorUtil.getCSSHex(UIConstants.WARN_COLOR));
							lb1.setStyle("-fx-text-fill: "+ColorUtil.getCSSHex(UIConstants.WARN_COLOR));
							lb2.setStyle("-fx-text-fill: "+ColorUtil.getCSSHex(UIConstants.WARN_COLOR));
						}
						sp.getChildren().add(lb1);
						sp.getChildren().add(l);
						sp.getChildren().add(lb2);
						hb.getChildren().add(sp);
						l.endXProperty().bind(lb2.widthProperty());
						return hb;
					}
					else {
						return lb;
					}
				case IMPROPER:
					return new Label((includeX ? "x" : "")+Fraction.getFraction(amt).toString());
			}
			return null;
		}
	}

}
