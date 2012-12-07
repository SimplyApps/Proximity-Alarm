package arm.proximity.alarm;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.maps.GeoPoint;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

public class SearchService
{
	private Context mContext;
	private Geocoder mGeocoder;

	private AsyncTask<String, Address, Boolean> mSearchTask;

	public SearchService(Context context)
	{
		mContext = context;

		mGeocoder = new Geocoder(context);
	}

	public void searchAsync(String query, final double lowerLeftLatitude, final double lowerLeftLongitude,
			final double upperRightLatitude, final double upperRightLongitude, final int maxResult,
			final Action1<List<Address>> callback)
	{
		if (mSearchTask != null)
			mSearchTask.cancel(true);

		mSearchTask = new AsyncTask<String, Address, Boolean>()
		{

			@Override
			protected Boolean doInBackground(String... params)
			{
				List<Address> result = null;
				try
				{
					result = mGeocoder.getFromLocationName(params[0], maxResult, lowerLeftLatitude, lowerLeftLongitude,
							upperRightLatitude, upperRightLongitude);
				}
				catch (IOException e)
				{
					Logger.e(e);
				}

				if (!isCancelled())
				{
					if (callback != null)
						callback.run(result);

					if (result != null)
						return true;
				}
				return false;
			}
		};

		mSearchTask.execute(query);
	}

	public void searchAsync(String query, final GeoPoint center, final int maxResult,
			final Action1<List<Address>> callback)
	{
		if (mSearchTask != null)
			mSearchTask.cancel(true);

		mSearchTask = new AsyncTask<String, Address, Boolean>()
		{

			@Override
			protected Boolean doInBackground(String... params)
			{
				List<Address> result = null;
				try
				{
					List<Address> centerAddress = mGeocoder.getFromLocation(center.getLatitudeE6() / 1000000d,
							center.getLongitudeE6() / 1000000d, 1);

					if (centerAddress != null && centerAddress.size() > 0)
					{
						Locale local = new Locale("en", "ID");
						Geocoder geocoder = new Geocoder(mContext, local);
						result = geocoder.getFromLocationName(params[0], 5);

						// Locale[] localeList = Locale.getAvailableLocales();
						// if (localeList != null)
						// {
						// Locale useLocale = null;
						//
						// for (Locale locale : localeList)
						// {
						// String country = locale.getCountry();
						// String addressCountry =
						// centerAddress.get(0).getCountryCode();
						//
						// if (country.equals(addressCountry))
						// useLocale = locale;
						// }
						//
						// if (useLocale != null)
						// {
						// Geocoder geoCode = new Geocoder(mContext, useLocale);
						// result = geoCode.getFromLocationName(params[0],
						// maxResult);
						// }
						// }
						// else
						// {
						// Geocoder geoCode = new Geocoder(mContext,
						// centerAddress.get(0).getLocale());
						// result = geoCode.getFromLocationName(params[0],
						// maxResult);
						// }

						// String[] isoCountries = Locale.getISOCountries();
						// if (isoCountries != null)
						// {
						// Locale useLocale = null;
						//
						// for (String locale : isoCountries)
						// {
						// String addressCountry =
						// centerAddress.get(0).getCountryCode();
						//
						// if (locale.equals(addressCountry))
						// {
						// useLocale = new Locale("en", locale);
						// break;
						// }
						// }
						//
						// if (useLocale != null)
						// {
						// Geocoder geoCode = new Geocoder(mContext, useLocale);
						// result = geoCode.getFromLocationName(params[0],
						// maxResult);
						// }
						// }
						// else
						// {
						// Geocoder geoCode = new Geocoder(mContext,
						// centerAddress.get(0).getLocale());
						// result = geoCode.getFromLocationName(params[0],
						// maxResult);
						// }
					}
				}
				catch (IOException e)
				{
					Logger.e(e);
				}

				if (!isCancelled())
				{
					if (callback != null)
						callback.run(result);

					if (result != null)
						return true;
				}
				return false;
			}
		};

		mSearchTask.execute(query);
	}

	public void searchAsync(String query, final int maxResult, final Action1<List<Address>> callback)
	{
		if (mSearchTask != null)
			mSearchTask.cancel(true);

		mSearchTask = new AsyncTask<String, Address, Boolean>()
		{

			@Override
			protected Boolean doInBackground(String... params)
			{
				List<Address> result = null;
				try
				{
					result = mGeocoder.getFromLocationName(params[0], maxResult);
				}
				catch (IOException e)
				{
					Logger.e(e);
				}

				if (!isCancelled())
				{
					if (callback != null)
						callback.run(result);

					if (result != null)
						return true;
				}
				return false;
			}
		};

		mSearchTask.execute(query);
	}
}
