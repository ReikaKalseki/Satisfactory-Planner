package Reika.SatisfactoryPlanner.Data;


public interface PowerOverride {

	public float getAveragePower();
	public float getMinimumPower();
	public float getPeakPower();

	public class ConstantPowerOverride implements PowerOverride {

		public final float value;

		public ConstantPowerOverride(float p) {
			value = p;
		}

		@Override
		public float getAveragePower() {
			return value;
		}

		@Override
		public float getMinimumPower() {
			return value;
		}

		@Override
		public float getPeakPower() {
			return value;
		}

	}

	public class LinearIncreasePower implements PowerOverride {

		public final float baseline;
		public final float variation;

		public LinearIncreasePower(float b, float var) {
			baseline = b;
			variation = var;
		}

		@Override
		public float getAveragePower() {
			return baseline;
		}

		@Override
		public float getMinimumPower() {
			return baseline-variation;
		}

		@Override
		public float getPeakPower() {
			return baseline+variation;
		}

	}

}
