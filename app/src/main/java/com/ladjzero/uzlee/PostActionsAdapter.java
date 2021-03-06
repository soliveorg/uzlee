package com.ladjzero.uzlee;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

/**
 * Created by ladjzero on 2015/3/31.
 */
public class PostActionsAdapter extends ArrayAdapter{
	private Context context;
	public final static String[] TYPES = new String[]{
			"回复",
			"逆序阅读",
			"刷新",
			"收藏",
			"复制链接",
			"截图",
			"从浏览器打开"
	};
	public final static String[] ICONS = new String[] {
			"{md-reply}",
			"{fa-sort-numeric-desc}",
			"{md-autorenew}",
			"{md-bookmark}",
			"{md-link}",
			"{md-crop}",
			"{md-open-in-browser}"
	};

	public PostActionsAdapter(Context context) {
		super(context, 0, TYPES);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		View row = inflater.inflate(R.layout.bs_type_row, parent, false);
		TextView icon = (TextView) row.findViewById(R.id.icon);
		TextView text = (TextView) row.findViewById(R.id.text);

		if (position == 1) {
			if (((ActivityPosts)context).orderType == 0) {
				icon.setText(ICONS[1]);
				text.setText(TYPES[1]);
			} else {
				icon.setText("{fa-sort-numeric-asc}");
				text.setText("顺序阅读");
			}
		} else {
			icon.setText(ICONS[position]);
			text.setText(TYPES[position]);
		}

		return row;
	}
}
