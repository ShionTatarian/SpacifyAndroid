package fi.android.spacify.animation;

import android.view.animation.LinearInterpolator;

/**
 * Interpolator that runs the animation backwards.
 * 
 * @author Tommy
 * 
 */
public class ReverseInterpolator extends LinearInterpolator {

	@Override
	public float getInterpolation(float input) {
		return Math.abs(input - 1f);
	}

}
