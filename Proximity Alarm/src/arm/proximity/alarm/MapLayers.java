package arm.proximity.alarm;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class MapLayers extends ItemizedOverlay<OverlayItem>
{

	private ArrayList<OverlayItem> mLayers = new ArrayList<OverlayItem>();
	private Context mContext;

	public MapLayers(Drawable defaultMarker)
	{
		super(boundCenterBottom(defaultMarker));
	}

	public MapLayers(Drawable defaultMarker, Context context)
	{
		super(boundCenterBottom(defaultMarker));
		mContext = context;
	}

	@Override
	protected OverlayItem createItem(int i)
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
		OverlayItem item = mLayers.get(index);
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet());
		dialog.show();
		return true;
	}

	public void addLayer(OverlayItem item)
	{
		mLayers.add(item);
		populate();
	}
}
