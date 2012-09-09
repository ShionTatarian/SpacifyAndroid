package fi.android.spacify.activity.bubblespace;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import fi.android.spacify.R;
import fi.android.spacify.model.Bubble;
import fi.android.spacify.model.ControlAction;

public class PopupControlFragment extends Fragment implements OnItemClickListener {

	private ListView controlList;
	private ControlAdapter adapter;
	private ControlCallback callback;
	private Bubble bubble;
	
	/**
	 * Set ControlCallback to this Fragment.
	 * 
	 * @param callback
	 */
	public void setCallback(ControlCallback callback) {
		this.callback = callback;
	}

	/**
	 * Set Bubble that is being controlled.
	 * 
	 * @param b
	 */
	public void setBubble(Bubble b) {
		this.bubble = b;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.popup_controls, container, false);
		controlList = (ListView) v.findViewById(R.id.popup_control_list);
		adapter = new ControlAdapter(getActivity());
		
		ControlAction fullScreen = new ControlAction();
		fullScreen.setNameResource(R.string.control_fullscreen);
		ControlAction ca = new ControlAction();
		ca.setNameResource(R.string.control_delete);
		
		adapter.add(fullScreen);
		adapter.add(ca);
		
		controlList.setAdapter(adapter);
		controlList.setOnItemClickListener(this);

		return v;
	}

	@Override
	public void onItemClick(AdapterView<?> list, View view, int position, long id) {
		ControlAction action = adapter.getItem(position);
		
		switch (action.getNameResource()) {
			case R.string.control_delete:
				callback.remove(bubble);
				getActivity().onBackPressed();
				break;
			default:
				break;
		}

	}
}
