package arm.proximity.alarm.apis;

import java.net.URLEncoder;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import arm.core.WebService;
import arm.core.WebServiceData;
import arm.proximity.alarm.Logger;

public class GooglePlaceAutocomplete extends WebService
{
	public static final String SEARCH_KEY = "input";
	public static final String IS_SENSOR = "sensor";
	public static final String LOCATION = "location";
	public static final String RADIUS = "radius";
	public static final String TEXT_LENGTH = "offset";

	private static final String API_KEY_NAME = "key";
	private static final String API_KEY = "AIzaSyAUBASwM0Op6_j-aK5Tv_gfDYPPH7cSHF8";

	@Override
	public String getUrl()
	{
		String url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?" + API_KEY_NAME + "=" + API_KEY;

		return url + "&" + getParams();
	}

	@Override
	public WebServiceData createData()
	{
		return new AutocompleteItem();
	}

	@Override
	protected String getResultTag()
	{
		return "predictions";
	}

	public class AutocompleteItem extends WebServiceData
	{
		public String description;
		public String id;
		public String reference;

		@Override
		public void parseData(JSONObject jsonObject)
		{
			try
			{
				if (jsonObject.has("description"))
					description = jsonObject.getString("description");

				if (jsonObject.has("id"))
					id = jsonObject.getString("id");

				if (jsonObject.has("reference"))
					reference = jsonObject.getString("reference");
			}
			catch (JSONException e)
			{
				Logger.e(e);
			}
		}
	}
}
