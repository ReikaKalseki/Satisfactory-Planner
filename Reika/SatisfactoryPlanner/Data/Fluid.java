package Reika.SatisfactoryPlanner.Data;


public class Fluid extends Consumable {

	protected Fluid(String n, String img) {
		super(n, img);
	}

	@Override
	public int compareTo(Consumable c) {
		return c instanceof Fluid ? super.compareTo(c) : 1;
	}

}
