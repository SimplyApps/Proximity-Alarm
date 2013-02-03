package arm.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import arm.core.HttpConnection.HttpConnectionResult;

public class JsonWebService<T extends JsonWebService.WebServiceData>
{
	private HttpConnection mConnection;

	private AsyncTask<String, T, WebServiceStatus> mTask;

	private JsonWebServiceListener<T> mListener;

	private ArrayList<BasicNameValuePair> mParams;
	private HttpEntity mEntity;

	private String[] mTags = { "docs" };

	public void setTags(String... tags)
	{
		mTags = tags;
	}

	public void setListener(JsonWebServiceListener<T> listener)
	{
		this.mListener = listener;
	}

	public boolean addParams(String name, String value)
	{
		if (mParams == null)
			mParams = new ArrayList<BasicNameValuePair>();

		mParams.add(new BasicNameValuePair(name, value));

		return true;
	}

	public boolean addParams(String name, int value)
	{
		if (mParams == null)
			mParams = new ArrayList<BasicNameValuePair>();

		mParams.add(new BasicNameValuePair(name, String.valueOf(value)));

		return true;
	}

	public boolean addParams(String name, double value)
	{
		if (mParams == null)
			mParams = new ArrayList<BasicNameValuePair>();

		DecimalFormat format = new DecimalFormat();
		DecimalFormatSymbols symbol = new DecimalFormatSymbols();
		symbol.setDecimalSeparator('.');
		format.setDecimalFormatSymbols(symbol);
		mParams.add(new BasicNameValuePair(name, format.format(value)));

		return true;
	}

	public boolean addParams(String name, boolean value)
	{
		if (mParams == null)
			mParams = new ArrayList<BasicNameValuePair>();

		mParams.add(new BasicNameValuePair(name, value ? "1" : "0"));

		return true;
	}

	public boolean setData(byte[] data)
	{
		if (mEntity == null)
		{
			mEntity = new ByteArrayEntity(data);
			return true;
		}
		return false;
	}

	public boolean setData(File data)
	{
		if (mEntity == null)
		{
			mEntity = new FileEntity(data, "image/jpeg");
			return true;
		}
		return false;
	}

	public boolean setData(String data)
	{
		if (mEntity == null)
		{
			try
			{
				mEntity = new StringEntity(data, HTTP.UTF_8);
			}
			catch (UnsupportedEncodingException e)
			{
			}
			return true;
		}
		return false;
	}

