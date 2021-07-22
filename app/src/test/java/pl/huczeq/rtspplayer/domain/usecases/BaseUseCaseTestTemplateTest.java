package pl.huczeq.rtspplayer.domain.usecases;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.HiltTestApplication;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

@RunWith(RobolectricTestRunner.class)
@HiltAndroidTest
@Config(application = HiltTestApplication.class)
public class BaseUseCaseTestTemplateTest extends BaseUseCaseTestTemplate{

    @Test
    public void mainThreadScheduler_Should_NotEquals_AndroidMainThreadScheduler() {
        assertNotEquals(appExecutors.mainThread().scheduler(), AndroidSchedulers.mainThread());
    }
}
