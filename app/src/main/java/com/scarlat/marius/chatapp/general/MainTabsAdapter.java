package com.scarlat.marius.chatapp.general;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.scarlat.marius.chatapp.fragments.ChatsFragment;
import com.scarlat.marius.chatapp.fragments.FriendsFragment;
import com.scarlat.marius.chatapp.fragments.FriendRequestsFragment;

public class MainTabsAdapter extends FragmentPagerAdapter{

    private static final String TAG = "MainTabsAdapter";

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
        Log.d(TAG, "getItem: Method was invoked!");

        Fragment fragment = null;

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
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return fragmentTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Log.d(TAG, "getPageTitle: Method was invoked");

        return fragmentTitles[position];
    }
}
