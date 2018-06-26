package net.mzi.trackengine.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import net.mzi.trackengine.Paid_Vouchers;
import net.mzi.trackengine.Pending_Vouchers;

/**
 * Created by Poonam on 1/19/2018.
 */
public class VoucherTabsPagerAdapter extends FragmentPagerAdapter {
    public VoucherTabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new Pending_Vouchers();
            case 1:
                return new Paid_Vouchers();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
