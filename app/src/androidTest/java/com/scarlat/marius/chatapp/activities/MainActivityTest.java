package com.scarlat.marius.chatapp.activities;

import android.app.Activity;
import android.app.Instrumentation;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import com.scarlat.marius.chatapp.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    private int TIMEOUT = 2000;

    /* Allow Test to launch Main Activity */
    @Rule
    public ActivityTestRule<MainActivity> mMainActivityActivityTestRule = new ActivityTestRule<MainActivity>(MainActivity.class);
    private MainActivity mMainActivity = null;

    /* Monitor other activities launched by MainActivity */
    private Instrumentation.ActivityMonitor monitorProfileSettings = getInstrumentation().addMonitor(ProfileSettingsActivity.class.getName(), null, false);
    private Instrumentation.ActivityMonitor monitorUserList = getInstrumentation().addMonitor(UserListActivity.class.getName(), null, false);
    private Instrumentation.ActivityMonitor monitorGoogleMaps = getInstrumentation().addMonitor(MapsActivity.class.getName(), null, false);
    private Instrumentation.ActivityMonitor monitorOfflineFeatures = getInstrumentation().addMonitor(OfflineFeaturesActivity.class.getName(), null, false);


    @Before
    public void setUp() throws Exception {
        /* Setup pre-conditions */
        mMainActivity = mMainActivityActivityTestRule.getActivity();
    }

    @Test
    public void testMainActivityViews() {
        View view = null;

        view = mMainActivity.findViewById(R.id.mainPageToolbar);
        assertNotNull(view);

        view = mMainActivity.findViewById(R.id.mainTabsViewPager);
        assertNotNull(view);

        view = mMainActivity.findViewById(R.id.mainTabLayout);
        assertNotNull(view);
    }


    @Test
    public void testLaunchedActivities() {

        /* Test Profile Settings Activity */
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText("Profile Settings")).perform(click());
        Activity profileSettingsActivity = getInstrumentation().waitForMonitorWithTimeout(monitorProfileSettings, TIMEOUT);
        assertNotNull(profileSettingsActivity);
        profileSettingsActivity.finish();

        /* Test User List Activity */
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText("View All Users")).perform(click());
        Activity userListActivity = getInstrumentation().waitForMonitorWithTimeout(monitorUserList, TIMEOUT);
        assertNotNull(userListActivity);
        userListActivity.finish();

        /* Test Google Maps Activity */
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText("Share Location")).perform(click());
        Activity mapsActivity = getInstrumentation().waitForMonitorWithTimeout(monitorGoogleMaps, TIMEOUT);
        assertNotNull(mapsActivity);
        mapsActivity.finish();

        /* Test Offline Features Activity */
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText("Offline Features")).perform(click());
        Activity offlineFeaturesActivity = getInstrumentation().waitForMonitorWithTimeout(monitorOfflineFeatures, TIMEOUT);
        assertNotNull(offlineFeaturesActivity);
        offlineFeaturesActivity.finish();
    }


    @After
    public void tearDown() throws Exception {
        /* Clean up */
        mMainActivity = null;
    }

}