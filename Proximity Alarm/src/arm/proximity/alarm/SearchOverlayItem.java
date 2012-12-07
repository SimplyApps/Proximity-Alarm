package arm.proximity.alarm;

import arm.proximity.alarm.apis.PlaceItem;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class SearchOverlayItem extends OverlayItem
{
	private PlaceItem mAddress;

	public PlaceItem getAddress()
	{
		return mAddress;
	}

	public SearchOverlayItem(PlaceItem address)
	{
		super(new GeoPoint((int) (address.latitude * 1e6), (int) (address.longitude * 1e6)), address.name,
				address.address);

		mAddress = address;
	}
}
