package Reika.SatisfactoryPlanner.Util;

import java.nio.file.Paths;

public class StringUtil {

	public static boolean isValidPath(String s) {
		try {
			Paths.get(s);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

}
