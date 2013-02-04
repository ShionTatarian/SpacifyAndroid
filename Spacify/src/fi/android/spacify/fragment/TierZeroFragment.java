package fi.android.spacify.fragment;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import fi.android.spacify.R;
import fi.android.spacify.activity.BubbleActivity;
import fi.android.spacify.view.BubbleView;
import fi.android.spacify.view.BubbleView.BubbleJSON;
import fi.qvik.android.util.ImageService;
import fi.spacify.android.util.StaticUtils;

public class TierZeroFragment extends BaseFragment {

	private final String TAG = "TierZeroFragment";

	private BubbleView bv;
	private BubbleActivity bubbleActivity;
	private TextView text;
	private ImageView background;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if(activity instanceof BubbleActivity) {
			bubbleActivity = (BubbleActivity) activity;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.tier_zero, container, false);
		text = (TextView) v.findViewById(R.id.zero_text);
		background = (ImageView) v.findViewById(R.id.tier_zero_background);
//		text.setOnClickListener(onBubbleClick);

		updateContent();
		return v;
	}

	public void setBubbleView(BubbleView bv) {
		this.bv = bv;

		updateContent();
	}

	private OnClickListener onBubbleClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			bubbleActivity.setTierOne(bv);
		}

	};

	private void updateContent() {
		if(bv == null || text == null) {
			Log.d(TAG, "Could not set tier zero content.");
			return;
		}

		text.setText(bv.getTitle());

		loadImage();

	}

	private void loadImage() {
		ImageService is = ImageService.getInstance();
		JSONObject style = bv.getStyleOverrides();

		String imageUrl = null;
		try {
			if(style != null) {
				imageUrl = style.getString(BubbleJSON.titleImageUrl);
			}
		} catch(JSONException e) {
		}

		background.setImageResource(R.drawable.lightblueball);
		is.assignHelpMethod(getActivity(), background, StaticUtils.IMAGE_MEDIUM, imageUrl,
				R.drawable.lightblueball, null);
	}

	public BubbleView getBubbleView() {
		return bv;
	}

	public void clear() {
		bv = null;
	}

}