	public void openAsync(String url)
	{
		if (mTask != null)
			mTask.cancel(true);

		mTask = new AsyncTask<String, T, WebServiceStatus>()
		{
			private Exception mError;

			@Override
			protected WebServiceStatus doInBackground(String... params)
			{
				WebServiceStatus status = new WebServiceStatus();
				int result = 0;

				if (isCancelled())
				{
					status.mStatus = WebServiceStatus.STATUS_CANCELED;
					return status;
				}

				if (params != null && params.length > 0)
				{
					InputStream stream = null;
					ByteArrayOutputStream baos = null;
					HttpConnectionResult connectionResult = null;

					mConnection = new HttpConnection();

					try
					{
						if (isCancelled())
						{
							status.mStatus = WebServiceStatus.STATUS_CANCELED;
							return status;
						}

						if (mParams == null && mEntity == null)
							connectionResult = mConnection.open(params[0]);
						else
						{
							if (isCancelled())
							{
								status.mStatus = WebServiceStatus.STATUS_CANCELED;
								return status;
							}

							if (mEntity == null && mParams != null && mParams.size() > 0)
								mEntity = new UrlEncodedFormEntity(mParams);

							if (mEntity != null)
								connectionResult = mConnection.post(params[0], mEntity);
						}

						if (isCancelled())
						{
							status.mStatus = WebServiceStatus.STATUS_CANCELED;
							return status;
						}
						stream = connectionResult.getStatusStream();

						baos = new ByteArrayOutputStream();

						if (isCancelled())
						{
							status.mStatus = WebServiceStatus.STATUS_CANCELED;
							return status;
						}
						int size = 1024;
						byte[] buffer = new byte[size];
						while (size > 0)
						{
							if (isCancelled())
							{
								status.mStatus = WebServiceStatus.STATUS_CANCELED;
								return status;
							}

							size = stream.read(buffer, 0, 1024);
							if (size > 0)
								baos.write(buffer, 0, size);
						}

						if (isCancelled())
						{
							status.mStatus = WebServiceStatus.STATUS_CANCELED;
							return status;
						}
						String resultString = baos.toString();
						baos.close();

						if (isCancelled())
						{
							status.mStatus = WebServiceStatus.STATUS_CANCELED;
							return status;
						}
						result = parseAsync(resultString);
						status.mStatus = result;
					}
					catch (MalformedURLException e)
					{
						// if (mListener != null)
						// mListener.onError(e);
						mError = e;
					}
					catch (IOException e)
					{
						// if (mListener != null)
						// mListener.onError(e);
						mError = e;
					}
					catch (Exception e)
					{
						// if (mListener != null)
						// mListener.onError(e);
						mError = e;
					}
					finally
					{
						try
						{
							if (connectionResult != null)
								connectionResult.close();
						}
						catch (IOException e)
						{
						}

						try
						{
							if (baos != null)
								baos.close();
						}
						catch (IOException e)
						{
						}
					}
				}

				return status;
			}

			private int parseAsync(String jsonString)
			{
				int status = 0;
				try
				{
					if (isCancelled())
						return WebServiceStatus.STATUS_CANCELED;

					JSONObject object = new JSONObject(jsonString);

					status = object.optInt("status", 0);

					if (mTags != null)
					{
						for (String tag : mTags)
						{
							Object obj = object.opt(tag);

							if (obj instanceof JSONArray)
							{
								JSONArray docs = (JSONArray) obj;

								int count = docs.length();
								for (int i = 0; i < count; i++)
								{
									if (isCancelled())
										return WebServiceStatus.STATUS_CANCELED;

									JSONObject doc = docs.optJSONObject(i);

									if (doc != null && mListener != null)
									{
										T data = mListener.createData(tag);
										if (data != null)
										{
											data.mStatus = status;
											data.mTag = tag;
											data.mRawData = doc;
											data.parseData();

											publishProgress(data);
										}
									}
								}
							}
							else if (obj instanceof JSONObject)
							{
								JSONObject doc = (JSONObject) obj;

								if (isCancelled())
									return WebServiceStatus.STATUS_CANCELED;

								if (doc != null && mListener != null)
								{
									T data = mListener.createData(tag);
									if (data != null)
									{
										data.mStatus = status;
										data.mTag = tag;
										data.mRawData = doc;
										data.parseData();

										publishProgress(data);
									}
								}
							}
							else if (obj != null)
							{
								if (isCancelled())
									return WebServiceStatus.STATUS_CANCELED;

								T data = mListener.createData(tag);
								if (data != null)
								{
									data.mStatus = status;
									data.mTag = tag;
									data.mRawData = new JSONObject();
									data.mRawData.put(tag, obj);
									data.parseData();

									publishProgress(data);
								}
							}
						}
					}

					Object obj = object.opt(WebServiceData.TAG_MESSAGE);

					// JSONObject doc =
					// object.optJSONObject(WebServiceData.TAG_MESSAGE);

					if (isCancelled())
						return WebServiceStatus.STATUS_CANCELED;

					if (obj instanceof JSONObject)
					{
						JSONObject doc = (JSONObject) obj;

						if (mListener != null)
						{
							T data = mListener.createData(WebServiceData.TAG_MESSAGE);
							if (data != null)
							{
								data.mStatus = status;
								data.mTag = WebServiceData.TAG_MESSAGE;
								data.mRawData = doc;
								data.parseMessage();

								publishProgress(data);
							}
						}
					}
					else if (obj instanceof JSONArray)
					{
						JSONArray doc = (JSONArray) obj;

						if (mListener != null)
						{
							int length = doc.length();

							HashMap<String, String> messageMaps = null;

							for (int i = 0; i < length; i++)
							{
								if (isCancelled())
									return WebServiceStatus.STATUS_CANCELED;

								obj = doc.opt(i);

								if (obj instanceof JSONObject)
								{
									T data = mListener.createData(WebServiceData.TAG_MESSAGE);
									if (data != null)
									{
										data.mStatus = status;
										data.mTag = WebServiceData.TAG_MESSAGE;
										data.mRawData = (JSONObject) obj;
										data.parseMessage();

										publishProgress(data);
									}
								}
								else if (obj != null)
								{
									if (messageMaps == null)
										messageMaps = new HashMap<String, String>();

									messageMaps.put(String.valueOf(i), obj.toString());
								}
							}

							if (messageMaps != null && messageMaps.size() > 0)
							{
								T data = mListener.createData(WebServiceData.TAG_MESSAGE);
								if (data != null)
								{
									data.mStatus = status;
									data.mTag = WebServiceData.TAG_MESSAGE;
									data.mMessages = messageMaps;

									publishProgress(data);
								}
							}
						}
					}
				}
				catch (JSONException e)
				{
					// if (mListener != null)
					// mListener.onError(e);
					mError = e;
				}

				return status;
			}

			@Override
			protected void onProgressUpdate(T... values)
			{
				if (values != null && values.length > 0)
				{
					for (T t : values)
					{
						if (mListener != null && !isCancelled())
							mListener.onProgress(t);
					}
				}
			}

			@Override
			protected void onPostExecute(WebServiceStatus result)
			{
				if (mListener != null && !isCancelled())
				{
					if (mError != null)
						mListener.onError(mError);
					else
						mListener.onResultReceived(result);
				}

				mEntity = null;

				if (mParams != null)
					mParams.clear();
			}

			@Override
			protected void onCancelled()
			{
				if (mConnection != null)
					mConnection.cancel();

				super.onCancelled();
			}
		};

		mTask.execute(url);
	}

