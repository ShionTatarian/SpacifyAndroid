package fi.android.spacify.fragment;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import fi.android.spacify.R;
import fi.android.spacify.view.BubbleView;

public class ContentFragment extends BaseFragment {

	private BubbleView bv;
	private TextView text;

	public void setBubbleView(BubbleView bv) {
		this.bv = bv;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.content_layout, container, false);

		text = (TextView) v.findViewById(R.id.content_text);
		text.setText(bv.getContents());
		text.setMovementMethod(LinkMovementMethod.getInstance());

		return v;
	}

}
