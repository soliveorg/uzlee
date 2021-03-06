package com.ladjzero.uzlee;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ladjzero.hipda.HttpClientCallback;
import com.ladjzero.hipda.LocalApi;
import com.ladjzero.hipda.User;
import com.ladjzero.uzlee.utils.UilUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ActivityUser extends ActivityEasySlide {

	LinearLayout mInfo;
	View chat;
	Button block;
	User mUser;
	int uid;
	@Bind(R.id.user_info_img)
	ImageView mImageView;
	@Bind(R.id.name)
	TextView mNameView;
	@Bind(R.id.level)
	TextView mLevelView;
	@Bind(R.id.uid)
	TextView mUid;
	private LocalApi mLocalApi;
	private AsyncTask mParseTask;
	@OnClick(R.id.user_info_img)
	public void onImageClick() {
		if (mUser != null) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(UilUtils.getInstance().getFile(mUser.getImage())), "image/*");
			startActivity(intent);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user);
		ButterKnife.bind(this);

		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mInfo = (LinearLayout) findViewById(R.id.user_info_list);
		chat = findViewById(R.id.chat);
		block = (Button) findViewById(R.id.block);

		chat.setVisibility(View.GONE);
		block.setVisibility(View.GONE);

		chat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mUser != null) {
					Intent intent = new Intent(ActivityUser.this, ActivityChat.class);
					intent.putExtra("uid", mUser.getId());
					intent.putExtra("name", mUser.getName());
					startActivity(intent);
				}
			}
		});

		uid = getIntent().getIntExtra("uid", 0);

		setProgressBarIndeterminateVisibility(true);

		mLocalApi = getCore().getLocalApi();

		getApp().getHttpClient().get("http://www.hi-pda.com/forum/space.php?uid=" + uid, new HttpClientCallback() {
			@Override
			public void onSuccess(String response) {
				mParseTask = new AsyncTask<String, Object, User>() {
					@Override
					protected User doInBackground(String... strings) {
						return getCore().getUserParser().parseUser(strings[0]);
					}

					@Override
					protected void onPostExecute(final User user) {
						ActivityUser.this.mUser = user;

						if (user.getId() != mLocalApi.getUser().getId()) {
							chat.setVisibility(View.VISIBLE);
							block.setVisibility(View.VISIBLE);
						}

						ImageLoader.getInstance().displayImage(user.getImage(), mImageView);

						mNameView.setText(user.getName());
						setTitle(user.getName());
						mNameView.getPaint().setFakeBoldText(true);
						mLevelView.setText(user.getLevel());
						mUid.setText("No." + user.getId());

						for (String kv : propertyToString(user)) {
							View view = getLayoutInflater().inflate(R.layout.user_info_row, null, false);
							TextView key = (TextView) view.findViewById(R.id.key);
							TextView value = (TextView) view.findViewById(R.id.value);
							View more = view.findViewById(R.id.more);

							String[] strings = kv.split(",");
							key.setText(strings[0]);
							value.setText(strings[1]);

							if (strings[0].equals("发帖数量")) {
								view.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										Intent intent = new Intent(ActivityUser.this, ActivityUserThreads.class);
										intent.putExtra("name", user.getName());
										startActivity(intent);
									}
								});

								more.setVisibility(View.VISIBLE);
							} else {
								more.setVisibility(View.INVISIBLE);
							}

							mInfo.addView(view);
						}
					}
				}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, response);
			}

			@Override
			public void onFailure(String reason) {
				showToast(reason);
			}
		});

		final String userName = getIntent().getStringExtra("name");
		setTitle(userName);

		updateBlockButton();

		block.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				User banned = new User().setId(uid);

				if (mLocalApi.getBanned().contains(banned)) {
					mLocalApi.deleteBanned(banned);
				} else {
					mLocalApi.insertBanned(banned);
				}

				updateBlockButton();
			}
		});
	}

	private void updateBlockButton() {
		if (mLocalApi.getBanned().contains(new User().setId(uid))) {
			block.setText("移除黑名单");
			block.setBackgroundResource(R.color.greenPrimary);
		} else {
			block.setText("加入黑名单");
			block.setBackgroundResource(R.color.redPrimary);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mParseTask != null && !mParseTask.isCancelled()) {
			mParseTask.cancel(true);
		}
	}

	public ArrayList<String> propertyToString(User user) {
		ArrayList<String> strings = new ArrayList<>();

		String qq = user.getQq();
		String registerDate = user.getRegisterDateStr();
		String totalThreads = user.getTotalThreads();
		String points = user.getPoints();

		if (qq != null && qq.length() > 0) strings.add("{fa-qq}," + qq);
		strings.add("发帖数量," + totalThreads);
		strings.add("积分," + points);
		strings.add("注册日期," + registerDate);

		return strings;
	}
}
