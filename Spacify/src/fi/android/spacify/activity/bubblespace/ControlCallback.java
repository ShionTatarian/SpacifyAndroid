package fi.android.spacify.activity.bubblespace;

import fi.android.spacify.model.Bubble;

/**
 * Callbacks from PopupControlFragment.
 * 
 * @author Tommy
 * 
 */
public interface ControlCallback {

	/**
	 * Remove bubble from BubbleSurface.
	 * 
	 * @param b
	 */
	public void remove(Bubble b);

}
