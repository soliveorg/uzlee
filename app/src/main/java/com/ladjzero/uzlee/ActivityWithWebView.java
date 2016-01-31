package com.ladjzero.uzlee;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;

import com.orhanobut.logger.Logger;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

/**
 * Created by chenzhuo on 15-10-4.
 */
public abstract class ActivityWithWebView extends ActivityBase implements OnToolbarClickListener {

	private SlidrInterface slidrInterface;
	private boolean initialized;

	@JavascriptInterface
	public void onImageClick(String src) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(src));
		startActivity(intent);
	}

	@JavascriptInterface
	public void onWebViewReady() {
		Logger.i("WebView is ready.");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		slidrInterface = Slidr.attach(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setupWebView();
	}

	public void setupWebView() {
		if (!initialized) {
			WebView2 webView = getWebView();

			webView.setOnScrollListener(new WebView2.OnScrollListener() {
				@Override
				public void onScrollStateChanged(WebView2 webView, int state) {
					if (state == WebView2.OnScrollListener.SCROLL_STATE_IDLE) {
						slidrInterface.unlock();
						Log.d("haha", "unlock");
					} else {
						slidrInterface.lock();
						Log.d("haha", "lock");

					}
				}
			});
			webView.addJavascriptInterface(this, "UZLEE");
			webView.setWebChromeClient(new WebChromeClient() {
				@Override
				public boolean onConsoleMessage(ConsoleMessage cm) {
					Logger.t("WebView").d(cm.message());
					return true;
				}
			});

			WebSettings settings = webView.getSettings();
			settings.setJavaScriptEnabled(true);
			settings.setCacheMode(WebSettings.LOAD_DEFAULT);
			settings.setBlockNetworkImage(disableImageFromNetwork());
			webView.setBackgroundColor(Utils.getThemeColor(this, android.R.attr.colorBackground));

			webView.loadUrl(getHTMLFilePath());
			initialized = true;
		}
	}

	public abstract WebView2 getWebView();

	public abstract String getHTMLFilePath();

	public SlidrInterface getSlidrInterface() {
		return slidrInterface;
	}

	@Override
	public void toolbarClick() {
		getWebView().scrollTo(0, 0);
	}
}
