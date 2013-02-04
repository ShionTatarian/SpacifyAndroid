package fi.android.spacify.view;

import java.lang.ref.WeakReference;

import org.json.JSONException;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import fi.android.spacify.R;
import fi.android.spacify.view.BubbleView.BubbleJSON;
import fi.qvik.android.util.CustomDrawInterface;
import fi.qvik.android.util.ImageCache;
import fi.qvik.android.util.ImageService;
import fi.qvik.android.util.ImageServiceListener;
import fi.spacify.android.util.StaticUtils;

public class ThirdLayer extends BaseBubbleView {

	private final ImageService is = ImageService.getInstance();

	private BubbleView bv;
	private ImageView bubbleImage;
	private WeakReference<Activity> weakActivity;

	public ThirdLayer(Activity activity) {
		super(activity);

		bubbleImage = (ImageView) findViewById(R.id.third_layer_image);
		bubbleText = findViewById(R.id.third_layer_text);
		// zoom & diameter
		// n * 150
		zoom(4);
		diameter = 600;

		updateContent();
	}

	public void setBubble(BubbleView bv, Activity act) {
		this.bv = bv;
		weakActivity = new WeakReference<Activity>(act);
		updateContent();
	}

	private void updateContent() {
		if(weakActivity != null) {
			if(bv != null && bubbleText != null) {
				String imageUrl = StaticUtils.parseStringJSON(bv.getStyleOverrides(),
						BubbleJSON.contentsImageUrl, null);
				setImage(imageUrl);
				setText(bv.getContents());

				String color = null;
				if(bv.getStyleOverrides() != null) {
					try {
						color = bv.getStyleOverrides().getString(BubbleJSON.contentsFontColor);
					} catch(JSONException e) {
					}
				}

				if(color != null) {
					((TextView) (bubbleText)).setTextColor(Color.parseColor(color));
				}
			}
		}
	}

	private void setImage(String imageUrl) {
		Activity act = weakActivity.get();
		if(imageUrl != null && act != null) {
			thirdLayerAssignHelpMethod(act, bubbleImage, StaticUtils.IMAGE_NORMAL, imageUrl,
					R.drawable.transparentball, null, false);
		} else {
			bubbleImage.setImageResource(R.drawable.greenball);
		}
	}
	
	private void thirdLayerAssignHelpMethod(Activity activity, ImageView imageView, int imageSize,
			String url,
			int defResource, CustomDrawInterface drawInterface, boolean useCache) {

		if(!TextUtils.isEmpty(url)) {
			bubbleText.setVisibility(View.INVISIBLE);
		}

		ImageCache cache = ImageCache.getInstance();
		if (cache != null) {
			Drawable d = cache.getImage(url);
			if (d != null) {
				imageView.setImageDrawable(d);
				return;
			}
		}

		imageView.setTag(defResource, url);

		final WeakReference<Activity> weakActivity = new WeakReference<Activity>(activity);
		final WeakReference<ImageView> weakView = new WeakReference<ImageView>(imageView);

		is.assignImageLazy(new ImageServiceListener() {

			@Override
			public void imageReady(final ImageService imageService, final String imageUrl, final Drawable image,
					final int defaultResId) {
				Activity act = weakActivity.get();
				if (act != null) {
					act.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							ImageView view = weakView.get();
							if (view != null) {
								String tag = (String) view.getTag(defaultResId);
								if (image != null && tag != null && tag.equals(imageUrl)) {
									view.setImageDrawable(image);
								} else {
									view.setImageResource(R.drawable.greenball);
								}
							}

							bubbleText.setVisibility(View.VISIBLE);
						}
					});
				}
			}

			@Override
			public void imageLoadStarted(ImageService imageService, String imageUrl, int defaultResId) {
			}

		}, url, url, defResource, imageSize, drawInterface, useCache);
	}

	@Override
	protected int getLayout() {
		return R.layout.third_layer_image;
	}

	@Override
	public void move(int x, int y, int maxX, int maxY) {
		int radius = getRadius();
		this.x = (x + offsetX - radius);
		this.y = (y + offsetY - radius);

		LayoutParams params = (LayoutParams) getLayoutParams();
		params.leftMargin = this.x;
		params.topMargin = this.y;
		setLayoutParams(params);
	}

}
