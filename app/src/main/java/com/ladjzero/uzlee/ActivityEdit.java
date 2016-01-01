package com.ladjzero.uzlee;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.ladjzero.hipda.Core;
import com.nineoldandroids.animation.Animator;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.rey.material.app.Dialog;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;


public class ActivityEdit extends ActivityBase implements Core.OnRequestListener {
	public static final int EDIT_SUCCESS = 10;

	int tid;
	int pid;
	int fid;
	int uid;
	int no;
	boolean isNewThread, isReplyToOne, isReply, isEdit;
	String content;
	TextView subjectInput;
	EditText mMessageInput;
	Intent intent;
	private static final int SELECT_PHOTO = 100;
	ArrayList<Integer> attachIds = new ArrayList<Integer>();
	ArrayList<Integer> existedAttachIds = new ArrayList<Integer>();
	private View mEmojiSelector;
	private InputMethodManager mImeManager;
	private boolean mIsAnimating = false;
	Dialog progress;
	boolean mSaveDraft = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_edit);

		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		intent = getIntent();

		tid = intent.getIntExtra("tid", 0);
		pid = intent.getIntExtra("pid", 0);
		fid = intent.getIntExtra("fid", 0);
		uid = intent.getIntExtra("uid", 0);
		no = intent.getIntExtra("no", 0);

		isNewThread = (tid == 0 && pid == 0);
		isReplyToOne = (tid != 0 && pid != 0 && fid == 0);
		isReply = (tid != 0 && pid == 0 && fid == 0);
		isEdit = (tid != 0 && pid != 0 && fid != 0);

		String title = intent.getStringExtra("title");
		if (title != null) setTitle(title);

		progress = new Dialog(this)
				.cancelable(false)
				.contentView(R.layout.progress_circular)
				.titleColor(Utils.getThemeColor(this, R.attr.colorText))
				.backgroundColor(Utils.getThemeColor(this, android.R.attr.colorBackground));

		Core.getExistedAttach(new Core.OnRequestListener() {
			@Override
			public void onError(String error) {

			}

			@Override
			public void onSuccess(String html) {
				String[] ids = html.split(",");

				for (String id : ids) {
					if (id.length() > 0) existedAttachIds.add(Integer.valueOf(id));
				}
			}
		});

		mImeManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		subjectInput = (TextView) findViewById(R.id.edit_title);
		mMessageInput = (EditText) findViewById(R.id.edit_body);

		String subject = intent.getStringExtra("subject");
		String message = intent.getStringExtra("message");

		if (subject != null) subjectInput.setText(subject);
		if (message != null) mMessageInput.setText(message);

		mMessageInput.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mEmojiSelector != null) mEmojiSelector.setVisibility(View.GONE);
			}
		});

		Slidr.attach(this, slidrConfig);
	}

	@Override
	public void onStart() {
		super.onStart();

		if (isNewThread) {
			subjectInput.setVisibility(View.VISIBLE);
		} else {
			if (uid != Core.getUser().getId()) {
				//reply
				subjectInput.setVisibility(View.GONE);
			} else if (no != 1) {
				//edit non-top
				subjectInput.setVisibility(View.GONE);
			} else {
				//edit top
				subjectInput.setVisibility(View.VISIBLE);
			}
		}

		// If subject and message have content.
		// This activity may be revived from a draft.
		if (isEdit && subjectInput.length() == 0 && mMessageInput.length() == 0) {
			progress.setTitle("载入中");
			progress.show();

			Core.getEditBody(fid, tid, pid, new Core.OnRequestListener() {
				@Override
				public void onError(String error) {
					progress.dismiss();
				}

				@Override
				public void onSuccess(String html) {
					subjectInput.setText(html);
					progress.dismiss();
				}
			}, new Core.OnRequestListener() {
				@Override
				public void onError(String error) {
					progress.dismiss();
				}

				@Override
				public void onSuccess(String html) {
					mMessageInput.setText(html);
					progress.dismiss();
				}
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_reply, menu);

		int color = Utils.getThemeColor(this, R.attr.colorTextInverse);
		menu.findItem(R.id.reply_send)
				.setIcon(new IconDrawable(this, MaterialIcons.md_send)
						.color(color)
						.actionBarSize());
		menu.findItem(R.id.reply_add_image)
				.setIcon(new IconDrawable(this, MaterialIcons.md_image)
						.color(color)
						.actionBarSize());
		menu.findItem(R.id.reply_add_emoji)
				.setIcon(new IconDrawable(this, MaterialIcons.md_tag_faces)
						.color(color)
						.actionBarSize());

		if (fid != 0 && tid != 0 && pid != 0 && no != 1)
			menu.findItem(R.id.delete_post)
					.setIcon(new IconDrawable(this, MaterialIcons.md_delete)
							.color(color)
							.actionBarSize());
		else
			menu.findItem(R.id.delete_post).setVisible(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.reply_send) {
			String sig = setting.getBoolean("use_sig", false) ? "有只梨" : "";

			String subject = subjectInput.getText().toString();
			String message = mMessageInput.getText().toString();

			if (isNewThread) {
				if (subject.length() == 0) {
					showToast("标题不能为空");
				} else if (message.length() == 0) {
					showToast("内容不能少于5字");
				} else {
					progress.setTitle("发送");
					progress.show();

					message += "\t\t\t[size=1][color=Gray]" + sig + "[/color][/size]";

					Core.newThread(fid, subject, message, attachIds, this);
				}
			} else if (isEdit) {
				if (no == 1 && subject.length() == 0) {
					showToast("标题不能为空");
				} else if (message.length() == 0) {
					showToast("内容不能少于5字");
				} else {
					progress.setTitle("发送");
					progress.show();

					Core.editPost(fid, tid, pid, subject, message, attachIds, this);
				}
			} else if (isReplyToOne) {
				progress.setTitle("发送");
				progress.show();

				message = "[b]回复 [url=http://www.hi-pda.com/forum/redirect.php?goto=findpost&pid=" + pid + "&ptid=" + tid + "]" + intent.getIntExtra("no", 0) + "#[/url] [i]" + intent.getStringExtra("userName") + "[/i] [/b]\n\n" + message;
				message += "\t\t\t[size=1][color=Gray]" + sig + "[/color][/size]";
				Core.sendReply(tid, message, attachIds, existedAttachIds, this);
			} else if (isReply) {
				progress.setTitle("发送");
				progress.show();

				message += "\t\t\t[size=1][color=Gray]" + sig + "[/color][/size]";
				Core.sendReply(tid, message, attachIds, existedAttachIds, this);
			}
		} else if (id == R.id.reply_add_image) {
			Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
			photoPickerIntent.setType("image/*");
			photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
			startActivityForResult(photoPickerIntent, SELECT_PHOTO);
		} else if (id == R.id.delete_post) {
			final Dialog mDialog = new Dialog(this);

			mDialog
					.title("删除该回复？(实验性)")
					.canceledOnTouchOutside(true)
					.positiveActionClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDialog.dismiss();
							Core.deletePost(fid, tid, pid, ActivityEdit.this);
						}
					})
					.negativeActionClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDialog.dismiss();
						}
					})
					.negativeAction("取消")
					.positiveAction("确认")
					.titleColor(Utils.getThemeColor(this, R.attr.colorText))
					.backgroundColor(Utils.getThemeColor(this, android.R.attr.colorBackground))
					.show();
		} else if (id == R.id.reply_add_emoji) {
			if (mEmojiSelector == null) {
				((ViewStub) findViewById(R.id.emoji_viewstub)).inflate();
				mEmojiSelector = findViewById(R.id.emoji);
			}

			if (!mIsAnimating) {
				if (mEmojiSelector.getVisibility() == View.GONE) {
					YoYo.with(Techniques.SlideInUp)
							.duration(200)
							.withListener(new Animator.AnimatorListener() {
								@Override
								public void onAnimationStart(Animator animation) {
									mEmojiSelector.setVisibility(View.VISIBLE);
									mIsAnimating = true;
								}

								@Override
								public void onAnimationEnd(Animator animation) {
									mIsAnimating = false;
									mImeManager.hideSoftInputFromWindow(mMessageInput.getWindowToken(), 0);
								}

								@Override
								public void onAnimationCancel(Animator animation) {

								}

								@Override
								public void onAnimationRepeat(Animator animation) {

								}
							})
							.playOn(mEmojiSelector);
				} else {
					YoYo.with(Techniques.SlideOutDown)
							.duration(200)
							.withListener(new Animator.AnimatorListener() {
								@Override
								public void onAnimationStart(Animator animation) {
									mIsAnimating = true;
								}

								@Override
								public void onAnimationEnd(Animator animation) {
									mEmojiSelector.setVisibility(View.GONE);
									mIsAnimating = false;
									mImeManager.showSoftInput(mMessageInput, 0);
								}

								@Override
								public void onAnimationCancel(Animator animation) {

								}

								@Override
								public void onAnimationRepeat(Animator animation) {

								}
							})
							.playOn(mEmojiSelector);
				}
			}
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
		if (imageReturnedIntent != null) {
			Uri uri = imageReturnedIntent.getData();
			File imageFile = new File(getRealPathFromURI(this, uri));

			final Dialog mDialog = new Dialog(this);

			mDialog.title("图片处理")
					.cancelable(false)
					.contentView(R.layout.progress_circular)
					.titleColor(Utils.getThemeColor(this, R.attr.colorText))
					.backgroundColor(Utils.getThemeColor(this, android.R.attr.colorBackground))
					.show();

			new AsyncTask<File, Void, File>() {

				@Override
				protected File doInBackground(File... params) {
					return Core.compressImage(params[0], Core.MAX_UPLOAD_LENGTH);
				}

				@Override
				protected void onPostExecute(File tempFile) {
					mDialog.title("图片上传").show();

					Core.uploadImage(tempFile, new Core.OnUploadListener() {
						@Override
						public void onUpload(String response) {

							if (response.startsWith("DISCUZUPLOAD")) {
								int attachId = -1;

								try {
									attachId = Integer.valueOf(response.split("\\|")[2]);
								} catch (Exception e) {

								}

								if (attachId != -1) {
									attachIds.add(attachId);
									mMessageInput.setText(mMessageInput.getText() + "[attachimg]" + attachId + "[/attachimg]");
								}
							}

							mDialog.dismiss();
						}
					});
				}
			}.execute(imageFile);
		}
	}

	private String getRealPathFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		try {
			String[] proj = {MediaStore.Images.Media.DATA};
			cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	@Override
	public void onError(String error) {
		Toast.makeText(ActivityEdit.this, error, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onSuccess(String html) {
		mSaveDraft = false;
		Intent returnIntent = new Intent();
		returnIntent.putExtra("html", html);
		setResult(EDIT_SUCCESS, returnIntent);
		finish();
	}

	public void addEmoji(View v) {
		String emojiText = (String) v.getTag();
		String emoji;

		if (emojiText.startsWith("coolmonkey")) {
			emoji = Core.icons.get("images/smilies/coolmonkey/" + emojiText.substring(10) + ".gif");
		} else if (emojiText.startsWith("grapeman")) {
			emoji = Core.icons.get("images/smilies/grapeman/" + emojiText.substring(8) + ".gif");
		} else {
			emoji = Core.icons.get("images/smilies/default/" + emojiText + ".gif");
		}

		String temp = mMessageInput.getText().toString();
		int start = mMessageInput.getSelectionStart();
		int end = mMessageInput.getSelectionEnd();
		temp = StringUtils.left(temp, start) + emoji + StringUtils.right(temp, temp.length() - end);

		mMessageInput.setText(temp);
		mMessageInput.setSelection(start + emoji.length());
	}

	@Override
	protected void onDestroy() {
		if (mSaveDraft && (subjectInput.length() > 0 || mMessageInput.length() > 0)) {
			Draft draft = new Draft();
			draft.activityTitle = getTitle().toString();
			draft.subject = subjectInput.getText().toString();
			draft.message = mMessageInput.getText().toString();
			draft.fid = fid;
			draft.tid = tid;
			draft.pid = pid;
			draft.uid = uid;
			draft.no = no;

			String json = JSON.toJSONString(draft);
			getSettings().edit().putString("draft", json).commit();
		}

		super.onDestroy();
	}

	public static class Draft {
		public String activityTitle;
		public String subject;
		public String message;
		public int tid;
		public int fid;
		public int pid;
		public int uid;
		public int no;
	}
}