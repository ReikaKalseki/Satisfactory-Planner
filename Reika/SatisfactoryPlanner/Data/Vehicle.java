package Reika.SatisfactoryPlanner.Data;

import Reika.SatisfactoryPlanner.Util.CountMap;
import Reika.SatisfactoryPlanner.Util.Logging;

public class Vehicle extends HandBuildable implements Comparable<Vehicle> {

	private final CountMap<Item> constructionCost = new CountMap();

	public Vehicle(String id, String dis, String icon) {
		super(id, dis, icon);
		Logging.instance.log("Registered vehicle type "+this);
	}

	@Override
	protected String getIconFolder() {
		return "Vehicles";
	}

	@Override
	public int compareTo(Vehicle o) {
		return String.CASE_INSENSITIVE_ORDER.compare(id, o.id);
	}

}
