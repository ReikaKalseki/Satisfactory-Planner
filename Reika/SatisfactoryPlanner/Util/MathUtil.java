/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.SatisfactoryPlanner.Util;

public final class MathUtil {

	public static int clamp(int val, int min, int max) {
		return Math.max(min, Math.min(max, val));
	}

	public static double clamp(double val, double min, double max) {
		return Math.max(min, Math.min(max, val));
	}

	/** Returns the pythagorean sum of the three inputs. Used mainly for vector magnitudes.
	 * Args: x,y,z */
	public static double py3d(double dx, double dy, double dz) {
		double val;
		val = dx*dx+dy*dy+dz*dz;
		return Math.sqrt(val);
	}

	/** Returns true if the input is within a percentage of its size of another value.
	 * Args: Input, target, percent tolerance */
	public static boolean approxp(double input, double target, double percent) {
		double low = input - input*percent/100;
		double hi = input + input*percent/100;
		if ((target >= low) && (target <= hi))
			return true;
		else
			return false;
	}

	/** Returns true if the input is within [target-range,target+range]. Args: input, target, range */
	public static boolean approxr(double input, double target, double range) {
		double low = input - range;
		double hi = input + range;
		if ((target >= low) && (target <= hi))
			return true;
		else
			return false;
	}

	/** Returns true if the input is within a percentage of its size of another value.
	 * Args: Input, target, percent tolerance */
	public static boolean approxpAbs(double input, double target, double percent) {
		return approxp(Math.abs(input), Math.abs(target), percent);
	}

	/** Returns true if the input is within [target-range,target+range]. Args: input, target, range */
	public static boolean approxrAbs(double input, double target, double range) {
		return approxr(Math.abs(input), Math.abs(target), range);
	}

	public static int logbase2(long inp) {
		return inp > 0 ? 63-Long.numberOfLeadingZeros(inp) : 0;
	}

	public static boolean isPositiveInteger(double num) {
		return num > 0 && (int)num == num;
	}

	public static boolean isPowerOfTwo(long num) {
		return num > 0 && (num & (num-1)) == 0; //alternate: num > 0 && (num & -num) == num
	}

	/** Returns the nearest higher power of 2. Args: input */
	public static int ceil2exp(int val) {
		if (val <= 0)
			return 0;
		val--;
		val = (val >> 1) | val;
		val = (val >> 2) | val;
		val = (val >> 4) | val;
		val = (val >> 8) | val;
		val = (val >> 16) | val;
		val++;
		return val;
	}

	/** Returns whether the two numbers are the same sign.
	 * Will return true if both are zero. Args: Input 1, Input 2*/
	public static boolean isSameSign(double val1, double val2) {
		return Math.signum(val1) == Math.signum(val2);
	}

	/** Returns the next multiple of a higher than b. Args: a, b */
	public static int nextMultiple(int a, int b) {
		while (b%a != 0) {
			b++;
		}
		return b;
	}

	/** Returns true if the value is not inside the bounds (inclusive). Args: Low Bound, Upper Bound, Value */
	public static boolean isValueOutsideBounds(int low, int hi, int val) {
		return !(val >= low && val <= hi);
	}

	/** Returns true if the value is inside the bounds (not inclusive). Args: Low Bound, Upper Bound, Value */
	public static boolean isValueInsideBounds(int low, int hi, int val) {
		return val < hi && val > low;
	}

	/** Returns true if the value is inside the bounds (inclusive). Args: Low Bound, Upper Bound, Value */
	public static boolean isValueInsideBoundsIncl(int low, int hi, int val) {
		return val <= hi && val >= low;
	}

	public static boolean isValueInsideBounds(double low, double hi, double val) {
		return val < hi && val > low;
	}

	public static boolean isValueInsideBoundsIncl(double low, double hi, double val) {
		return val <= hi && val >= low;
	}

	/** Returns a double's value when reduced until it is less than a thousand.
	 * Equivalent to getScientificNotation[0]. Args: Value */
	public static double getThousandBase(double val) {
		if (Math.abs(val) == Double.POSITIVE_INFINITY)
			return val;
		if (val == Double.NaN)
			return val;
		boolean neg = val < 0;
		val = Math.abs(val);
		while (val >= 1000) {
			val /= 1000D;
		}
		while (val < 1 && val > 0) {
			val *= 1000D;
		}
		return neg ? -val : val;
	}

