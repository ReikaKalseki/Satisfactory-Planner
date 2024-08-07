package Reika.SatisfactoryPlanner;

import Reika.SatisfactoryPlanner.GUI.GuiUtil;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public interface NamedIcon {

	public default Image createIcon() {
		return this.createIcon(32);
	}

	public Image createIcon(int size);

	public String getDisplayName();

	public default ImageView createImageView() {
		return this.createImageView(32);
	}

	public default ImageView createImageView(int size) {
		ImageView ret = new ImageView(this.createIcon(size));
		GuiUtil.setTooltip(ret, this.getDisplayName());
		return ret;
	}

}
