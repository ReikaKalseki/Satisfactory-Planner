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
		String raw = val.toString();
		int idx = raw.indexOf('.');
		if (idx < 0 || raw.length()-idx <= 4) //allow up to three decimal places
			return GuiUtil.formatProductionDecimal(val.doubleValue());
		Fraction f = Fraction.getFraction(val.doubleValue()); //this is hiding a LOT of math, yet seems to perform fine...
		return f.toProperString();//GuiUtil.formatProductionDecimal(val.doubleValue());
	}

	@Override
	public Double fromString(String s) {
		if (Strings.isNullOrEmpty(s))
			return 0D;
		try {
			/*
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
		}*/
			char end = s.charAt(s.length()-1);
			if (end == '.')
				return Double.parseDouble(s.substring(0, s.length()-1));
			else if (end == '/')
				return Double.parseDouble(s.substring(0, s.length()-1).trim());
			else
				return Fraction.getFraction(s).doubleValue();
		}
		catch (NumberFormatException e) {
			return 0D;
		}
	}

}
