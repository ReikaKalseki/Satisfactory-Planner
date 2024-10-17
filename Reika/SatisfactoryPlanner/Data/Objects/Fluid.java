package Reika.SatisfactoryPlanner.Data.Objects;

import javafx.scene.paint.Color;

public class Fluid extends Consumable {

	public final Color baseColor;
	public final boolean isGas;

	public Fluid(String id, String dn, String img, String desc, String cat, float nrg, Color c, boolean gas) {
		super(id, dn, img, desc, cat, nrg);
		isGas = gas;
		baseColor = c;
	}

	@Override
	public int compareTo(Consumable c) {
		return c instanceof Fluid ? super.compareTo(c) : 1;
	}

}
