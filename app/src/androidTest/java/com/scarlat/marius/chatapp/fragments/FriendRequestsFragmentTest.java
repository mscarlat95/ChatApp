package com.scarlat.marius.chatapp.fragments;

import android.support.test.rule.ActivityTestRule;
import android.view.View;

import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.activities.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;

public class FriendRequestsFragmentTest {

    /* Allow Test to launch Main Activity */
    @Rule
    public ActivityTestRule<MainActivity> mMainActivityActivityTestRule = new ActivityTestRule<MainActivity>(MainActivity.class);
    private MainActivity mMainActivity = null;


    @Before
    public void setUp() throws Exception {
        /* Setup pre-conditions */
        mMainActivity = mMainActivityActivityTestRule.getActivity();
    }

    @Test
    public void testLaunchFragment() {
        onView(withText("FRIEND REQUESTS")).perform(click());
        View view = mMainActivity.findViewById(R.id.mainTabsViewPager);
        assertNotNull(view);
    }


    @After
    public void tearDown() throws Exception {
        /* Clean up */
        mMainActivity = null;
    }

}