package Reika.SatisfactoryPlanner.Data;

import java.util.Locale;
import java.util.function.Supplier;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Data.Constants.RateLimitedSupplyLine;
import Reika.SatisfactoryPlanner.GUI.GuiSystem;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.FontModifier;
import Reika.SatisfactoryPlanner.GUI.GuiUtil;
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
			super(WarningSeverity.SEVERE, String.format("Consumption (%s) exceeds production/supply (%s) of %s", GuiUtil.formatProductionDecimal(need), GuiUtil.formatProductionDecimal(have), c.displayName), new ResourceIconName(c));
		}

	}

	public static class ExcessResourceWarning extends Warning {

		public ExcessResourceWarning(Consumable c, float need, float have) {
			super(WarningSeverity.MINOR, String.format("Production (%s) exceeds requirements (%s) of %s", GuiUtil.formatProductionDecimal(have), GuiUtil.formatProductionDecimal(need), c.displayName), new ResourceIconName(c));
		}

	}

	public static class ItemDeadlockWarning extends Warning {

		public ItemDeadlockWarning(RecipeProductLoop p) {
			super(WarningSeverity.MINOR, p.item.displayName+" exists in a production loop between "+p.recipe1.displayName+" and "+p.recipe2.displayName+", but is also supplied externally. This risks a deadlock if consumption of that item drops or supply exceeds expectations. Consider a smart splitter and AWESOME sink to handle excess.", new ResourceIconName(p.item));
		}

	}

	public static class FluidDeadlockWarning extends Warning {

		public FluidDeadlockWarning(RecipeProductLoop p) {
			super(WarningSeverity.SEVERE, p.item.displayName+" exists in a production loop between "+p.recipe1.displayName+" and "+p.recipe2.displayName+", but is also supplied externally. This risks a deadlock if consumption of that fluid drops or supply exceeds expectations. Consider either splitting these into two isolated fluid networks, or adding another recipe to consume excess "+p.item.displayName, new ResourceIconName(p.item));
		}

	}

	public static class MultipleBeltsWarning extends Warning {

		public MultipleBeltsWarning(Consumable c, float amt, RateLimitedSupplyLine lim) {
			super(WarningSeverity.INFO, String.format("Flow (%s) of %s exceeds the capacity (%d) of a single %s, multiple parallel lines (%d) will be needed, and/or production of %s must be segmented", GuiUtil.formatProductionDecimal(amt), c.displayName, lim.getMaxThroughput(), lim.getDesc(), (int)Math.ceil(amt/lim.getMaxThroughput()), c.displayName), THROUGHPUT_BOTTLENECK);
		}

	}

	public static class PortThroughputWarning extends Warning {

		public PortThroughputWarning(String desc, int amt, RateLimitedSupplyLine max, int count) {
			super(WarningSeverity.SEVERE, desc+": Flow rate ("+amt+") exceeds maximum throughput ("+max.getMaxThroughput()*count+") of possible "+max.getDesc().toLowerCase(Locale.ENGLISH)+"s ("+count+")", THROUGHPUT_BOTTLENECK);
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
