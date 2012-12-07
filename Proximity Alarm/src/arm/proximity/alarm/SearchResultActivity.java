package arm.proximity.alarm;

import android.app.ListActivity;
import android.os.Bundle;

public class SearchResultActivity extends ListActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.search_result_layout);
	}
}
