package pl.huczeq.rtspplayer;

import androidx.annotation.CallSuper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.HiltTestApplication;

@RunWith(RobolectricTestRunner.class)
@HiltAndroidTest
@Config(application = HiltTestApplication.class)
public abstract class BaseDITestTemplate {

    @Rule
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Before
    public void prepareBaseDiTestTemplate() {
        this.hiltRule.inject();
    }
}