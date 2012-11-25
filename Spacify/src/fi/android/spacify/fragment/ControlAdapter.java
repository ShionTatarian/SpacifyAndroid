package fi.android.spacify.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import fi.android.spacify.R;
import fi.android.spacify.view.BubbleView;

public class ControlAdapter extends ArrayAdapter<Integer> {

	public class COMMANDS {
		public static final int TOGGLE_LINKS = 1000;
		public static final int PLAY = 1001;
	}

	private BubbleView bv;
	private BubbleFragment bf;

	public ControlAdapter(Context context, BubbleView bv, BubbleFragment bf) {
		super(context, 0);
		this.bv = bv;
		this.bf = bf;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.control_bubble, parent, false);
			convertView.setTag(new ViewHolder(convertView));
		}
		ViewHolder holder = (ViewHolder) convertView.getTag();
		int item = getItem(position);

		switch (item) {
			default:
			case COMMANDS.TOGGLE_LINKS:
				convertView.setOnClickListener(toggleLinks);
				holder.text.setVisibility(View.GONE);
				holder.image.setVisibility(View.VISIBLE);
				holder.image.setImageResource(android.R.drawable.ic_menu_share);
				break;
			case COMMANDS.PLAY:
				convertView.setOnClickListener(onPlayClick);
				holder.text.setVisibility(View.GONE);
				holder.image.setVisibility(View.VISIBLE);
				holder.image.setImageResource(android.R.drawable.ic_media_play);
				break;
		}

		return convertView;
	}

	private OnClickListener toggleLinks = new OnClickListener() {

		@Override
		public void onClick(View v) {
			bf.onBubbleViewClick(bv);
		}
	};

	private OnClickListener onPlayClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			bf.onPlayClick(bv);
		}
	};

	private class ViewHolder {

		TextView text;
		ImageView image;

		public ViewHolder(View v) {
			text = (TextView) v.findViewById(R.id.control_bubble_text);
			image = (ImageView) v.findViewById(R.id.control_bubble_image);
		}
	}

}
