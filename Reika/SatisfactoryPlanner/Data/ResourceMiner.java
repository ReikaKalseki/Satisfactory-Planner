package Reika.SatisfactoryPlanner.Data;

public class ResourceMiner extends FunctionalBuilding {

	public ResourceMiner(String id, String dis, String icon, float mw) {
		super(id, dis, icon, mw);
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