	public void cancel()
	{
		if (mTask != null)
			mTask.cancel(true);

		mEntity = null;

		if (mParams != null)
			mParams.clear();
	}

	public static class WebServiceData
	{
		public static String TAG_MESSAGE = "message";

		private int mStatus;
		protected String mTag;
		private HashMap<String, String> mMessages;
		protected JSONObject mRawData = null;

		protected void parseData()
		{
		}

		private void parseMessage()
		{
			mMessages = new HashMap<String, String>();

			Iterator<String> keys = mRawData.keys();
			while (keys.hasNext())
			{
				String key = keys.next();
				mMessages.put(key, mRawData.optString(key));
			}
		}

		public HashMap<String, String> getMessages()
		{
			return mMessages;
		}

		public String getMessage(String code)
		{
			if (mMessages != null)
				return mMessages.get(code);
			return null;
		}

		public String getTag()
		{
			return mTag;
		}

		public JSONObject getRawData()
		{
			return mRawData;
		}

		public String getString(String key)
		{
			return mRawData.optString(key);
		}

		public int getInt(String key, int def)
		{
			return mRawData.optInt(key, def);
		}

		public int getInt(String key)
		{
			return getInt(key, -1);
		}

		public double getDouble(String key, double def)
		{
			return mRawData.optDouble(key, def);
		}

		public double getDouble(String key)
		{
			return getDouble(key, -1);
		}

		public boolean isTrue(String key, boolean def)
		{
			if (mRawData.has(key))
			{
				String result = mRawData.optString(key);

				try
				{
					if ("1".equals(result))
						return true;
					else
						return Boolean.parseBoolean(result);
				}
				catch (Exception e)
				{
				}
			}

			return def;
		}

		public boolean isTrue(String key)
		{
			return isTrue(key, false);
		}

		public int getStatus()
		{
			return mStatus;
		}
	}

	public class WebServiceStatus
	{
		public static final int STATUS_FAILED = 0;
		public static final int STATUS_SUCCESS = 1;
		public static final int STATUS_MODERATED = 2;
		public static final int STATUS_CANCELED = -1;

		private int mStatus = 0;;

		public int getStatus()
		{
			return mStatus;
		}
	}

	public interface JsonWebServiceListener<T extends WebServiceData>
	{
		public T createData(String tag);

		public void onProgress(T data);

		public void onResultReceived(JsonWebService<T>.WebServiceStatus result);

		public void onError(Exception e);
	}
}
