package fi.android.spacify.fragment;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import fi.android.spacify.R;
import fi.android.spacify.view.BubbleView;

public class BubbleControlFragment extends BaseFragment {

	protected ViewGroup layout;
	private BubbleView bv;
	private ArrayAdapter<Integer> adapter;
	protected List<View> views = new ArrayList<View>();

	public void setAdapter(ArrayAdapter<Integer> adapter) {
		this.adapter = adapter;
	}

	public void setSize(int size) {
		layout.removeAllViews();

		double controlBubbleSize = (int) getResources().getDimension(R.dimen.popup_control);
		double c = (int) (Math.PI * size);
		double count = c / (controlBubbleSize * 3 / 2);
		double angle = 360d / count;

		android.widget.FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) layout.getLayoutParams();
		params.height = size;
		params.width = size;
		layout.setLayoutParams(params);

		// position offset
		int POS = 1;
		for(int i = POS; i < adapter.getCount() + POS; i++) {
			View v = adapter.getView(i - POS, null, layout);
			v.setRotation((int) (i * angle));
			layout.addView(v);
			views.add(v);
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

}
