package Reika.SatisfactoryPlanner.Data.Objects;


public class Item extends Consumable {

	public final int stackSize;
	public final int sinkValue;
	public final float radioactivity;

	public Item(String id, String dn, String img, String desc, String cat, float nrg, int stack, int sink, float radio) {
		super(id, dn, img, desc, cat, nrg);
		sinkValue = sink;
		stackSize = stack;
		radioactivity = radio;
	}

	@Override
	public int compareTo(Consumable c) {
		return c instanceof Fluid ? -1 : super.compareTo(c);
	}

}
