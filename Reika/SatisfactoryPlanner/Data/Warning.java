package Reika.SatisfactoryPlanner.Data;

import java.util.function.Supplier;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.GUI.GuiSystem;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.FontModifier;
import Reika.SatisfactoryPlanner.GUI.UIConstants;
import Reika.SatisfactoryPlanner.Util.ColorUtil;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class Warning implements Comparable<Warning> {

	public final Supplier<Image> iconProvider;
	public final String warningText;
	public final WarningSeverity severity;

	public static final StringIconName THROUGHPUT_BOTTLENECK = new StringIconName("throughput-bottleneck.png");
	public static final StringIconName UNSTABLE = new StringIconName("unstable.png");

	public Warning(WarningSeverity sev, String txt) {
		this(sev, txt, null);
	}

	public Warning(WarningSeverity sev, String txt, Supplier<Image> ico) {
		severity = sev;
		warningText = txt;
		iconProvider = ico;
	}

	public Node createUI() {
		Label lb = new Label(warningText);
		lb.setFont(GuiSystem.getFont(severity.getFont()));
		lb.setStyle(severity.getStyle());
		Node root = lb;
		if (iconProvider != null) {
			ImageView img = new ImageView(iconProvider.get());
			HBox hb = new HBox();
			hb.setSpacing(4);
			hb.getChildren().add(img);
			hb.getChildren().add(lb);
			root = hb;
		}
		return root;
	}

	@Override
	public int compareTo(Warning o) {
		return severity == o.severity ? String.CASE_INSENSITIVE_ORDER.compare(warningText, o.warningText) : severity.compareTo(o.severity);
	}

	public static enum WarningSeverity {
		//FATAL,
		SEVERE,
		MINOR,
		INFO,;

		private String getStyle() {
			switch(this) {
				//case FATAL:
				//	return "-fx-text-fill: "+ColorUtil.getCSSHex(UIConstants.SEVERE_COLOR)+";";
				case SEVERE:
					return "-fx-text-fill: "+ColorUtil.getCSSHex(UIConstants.SEVERE_COLOR)+"; -fx-font-weight: bold;";
				case MINOR:
					return "-fx-text-fill: "+ColorUtil.getCSSHex(UIConstants.WARN_COLOR)+";";
				case INFO:
					return "-fx-text-fill: "+ColorUtil.getCSSHex(UIConstants.HIGHLIGHT_COLOR)+";";
				default:
					throw new IllegalStateException();
			}
		}

		private FontModifier getFont() {
			switch(this) {
				case SEVERE:
					return FontModifier.BOLD;
				default:
					return FontModifier.SEMIBOLD;
			}
		}
	}

	public static class InsufficientResourceWarning extends Warning {

		public InsufficientResourceWarning(Consumable c, float need, float have) {
			super(WarningSeverity.SEVERE, String.format("Consumption (%.3f) exceeds production/supply (%.3f) of %s", need, have, c.displayName), new ResourceIconName(c));
		}

	}

	public static class ExcessResourceWarning extends Warning {

		public ExcessResourceWarning(Consumable c, float need, float have) {
			super(WarningSeverity.MINOR, String.format("Production (%.3f) exceeds requirements (%.3f) of %s", have, need, c.displayName), new ResourceIconName(c));
		}

	}

	public static class ThroughputWarning extends Warning {

		public ThroughputWarning(String desc, int amt, int max) {
			super(WarningSeverity.SEVERE, desc+": Flow rate ("+amt+") exceeds maximum throughput ("+max+")", THROUGHPUT_BOTTLENECK);
		}

	}

	public static class ResourceIconName implements Supplier<Image> {

		public final Resource item;

		public ResourceIconName(Resource ico) {
			item = ico;
		}

		@Override
		public Image get() {
			return item.createIcon(16);
		}

	}

	public static class StringIconName implements Supplier<Image> {

		public final String iconName;

		public StringIconName(String ico) {
			iconName = ico;
		}

		@Override
		public Image get() {
			return new Image(Main.class.getResourceAsStream("Resources/Graphics/Icons/"+iconName));
		}

	}

}
