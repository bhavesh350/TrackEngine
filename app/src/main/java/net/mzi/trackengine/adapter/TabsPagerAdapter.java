package net.mzi.trackengine.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import net.mzi.trackengine.History_Battery;
import net.mzi.trackengine.History_CheckIn;
import net.mzi.trackengine.History_Location;
import net.mzi.trackengine.History_User;

/**
 * Created by Poonam on 2/14/2017.
 */

public class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                // Top Rated fragment activity
                return new History_CheckIn();
            case 1:
                // Games fragment activity
                return new History_Location();
            case 2:
                // Movies fragment activity
                return new History_Battery();
            case 3:
                // Movies fragment activity
                return new History_User();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 4;
    }
}
