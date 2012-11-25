package fi.android.spacify.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import fi.android.spacify.R;
import fi.android.spacify.view.BubbleView;

public class ContextAdapter extends BaseAdapter {

	private final int NOTHING_SELECTED = -1;

	private Context context;
	private List<BubbleView> list = new ArrayList<BubbleView>();

	private int selected = NOTHING_SELECTED;

	public ContextAdapter(Context context) {
		super();
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.context_list_row,
					parent, false);
			convertView.setTag(new ViewHolder(convertView));
		}
		ViewHolder h = (ViewHolder) convertView.getTag();
		BubbleView bv = getItem(position);

		if(selected == bv.getID()) {
			h.text.setBackgroundResource(R.drawable.greenball);
		} else {
			h.text.setBackgroundResource(R.drawable.lightblueball);
		}

		h.text.setText(bv.getTitle());

		return convertView;
	}

	private class ViewHolder {

		TextView text;

		public ViewHolder(View v) {
			text = (TextView) v.findViewById(R.id.context_text);
		}

	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public BubbleView getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getID();
	}

	public void add(BubbleView bv) {
		if(!contains(bv)) {
			list.add(0, bv);
		}
	}

	public boolean contains(BubbleView bv) {
		for(BubbleView b : list) {
			if(b.getID() == bv.getID()) {
				return true;
			}
		}
		return false;
	}

	public BubbleView getSelected() {
		for(BubbleView bv : list) {
			if(bv.getID() == selected) {
				return bv;
			}
		}
		return null;
	}

	public void setSelected(BubbleView bv) {
		selected = bv.getID();
		notifyDataSetChanged();
	}

	public int getPosition(BubbleView bv) {
		int position = 0;
		for(int i = 0; i < list.size(); i++) {
			BubbleView b = list.get(i);
			if(b.getID() == bv.getID()) {
				position = i;
				break;
			}
		}
		return position;
	}

	public void clear() {
		list.clear();
		notifyDataSetChanged();
	}

}
