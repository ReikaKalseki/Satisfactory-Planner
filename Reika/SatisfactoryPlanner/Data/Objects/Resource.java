package Reika.SatisfactoryPlanner.Data.Objects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.NamedIcon;
import Reika.SatisfactoryPlanner.Setting;
import Reika.SatisfactoryPlanner.GUI.GuiUtil;
import Reika.SatisfactoryPlanner.Util.Logging;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public abstract class Resource implements NamedIcon {

	private static boolean anyIcons = false;
	private static boolean missingAnyVanillaIcons = false;
	private static boolean missingAnyIcons = false;

	public final String id;
	public final String displayName;
	public final String iconName;

	private String sourceMod;

	private final HashMap<Integer, Image> iconCache = new HashMap();

	protected Resource(String id, String dn, String img) {
		this.id = id;
		displayName = dn;
		iconName = img;
	}

	@Override
	public final String toString() {
		return displayName+" ["+this.getClass().getName()+"]";
	}

	public final Resource markModded(String mod) {
		sourceMod = mod;
		return this;
	}

	public final String getMod() {
		return sourceMod;
	}

	public final String getDisplayName() {
		return displayName;
	}

	private InputStream getIcon() {
		File f = new File(Setting.GAMEDIR.getCurrentValue(), "FactoryGame/Icons/"+iconName+".png");
		try {
			if (!f.exists()) {
				missingAnyIcons = true;
				if (sourceMod == null)
					missingAnyVanillaIcons = true;

				InputStream fallback = Main.class.getResourceAsStream("Resources/Graphics/Icons/Game/"+iconName+".png");
				return fallback;
			}
			anyIcons = true;
			return new FileInputStream(f);
		}
		catch (FileNotFoundException e) {
			missingAnyIcons = true;
			if (sourceMod == null)
				missingAnyVanillaIcons = true;
			return null;
		}
	}

	public final Image createIcon() {
		return this.createIcon(32);
	}

	public final Image createIcon(int size) {
		Image img = iconCache.get(size);
		if (img == null) {
			InputStream in = this.getIcon();
			if (in == null) {
				Logging.instance.log("No icon '"+iconName+"' for "+this, System.err);
				in = Main.class.getResourceAsStream("Resources/Graphics/Icons/NotFound.png");
			}
			img = new Image(in, size, size, true, true);
			iconCache.put(size, img);
		}
		return img;
	}

	public final ImageView createImageView() {
		return this.createImageView(32);
	}

	public final ImageView createImageView(int size) {
		ImageView ret = new ImageView(this.createIcon(size));
		ret.setFitHeight(size);
		ret.setFitWidth(size);
		ret.setSmooth(true);
		GuiUtil.setTooltip(ret, displayName);
		return ret;
	}

	protected abstract String getIconFolder();

	public static void resetIconCheck() {
		anyIcons = false;
		missingAnyIcons = false;
		missingAnyVanillaIcons = false;
	}

	public static boolean doAnyIconsExist() {
		return anyIcons;
	}

	public static boolean areAnyIconsMissing() {
		return missingAnyIcons;
	}

	public static boolean areAnyVanillaIconsMissing() {
		return missingAnyVanillaIcons;
	}

}
