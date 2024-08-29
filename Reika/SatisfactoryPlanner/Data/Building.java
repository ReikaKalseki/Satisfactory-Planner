package Reika.SatisfactoryPlanner.Data;

import Reika.SatisfactoryPlanner.Util.Logging;

public class Building extends HandBuildable implements Comparable<Building> {

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
		return String.CASE_INSENSITIVE_ORDER.compare(id, o.id);
	}

}
