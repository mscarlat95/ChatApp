package com.scarlat.marius.chatapp.external_libraries;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.rule.ActivityTestRule;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.activities.ProfileSettingsActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class GlideTest {

    @Rule
    public ActivityTestRule<ProfileSettingsActivity> mProfileSettingsActivityActivityTestRule =
            new ActivityTestRule<ProfileSettingsActivity>(ProfileSettingsActivity.class);

    private ProfileSettingsActivity mProfileSettingsActivity = null;

    /* Required components */
    private ImageView profileImageView;

    @Before
    public void setup() {
        mProfileSettingsActivity = mProfileSettingsActivityActivityTestRule.getActivity();
        profileImageView = mProfileSettingsActivity.findViewById(R.id.avatarCircleImageView);
    }

    @Test
    public void testGlideClearImage() {
        final Glide glide = Mockito.mock(Glide.class);
        verify(glide).with(mProfileSettingsActivity).clear(profileImageView);
    }

    @Test
    public void testGlideChangeImage() {
        final Glide glide = Mockito.mock(Glide.class);

        mProfileSettingsActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                verify(glide).with(mProfileSettingsActivity).load(R.drawable.default_avatar).into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        profileImageView.setImageDrawable(resource);
                    }
                });
            }
        });
    }


    @After
    public void tearDown() throws Exception {
        /* Clean up */
//        mProfileSettingsActivity = null;
    }


}
