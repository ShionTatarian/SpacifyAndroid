package fi.spacify.android.util;

public enum SpacifyEvents {

	AVATAR_LOGIN_SUCCESS,

	AVATAR_LOGIN_FAIL,

	AVATAR_LOGIN_STARTED,

	/**
	 * Event for successfully fetching all bubbles.
	 */
	ALL_BUBBLES_FETCHED,

	/**
	 * Bubble fetch could not be completed.
	 */
	BUBBLE_FETCH_FAILED,

	/**
	 * Most images have finished loading into memory.
	 */
	ALL_IMAGES_LOADED,

	/**
	 * Comics have been fetched.
	 */
	COMICS_UPDATED;


}
