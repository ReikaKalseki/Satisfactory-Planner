package Reika.SatisfactoryPlanner.Data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class Generator extends FunctionalBuilding {

	public final float powerGenerationMW;
	private final HashMap<Consumable, Fuel> fuels = new HashMap();

	public Generator(String id, String dis, String icon, float mw) {
		super(id, dis, icon, 0);
		powerGenerationMW = mw;
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

	/** Per minute */
	public float getBurnRate(Consumable c) {
		return c.energyValue > 0 ? powerGenerationMW/c.energyValue*60 : 0;
	}

	@Override
	public int compareTo(Building o) {
		if (o instanceof Generator)
			return Float.compare(powerGenerationMW, ((Generator)o).powerGenerationMW); //loosely correlates with tier/advancement
		else
			return super.compareTo(o);
	}

}
