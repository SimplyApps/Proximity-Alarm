package arm.proximity.alarm.apis;

import org.json.JSONException;
import org.json.JSONObject;

import arm.core.WebService;
import arm.core.WebServiceData;
import arm.proximity.alarm.Logger;

public class GooglePlaceDetail extends WebService
{
	public static final String REFERENCE = "reference";
	public static final String IS_USING_SENSOR = "sensor";

	private static final String API_KEY_NAME = "key";
	private static final String API_KEY = "AIzaSyAUBASwM0Op6_j-aK5Tv_gfDYPPH7cSHF8";

	@Override
	protected String getUrl()
	{
		String url = "https://maps.googleapis.com/maps/api/place/details/json?" + API_KEY_NAME + "=" + API_KEY;
		return url + "&" + getParams();
	}

	@Override
	protected WebServiceData createData()
	{
		return new PlaceDetailItem();
	}

	@Override
	protected String getResultTag()
	{
		return "result";
	}

	public class PlaceDetailItem extends WebServiceData
	{
		public String name;
		public String address;
		public double longitude;
		public double latitude;

		@Override
		public void parseData(JSONObject jsonObject)
		{
			try
			{
				if (jsonObject.has("name"))
					name = jsonObject.getString("name");

				if (jsonObject.has("formatted_address"))
					address = jsonObject.getString("formatted_address");

				if (jsonObject.has("geometry"))
				{
					Object obj = jsonObject.get("geometry");
					if (obj instanceof JSONObject)
					{
						JSONObject jsonObj = (JSONObject) obj;
						if (jsonObj.has("location"))
						{
							obj = jsonObj.get("location");

							if (obj instanceof JSONObject)
							{
								jsonObj = (JSONObject) obj;

								if (jsonObj.has("lat"))
									latitude = jsonObj.getDouble("lat");

								if (jsonObj.has("lng"))
									longitude = jsonObj.getDouble("lng");
							}
						}
					}
				}
			}
			catch (JSONException e)
			{
				Logger.e(e);
			}
		}
	}
}
