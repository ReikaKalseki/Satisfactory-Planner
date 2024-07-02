package Reika.SatisfactoryPlanner.Data;

import java.io.InputStream;
import java.util.HashMap;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.GUI.GuiUtil;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public abstract class Resource {

	public final String id;
	public final String displayName;
	public final String iconName;

	private final HashMap<Integer, Image> iconCache = new HashMap();

	protected Resource(String id, String dn, String img) {
		this.id = id;
		displayName = dn;
		iconName = img;
	}

	@Override
	public String toString() {
		return displayName+" ["+this.getClass().getName()+"]";
	}

	private InputStream getIcon() {
		return Main.class.getResourceAsStream("Resources/Graphics/Icons/"+this.getIconFolder()+"/"+iconName+".png");
	}

	public final Image createIcon() {
		return this.createIcon(32);
	}

	public final Image createIcon(int size) {
		Image img = iconCache.get(size);
		if (img == null) {
			img = new Image(this.getIcon(), size, size, true, true);
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
