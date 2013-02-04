package fi.android.spacify.adapter;

import org.json.JSONException;
import org.json.JSONObject;

import android.view.View;
import android.view.ViewGroup;
import fi.android.spacify.R;
import fi.android.spacify.activity.BubbleActivity;
import fi.android.spacify.view.BubbleView;
import fi.android.spacify.view.BubbleView.BubbleJSON;
import fi.qvik.android.util.ImageService;
import fi.spacify.android.util.StaticUtils;

public class TierOneAdapter extends WheelAdapter {

	public TierOneAdapter(BubbleActivity context) {
		super(context);
	}

	@Override
	public void onSingleClick(View from, BubbleView bv) {
		bubbleAct.onTierOneClick(from, bv);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			convertView = getEmptyView(parent);
		}
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.position = position;
		BubbleView bv = getItem(position);
		if(bv != null) {
			if(selected != null && bv.getID() == selected.getID()) {
				holder.background.setImageResource(R.drawable.greenball);
			} else {
				holder.background.setImageResource(R.drawable.lightblueball);
			}

			holder.text.setText(bv.getTitle());
			holder.text.setTextSize(getContext().getResources().getDimension(R.dimen.text_small));
			holder.linkCount.setVisibility(View.VISIBLE);
			holder.linkCount.setText("" + bv.getLinks().size());
			loadImage(holder, bv);
		}

		return convertView;
	}

	private void loadImage(final ViewHolder holder, BubbleView bv) {
		ImageService is = ImageService.getInstance();
		JSONObject style = bv.getStyleOverrides();
		String imageUrl = null;
		try {
			imageUrl = style.getString(BubbleJSON.titleImageUrl);
		} catch(JSONException e) {
			holder.background.setImageResource(R.drawable.lightblueball);
		}
		is.assignHelpMethod(bubbleAct, holder.background, StaticUtils.IMAGE_MEDIUM, imageUrl,
				R.drawable.lightblueball, null);
	}

}
