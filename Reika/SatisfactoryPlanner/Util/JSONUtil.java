package Reika.SatisfactoryPlanner.Util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.base.Charsets;

public class JSONUtil {

	public static JSONObject readFile(File f) throws IOException {
		return f.exists() && f.isFile() ? new JSONObject(FileUtils.readFileToString(f, Charsets.UTF_8)) : new JSONObject();
	}

	public static void saveFile(File f, JSONObject obj) throws IOException {
		f.getAbsoluteFile().getParentFile().mkdirs();
		FileUtils.write(f, obj.toString(4), Charsets.UTF_8);
	}

	public static boolean getBoolean(JSONObject obj, String key) {
		return obj.has(key) && obj.getBoolean(key);
	}

	public static String getString(JSONObject obj, String key, String fallback) {
		return obj.has(key) ? obj.getString(key) : fallback;
	}

	public static int getInt(JSONObject obj, String key, int fallback) {
		return obj.has(key) ? obj.getInt(key) : fallback;
	}

	public static float getFloat(JSONObject obj, String key, float fallback) {
		return obj.has(key) ? obj.getFloat(key) : fallback;
	}

	public static JSONArray getArray(JSONObject obj, String key, JSONArray fallback) {
		return obj.has(key) ? obj.getJSONArray(key) : fallback;
	}

}
