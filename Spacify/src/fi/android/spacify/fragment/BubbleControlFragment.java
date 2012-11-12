package fi.android.spacify.fragment;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import fi.android.spacify.R;
import fi.android.spacify.view.BubbleView;

public class BubbleControlFragment extends BaseFragment {

	protected ViewGroup layout;
	private BubbleFragment frame;
	private BubbleView bv;
	private ArrayAdapter<Object> adapter;
	protected List<View> views = new ArrayList<View>();

	public void setBubbleFrame(BubbleFragment frame) {
		this.frame = frame;
	}

	public void setAdapter(ArrayAdapter<Object> adapter) {
		this.adapter = adapter;
	}

	public void setSize(int size) {
		layout.removeAllViews();

		int controlBubbleSize = (int) getResources().getDimension(R.dimen.popup_control);
		int c = (int) (Math.PI * size);
		int count = c / (controlBubbleSize * 3 / 2);
		int angle = 360 / count;

		for(int i = 0; i<count; i++) {
			View controlBubbleFrame = LayoutInflater.from(getActivity()).inflate(
					R.layout.control_bubble, layout, false);
			controlBubbleFrame.setRotation((i * angle));
			layout.addView(controlBubbleFrame);
			views.add(controlBubbleFrame);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		layout = (ViewGroup) inflater.inflate(R.layout.control_layout, container, false);

		return layout;
	}

	public void openMenu(BubbleView bv) {
		this.bv = bv;
	}

	public void closeMenu() {

	}

	private OnClickListener onLinkClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if(bv != null && frame != null) {
				frame.onBubbleViewClick(bv);
				frame.closePopup();
			}
		}
	};

	private OnClickListener on90Click = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if(bv != null && frame != null) {
				frame.closePopup();
			}
		}
	};

	private OnClickListener on180Click = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if(bv != null && frame != null) {
				frame.closePopup();
			}
		}
	};

	private OnClickListener on270Click = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if(bv != null && frame != null) {
				frame.closePopup();
			}
		}
	};
}
