package Reika.SatisfactoryPlanner.GUI;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Util.Errorable;

public class Setting<S> {

	public static final Setting<File> GAMEDIR = new Setting<File>(Main.getRelativeFile(""), FileConverter.instance).addChangeCallback(() -> Main.parseGameData());
	public static final Setting<Boolean> ALLOWDECIMAL = new Setting<Boolean>(false, BoolConverter.instance);

	public final S defaultValue;
	private S currentValue;
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

	public void setValue(S val) {
		changed = true;
		currentValue = val;
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

	public static void applyChanges() throws Exception {
		for (SettingRef s : getSettings()) {
			if (s.setting.changed && s.setting.ifChanged != null)
				s.setting.ifChanged.run();
			s.setting.changed = false;
		}
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

}
