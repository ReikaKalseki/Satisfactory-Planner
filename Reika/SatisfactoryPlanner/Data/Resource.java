package Reika.SatisfactoryPlanner.Data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.GUI.GuiUtil;
import Reika.SatisfactoryPlanner.GUI.Setting;
import Reika.SatisfactoryPlanner.Util.Logging;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public abstract class Resource {

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

	private InputStream getIcon() {
		//return Main.class.getResourceAsStream("Resources/Graphics/Icons/"+this.getIconFolder()+"/"+iconName+".png");
		try {
			//TODO add a way to autoinstall the mod?
			return new FileInputStream(new File(Setting.GAMEDIR.getCurrentValue(), "FactoryGame/Icons/"+iconName+".png")); //from the icon dump mod
		}
		catch (FileNotFoundException e) {
			throw new RuntimeException(e);
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
				Logging.instance.log("No icon '"+iconName+"' for "+this);
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
		GuiUtil.setTooltip(ret, displayName);
		return ret;
	}

	protected abstract String getIconFolder();

}
