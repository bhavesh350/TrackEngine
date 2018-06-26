package net.mzi.trackengine.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import net.mzi.trackengine.Offline_CheckIn;
import net.mzi.trackengine.Offline_Tickets;

/**
 * Created by Poonam on 1/19/2018.
 */
public class OfflneTabsPagerAdapter extends FragmentPagerAdapter {
    public OfflneTabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new Offline_CheckIn();
            case 1:
                return new Offline_Tickets();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
