package arm.proximity.alarm;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import arm.core.WebService.Progress;
import arm.core.WebService.Result;
import arm.proximity.alarm.apis.GooglePlaceAutocomplete;
import arm.proximity.alarm.apis.GooglePlaceAutocomplete.AutocompleteItem;
import arm.proximity.alarm.apis.GooglePlaceDetail;
import arm.proximity.alarm.apis.GooglePlaceDetail.PlaceDetailItem;
import arm.proximity.alarm.apis.GooglePlaceSearch;
import arm.proximity.alarm.apis.PlaceItem;

public class SearchView extends LinearLayout
{
	private GooglePlaceAutocomplete mAutoCompleteApi;
	// private GooglePlaceDetail mPlaceDetailApi;
	private GooglePlaceSearch mPlaceSearchApi;
	private MapView mMapView;

	private EditText mEditTextQuery;
	private ListView mListViewPlace;
	private ListView mListViewAutocomplete;
	private RelativeLayout mLayoutResult;
	private ImageButton mButtonClear;
	private ProgressBar mProgressBarSearch;
	private ImageButton mButtonSearch;

	private AutocompleteAdapter mAutocompleteAdapter;
	private ArrayList<AutocompleteItem> mAutocompleteList = new ArrayList<AutocompleteItem>();

	private PlaceAdapter mPlaceAdapter;
	private ArrayList<PlaceItem> mPlaceList = new ArrayList<PlaceItem>();

	private InputMethodManager mInputMethodManager;

	private boolean mUseAutoSuggest;

	private Address mLastSelectedAddress;

	private ProgressDialog mProgressDialog;

	private Context mContext;

	private Runnable mAutoCompleteRunnable = new Runnable()
	{
		public void run()
		{
			if (mAutoCompleteApi != null)
				mAutoCompleteApi.cancel();

			mAutocompleteList.clear();
			mAutocompleteAdapter.notifyDataSetChanged();

			searchAutocomplete(mEditTextQuery.getText().toString());
		}
	};

	public SearchView(Context context)
	{
		super(context);
		initialize(context);
	}

