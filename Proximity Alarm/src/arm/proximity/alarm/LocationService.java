package arm.proximity.alarm;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

public class LocationService implements LocationListener
{
	public static final int SETTING_REQUEST_CODE = 1;

	private Context mContext;
	private LocationManager mLocationManager;

	private Location mLastLocation;

	public LocationService(Context context)
	{
		mContext = context;
		mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	}

	public void initialize()
	{
		Criteria criteria = new Criteria();
	}

	public boolean isEnabled()
	{
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);

		String provider = mLocationManager.getBestProvider(criteria, true);

		if (provider != null)
			return true;
		return false;
	}

	public void start()
	{
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);

		String provider = mLocationManager.getBestProvider(criteria, true);

		if (provider != null)
		{
			mLocationManager.requestLocationUpdates(provider, 1000, 10, this);
		}
	}

	public void stop()
	{
		mLocationManager.removeUpdates(this);
	}

	public void callSettings(Activity activity)
	{
		Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		activity.startActivityForResult(settingsIntent, SETTING_REQUEST_CODE);
	}

	private Action1<Location> onLocationReceivedCallback;

	public void setOnLocationReceivedCallback(Action1<Location> onLocationReceivedCallback)
	{
		this.onLocationReceivedCallback = onLocationReceivedCallback;
	}

	protected void onLocationReceived(Location location)
	{
		if (isBetterLocation(location, mLastLocation))
		{
			mLastLocation = location;

			if (onLocationReceivedCallback != null)
				onLocationReceivedCallback.run(location);
		}
	}

	public void onLocationChanged(Location location)
	{
		onLocationReceived(location);
	}

	public void onProviderDisabled(String provider)
	{
		// TODO Auto-generated method stub

	}

	public void onProviderEnabled(String provider)
	{
		// TODO Auto-generated method stub

	}

	public void onStatusChanged(String provider, int status, Bundle extras)
	{
		// TODO Auto-generated method stub

	}

	private static final int TWO_MINUTES = 1000 * 60 * 2;

	private boolean isBetterLocation(Location location, Location currentLocation)
	{
		if (currentLocation == null)
			return true;

		long timeDelta = location.getTime() - currentLocation.getTime();

		if (timeDelta > TWO_MINUTES)
			return true;
		if (timeDelta < TWO_MINUTES)
			return false;

		boolean isNewer = timeDelta > 0;

		int accuracyDelta = (int) (location.getAccuracy() - currentLocation.getAccuracy());

		if (accuracyDelta < 0)
			return true;
		else if (isNewer)
		{
			if (accuracyDelta == 0)
				return true;
			else if (accuracyDelta < 200 && isSameProvider(location.getProvider(), currentLocation.getProvider()))
				return true;
		}

		return false;
	}

	private boolean isSameProvider(String provider1, String provider2)
	{
		if (provider1 == null)
			return provider2 == null;

		return provider1.equals(provider2);
	}
}