	public static String getSIPrefix(double val) {
		if (val == 0 || (val < 10 && val >= 1))
			return "";
		int log = (int)Math.floor(Math.log(val)/Math.log(1000));
		switch(log) {
			case 1:
				return "k";
			case 2:
				return "M";
			case 3:
				return "G";
			case 4:
				return "T";
			case 5:
				return "P";
			case 6:
				return "E";
			case 7:
				return "Z";

			case -1:
				return "m";
			case -2:
				return "micro";
			case -3:
				return "n";
			case -4:
				return "p";
			case -5:
				return "f";

			default:
				return "";
		}
	}

	/** Returns the factorial of a positive integer. Take care with this, given
	 * how rapidly that function's output rises. */
	public static int factorial(int val) {
		int base = 1;
		for (int i = val; i > 0; i--) {
			base *= i;
		}
		return base;
	}

	/** Simple test to see if two number ranges overlap. Args: range 1 min/max; range 2 min/max */
	public static boolean doRangesOverLap(int min1, int max1, int min2, int max2) {
		return max2 >= min1 && min2 <= max1;
	}

	/** Returns true if and only if n and only n of the args are true. */
	public static boolean nBoolsAreTrue(int number, boolean... args) {
		int count = 0;
		for (int i = 0; i < args.length; i++) {
			if (args[i])
				count++;
		}
		return count == number;
	}

	/** Returns true if n or more of the args are true. */
	public static boolean nPlusBoolsAreTrue(int number, boolean... args) {
		int count = 0;
		for (int i = 0; i < args.length; i++) {
			if (args[i])
				count++;
		}
		return count >= number;
	}

	public static int roundDownToX(int multiple, int val) {
		int ret = val - val%multiple;
		if (val < 0)
			ret -= multiple;
		return ret;
	}

	public static int roundUpToX(int multiple, int val) {
		return roundDownToX(multiple, val)+multiple;
	}

	public static int roundToNearestX(int multiple, int val) {
		return ((val+multiple/2*(int)Math.signum(val))/multiple)*multiple;
	}

	public static float getDecimalPart(float f) {
		return f-(int)f;
	}

	public static double getDecimalPart(double d) {
		return d%1D;//-(int)d;
	}

	public static int addAndRollover(int a, int b, int min, int max) {
		int sum = a+b;
		int over = sum-max;
		int under = min-sum;
		int range = max-min;
		while (over > 0) {
			sum = Math.min(max, min+over);
			over -= range;
		}
		while (under > 0) {
			sum = Math.max(min, max-under);
			under -= range;
		}
		return sum;
	}

	public static boolean isPointInsideEllipse(double x, double y, double z, double ra, double rb, double rc) {
		return (ra > 0 ? ((x*x)/(ra*ra)) : 0) + (rb > 0 ? ((y*y)/(rb*rb)) : 0) + (rc > 0 ? ((z*z)/(rc*rc)) : 0) <= 1;
	}

	public static boolean isPointInsidePowerEllipse(double x, double y, double z, double rx, double ry, double rz, double pow) {
		x = Math.abs(x);
		y = Math.abs(y);
		z = Math.abs(z);
		return (rx > 0 ? (Math.pow(x, pow)/Math.pow(rx, pow)) : 0) + (ry > 0 ? (Math.pow(y, pow)/Math.pow(ry, pow)) : 0) + (rz > 0 ? (Math.pow(z, pow)/Math.pow(rz, pow)) : 0) <= 1;
	}

	public static double linterpolate(double x, double x1, double x2, double y1, double y2) {
		return linterpolate(x, x1, x2, y1, y2, false);
	}

	public static double linterpolate(double x, double x1, double x2, double y1, double y2, boolean clamp) {
		if (clamp && x <= x1)
			return y1;
		if (clamp && x >= x2)
			return y2;
		return y1+(x-x1)/(x2-x1)*(y2-y1);
	}

	public static int bitRound(int val, int bits) {
		return (val >> bits) << bits;
	}

