package Reika.SatisfactoryPlanner.Data;

public class LogisticBuilding extends FunctionalBuilding {

	public LogisticBuilding(String id, String dis, String icon, float mw, float exp, float sexp, int sslots) {
		super(id, dis, icon, mw, exp, sexp, sslots);
	}

	@Override
	public BuildingCategory getCategory() {
		return BuildingCategory.LOGISTIC;
	}

}
