package Reika.SatisfactoryPlanner.Data;


public class Constants {

	public static final int PIPE1_LIMIT = 300;
	public static final int PIPE2_LIMIT = 600;

	public static final int BELT1_LIMIT = 60;
	public static final int BELT2_LIMIT = 120;
	public static final int BELT3_LIMIT = 270;
	public static final int BELT4_LIMIT = 480;
	public static final int BELT5_LIMIT = 780;

	public static final int WATER_PUMP_PRODUCTION = 120;

	public static final int TRUCK_STOP_PORTS = 2;

	public static final int BASE_SOLID_YIELD = 60;
	public static final int BASE_OIL_YIELD = 120;

	public static int getMinerYield(Purity purity, MinerTier miner, float overclock) {
		return (int)(purity.getSolidYield()*miner.speedMultiplier*overclock);
	}

	public static int getOilYield(Purity purity, MinerTier miner, float overclock) {
		return (int)(purity.getOilYield()*miner.speedMultiplier*overclock);
	}

	public static enum Purity {
		IMPURE(0.5F),
		NORMAL(1F),
		PURE(2F);

		public final float yieldMultiplier;

		private Purity(float y) {
			yieldMultiplier = y;
		}

		public int getSolidYield() {
			return (int)(BASE_SOLID_YIELD*yieldMultiplier);
		}

		public int getOilYield() {
			return (int)(BASE_OIL_YIELD*yieldMultiplier);
		}
	}

	public static enum MinerTier {
		ONE(1),
		TWO(2),
		THREE(4);

		public final float speedMultiplier;

		private MinerTier(float s) {
			speedMultiplier = s;
		}
	}

}
