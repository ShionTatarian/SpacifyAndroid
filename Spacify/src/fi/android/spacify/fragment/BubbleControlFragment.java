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
	private BubbleFragment frame;
	private BubbleView bv;
	private ArrayAdapter<Integer> adapter;
	protected List<View> views = new ArrayList<View>();

	public void setBubbleFrame(BubbleFragment frame) {
		this.frame = frame;
	}

	public void setAdapter(ArrayAdapter<Integer> adapter) {
		this.adapter = adapter;
	}

	public void setSize(int size) {
		layout.removeAllViews();

		int controlBubbleSize = (int) getResources().getDimension(R.dimen.popup_control);
		int c = (int) (Math.PI * size);
		int count = c / (controlBubbleSize * 3 / 2);
		int angle = 360 / count;

		for(int i = 0; i < adapter.getCount(); i++) {
			View v = adapter.getView(i, null, layout);
			v.setRotation((i * angle));
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
