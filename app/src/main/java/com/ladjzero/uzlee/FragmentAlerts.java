package com.ladjzero.uzlee;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.HttpApi;
import com.ladjzero.hipda.HttpClientCallback;
import com.ladjzero.hipda.Post;
import com.ladjzero.hipda.Posts;
import com.ladjzero.hipda.PostsParser;
import com.ladjzero.hipda.Thread;
import com.ladjzero.hipda.Threads;
import com.ladjzero.hipda.ThreadsParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ladjzero on 2015/1/1.
 */
public class FragmentAlerts extends FragmentBase implements AbsListView.OnItemClickListener {
	Core core;
	private int tabIndex = -1;
	private AbsListView mListView;
	private ArrayAdapter mAdapter;
	private HttpApi mApi;
	private PostsParser mPostsParser;
	private ThreadsParser mThreadsParser;
	private AsyncTask mParseTask1, mParseTask2;
	private Threads mThreads;
	private Posts mPosts;

	public static FragmentAlerts newInstance(int position) {
		FragmentAlerts fragment = new FragmentAlerts();
		Bundle args = new Bundle();
		args.putInt("tab_index", position);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		tabIndex = getArguments().getInt("tab_index");

		try {
			if (tabIndex == 0) {
				mThreads = new Threads();
				String cached = getApp().getMemCache().get("alerts_tab_" + tabIndex);
				List<Thread> ts = JSON.parseArray(cached, Thread.class);
				mThreads.addAll(ts);
			} else {
				mPosts = new Posts();
				String cached = getApp().getMemCache().get("alerts_tab_" + tabIndex);
				List<Post> ps = JSON.parseArray(cached, Post.class);
				mPosts.addAll(ps);
			}
		} catch (Exception e) {

		}

		switch (tabIndex) {
			case 0:
				mAdapter = new AdapterMessageSummary(getActivity(), new ArrayList<Thread>());
				break;
			case 1:
				mAdapter = new ArrayAdapter<Post>(getActivity(), R.layout.simple_post, R.id.simple_thread_text) {
					@Override
					public View getView(int position, View convertView, ViewGroup parent) {
						View row = super.getView(position, convertView, parent);

						TextView title = (TextView) row.findViewById(R.id.simple_thread_text);
						TextView content = (TextView) row.findViewById(R.id.simple_post_text);
						Post post = getItem(position);
						title.setText(post.getTitle());

						String body = post.getBody();

						if (body.length() > 0) {
							content.setText(post.getBody());
							content.setVisibility(View.VISIBLE);
						} else {
							content.setVisibility(View.GONE);
						}

						return row;
					}
				};
				break;
		}

		mApi = getCore().getHttpApi();
		mPostsParser = getCore().getPostsParser();
		mThreadsParser = getCore().getThreadsParser();
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.simple_threads, container, false);

		mListView = (AbsListView) view.findViewById(R.id.simple_thread_list);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setEmptyView(view.findViewById(R.id.empty_view));

		switch (tabIndex) {
			case 0:
				if (mThreads.size() == 0) {
					mApi.getMessages(new HttpClientCallback() {
						@Override
						public void onSuccess(String response) {
							mParseTask1 = new AsyncTask<String, Object, Threads>() {
								@Override
								protected Threads doInBackground(String... strings) {
									return mThreadsParser.parseMessages(strings[0]);
								}

								@Override
								protected void onPostExecute(Threads threads) {
									mThreads.addAll(threads);
									mAdapter.addAll(threads);
								}
							}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, response);
						}

						@Override
						public void onFailure(String reason) {
							((ActivityBase) getActivity()).showToast(reason);
						}
					});
				} else {
					mAdapter.addAll(mThreads);
				}

				break;
			case 1:
				if (mPosts.size() == 0) {
					mApi.getMentions(new HttpClientCallback() {
						@Override
						public void onSuccess(String response) {
							mParseTask2 = new AsyncTask<String, Object, Posts>() {
								@Override
								protected Posts doInBackground(String... strings) {
									return mPostsParser.parseMentions(strings[0]);
								}

								@Override
								protected void onPostExecute(Posts posts) {
									mPosts.addAll(posts);
									mAdapter.addAll(posts);
								}
							}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, response);
						}

						@Override
						public void onFailure(String reason) {
							((ActivityBase) getActivity()).showToast(reason);
						}
					});
				} else {
					mAdapter.addAll(mPosts);
				}

				break;
		}

		return view;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (tabIndex == 0) {
			Thread chat = (Thread) parent.getAdapter().getItem(position);
			Intent intent = new Intent(getActivity(), ActivityChat.class);
			intent.putExtra("name", chat.getAuthor().getName());
			intent.putExtra("uid", chat.getAuthor().getId());
			startActivity(intent);

		} else {
			Post post = (Post) parent.getAdapter().getItem(position);
			Intent intent = new Intent(getActivity(), ActivityPosts.class);
			intent.putExtra("tid", post.getTid());
			intent.putExtra("fid", post.getFid());
			intent.putExtra("title", post.getTitle());
			intent.putExtra("pid", post.getId());
			startActivity(intent);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mParseTask1 != null && !mParseTask1.isCancelled()) {
			mParseTask1.cancel(true);
		}

		if (mParseTask2 != null && !mParseTask2.isCancelled()) {
			mParseTask2.cancel(true);
		}

		if (tabIndex == 0) {
			getApp().getMemCache().put("alerts_tab_" + tabIndex, JSON.toJSONString(mThreads));
		} else {
			getApp().getMemCache().put("alerts_tab_" + tabIndex, JSON.toJSONString(mPosts));
		}
	}
}
