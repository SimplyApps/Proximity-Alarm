package arm.proximity.alarm;

import java.util.ArrayList;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class CopyOfSearchView extends LinearLayout
{
	private SearchService mSearchService;
	private MapView mMapView;

	private EditText mEditTextQuery;
	private ListView mListViewResult;

	private AddressAdapter mAdapter;
	private ArrayList<Address> mAddressList = new ArrayList<Address>();

	private Action1<List<Address>> mSearchServiceCallback = new Action1<List<Address>>()
	{
		@Override
		public void run(List<Address> list)
		{
			mAddressList.clear();
			if (list != null)
				mAddressList.addAll(list);
			post(new Runnable()
			{
				public void run()
				{
					mAdapter.notifyDataSetChanged();
				}
			});
		}
	};

	public CopyOfSearchView(Context context)
	{
		super(context);
		initialize(context);
	}

	public CopyOfSearchView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initialize(context);
	}

	private void initialize(Context context)
	{
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.search_layout, this);

		mEditTextQuery = (EditText) findViewById(R.id.editTextQuery);
		mEditTextQuery.setOnFocusChangeListener(new OnFocusChangeListener()
		{
			public void onFocusChange(View v, boolean hasFocus)
			{
				if (hasFocus)
					mListViewResult.setVisibility(VISIBLE);
			}
		});
		mEditTextQuery.addTextChangedListener(new TextWatcher()
		{
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				if (s.length() > 0)
				{
					// if (mMapView != null)
					// {
					// GeoPoint center = mMapView.getMapCenter();
					// int range = mMapView.getLongitudeSpan();
					//
					// double lowerLeftLongitude = (center.getLongitudeE6() -
					// range) / 1000000d;
					// double upperRightLongitude = (center.getLongitudeE6() +
					// range) / 1000000d;
					//
					// range = mMapView.getLatitudeSpan();
					// double lowerLeftLatitude = (center.getLatitudeE6() -
					// range) / 1000000d;
					// double upperRightLatitude = (center.getLatitudeE6() +
					// range) / 1000000d;
					//
					// mSearchService.searchAsync(s.toString(),
					// lowerLeftLatitude, lowerLeftLongitude,
					// upperRightLatitude, upperRightLongitude, 5,
					// mSearchServiceCallback);
					// // mSearchService.searchAsync(s.toString(), center, 5,
					// // mSearchServiceCallback);
					// }
					// else
					mSearchService.searchAsync(s.toString(), 5, mSearchServiceCallback);
				}
				else
				{
					mAddressList.clear();
					mAdapter.notifyDataSetChanged();
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}

			public void afterTextChanged(Editable s)
			{
			}
		});

		mListViewResult = (ListView) findViewById(R.id.listViewResult);
		mListViewResult.setOnItemClickListener(new ListView.OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Address address = mAddressList.get(position);
				mListViewResult.setVisibility(GONE);
				onAddressSelected(address);
			}
		});

		mAdapter = new AddressAdapter(context, 0, mAddressList);
		mListViewResult.setAdapter(mAdapter);

		mSearchService = new SearchService(context);
	}

	public void setMapView(MapView mapView)
	{
		mMapView = mapView;
	}

	private Action1<Address> onAddressSelectedCallback;

	public void setOnAddressSelectedCallback(Action1<Address> onAddressSelectedCallback)
	{
		this.onAddressSelectedCallback = onAddressSelectedCallback;
	}

	protected void onAddressSelected(Address address)
	{
		if (onAddressSelectedCallback != null)
			onAddressSelectedCallback.run(address);
	}

	public class AddressAdapter extends ArrayAdapter<Address>
	{
		private LayoutInflater mLayoutInflater;

		public AddressAdapter(Context context, int textViewResourceId, List<Address> objects)
		{
			super(context, textViewResourceId, objects);

			mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			AddressHandler handler = null;

			if (convertView == null)
				convertView = mLayoutInflater.inflate(android.R.layout.simple_list_item_1, null);
			else
			{
				Object tag = convertView.getTag();
				if (tag instanceof AddressHandler)
					handler = (AddressHandler) tag;
			}

			if (handler == null)
			{
				handler = new AddressHandler();
				handler.initialize(convertView);

				convertView.setTag(handler);
			}

			Address address = getItem(position);
			handler.setData(address);

			return convertView;
		}

		public class AddressHandler
		{
			private TextView mTextView;
			private Address mAddress;

			public void initialize(View convertView)
			{
				convertView.setBackgroundColor(Color.WHITE);
				mTextView = (TextView) convertView.findViewById(android.R.id.text1);
				mTextView.setTextColor(Color.BLACK);
			}

			public void setData(Address address)
			{
				mAddress = address;

				String title = "";

				int count = address.getMaxAddressLineIndex();

				for (int i = 0; i < count; i++)
				{
					if (title.length() > 0)
						title += ", ";

					title += address.getAddressLine(i);
				}

				mTextView.setText(title);
			}
		}
	}
}
