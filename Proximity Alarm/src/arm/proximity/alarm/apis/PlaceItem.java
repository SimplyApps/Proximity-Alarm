package arm.proximity.alarm.apis;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import arm.core.WebServiceData;

public class PlaceItem extends WebServiceData implements Parcelable
{
	public String name;
	public String address;
	public double longitude;
	public double latitude;

	public PlaceItem()
	{
	}

	@Override
	public void parseData(JSONObject jsonObject)
	{
		name = jsonObject.optString("name");
		address = jsonObject.optString("vicinity");

		JSONObject jo = jsonObject.optJSONObject("geometry");
		if (jo != null)
		{
			jo = jo.optJSONObject("location");
			if (jo != null)
			{
				latitude = jo.optDouble("lat", 0);
				longitude = jo.optDouble("lng", 0);
			}
		}
	}

	public int describeContents()
	{
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(name);
		dest.writeString(address);
		dest.writeDouble(longitude);
		dest.writeDouble(latitude);
	}

	public static final Parcelable.Creator<PlaceItem> CREATOR = new Creator<PlaceItem>()
	{
		public PlaceItem[] newArray(int size)
		{
			return new PlaceItem[size];
		}

		public PlaceItem createFromParcel(Parcel source)
		{
			return new PlaceItem(source);
		}
	};

	private PlaceItem(Parcel source)
	{
		name = source.readString();
		address = source.readString();
		longitude = source.readDouble();
		latitude = source.readDouble();
	}
}