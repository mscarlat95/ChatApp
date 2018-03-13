package com.scarlat.marius.chatapp.util;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.scarlat.marius.chatapp.fragments.ChatsFragment;
import com.scarlat.marius.chatapp.fragments.FriendsFragment;
import com.scarlat.marius.chatapp.fragments.FriendRequestsFragment;

public class MainTabsAdapter extends FragmentPagerAdapter{

    private int numberOfTabs = 3;
    private String[] fragmentTitles = {
            "FRIEND REQUESTS",
            "CHAT",
            "FRIENDS"
    };

    public MainTabsAdapter(FragmentManager fm) {
        super(fm);
    }

    // Assign one fragment for each tab
    @Override
    public Fragment getItem(int position) {
        if (position < 0 || position > 2)
            Log.d(Constants.CURRENT_TAB, "NULL. Invalid position = " + Integer.toString(position));
        else
            Log.d(Constants.CURRENT_TAB, fragmentTitles[position]);

        Fragment fragment;

        switch (position) {
            case 0:
                fragment = new FriendRequestsFragment();
                break;
            case 1:
                fragment = new ChatsFragment();
                break;
            case 2:
                fragment = new FriendsFragment();
                break;
            default:
                fragment = null;
                break;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        if (position < 0 || position > 2) {
            Log.d(Constants.CURRENT_TAB, "getPageTitle: Invalid position = " + Integer.toString(position));
            return null;
        }

        return fragmentTitles[position];
    }
}
