package com.gdgl.drawer;

import com.gdgl.activity.MessageListFragment;
import com.gdgl.activity.MessageTabFragment;
import com.gdgl.activity.MyActionBarActivity;
import com.gdgl.smarthome.R;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.view.Menu;
import android.view.MenuItem;

public class AlarmMessageActivity extends MyActionBarActivity {
	private Toolbar mToolbar;
	private ActionBar mActionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(mToolbar);
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(true);
		mActionBar.setTitle("消息管理");
		
		final MessageTabFragment mfragent = new MessageTabFragment();
		mToolbar.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				// TODO Auto-generated method stub
				switch (item.getItemId()) {
				case R.id.menu_delete:
					mfragent.clickDelete();
					break;

				default:
					break;
				}
				return false;
			}
		});
		
		FragmentTransaction fragmentTransaction = getSupportFragmentManager()
				.beginTransaction();

		fragmentTransaction.replace(R.id.container, mfragent);
		fragmentTransaction.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_messagedelete, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onSupportNavigateUp() {
		// TODO Auto-generated method stub
		finish();
		return super.onSupportNavigateUp();
	}
}