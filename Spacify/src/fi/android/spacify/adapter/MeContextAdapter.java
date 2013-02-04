package fi.android.spacify.adapter;

import org.json.JSONException;

import android.view.View;
import android.view.ViewGroup;
import fi.android.spacify.R;
import fi.android.spacify.activity.BubbleActivity;
import fi.android.spacify.view.BubbleView;
import fi.android.spacify.view.BubbleView.BubbleJSON;
import fi.qvik.android.util.ImageService;
import fi.spacify.android.util.StaticUtils;

public class MeContextAdapter extends WheelAdapter {

	private ImageService is = ImageService.getInstance();

	public MeContextAdapter(BubbleActivity context) {
		super(context);
	}

	@Override
	public void onSingleClick(View from, BubbleView bv) {
		bubbleAct.setTierZeroFromMeContext(from, bv);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			convertView = getEmptyView(parent);
		}
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.position = position;
		BubbleView bv = getItem(position);
		String imageUrl = null;
		if(bv != null && bv.getStyleOverrides() != null) {
			try {
				imageUrl = bv.getStyleOverrides().getString(BubbleJSON.titleImageUrl);
			} catch(JSONException e) {
			}

			holder.text.setText(bv.getTitle());
			// holder.text.setText("" + position);
		}
		if(imageUrl != null) {
			holder.background.setImageResource(R.drawable.lightblueball);
			is.assignHelpMethod(bubbleAct, holder.background, StaticUtils.IMAGE_MEDIUM, imageUrl,
					R.drawable.lightblueball, null);
		} else {
			holder.background.setImageResource(R.drawable.lightblueball);
		}

		return convertView;
	}

}
