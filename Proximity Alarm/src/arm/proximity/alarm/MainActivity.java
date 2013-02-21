package arm.proximity.alarm;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.widget.SearchView;

import android.os.Bundle;

public class MainActivity extends SherlockFragmentActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);

		SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
		if (searchView != null)
			searchView.setQueryHint(getString(R.string.search_hint));

		return true;
	}

}
