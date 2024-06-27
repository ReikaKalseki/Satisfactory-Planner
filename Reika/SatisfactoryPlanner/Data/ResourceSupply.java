package Reika.SatisfactoryPlanner.Data;


public interface ResourceSupply<R extends Consumable> {

	public int getYield();
	public R getResource();

}
