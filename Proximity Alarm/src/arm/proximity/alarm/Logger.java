package arm.proximity.alarm;

import android.util.Log;

public class Logger
{
	private static final String tag = "arm";

	public static void e(String message)
	{
		Log.e(tag, message);
	}

	public static void e(Exception e)
	{
		if (e != null)
			Logger.e(e.getMessage());
	}
}
