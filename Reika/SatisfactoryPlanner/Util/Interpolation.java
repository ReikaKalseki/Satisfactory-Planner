/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.SatisfactoryPlanner.Util;

public class Interpolation {

	private final ThresholdMapping<Double> data = new ThresholdMapping();

	private final boolean isColor;

	public boolean cosInterpolate = false;

	/** "Is this for color mixing or simple interpolation" */
	public Interpolation(boolean iscolor) {
		isColor = iscolor;
	}

	public Interpolation addPoint(double c, double val) {
		data.addMapping(c, val);
		return this;
	}

	public double getValue(double key) {
		Double x1 = data.getKeyForValue(key, false);
		Double x2 = data.getKeyForValue(key, true);
		Double d1 = data.getForValue(key, false);
		Double d2 = data.getForValue(key, true);
		if (x1 == x2) {
			return d1;
		}
		else if (x1 == null) {
			return d2;
		}
		else if (x2 == null) {
			return d1;
		}
		float f = (float)((key-x1)/(x2-x1));
		return isColor ? ColorUtil.mixColors(d2.intValue(), d1.intValue(), f) : (cosInterpolate ? MathUtil.cosInterpolation(key, x1, x2, d1, d2) : MathUtil.linterpolate(key, x1, x2, d1, d2));
	}

	@Override
	public String toString() {
		return data.toString();
	}

	public double getInitialValue() {
		return this.getValue(data.firstValue());
	}

	public double getFinalValue() {
		return this.getValue(data.lastValue());
	}

	public double getLowestKey() {
		return data.firstValue();
	}

	public double getHighestKey() {
		return data.lastValue();
	}

}
