package arm.proximity.alarm;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import arm.core.CrashHandler;
import arm.proximity.alarm.apis.GooglePlaceAutocomplete.AutocompleteItem;
import arm.proximity.alarm.apis.GooglePlaceDetail.PlaceDetailItem;
import arm.proximity.alarm.apis.PlaceItem;

public class MainActivity extends MapActivity
{
	private LocationService mLocationService;
	private MapSearchLayer mMapSearchLayers;
	private MapController mMapController;
	private MapView mMapView;
	private SearchView mSearchView;
	private ImageButton mButtonShowResult;
	private ImageButton mButtonToggleView;

	private GeoPoint mCenterPoint;

	private SearchOverlayItem mSelectedAddress;
	private PlaceInfoView mPlaceInfoView;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));

		mMapView = (MapView) findViewById(R.id.mapView);
		mMapView.setSatellite(false);
		// mMapView.setBuiltInZoomControls(true);

		List<Overlay> overlayList = mMapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(R.drawable.androidmarker);
		mMapSearchLayers = new MapSearchLayer(drawable, this);
		mMapSearchLayers.setOnItemSelectedCallback(new Action1<SearchOverlayItem>()
		{
			@Override
			public void run(SearchOverlayItem overlayItem)
			{
				PlaceItem item = overlayItem.getAddress();
				if (item != null)
				{
					mPlaceInfoView.setItem(item);
					mPlaceInfoView.show();

					mCenterPoint = new GeoPoint((int) (item.latitude * 1e6), (int) (item.longitude * 1e6));
					mMapController.animateTo(mCenterPoint);
				}
			}
		});

		// GeoPoint point = new GeoPoint(-6202619, 106777804);
		// OverlayItem overlayItem = new OverlayItem(point, "Kontrakan",
		// "Deket Binus Anggrek");

		// mMapLayers.addLayer(overlayItem);
		overlayList.add(mMapSearchLayers);

		mMapController = mMapView.getController();
		// mapController.setCenter(point);
		// mapController.setZoom(18);

		mPlaceInfoView = new PlaceInfoView(this, mMapView);
		mPlaceInfoView.hide();

		mSearchView = (SearchView) findViewById(R.id.searchView);
		mSearchView.setUseAutoSuggest(false);
		mSearchView.setMapView(mMapView);
		mSearchView.setOnAddressSelectedCallback(new Action1<PlaceItem>()
		{
			@Override
			public void run(PlaceItem address)
			{
				mCenterPoint = new GeoPoint((int) (address.latitude * 1e6), (int) (address.longitude * 1e6));

				mPlaceInfoView.setItem(address);
				mPlaceInfoView.show();

				mMapController.animateTo(mCenterPoint);
			}
		});
		mSearchView.setOnStartSearchCallback(new Action()
		{
			@Override
			public void run()
			{
				mMapSearchLayers.clear();
			}
		});
		mSearchView.setOnSearchResultReceivedCallback(new Action()
		{
			@Override
			public void run()
			{
				List<PlaceItem> placeList = mSearchView.getPlaceList();
				if (placeList != null)
				{
					for (PlaceItem placeItem : placeList)
					{
						SearchOverlayItem layer = new SearchOverlayItem(placeItem);
						mMapSearchLayers.addLayer(layer);
					}

					if (placeList.size() > 1)
						mButtonShowResult.setVisibility(View.VISIBLE);
					else
						mButtonShowResult.setVisibility(View.GONE);
				}
			}
		});

		mLocationService = new LocationService(this);
		mLocationService.initialize();
		mLocationService.setOnLocationReceivedCallback(new Action1<Location>()
		{
			@Override
			public void run(Location location)
			{
				if (location != null)
				{
					int longitude = (int) (location.getLongitude() * 1000000);
					int latitude = (int) (location.getLatitude() * 1000000);
					mCenterPoint = new GeoPoint(latitude, longitude);

					mMapView.post(new Runnable()
					{
						public void run()
						{
							mMapController.animateTo(mCenterPoint);
							mMapController.setZoom(21);
						}
					});
				}
			}
		});

		mButtonShowResult = (ImageButton) findViewById(R.id.buttonShowResult);
		mButtonShowResult.setOnClickListener(new ImageButton.OnClickListener()
		{
			public void onClick(View v)
			{
				mSearchView.showResult();
			}
		});

		if (!mLocationService.isEnabled())
		{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("Error");
			dialog.setMessage("Location Service is disabled, please enable Location Service");
			dialog.setPositiveButton("OK", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					mLocationService.callSettings(MainActivity.this);
				}
			});
			dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					finish();
				}
			});
			dialog.show();
		}

		mButtonToggleView = (ImageButton) findViewById(R.id.buttonToggleView);
		mButtonToggleView.setOnClickListener(new ImageButton.OnClickListener()
		{
			public void onClick(View v)
			{
				mMapView.setSatellite(!mMapView.isSatellite());
			}
		});

		mLocationService.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.menu_search)
		{
			mSearchView.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed()
	{
		if (mSearchView.isVisible())
			mSearchView.hide();
		else
			super.onBackPressed();
	}

	@Override
	protected boolean isRouteDisplayed()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		mLocationService.stop();
	}

	@Override
	public boolean onSearchRequested()
	{
		return super.onSearchRequested();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == LocationService.SETTING_REQUEST_CODE)
		{

		}
	}
}
