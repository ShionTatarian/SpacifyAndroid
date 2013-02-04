package fi.android.spacify.activity;

import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import fi.qvik.android.util.EventBus;
import fi.qvik.android.util.WorkService;

/**
 * Base activity for all activities in Spacify application. Extends
 * FragmentActivity from Android support package v4.
 * 
 * @author Tommy
 * 
 */
public class BaseActivity extends FragmentActivity implements Callback {

	private final String TAG = "BaseActivity";

	protected final WorkService ws = WorkService.getInstance();
	protected boolean onTop = false;
	protected boolean started = false;

	protected EventBus es;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		started = true;
		es = EventBus.getInstance();
		es.addCallback(this);
	}

	@Override
	protected void onResume() {
		onTop = true;
		super.onResume();
	}

	@Override
	protected void onPause() {
		onTop = false;
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		started = false;
		super.onDestroy();
	}

	@Override
	public boolean handleMessage(Message msg) {
		Log.v(TAG, "Got message:" + msg.what);
		return false;
	}

	protected void addFragment(int resID, Fragment frag) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(resID, frag);
		ft.commit();
	}

	protected void addFragment(int resID, Fragment frag, int enterAnimation, int exitAnimation) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setCustomAnimations(enterAnimation, exitAnimation);
		ft.replace(resID, frag);
		ft.commit();
	}

	protected void removeFragment(Fragment frag) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.remove(frag);
		ft.commit();
	}

	protected void changeFragment(int resID, Fragment frag) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(resID, frag);
		ft.commit();
	}

	protected void changeFragment(int resID, Fragment frag, int enterAnimation, int exitAnimation) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setCustomAnimations(enterAnimation, exitAnimation);
		ft.replace(resID, frag);
		ft.commit();
	}

}
