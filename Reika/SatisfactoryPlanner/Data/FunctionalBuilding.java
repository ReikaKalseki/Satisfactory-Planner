package Reika.SatisfactoryPlanner.Data;

import Reika.SatisfactoryPlanner.Util.CountMap;

public class FunctionalBuilding extends Building {

	private final CountMap<Item> constructionCost = new CountMap();
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
