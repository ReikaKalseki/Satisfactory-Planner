package Reika.SatisfactoryPlanner.Data;

public class FunctionalBuilding extends Building {

	public final float basePowerCostMW;

	public FunctionalBuilding(String id, String dis, String icon, float mw) {
		super(id, dis, icon);
		basePowerCostMW = mw;
	}

	@Override
	public int compareTo(Building o) {
		if (o instanceof FunctionalBuilding)
			return Float.compare(basePowerCostMW, ((FunctionalBuilding)o).basePowerCostMW); //loosely correlates with tier/advancement
		else
			return super.compareTo(o);
	}

}
