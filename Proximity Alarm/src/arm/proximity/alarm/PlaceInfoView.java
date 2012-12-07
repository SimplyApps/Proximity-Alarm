package arm.proximity.alarm;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import arm.proximity.alarm.apis.PlaceItem;

public class PlaceInfoView extends LinearLayout
{
	private Context mContext;
	private MapView mMapView;
	private MapView.LayoutParams mPopupLayoutParams;

	private TextView mTextView;
	private Button mButtonDetails;
	private View mLayoutInfo;

	private PlaceItem mPlaceItem;

	private ScaleAnimation mShowAnimation;
	private AnimationListener mShowAnimationListener;
	private ScaleAnimation mHideAnimation;
	private AnimationListener mHideAnimationListener;

	public PlaceInfoView(Context context, MapView mapView)
	{
		super(context);

		mContext = context;
		mMapView = mapView;

		((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.place_info, this);

		mShowAnimation = new ScaleAnimation(0.1f, 1, 0.5f, 1, ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
				ScaleAnimation.RELATIVE_TO_SELF, 1f);
		mShowAnimation.setDuration(200);
		mShowAnimationListener = new AnimationListener()
		{
			public void onAnimationStart(Animation animation)
			{

			}

			public void onAnimationRepeat(Animation animation)
			{

			}

			public void onAnimationEnd(Animation animation)
			{
				mLayoutInfo.setVisibility(VISIBLE);
			}
		};
		mShowAnimation.setAnimationListener(mShowAnimationListener);

		mHideAnimation = new ScaleAnimation(1, 0.1f, 1, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
				ScaleAnimation.RELATIVE_TO_SELF, 1f);
		mHideAnimation.setDuration(200);
		mHideAnimationListener = new AnimationListener()
		{
			public void onAnimationStart(Animation animation)
			{
				mLayoutInfo.setVisibility(INVISIBLE);
			}

			public void onAnimationRepeat(Animation animation)
			{

			}

			public void onAnimationEnd(Animation animation)
			{
				setVisibility(GONE);
			}
		};
		mHideAnimation.setAnimationListener(mHideAnimationListener);

		setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				hide();
			}
		});

		mPopupLayoutParams = new MapView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT, mMapView.getMapCenter(), 0, 0, MapView.LayoutParams.BOTTOM_CENTER);
		mMapView.addView(this, mPopupLayoutParams);

		mLayoutInfo = findViewById(R.id.layoutInfo);

		mTextView = (TextView) findViewById(R.id.textViewPlaceInfoName);
		mButtonDetails = (Button) findViewById(R.id.buttonDetails);
		mButtonDetails.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{

			}
		});
	}

	public void setPoint(GeoPoint point)
	{
		mPopupLayoutParams.point = point;
	}

	public void show()
	{
		setVisibility(VISIBLE);
		startAnimation(mShowAnimation);
	}

	public void hide()
	{
		startAnimation(mHideAnimation);
		// setVisibility(GONE);
	}

	public void setText(String text)
	{
		mTextView.setText(text);
	}

	public void setItem(PlaceItem item)
	{
		mPlaceItem = item;

		GeoPoint point = new GeoPoint((int) (item.latitude * 1e6), (int) (item.longitude * 1e6));
		setPoint(point);
		setText(item.name);
	}
}
