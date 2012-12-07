package arm.proximity.alarm.apis;

import arm.core.WebService;
import arm.core.WebServiceData;

public class GooglePlaceSearch extends WebService
{
	private static final String API_KEY_NAME = "key";
	private static final String API_KEY = "AIzaSyAUBASwM0Op6_j-aK5Tv_gfDYPPH7cSHF8";

	public static final String LOCATION = "location";
	// public static final String RADIUS = "radius";
	public static final String IS_SENSOR = "sensor";
	public static final String KEYWORD = "keyword";

	@Override
	protected String getUrl()
	{
		String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" + API_KEY_NAME + "=" + API_KEY
				+ "&rankby=distance";

		return url + "&" + getParams();
	}

	@Override
	protected WebServiceData createData()
	{
		return new PlaceItem();
	}

	@Override
	protected String getResultTag()
	{
		return "results";
	}
}
