package fi.android.spacify.activity;

import android.app.Activity;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import fi.android.service.EventService;
import fi.spacify.android.util.Events;

public class BaseFragment extends Fragment implements Callback {

	private final String TAG = "BaseFragment";
	protected EventService es = EventService.getInstance();
	private Handler eventHandler;

	@Override
	public void onDetach() {
		super.onDetach();
		if (eventHandler != null) {
			es.removeCallback(eventHandler);
			eventHandler = null;
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (eventHandler == null) {
			eventHandler = es.addCallback(this);
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		Log.v(TAG, "" + Events.values()[msg.what]);
		return false;
	}

}
