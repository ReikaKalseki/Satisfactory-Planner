package Reika.SatisfactoryPlanner.Data.Objects.Buildables;

import Reika.SatisfactoryPlanner.Data.Constants;

public abstract class FunctionalBuilding extends Building {

	public final float basePowerCostMW;
	public final float overclockPowerExponent;
	public final float somersloopPowerExponent;
	public final int numberSomersloopSlots;

	public FunctionalBuilding(String id, String dis, String icon, float mw, int sslots) {
		this(id, dis, icon, mw, Constants.DEFAULT_OVERCLOCK_EXPONENT, Constants.DEFAULT_SOMERSLOOP_EXPONENT, sslots);
	}

	public FunctionalBuilding(String id, String dis, String icon, float mw, float exp, float sexp, int sslots) {
		super(id, dis, icon);
		basePowerCostMW = mw;
		overclockPowerExponent = exp;
		somersloopPowerExponent = sexp;
		numberSomersloopSlots = sslots;
	}

	@Override
	public int compareTo(Building o) {
		int ret = super.compareTo(o);
		if (o instanceof FunctionalBuilding)
			ret += (int)Math.signum(Float.compare(basePowerCostMW, ((FunctionalBuilding)o).basePowerCostMW))*100;
		return ret;
	}

	/** Clock is 0-250, not 0-2.5 */
	public float getPowerCost(float clock, int somersloop) {
		float val = basePowerCostMW;
		val *= Math.pow(clock/100D, overclockPowerExponent);
		if (somersloop > 0 && numberSomersloopSlots > 0)
			val *= Math.pow(1+somersloop/(double)numberSomersloopSlots, somersloopPowerExponent);
		return val;
	}

}
