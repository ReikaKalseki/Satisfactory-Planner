package Reika.SatisfactoryPlanner.Data;


public class Item extends Consumable {

	public Item(String n, String img) {
		super(n, img);
	}

	@Override
	public int compareTo(Consumable c) {
		return c instanceof Fluid ? -1 : super.compareTo(c);
	}

}
