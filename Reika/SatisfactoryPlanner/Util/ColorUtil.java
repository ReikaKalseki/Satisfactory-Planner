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

import javafx.scene.paint.Color;

public class ColorUtil {

	public static int RGBtoHex(int R, int G, int B, int A) {
		int color = (B | G << 8 | R << 16 | A << 24);
		return color;
	}

	public static int RGBtoHex(int R, int G, int B) {
		return RGBtoHex(R, G, B, 255);
	}

	public static int GStoHex(int gs) {
		return RGBtoHex(gs, gs, gs);
	}

	public static int RGBFtoHex(double r, double g, double b) {
		return RGBtoHex((int)(r*255), (int)(g*255), (int)(b*255), 255);
	}

	public static int[] HexToRGB(int hex) {
		int[] color = new int[4];
		color[0] = (hex >>> 16) & 0xFF;
		color[1] = (hex >>> 8) & 0xFF;
		color[2] = (hex) & 0xFF;
		color[3] = (hex >>> 24) & 0xFF;
		return color;
	}

	public static int getColorWithBrightnessMultiplier(int argb, float mult) {
		int alpha = ((argb >>> 24) & 0xFF);
		int red = Math.min(255, (int) (((argb >>> 16) & 0xFF)*mult)) & 0xFF;
		int green = Math.min(255, (int) (((argb >>> 8) & 0xFF)*mult)) & 0xFF;
		int blue = Math.min(255, (int) ((argb & 0xFF)*mult)) & 0xFF;
		int color = alpha;
		color = (color << 8) + red;
		color = (color << 8) + green;
		color = (color << 8) + blue;
		return color;
	}

	public static int getColorWithBrightnessMultiplierRGBA(int rgba, float mult) {
		int alpha = (rgba & 0xFF);
		int red = Math.min(255, (int) (((rgba >>> 24) & 0xFF)*mult)) & 0xFF;
		int green = Math.min(255, (int) (((rgba >>> 16) & 0xFF)*mult)) & 0xFF;
		int blue = Math.min(255, (int) (((rgba >>> 8) & 0xFF)*mult)) & 0xFF;
		int color = red;
		color = (color << 8) + green;
		color = (color << 8) + blue;
		color = (color << 8) + alpha;
		return color;
	}

	private static int getRGB(Color color) {
		return RGBFtoHex(color.getRed(), color.getGreen(), color.getBlue());
	}

	public static int getRed(int color) {
		return (color >>> 16) & 0xFF;
	}

	public static int getGreen(int color) {
		return (color >>> 8) & 0xFF;
	}

	public static int getBlue(int color) {
		return (color >>> 0) & 0xFF;
	}

	public static int getAlpha(int color) {
		return (color >>> 24) & 0xFF;
	}

	public static boolean isRGBNonZero(int color) {
		return (color & 0xffffff) != 0;
	}

	public static boolean isAlphaNonZero(int color) {
		return (color & 0xff000000) > 0;
	}

	/** If ratio is < 0.5, c2 is dominant */
	public static int mixColors(int c1, int c2, float ratio) {
		int a1 = (c1 & 0xff000000) >> 24;
		int a2 = (c2 & 0xff000000) >> 24;
		int r1 = (c1 & 0xff0000) >> 16;
		int r2 = (c2 & 0xff0000) >> 16;
		int g1 = (c1 & 0xff00) >> 8;
		int g2 = (c2 & 0xff00) >> 8;
		int b1 = (c1 & 0xff);
		int b2 = (c2 & 0xff);

		int r = (int)(r1*ratio + r2*(1-ratio));
		int g = (int)(g1*ratio + g2*(1-ratio));
		int b = (int)(b1*ratio + b2*(1-ratio));
		int a = (int)(a1*ratio + a2*(1-ratio));

		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	public static int additiveBlend(int color) {
		int rgb = color&0xFFFFFF;
		int r = getRed(color);
		int g = getGreen(color);
		int b = getBlue(color);
		int alpha = (r+g+b)/3;
		return rgb | (alpha << 24);
	}

	public static int invertColor(int rgb) {
		return RGBtoHex(255-getRed(rgb), 255-getGreen(rgb), 255-getBlue(rgb));
	}

	public static int multiplyChannels(int c, float r, float g, float b) {
		int r2 = (int)Math.min(255, r*getRed(c));
		int g2 = (int)Math.min(255, g*getGreen(c));
		int b2 = (int)Math.min(255, b*getBlue(c));
		return RGBtoHex(r2, g2, b2);
	}

	/** Alpha is 0-255, not 0-1! */
	public static int getColorWithAlpha(int color, float alpha) {
		return (color & 0xffffff) | (((int)alpha) << 24);
	}

	public static int mixColorBiDirectional(int c, int c1, int c2, float f) {
		return f <= 0.5 ? mixColors(c, c1, f*2) : mixColors(c2, c, (f-0.5F)*2);
	}

	public static Color getColor(int argb) {
		return Color.rgb(getRed(argb), getGreen(argb), getBlue(argb), getAlpha(argb)/255D);
	}

	public static Color getColor(int rgb, double alpha) {
		return Color.rgb(getRed(rgb), getGreen(rgb), getBlue(rgb), alpha);
	}

	public static String getCSSHexForColor(double frac) {
		return String.format("%02X", (int)(frac*255));
	}

	public static String getCSSHex(Color c) {
		return "#"+getCSSHexForColor(c.getRed())+getCSSHexForColor(c.getGreen())+getCSSHexForColor(c.getBlue());
	}
}
