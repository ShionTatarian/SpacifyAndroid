package fi.android.spacify.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import fi.android.spacify.R;

public class BaseBubbleView extends FrameLayout {

	private TextView bubble, links;

	public BaseBubbleView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.base_bubble, this, true);
		bubble = (TextView) findViewById(R.id.base_bubble_bubble);
		links = (TextView) findViewById(R.id.base_bubble_link_count);
	}

	protected void setText(String text) {
		bubble.setText(text);
	}
	
	public void setLinkCount(int count) {
		if(count == 0) {
			links.setVisibility(View.GONE);
		} else {
			links.setVisibility(View.VISIBLE);
			links.setText("" + count);
		}
	}

}
