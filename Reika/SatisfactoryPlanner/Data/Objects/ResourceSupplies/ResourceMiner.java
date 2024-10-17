package Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies;

import Reika.SatisfactoryPlanner.Data.Objects.Buildables.Building;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.FunctionalBuilding;

public class ResourceMiner extends FunctionalBuilding {

	public ResourceMiner(String id, String dis, String icon, float mw, float exp, float sexp, int sslots) {
		super(id, dis, icon, mw, exp, sexp, sslots);
	}

	@Override
	public BuildingCategory getCategory() {
		return BuildingCategory.MINER;
	}

	@Override
	public int compareTo(Building o) {
		int ret = super.compareTo(o);
		if (o instanceof ResourceMiner) {
			if (id.contains("Fracking") && !o.id.contains("Fracking"))
				return 1;
			else if (!id.contains("Fracking") && o.id.contains("Fracking"))
				return -1;
		}
		return ret;
	}

}
