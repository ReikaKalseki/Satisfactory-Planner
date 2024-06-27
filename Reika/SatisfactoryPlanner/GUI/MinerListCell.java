package Reika.SatisfactoryPlanner.GUI;

import com.google.common.base.Strings;

import Reika.SatisfactoryPlanner.Data.Constants.MinerTier;

import javafx.scene.Node;
import javafx.util.StringConverter;

class MinerListCell extends DecoratedListCell<MinerTier> {

	public static final StringConverter<MinerTier> converter = new StringConverter<MinerTier>() {
		@Override
		public String toString(MinerTier mt) {
			return mt == null ? "" : mt.getMiner().name;
		}

		@Override
		public MinerTier fromString(String s) {
			return Strings.isNullOrEmpty(s) ? null : MinerTier.values()[Integer.parseInt(s.substring(s.length()-1))-1];
		}
	};

	public MinerListCell(String ptext, boolean isButton) {
		super(ptext, isButton);
	}

	@Override
	protected String getString(MinerTier obj) {
		return converter.toString(obj);
	}

	@Override
	protected Node createDecoration(MinerTier obj) {
		return obj.getMiner().createImageView();
	}

}