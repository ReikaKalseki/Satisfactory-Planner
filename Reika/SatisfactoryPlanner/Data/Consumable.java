package Reika.SatisfactoryPlanner.Data;


public abstract class Consumable extends Resource implements Comparable<Consumable> {

	protected Consumable(String n, String img) {
		super(n, img);
	}

	public int compareTo(Consumable c) {
		return String.CASE_INSENSITIVE_ORDER.compare(name, c.name);
	}

	@Override
	protected final String getIconFolder() {
		return "Items";
	}

}
