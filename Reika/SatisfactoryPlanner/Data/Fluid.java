package Reika.SatisfactoryPlanner.Data;

import javafx.scene.paint.Color;

public class Fluid extends Consumable {

	public final Color baseColor;

	protected Fluid(String id, String dn, String img, String desc, String cat, float nrg, Color c) {
		super(id, dn, img, desc, cat, nrg);
		baseColor = c;
	}

	@Override
	public int compareTo(Consumable c) {
		return c instanceof Fluid ? super.compareTo(c) : 1;
	}

}
