package Reika.SatisfactoryPlanner.Data;

import Reika.SatisfactoryPlanner.Util.Logging;

public abstract class Consumable extends Resource implements Comparable<Consumable> {

	public final String description;

	protected Consumable(String id, String dn, String img, String desc) {
		super(id, dn, img);
		description = desc;
		Logging.instance.log("Registered item type "+this);
	}

	public int compareTo(Consumable c) {
		return String.CASE_INSENSITIVE_ORDER.compare(id, c.id);
	}

	@Override
	protected final String getIconFolder() {
		return "Items";
	}

}
