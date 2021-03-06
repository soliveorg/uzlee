package com.ladjzero.uzlee;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ladjzero.hipda.HttpApi;
import com.ladjzero.hipda.HttpClientCallback;
import com.ladjzero.hipda.Threads;
import com.ladjzero.hipda.ThreadsParser;

/**
 * Created by chenzhuo on 15-12-14.
 */
public class FragmentUserThreads extends FragmentThreadsAbs {

	private String userName;
	private AsyncTask mParseTask;

	public static FragmentThreadsAbs newInstance() {
		return new FragmentUserThreads();
	}

	@Override
	public int layout() {
		return R.layout.threads_can_refresh;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);
		Bundle args = getArguments();

		userName = args.getString("userName");
		getApp().getMemCache().put("search_key", userName);

		assert userName != null;

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mThreads != null && mThreads.size() == 0) {
			fetch(1);
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	void fetchPageAt(int page) {
		getCore().getHttpApi().searchUserThreads(userName, page, new HttpClientCallback() {
			@Override
			public void onSuccess(String response) {
				mParseTask = new AsyncTask<String, Object, Threads>() {
					@Override
					protected Threads doInBackground(String... strings) {
						return getCore().getThreadsParser().parseThreads(strings[0], getSettings().getBoolean("show_fixed_threads", false));
					}

					@Override
					protected void onPostExecute(Threads threads) {
						model.setFetchingAndParsing(false);
						onThreads(threads);
					}
				}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, response);
			}

			@Override
			public void onFailure(String reason) {

			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mParseTask != null && !mParseTask.isCancelled()) {
			mParseTask.cancel(true);
		}
	}

	@Override
	protected String keyOfThreadsToCache() {
		return "threads-user-name-" + getApp().getMemCache().get("search_key");
	}
}
