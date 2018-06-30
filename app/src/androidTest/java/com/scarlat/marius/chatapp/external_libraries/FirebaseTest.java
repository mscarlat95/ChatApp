package com.scarlat.marius.chatapp.external_libraries;

import android.support.test.rule.ActivityTestRule;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.scarlat.marius.chatapp.activities.MainActivity;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.storage.SharedPref;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FirebaseTest {

    /* Allow Test to launch Main Activity */
    @Rule
    public ActivityTestRule<MainActivity> mMainActivityActivityTestRule = new ActivityTestRule<MainActivity>(MainActivity.class);
    private MainActivity mMainActivity = null;

    @Before
    public void setup() {
        mMainActivity = mMainActivityActivityTestRule.getActivity();
    }

    @Test
    public void testAuth() {
        final FirebaseAuth mAuth = Mockito.mock(FirebaseAuth.class);
        assertNotNull(mAuth);

        SharedPref.setup(mMainActivity);
        final String userId = SharedPref.getString(Constants.USER_ID);

        if (!userId.equals("")) {
            when(mAuth.getUid()).thenReturn(userId);
        }
    }

    @Test
    public void testDatabase() {
        final FirebaseDatabase database = Mockito.mock(FirebaseDatabase.class);
        assertNotNull(database);
    }

    @Test
    public void testStorage() {
        final FirebaseStorage storage = Mockito.mock(FirebaseStorage.class);
        assertNotNull(storage);
    }

    @After
    public void tearDown() throws Exception {
        /* Clean up */
//        mMainActivity = null;
    }
}
