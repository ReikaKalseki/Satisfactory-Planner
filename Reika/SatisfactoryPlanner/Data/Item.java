package Reika.SatisfactoryPlanner.Data;


public class Item extends Consumable {

	public Item(String id, String dn, String img, String desc, String cat, float nrg) {
		super(id, dn, img, desc, cat, nrg);
	}

	@Override
	public int compareTo(Consumable c) {
		return c instanceof Fluid ? -1 : super.compareTo(c);
	}

}
