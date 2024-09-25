package Reika.SatisfactoryPlanner.Data;

public class LogisticBuilding extends FunctionalBuilding {

	public LogisticBuilding(String id, String dis, String icon, float mw) {
		super(id, dis, icon, mw);
	}

	@Override
	public BuildingCategory getCategory() {
		return BuildingCategory.LOGISTIC;
	}

}
