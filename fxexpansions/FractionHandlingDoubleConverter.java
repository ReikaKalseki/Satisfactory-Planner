package fxexpansions;

import org.apache.commons.lang3.math.Fraction;

import com.google.common.base.Strings;

import Reika.SatisfactoryPlanner.GUI.GuiUtil;

import javafx.util.StringConverter;


public class FractionHandlingDoubleConverter extends StringConverter<Double> {

	public static final StringConverter<Double> instance = new FractionHandlingDoubleConverter(4);

	public final int numDecimals;

	public FractionHandlingDoubleConverter(int n) {
		numDecimals = n;
	}

	@Override
	public String toString(Double val) {
		Fraction f = Fraction.getFraction(val.doubleValue()); //this is hiding a LOT of math, yet seems to perform fine...
		return GuiUtil.formatProductionDecimal(f);
	}

	@Override
	public Double fromString(String s) {
		if (Strings.isNullOrEmpty(s))
			return 0D;
		try {
			int div = s.indexOf('/');
			if (div == -1) {
				return Double.parseDouble(s);
			}
			else {
				double denom = div == s.length()-1 ? 1 : Double.parseDouble(s.substring(div+1));
				return Double.parseDouble(s.substring(0, div))/denom;
			}
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

}
