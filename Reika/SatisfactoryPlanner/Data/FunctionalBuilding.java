package Reika.SatisfactoryPlanner.Data;

public abstract class FunctionalBuilding extends Building {

	public final float basePowerCostMW;

	public FunctionalBuilding(String id, String dis, String icon, float mw) {
		super(id, dis, icon);
		basePowerCostMW = mw;
	}

	@Override
	public int compareTo(Building o) {
		int ret = super.compareTo(o);
		if (o instanceof FunctionalBuilding)
			ret += (int)Math.signum(Float.compare(basePowerCostMW, ((FunctionalBuilding)o).basePowerCostMW))*100;
		return ret;
	}

}
