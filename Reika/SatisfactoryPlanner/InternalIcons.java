package Reika.SatisfactoryPlanner;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public enum InternalIcons implements NamedIcon {

	ADD(32),
	RESET(32),
	DELETE(32),
	DUPLICATE(32),
	POWER(32),
	SUPPLY(32),
	BOTTLENECK(16),
	OVERVIEW(16),
	INPUTOUTPUT(16),
	POWERTAB(16),
	MATRICES(16),
	;

	public final int defaultSize;
	private Image image;

	private InternalIcons(int s) {
		defaultSize = s;
	}

	public Image createIcon() {
		return this.createIcon(defaultSize);
	}

	public ImageView createImageView() {
		return this.createImageView(defaultSize);
	}

	@Override
	public Image createIcon(int size) {
		if (image == null)
			image = new Image(Main.class.getResourceAsStream("Resources/Graphics/Icons/"+this.name().toLowerCase(Locale.ENGLISH)+".png"), size, size, true, true);
		return image;
	}

	@Override
	public String getDisplayName() {
		return StringUtils.capitalize(this.name());
	}

}
