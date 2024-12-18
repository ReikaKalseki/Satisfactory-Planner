package Reika.SatisfactoryPlanner.Data.Objects.Buildables;

import Reika.SatisfactoryPlanner.Data.Objects.Consumable;
import Reika.SatisfactoryPlanner.Util.CountMap;
import Reika.SatisfactoryPlanner.Util.Logging;

public class Vehicle extends HandBuildable implements Comparable<Vehicle> {

	private final CountMap<Consumable> constructionCost = new CountMap();

	public Vehicle(String id, String dis, String icon) {
		super(id, dis, icon);
		Logging.instance.log("Registered vehicle type "+this);
	}

	@Override
	public int compareTo(Vehicle o) {
		return String.CASE_INSENSITIVE_ORDER.compare(id, o.id);
	}

}
