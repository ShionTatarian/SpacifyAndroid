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
		public static final int IMAGE = 1001;
		public static final int VIDEO = 1002;
		public static final int EDIT = 1003;
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
				holder.text.setVisibility(View.GONE);
				holder.image.setVisibility(View.VISIBLE);
				holder.image.setOnClickListener(toggleLinks);
				holder.image.setImageResource(android.R.drawable.ic_menu_share);
				break;
			case COMMANDS.IMAGE:
				holder.text.setVisibility(View.GONE);
				holder.image.setVisibility(View.VISIBLE);
				holder.image.setOnClickListener(onImageClick);
				holder.image.setImageResource(android.R.drawable.ic_menu_gallery);
				break;
			case COMMANDS.VIDEO:
				holder.text.setVisibility(View.GONE);
				holder.image.setVisibility(View.VISIBLE);
				holder.image.setOnClickListener(onPlayClick);
				holder.image.setImageResource(android.R.drawable.ic_media_play);
				break;
			case COMMANDS.EDIT:
				holder.text.setVisibility(View.GONE);
				holder.image.setVisibility(View.VISIBLE);
				holder.image.setOnClickListener(onEditClick);
				holder.image.setImageResource(android.R.drawable.ic_menu_edit);
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

	private OnClickListener onImageClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			bf.onImageClick(bv);
		}
	};

	private OnClickListener onPlayClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			bf.onPlayClick(bv);
		}
	};

	private OnClickListener onEditClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			bf.onEditClick(bv);
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
