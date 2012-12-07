package arm.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;

public class CrashHandler implements UncaughtExceptionHandler
{
	private String mPath = "";

	public CrashHandler(Context context)
	{
		mPath = context.getFilesDir().getPath() + "/proximity_crash_report/";
	}

	public void uncaughtException(Thread thread, Throwable ex)
	{
		if (ex != null)
		{
			File file = new File(mPath);
			if (!file.exists())
				file.mkdirs();

			FileOutputStream fos = null;
			try
			{
				fos = new FileOutputStream(mPath + System.currentTimeMillis(), true);
				fos.write(ex.getMessage().getBytes());
			}
			catch (Exception e)
			{
			}
			finally
			{
				try
				{
					if (fos != null)
						fos.close();
				}
				catch (Exception e)
				{
				}
			}
		}
	}
}
