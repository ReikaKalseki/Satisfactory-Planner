package Reika.SatisfactoryPlanner.Data;

@Deprecated
public class ItemFlow {

	public final Consumable item;
	float production;
	float externalInput;
	float consumption;

	public ItemFlow(Consumable c) {
		item = c;
	}

	public float getProduction() {
		return production;
	}

	public float getInput() {
		return externalInput;
	}

	public float getConsumption() {
		return consumption;
	}

	public float getNetYield() {
		return this.getTotalAvailable()-consumption;
	}

	public float getTotalAvailable() {
		return production+externalInput;
	}

	public boolean isDeficit() {
		return consumption > this.getTotalAvailable();
	}

}
