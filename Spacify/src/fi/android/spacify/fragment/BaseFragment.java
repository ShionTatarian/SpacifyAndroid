package fi.android.spacify.fragment;

import android.app.Activity;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import fi.qvik.android.util.EventBus;
import fi.spacify.android.util.SpacifyEvents;

public class BaseFragment extends Fragment implements Callback {

	private final String TAG = "BaseFragment";
	protected EventBus eb = EventBus.getInstance();
	private Handler eventHandler;

	@Override
	public void onDetach() {
		super.onDetach();
		if (eventHandler != null) {
			eb.removeCallback(eventHandler);
			eventHandler = null;
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (eventHandler == null) {
			eventHandler = eb.addCallback(this);
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		Log.v(TAG, "" + SpacifyEvents.values()[msg.what]);
		return false;
	}

}
