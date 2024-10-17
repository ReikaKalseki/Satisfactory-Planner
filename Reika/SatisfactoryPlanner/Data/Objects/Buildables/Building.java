package Reika.SatisfactoryPlanner.Data.Objects.Buildables;

import Reika.SatisfactoryPlanner.Util.Logging;

public abstract class Building extends HandBuildable implements Comparable<Building> {

	public Building(String id, String dis, String icon) {
		super(id, dis, icon);
		Logging.instance.log("Registered building type "+this);
	}

	@Override
	protected final String getIconFolder() {
		return "Buildings";
	}

	@Override
	public int compareTo(Building o) {
		BuildingCategory s1 = this.getCategory();
		BuildingCategory s2 = o.getCategory();
		return (int)Math.signum(String.CASE_INSENSITIVE_ORDER.compare(id, o.id))+s1.compareTo(s2)*10000;
	}
	/*
	@Override
	public final int compareTo(Building o) {
		int ret = Integer.compare(this.getSortIndex(), o.getSortIndex());
		if (ret == 0)
			ret = String.CASE_INSENSITIVE_ORDER.compare(id, o.id);
		return ret;
	}

	protected int getSortIndex() {
		return this.getCategory().ordinal();
	}
	 */
	public abstract BuildingCategory getCategory();

	public static enum BuildingCategory {
		SIMPLEPROD,
		CRAFTER,
		LINE,
		MINER,
		LOGISTIC,
		GENERATOR
	}

}
