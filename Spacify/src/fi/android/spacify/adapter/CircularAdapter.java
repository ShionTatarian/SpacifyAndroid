package fi.android.spacify.adapter;

import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * Adapter that allows scrolling infinitely.
 * 
 * @author Tommy
 * 
 * @param <T>
 */
public class CircularAdapter<T> extends ArrayAdapter<T> {

	protected Context context;

	/**
	 * The center position of CircularAdapter.
	 */
	public static final int HALF_MAX_VALUE = Integer.MAX_VALUE / 2;
	private int middle;
	private final List<T> list;

	/**
	 * Initializes the adapter with correct object type list.
	 * 
	 * @param ctx
	 * @param initialList
	 */
	public CircularAdapter(Context ctx, List<T> initialList) {
		super(ctx, -1);
		context = ctx;
		list = initialList;
		if(initialList.size() > 0) {
			middle = HALF_MAX_VALUE - HALF_MAX_VALUE % list.size();
		} else {
			middle = HALF_MAX_VALUE;
		}

	}

	/**
	 * Get the middle position of this ArrayAdapter. This position should always
	 * be set when returning to the view with the list so that it can rotate
	 * better.
	 * 
	 * @return Integer
	 */
	public int getMiddle() {
		return middle;
	}

	/**
	 * Get the number of actual objects in this list.
	 * 
	 * @return Integer number of objects.
	 */
	public int getRealCount() {
		return list.size();
	}

	@Override
	public int getCount() {
		return Integer.MAX_VALUE;
	}

	@Override
	public T getItem(int position) {
		T item = null;
		if(list.size() > 0) {
			item = list.get((position % list.size()));
		}

		return item;
	}

	@Override
	public void add(T object) {
		list.add(object);
		middle = HALF_MAX_VALUE - HALF_MAX_VALUE % list.size();
	};

	@Override
	public void addAll(Collection<? extends T> collection) {
		list.addAll(collection);
		middle = HALF_MAX_VALUE - HALF_MAX_VALUE % list.size();
	}

	@Override
	public void remove(T object) {
		list.remove(object);
		middle = HALF_MAX_VALUE - HALF_MAX_VALUE % list.size();
	};

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return convertView;
	}

	public void addTo(int position, T item) {
		if(position >= 0 && position < list.size()) {
			list.add(position, item);
		} else {
			list.add(item);
		}
	}

}