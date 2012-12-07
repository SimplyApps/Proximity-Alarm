package arm.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;
import arm.proximity.alarm.Action1;
import arm.proximity.alarm.Logger;

public abstract class WebService
{
	protected HashMap<String, String> mInputList = new HashMap<String, String>();

	protected abstract String getUrl();

	protected String getParams()
	{
		String params = "";
		for (Entry<String, String> input : mInputList.entrySet())
		{
			if (params.length() > 0)
				params += "&";

			params += input.getKey() + "=" + URLEncoder.encode(input.getValue());
		}
		return params;
	}

	protected abstract WebServiceData createData();

	protected abstract String getResultTag();

	protected byte[] readStream(InputStream stream) throws Exception
	{
		ByteArrayOutputStream baos = null;

		try
		{
			baos = new ByteArrayOutputStream();
			int length = 1024;
			while (length > 0)
			{
				byte[] buffer = new byte[1024];
				length = stream.read(buffer, 0, length);

				if (length > 0)
					baos.write(buffer, 0, length);
			}

			return baos.toByteArray();
		}
		finally
		{
			if (baos != null)
				baos.close();
		}
	}

	protected void parseData(byte[] data, Result result)
	{
		try
		{
			String stringData = EncodingUtils.getString(data, 0, data.length, "UTF-8");
			// JSONArray jsonArray = new JSONArray(stringData);
			//
			// int jsonArraySize = jsonArray.length();
			// for (int i = 0; i < jsonArraySize; i++)
			// {
			// Object obj = jsonArray.get(0);
			// if (obj instanceof JSONObject)
			// {
			JSONObject jsonObject = new JSONObject(stringData); // (JSONObject)
																// obj;
			if (jsonObject.has("status"))
				result.jsonStatus = jsonObject.getString("status");
			if (jsonObject.has("html_attributions"))
				result.htmlAttribution = jsonObject.getString("html_attributions");
			if (jsonObject.has(getResultTag()))
			{
				if (result.isAsync && mServiceTask != null && mServiceTask.isCancelled())
					return;

				Object resultObject = jsonObject.get(getResultTag());
				if (resultObject instanceof JSONObject)
				{
					WebServiceData progressData = createData();
					progressData.parseData((JSONObject) resultObject);

					if (result.isAsync)
					{
						Progress progress = new Progress();
						progress.data = progressData;

						if (result.isAsync && mServiceTask != null && mServiceTask.isCancelled())
							return;

						onProgress(progress);
					}
					else
						result.dataList.add(progressData);
				}
				else if (resultObject instanceof JSONArray)
				{
					JSONArray resultArray = (JSONArray) resultObject;
					int resultArraySize = resultArray.length();
					for (int j = 0; j < resultArraySize; j++)
					{
						Object resultArrayObject = resultArray.get(j);
						if (resultArrayObject instanceof JSONObject)
						{
							WebServiceData progressData = createData();
							progressData.parseData((JSONObject) resultArrayObject);

							if (result.isAsync)
							{
								Progress progress = new Progress();
								progress.data = progressData;

								if (result.isAsync && mServiceTask != null && mServiceTask.isCancelled())
									return;

								onProgress(progress);
							}
							else
								result.dataList.add(progressData);
						}

						if (result.isAsync && mServiceTask != null && mServiceTask.isCancelled())
							return;
					}
				}
			}
			// }

			if (result.isAsync && mServiceTask != null && mServiceTask.isCancelled())
				return;
			// }
		}
		catch (Exception e)
		{
			Logger.e(e);
			result.exception = e;
		}
	}

	private Result open(boolean async)
	{
		Result result = new Result();
		result.isAsync = async;
		if (!async)
			result.dataList = new ArrayList<WebServiceData>();

		HttpURLConnection connection = null;
		InputStream inputStream = null;
		try
		{
			URL url = new URL(getUrl());
			connection = (HttpURLConnection) url.openConnection();
			connection.connect();
			result.responseCode = connection.getResponseCode();
			result.responseMessage = connection.getResponseMessage();

			if (async && mServiceTask != null && mServiceTask.isCancelled())
				return null;

			if (result.responseCode == HttpsURLConnection.HTTP_OK)
			{
				inputStream = connection.getInputStream();

				if (async && mServiceTask != null && mServiceTask.isCancelled())
					return null;

				byte[] data = readStream(inputStream);

				if (async && mServiceTask != null && mServiceTask.isCancelled())
					return null;

				parseData(data, result);
			}
		}
		catch (MalformedURLException e)
		{
			Logger.e(e);
			result.exception = e;
		}
		catch (IOException e)
		{
			Logger.e(e);
			result.exception = e;
		}
		catch (Exception e)
		{
			Logger.e(e);
			result.exception = e;
		}
		finally
		{
			try
			{
				if (inputStream != null)
					inputStream.close();
			}
			catch (IOException e)
			{
				Logger.e(e);
			}

			if (connection != null)
				connection.disconnect();
		}

		return result;
	}

	public Result open()
	{
		return open(false);
	}

	private AsyncTask<Void, Void, Void> mServiceTask = null;

	public void openAsync()
	{
		if (mServiceTask != null)
			mServiceTask.cancel(true);

		mServiceTask = new AsyncTask<Void, Void, Void>()
		{
			@Override
			protected Void doInBackground(Void... params)
			{
				Result result = open(true);

				if (!isCancelled())
					onResult(result);

				return null;
			}
		};

		mServiceTask.execute(new Void[] { null });
	}

	public void cancel()
	{
		if (mServiceTask != null)
			mServiceTask.cancel(true);

		onProgressCallback = null;
		onResultCallback = null;
	}

	public void addInput(String key, String value)
	{
		mInputList.put(key, value);
	}

	public String getInput(String key)
	{
		return mInputList.get(key);
	}

	private Action1<Progress> onProgressCallback;

	public void setOnProgressCallback(Action1<Progress> onProgressCallback)
	{
		this.onProgressCallback = onProgressCallback;
	}

	protected void onProgress(Progress progress)
	{
		if (onProgressCallback != null)
			onProgressCallback.run(progress);
	}

	private Action1<Result> onResultCallback;

	public void setOnResultCallback(Action1<Result> onResultCallback)
	{
		this.onResultCallback = onResultCallback;
	}

	protected void onResult(Result result)
	{
		if (onResultCallback != null)
			onResultCallback.run(result);
	}

	public class Progress
	{
		public WebServiceData data;
	}

	public class Result
	{
		private boolean isAsync;
		public int responseCode;
		public String responseMessage;
		public Exception exception;
		public String jsonStatus;
		public String htmlAttribution;
		private ArrayList<WebServiceData> dataList;

		public ArrayList<WebServiceData> getDataList()
		{
			return dataList;
		}
	}
}
