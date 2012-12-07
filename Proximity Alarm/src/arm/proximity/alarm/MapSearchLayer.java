package arm.proximity.alarm;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MapSearchLayer extends ItemizedOverlay<SearchOverlayItem>
{
	private ArrayList<SearchOverlayItem> mLayers = new ArrayList<SearchOverlayItem>();
	private Context mContext;

	public MapSearchLayer(Drawable defaultMarker)
	{
		super(boundCenterBottom(defaultMarker));
	}

	public MapSearchLayer(Drawable defaultMarker, Context context)
	{
		super(boundCenterBottom(defaultMarker));
		mContext = context;
	}

	@Override
	protected SearchOverlayItem createItem(int i)
	{
		return mLayers.get(i);
	}

	@Override
	public int size()
	{
		return mLayers.size();
	}

	@Override
	protected boolean onTap(int index)
	{
		SearchOverlayItem item = mLayers.get(index);
		// AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		// dialog.setTitle(item.getTitle());
		// dialog.setMessage(item.getSnippet());
		// dialog.show();
		onItemSelected(item);
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView)
	{
		if (mLayers.size() > 0)
			return super.onTouchEvent(event, mapView);
		return false;
	}

	@Override
	public boolean onTap(GeoPoint p, MapView mapView)
	{
		if (mLayers.size() > 0)
			return super.onTap(p, mapView);
		return false;
	}

	public void addLayer(SearchOverlayItem item)
	{
		mLayers.add(item);
		populate();
	}

	public void removeLayer(SearchOverlayItem item)
	{
		if (mLayers.contains(item))
			mLayers.remove(item);
		populate();
	}

	public void clear()
	{
		mLayers.clear();
		populate();
	}

	private Action1<SearchOverlayItem> onItemSelectedCallback;

	public void setOnItemSelectedCallback(Action1<SearchOverlayItem> onItemSelectedCallback)
	{
		this.onItemSelectedCallback = onItemSelectedCallback;
	}

	protected void onItemSelected(SearchOverlayItem item)
	{
		if (onItemSelectedCallback != null)
			onItemSelectedCallback.run(item);
	}
}
