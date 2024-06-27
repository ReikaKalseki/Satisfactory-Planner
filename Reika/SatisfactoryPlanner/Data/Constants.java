package Reika.SatisfactoryPlanner.Data;

public class Constants {

	public static final int PIPE1_LIMIT = 300;
	public static final int PIPE2_LIMIT = 600;

	public static final int WATER_PUMP_PRODUCTION = 120;

	public static final int TRUCK_STOP_PORTS = 2;
	public static final int TRAIN_STATION_PORTS = 2;

	public static final int BASE_SOLID_YIELD = 60;
	public static final int BASE_OIL_YIELD = 120;
	public static final int BASE_FRACKING_YIELD = 60;

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

		public int getFrackingYield() {
			return (int)(BASE_FRACKING_YIELD*yieldMultiplier);
		}
	}

	public static enum BeltTier {
		ONE(60),
		TWO(120),
		THREE(270),
		FOUR(480),
		FIVE(780);

		public final int maxThroughput;

		private BeltTier(int s) {
			maxThroughput = s;
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
	/*
	public static enum SolidResources {
		IRON("Iron Ore"),
		COPPER("Copper Ore"),
		LIMESTONE(),
		COAL(),
		CATERIUM("Caterium Ore"),
		QUARTZ("Raw Quartz"),
		SULFUR(),
		BAUXITE(),
		URANIUM(),
		SAM("SAM Ore");

		private final String itemName;

		private SolidResources() {
			this(null);
		}

		private SolidResources(String n) {
			itemName = Strings.isNullOrEmpty(n) ? StringUtils.capitalize(this.name()) : n;
		}

		public Item getItem() {
			return (Item)Database.lookupItem(itemName);
		}
	}

	public static enum FrackableResources {
		WATER(),
		OIL(),
		NITROGEN();

		private final String itemName;

		private FrackableResources() {
			this(null);
		}

		private FrackableResources(String n) {
			itemName = Strings.isNullOrEmpty(n) ? StringUtils.capitalize(this.name()) : n;
		}

		public Fluid getItem() {
			return (Fluid)Database.lookupItem(itemName);
		}
	}
	 */
}
