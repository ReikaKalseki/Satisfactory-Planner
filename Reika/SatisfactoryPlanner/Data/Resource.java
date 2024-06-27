package Reika.SatisfactoryPlanner.Data;

import java.io.InputStream;
import java.util.HashMap;

import Reika.SatisfactoryPlanner.Main;

import javafx.scene.image.Image;

public abstract class Resource {

	public final String name;
	public final String iconName;

	private final HashMap<Integer, Image> iconCache = new HashMap();

	protected Resource(String n, String img) {
		name = n;
		iconName = img;
	}

	@Override
	public String toString() {
		return name+" ["+this.getClass().getName()+"]";
	}

	private InputStream getIcon() {
		return Main.class.getResourceAsStream("Resources/Graphics/Icons/"+this.getIconFolder()+"/"+iconName+".png");
	}

	public Image createIcon() {
		return this.createIcon(32);
	}

	public Image createIcon(int size) {
		Image img = iconCache.get(size);
		if (img == null) {
			img = new Image(this.getIcon(), size, size, true, true);
			iconCache.put(size, img);
		}
		return img;
	}

	protected abstract String getIconFolder();

}
