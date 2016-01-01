package com.ladjzero.uzlee;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;
import com.rey.material.widget.ProgressView;

public class ActivitySearch extends ActivityBase implements View.OnKeyListener {

	private FragmentSearchThreads mFragment;
	protected SlidrInterface slidrInterface;


	private EditText mSearch;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		slidrInterface = Slidr.attach(this);

		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

		ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowCustomEnabled(true);
		actionbar.setCustomView(LayoutInflater.from(this).inflate(R.layout.search_action_bar, null));

		mFragment = new FragmentSearchThreads();
		Bundle args = new Bundle();
//		bundle.putInt("dataSource", FragmentThreadsAbs.DATA_SOURCE_SEARCH);
		args.putBoolean("enablePullToRefresh", false);
		mFragment.setArguments(args);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.place_holder, mFragment);
		ft.commit();


		mSearch = (EditText) findViewById(R.id.search_input);

		mSearch.setOnKeyListener(this);
	}

	@Override
	public boolean onKey(View view, int i, KeyEvent keyEvent) {
		if (keyEvent.getAction() == KeyEvent.ACTION_UP && (i == KeyEvent.KEYCODE_SEARCH || i == KeyEvent.KEYCODE_ENTER)) {
			String query = mSearch.getText().toString();
			query = query.trim();

			if (query.length() != 0) {
				mFragment.updateSearch(query);
			}
		}

		return false;
	}
}