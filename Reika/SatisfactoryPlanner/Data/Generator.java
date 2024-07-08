package Reika.SatisfactoryPlanner.Data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class Generator extends FunctionalBuilding {

	public final float powerGenerationMW;
	/** supplemental (eg water) cost:
(60 * {@link Generator#powerGenerationMW} * clock * {@link Generator#supplementalRatio}),  / 1000 for fluids */
	public final float supplementalRatio;
	private final HashMap<Consumable, Fuel> fuels = new HashMap();

	public Generator(String id, String dis, String icon, float mw, float sup) {
		super(id, dis, icon, 0);
		powerGenerationMW = mw;
		supplementalRatio = sup;
	}

	public Generator addFuel(Fuel f) {
		fuels.put(f.item, f);
		return this;
	}

	public Fuel getFuel(Consumable i) {
		return fuels.get(i);
	}

	public Collection<Fuel> getFuels() {
		return Collections.unmodifiableCollection(fuels.values());
	}
	/*
	/** Per minute *//*
	public float getBurnRate(Consumable c) {
		return c.energyValue > 0 ? powerGenerationMW/c.energyValue*60 : 0;
	}

	/** Per minute *//*
	public float getSecondaryBurnRate(float clock) {
		return 60 * powerGenerationMW * supplementalRatio * clock;
	}*/

	@Override
	public int compareTo(Building o) {
		if (o instanceof Generator)
			return Float.compare(powerGenerationMW, ((Generator)o).powerGenerationMW); //loosely correlates with tier/advancement
		else
			return super.compareTo(o);
	}

}
