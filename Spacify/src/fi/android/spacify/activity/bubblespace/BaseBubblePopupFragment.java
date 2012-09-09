package fi.android.spacify.activity.bubblespace;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import fi.android.spacify.R;
import fi.android.spacify.model.Bubble;

public class BaseBubblePopupFragment extends Fragment {

	private Bubble bubble;
	private TextView contentText, name;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.bubble_popup, container, false);
		contentText = (TextView) view.findViewById(R.id.bubble_popup_text);
		name = (TextView) view.findViewById(R.id.bubble_popup_name);

		if(bubble != null) {
			name.setText(bubble.getTitle());
			contentText.setText(bubble.getContents());
		}

		return view;
	}

	public void setBubble(Bubble b) {
		bubble = b;

		if(contentText != null && bubble != null) {
			contentText.setText(bubble.getContents());
		}
	}

	public Bubble getBubble() {
		return bubble;
	}

}