	public SearchView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initialize(context);
	}

	private void initialize(Context context)
	{
		mContext = context;

		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.search_layout, this);

		mUseAutoSuggest = true;

		mInputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

		mEditTextQuery = (EditText) findViewById(R.id.editTextQuery);
		mEditTextQuery.setOnFocusChangeListener(new OnFocusChangeListener()
		{
			public void onFocusChange(View v, boolean hasFocus)
			{
				if (hasFocus && mAutocompleteList.size() > 0)
					mLayoutResult.setVisibility(VISIBLE);

				mListViewAutocomplete.setVisibility(VISIBLE);
				mListViewPlace.setVisibility(GONE);
			}
		});
		mEditTextQuery.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (mAutocompleteList.size() > 0)
					mLayoutResult.setVisibility(VISIBLE);

				mListViewAutocomplete.setVisibility(VISIBLE);
				mListViewPlace.setVisibility(GONE);
			}
		});
		mEditTextQuery.addTextChangedListener(new TextWatcher()
		{
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				if (s.length() >= 3)
				{
					mListViewAutocomplete.setVisibility(VISIBLE);
					mListViewPlace.setVisibility(GONE);

					removeCallbacks(mAutoCompleteRunnable);
					if (mUseAutoSuggest)
						postDelayed(mAutoCompleteRunnable, 500);
				}
				else if (s.length() == 0)
				{
					mButtonClear.setVisibility(GONE);

					if (mAutoCompleteApi != null)
						mAutoCompleteApi.cancel();

					mAutocompleteList.clear();
					mAutocompleteAdapter.notifyDataSetChanged();
					mLayoutResult.setVisibility(GONE);
				}
				else
				{
					mButtonClear.setVisibility(VISIBLE);
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}

			public void afterTextChanged(Editable s)
			{
			}
		});
		mEditTextQuery.setOnKeyListener(new OnKeyListener()
		{
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				if (keyCode == KeyEvent.KEYCODE_ENTER)
				{
					search();
					return true;
				}
				return false;
			}
		});
		mEditTextQuery.setOnEditorActionListener(new EditText.OnEditorActionListener()
		{
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				if (actionId == 1)
				{
					search();
					return true;
				}
				return false;
			}
		});

		mListViewPlace = (ListView) findViewById(R.id.listViewResult);
		mListViewPlace.setOnItemClickListener(new ListView.OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				PlaceItem item = mPlaceList.get(position);
				mLayoutResult.setVisibility(GONE);

				onAddressSelected(item);
			}
		});
		mPlaceAdapter = new PlaceAdapter(mContext, 0, mPlaceList);
		mListViewPlace.setAdapter(mPlaceAdapter);

		mListViewAutocomplete = (ListView) findViewById(R.id.listViewAutocomplete);
		mListViewAutocomplete.setOnItemClickListener(new ListView.OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				AutocompleteItem address = mAutocompleteList.get(position);
				// mLayoutResult.setVisibility(GONE);
				// requestDetail(address);

				if (mAutoCompleteApi != null)
					mAutoCompleteApi.cancel();

				mProgressBarSearch.setVisibility(GONE);
				mButtonClear.setVisibility(VISIBLE);

				mListViewAutocomplete.setVisibility(GONE);
				mListViewPlace.setVisibility(VISIBLE);

				mLayoutResult.setVisibility(GONE);

				mPlaceAdapter.clear();

				mInputMethodManager.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

				search(address.description, true);
			}
		});

		mAutocompleteAdapter = new AutocompleteAdapter(context, 0, mAutocompleteList);
		mListViewAutocomplete.setAdapter(mAutocompleteAdapter);

		mLayoutResult = (RelativeLayout) findViewById(R.id.layoutResult);

		mButtonClear = (ImageButton) findViewById(R.id.buttonClear);
		mButtonClear.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				mEditTextQuery.setText(null);
				mListViewPlace.setVisibility(GONE);

				mPlaceList.clear();
				mPlaceAdapter.notifyDataSetChanged();
			}
		});

		mProgressBarSearch = (ProgressBar) findViewById(R.id.progressBarSearch);

		mButtonSearch = (ImageButton) findViewById(R.id.buttonSearch);
		mButtonSearch.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				search();
			}
		});
	}

	private void search()
	{
		if (mEditTextQuery.length() > 0)
		{
			if (mAutoCompleteApi != null)
				mAutoCompleteApi.cancel();

			mProgressBarSearch.setVisibility(GONE);
			mButtonClear.setVisibility(VISIBLE);

			mListViewAutocomplete.setVisibility(GONE);
			mListViewPlace.setVisibility(VISIBLE);

			mPlaceAdapter.clear();

			mInputMethodManager.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

			search(mEditTextQuery.getText().toString(), false);
		}
	}

	public void setMapView(MapView mapView)
	{
		mMapView = mapView;
	}

	private Action1<PlaceItem> onAddressSelectedCallback;

	public void setOnAddressSelectedCallback(Action1<PlaceItem> onAddressSelectedCallback)
	{
		this.onAddressSelectedCallback = onAddressSelectedCallback;
	}

	protected void onAddressSelected(PlaceItem address)
	{
		if (onAddressSelectedCallback != null)
			onAddressSelectedCallback.run(address);
	}

	private Action onStartSearchCallback;

	public void setOnStartSearchCallback(Action onStartSearchCallback)
	{
		this.onStartSearchCallback = onStartSearchCallback;
	}

	protected void onStartSearch()
	{
		if (onStartSearchCallback != null)
			onStartSearchCallback.run();
	}

	private Action onSearchResultReceivedCallback;

	public void setOnSearchResultReceivedCallback(Action onSearchResultReceivedCallback)
	{
		this.onSearchResultReceivedCallback = onSearchResultReceivedCallback;
	}

	protected void onSearchResultResceived()
	{
		if (onSearchResultReceivedCallback != null)
			onSearchResultReceivedCallback.run();
	}

	private void searchAutocomplete(String key)
	{
		mButtonClear.setVisibility(GONE);
		mProgressBarSearch.setVisibility(VISIBLE);

		if (mAutoCompleteApi == null)
			mAutoCompleteApi = new GooglePlaceAutocomplete();

		mAutoCompleteApi.addInput(GooglePlaceAutocomplete.SEARCH_KEY, key);
		mAutoCompleteApi.addInput(GooglePlaceAutocomplete.IS_SENSOR, "true");

		GeoPoint center = mMapView.getMapCenter();

		mAutoCompleteApi.addInput(GooglePlaceAutocomplete.LOCATION,
				(center.getLatitudeE6() / 1e6) + "," + (center.getLongitudeE6() / 1e6));

		mAutoCompleteApi.addInput(GooglePlaceAutocomplete.RADIUS, String.valueOf(1e3));

		mAutoCompleteApi.setOnProgressCallback(new Action1<GooglePlaceAutocomplete.Progress>()
		{
			@Override
			public void run(final GooglePlaceAutocomplete.Progress progress)
			{
				if (progress != null && progress.data instanceof AutocompleteItem)
				{
					post(new Runnable()
					{
						public void run()
						{
							mAutocompleteList.add((AutocompleteItem) progress.data);
							mAutocompleteAdapter.notifyDataSetChanged();

							mLayoutResult.setVisibility(VISIBLE);
						}
					});
				}
			}
		});

		mAutoCompleteApi.setOnResultCallback(new Action1<GooglePlaceAutocomplete.Result>()
		{
			@Override
			public void run(GooglePlaceAutocomplete.Result result)
			{
				post(new Runnable()
				{
					public void run()
					{
						mButtonClear.setVisibility(VISIBLE);
						mProgressBarSearch.setVisibility(GONE);
					}
				});
			}
		});
		mAutoCompleteApi.openAsync();
	}

	// public void requestDetail(AutocompleteItem item)
	// {
	// if (mPlaceDetailApi != null)
	// mPlaceDetailApi.cancel();
	//
	// if (mProgressDialog == null)
	// {
	// mProgressDialog = new ProgressDialog(mContext);
	// mProgressDialog.setMessage("Searching location...");
	// }
	//
	// mProgressDialog.show();
	//
	// mPlaceDetailApi = new GooglePlaceDetail();
	// mPlaceDetailApi.addInput(GooglePlaceDetail.IS_USING_SENSOR, "true");
	// mPlaceDetailApi.addInput(GooglePlaceDetail.REFERENCE, item.reference);
	//
	// mPlaceDetailApi.setOnProgressCallback(new
	// Action1<GooglePlaceDetail.Progress>()
	// {
	// @Override
	// public void run(GooglePlaceDetail.Progress progress)
	// {
	// if (progress != null && progress.data instanceof PlaceDetailItem)
	// {
	// final PlaceDetailItem item = (PlaceDetailItem) progress.data;
	//
	// post(new Runnable()
	// {
	// public void run()
	// {
	// mLastSelectedAddress = new Address(Locale.getDefault());
	// mLastSelectedAddress.setFeatureName(item.name);
	// mLastSelectedAddress.setAddressLine(0, item.address);
	// mLastSelectedAddress.setLatitude(item.latitude);
	// mLastSelectedAddress.setLongitude(item.longitude);
	//
	// if (mProgressDialog != null)
	// mProgressDialog.dismiss();
	//
	// onAddressSelected(mLastSelectedAddress);
	// }
	// });
	// }
	// }
	// });
	//
	// mPlaceDetailApi.setOnResultCallback(new
	// Action1<GooglePlaceDetail.Result>()
	// {
	// @Override
	// public void run(GooglePlaceDetail.Result result)
	// {
	//
	// }
	// });
	//
	// mPlaceDetailApi.openAsync();
	// }

	public void search(String key, final boolean gotoFirstLocation)
	{
		onStartSearch();

		if (mPlaceSearchApi != null)
			mPlaceSearchApi.cancel();

		if (mProgressDialog == null)
		{
			mProgressDialog = new ProgressDialog(mContext);
			mProgressDialog.setMessage("Searching location...");
		}

		mProgressDialog.show();

		mPlaceSearchApi = new GooglePlaceSearch();
		mPlaceSearchApi.addInput(GooglePlaceSearch.KEYWORD, key);
		mPlaceSearchApi.addInput(GooglePlaceSearch.IS_SENSOR, "true");

		GeoPoint center = mMapView.getMapCenter();

		mPlaceSearchApi.addInput(GooglePlaceSearch.LOCATION,
				(center.getLatitudeE6() / 1e6) + "," + (center.getLongitudeE6() / 1e6));

		// mPlaceSearchApi.addInput(GooglePlaceSearch.RADIUS, "10000");

		mPlaceSearchApi.setOnProgressCallback(new Action1<GooglePlaceSearch.Progress>()
		{
			@Override
			public void run(final GooglePlaceSearch.Progress progress)
			{
				if (progress != null && progress.data instanceof PlaceItem)
				{
					post(new Runnable()
					{
						public void run()
						{
							mPlaceList.add((PlaceItem) progress.data);
							mPlaceAdapter.notifyDataSetChanged();

							if (gotoFirstLocation)
								onAddressSelected((PlaceItem) progress.data);
						}
					});
				}
			}
		});

		mPlaceSearchApi.setOnResultCallback(new Action1<GooglePlaceSearch.Result>()
		{
			@Override
			public void run(GooglePlaceSearch.Result result)
			{
				post(new Runnable()
				{
					public void run()
					{
						if (mProgressDialog != null)
							mProgressDialog.dismiss();

						if (!gotoFirstLocation)
							mListViewPlace.setVisibility(VISIBLE);

						onSearchResultResceived();
					}
				});
			}
		});

		mPlaceSearchApi.openAsync();
	}

	public void show()
	{
		setVisibility(VISIBLE);
		mInputMethodManager.showSoftInput(mEditTextQuery, InputMethodManager.SHOW_FORCED);
	}

	public void hide()
	{
		setVisibility(GONE);
	}

	public boolean isVisible()
	{
		return getVisibility() == VISIBLE;
	}

	public void showResult()
	{
		setVisibility(VISIBLE);

		if (mPlaceList.size() > 0)
		{
			mLayoutResult.setVisibility(VISIBLE);
			mListViewAutocomplete.setVisibility(GONE);
			mListViewPlace.setVisibility(VISIBLE);
		}
	}

	public ArrayList<PlaceItem> getPlaceList()
	{
		return mPlaceList;
	}

	public void setUseAutoSuggest(boolean useAutoSuggest)
	{
		this.mUseAutoSuggest = useAutoSuggest;
	}

	public class AutocompleteAdapter extends ArrayAdapter<AutocompleteItem>
	{
		private LayoutInflater mLayoutInflater;

		public AutocompleteAdapter(Context context, int textViewResourceId, List<AutocompleteItem> objects)
		{
			super(context, textViewResourceId, objects);

			mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			AutocompleteHandler handler = null;

			if (convertView == null)
				convertView = mLayoutInflater.inflate(android.R.layout.simple_list_item_1, null);
			else
			{
				Object tag = convertView.getTag();
				if (tag instanceof AutocompleteHandler)
					handler = (AutocompleteHandler) tag;
			}

			if (handler == null)
			{
				handler = new AutocompleteHandler();
				handler.initialize(convertView);

				convertView.setTag(handler);
			}

			AutocompleteItem address = getItem(position);
			handler.setData(address);

			return convertView;
		}

		public class AutocompleteHandler
		{
			private TextView mTextView;
			private AutocompleteItem mAddress;

			public void initialize(View convertView)
			{
				convertView.setBackgroundColor(Color.WHITE);
				mTextView = (TextView) convertView.findViewById(android.R.id.text1);
				mTextView.setTextColor(Color.BLACK);
				mTextView.setTextAppearance(mContext, android.R.attr.textAppearanceSmall);
			}

			public void setData(AutocompleteItem address)
			{
				mAddress = address;

				mTextView.setText(address.description);
			}
		}
	}

	public class PlaceAdapter extends ArrayAdapter<PlaceItem>
	{
		private LayoutInflater mLayoutInflater;

		public PlaceAdapter(Context context, int textViewResourceId, List<PlaceItem> objects)
		{
			super(context, textViewResourceId, objects);

			mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			PlaceItemHandler handler = null;

			if (convertView == null)
				convertView = mLayoutInflater.inflate(android.R.layout.simple_list_item_1, null);
			else
			{
				Object tag = convertView.getTag();
				if (tag instanceof PlaceItemHandler)
					handler = (PlaceItemHandler) tag;
			}

			if (handler == null)
			{
				handler = new PlaceItemHandler();
				handler.initialize(convertView);

				convertView.setTag(handler);
			}

			PlaceItem address = getItem(position);
			handler.setData(address);

			return convertView;
		}

		public class PlaceItemHandler
		{
			private TextView mTextView;
			private PlaceItem mAddress;

			public void initialize(View convertView)
			{
				convertView.setBackgroundColor(Color.WHITE);
				mTextView = (TextView) convertView.findViewById(android.R.id.text1);
				mTextView.setTextColor(Color.BLACK);
				mTextView.setTextAppearance(mContext, android.R.attr.textAppearanceSmall);
			}

			public void setData(PlaceItem address)
			{
				mAddress = address;

				mTextView.setText(address.name + ", " + address.address);
			}
		}
	}
}
