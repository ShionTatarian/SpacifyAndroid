package fi.android.spacify.activity.bubblespace;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Adapter to flip trough popups on top of BubbleSpace.
 * 
 * @author Tommy
 * 
 */
public class PopupFragmentAdapter extends FragmentStatePagerAdapter {

	private List<BaseBubblePopupFragment> fragmentList = new ArrayList<BaseBubblePopupFragment>();

	/**
	 * Constructor.
	 * 
	 * @param fm
	 */
	public PopupFragmentAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		return fragmentList.get(position);
	}

	@Override
	public int getCount() {
		return fragmentList.size();
	}
	
	public boolean addPopup(BaseBubblePopupFragment frag) {
		for(BaseBubblePopupFragment f : fragmentList) {
			if(frag.getBubble().getID() == f.getBubble().getID()) {
				return false;
			}
		}

		fragmentList.add(frag);
		return true;
	}

	public void removePopup(int position) {
		if(position < fragmentList.size() && position >= 0) {
			fragmentList.remove(position);
		}
	}

	public void clear() {
		fragmentList.clear();
	}
	
	@Override
	public int getItemPosition(Object object) {
        return POSITION_NONE;
	}

}
