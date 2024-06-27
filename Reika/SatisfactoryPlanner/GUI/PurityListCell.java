package Reika.SatisfactoryPlanner.GUI;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Strings;

import Reika.SatisfactoryPlanner.Data.Constants.Purity;

import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.util.StringConverter;

class PurityListCell extends DecoratedListCell<Purity> {

	public static final StringConverter<Purity> converter = new StringConverter<Purity>() {
		@Override
		public String toString(Purity mt) {
			return mt == null ? "" : StringUtils.capitalize(mt.name());
		}

		@Override
		public Purity fromString(String s) {
			return Strings.isNullOrEmpty(s) ? null : Purity.valueOf(s.toUpperCase(Locale.ENGLISH));
		}
	};

	public PurityListCell(String ptext, boolean isButton) {
		super(ptext, isButton);
	}

	@Override
	protected String getString(Purity obj) {
		return converter.toString(obj);
	}

	@Override
	protected Node createDecoration(Purity obj) {
		return new ImageView(obj.image);
	}

}