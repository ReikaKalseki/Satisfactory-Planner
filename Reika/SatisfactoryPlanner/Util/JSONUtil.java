package Reika.SatisfactoryPlanner.Util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
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

}
