package pl.huczeq.rtspplayer;

import org.junit.After;

import javax.inject.Inject;

import pl.huczeq.rtspplayer.data.sources.local.database.AppDatabase;

public abstract class BaseAndroidTest extends BaseDITestTemplate {

    @Inject
    public AppDatabase database;

    @After
    public void after() {
        this.database.close();
    }
}