	/** Whole wave! */
	public static double cosInterpolation(double min, double max, double val) {
		if (!isValueInsideBoundsIncl(min, max, val))
			return 0;
		double size = (max-min)/2D;
		double mid = min+size;
		if (val == mid) {
			return 1;
		}
		else {
			return 0.5+0.5*Math.cos(Math.toRadians((val-mid)/size*180));
		}
	}

	/** Whole wave! */
	public static double cosInterpolation(double min, double max, double val, double y1, double y2) {
		return y1+(y2-y1)*cosInterpolation(min, max, val);
	}

	/** Half a wave rather than the whole one */
	public static double cosInterpolation2(double min, double max, double val, double y1, double y2) {
		double dx = max-min;
		double mm = min+dx*2;
		return y1+(y2-y1)*cosInterpolation(min, mm, val);
	}

	public static boolean isPerfectSquare(int val) {
		double sqrt = Math.sqrt(val);
		return sqrt == (int)sqrt;
	}

	/** Assumes val ranges from [-1 to +1] */
	public static double normalizeToBounds(double val, double min, double max) {
		return normalizeToBounds(val, min, max, -1, 1);
	}

	public static double normalizeToBounds(double val, double min, double max, double low, double high) {
		return min+((max-min)*(val-low)/(high-low));
	}

	public static float roundToDecimalPlaces(float f, int i) {
		float pow = (float)Math.pow(10, i);
		return Math.round(f*pow)/pow;
	}

	public static double roundToNearestFraction(double val, double frac) {
		double fac = 1D/frac;
		return Math.round(val*fac)/fac;
	}

	public static double roundUpToFraction(double val, double frac) {
		double fac = 1D/frac;
		return Math.ceil(val*fac)/fac;
	}

	public static double roundDownToFraction(double val, double frac) {
		double fac = 1D/frac;
		return Math.floor(val*fac)/fac;
	}

	public static int getWithinBoundsElse(int val, int min, int max, int fall) {
		return isValueInsideBoundsIncl(min, max, val) ? val : fall;
	}

	public static int cycleBitsLeft(int num, int n) {
		n = n&31;
		return (num << n) | (num >> (32-n));
	}

	public static long cycleBitsLeft(long num, int n) {
		n = n&63;
		return (num << n) | (num >> (64-n));
	}

	public static int cycleBitsRight(int num, int n) {
		n = n&31;
		return (num >> n) | (num << (32-n));
	}

	public static long cycleBitsRight(long num, int n) {
		n = n&63;
		return (num >> n) | (num << (64-n));
	}

	public static double getUnequalAverage(double a, double b, double bias) {
		return (1-bias)*a+bias*b;
	}

	public static int clipLeadingHexBits(int val) {
		while (val > 0 && (val&0xF) == 0)
			val = val >> 4;
		return val;
	}

	public static double ellipticalInterpolation(double x, double x1, double x2, double y1, double y2) {
		return (y2-y1)*Math.sqrt(Math.pow(x2-x1, 2)-Math.pow(x-x1, 2))/(x2-x1);
	}

	public static double powerInterpolation(double x, double x1, double x2, double y1, double y2, double power) {
		return (y2-y1)*Math.pow(Math.pow(x2-x1, power)-Math.pow(x-x1, power), 1D/power)/(x2-x1);
	}

	public static long cantorCombine(long... vals) {
		long ret = cantorCombine(vals[0], vals[1]);
		for (int i = 2; i < vals.length; i++) {
			ret = cantorCombine(ret, vals[i]);
		}
		return ret;
	}

	public static long cantorCombine(long a, long b) {
		long k1 = a*2;
		long k2 = b*2;
		if (a < 0)
			k1 = a*-2-1;
		if (b < 0)
			k2 = b*-2-1;
		return (long)(0.5*(k1 + k2)*(k1 + k2 + 1) + k2);
	}

	public static int countDecimalPlaces(float f) {
		if (f == Float.NaN || f == Float.NEGATIVE_INFINITY || f == Float.POSITIVE_INFINITY)
			return 0;
		float dec = f-(int)f;
		if (dec == 0)
			return 0;
		return String.valueOf(dec).split("\\.")[1].length();
	}

	public static int sumDigits(long val) {
		val = Math.abs(val);
		int sum = 0;
		while (val > 0) {
			sum += (int)(val%10);
			val /= 10;
		}
		return sum;
	}
}
