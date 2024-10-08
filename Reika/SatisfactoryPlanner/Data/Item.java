package Reika.SatisfactoryPlanner.Data;


public class Item extends Consumable {

	public final int stackSize;

	public Item(String id, String dn, String img, String desc, String cat, float nrg, int stack) {
		super(id, dn, img, desc, cat, nrg);
		stackSize = stack;
	}

	@Override
	public int compareTo(Consumable c) {
		return c instanceof Fluid ? -1 : super.compareTo(c);
	}

}
