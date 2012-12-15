package fi.android.spacify.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import fi.android.spacify.R;
import fi.android.spacify.activity.BubbleActivity;
import fi.android.spacify.view.BubbleView;

public class TierZeroFragment extends BaseFragment {

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
		background = (ImageView) v.findViewById(R.id.zero_background);
		background.setOnClickListener(onBubbleClick);

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
			return;
		}

		text.setText(bv.getTitle());
	}

	public BubbleView getBubbleView() {
		return bv;
	}

}
