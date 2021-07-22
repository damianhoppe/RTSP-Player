package pl.huczeq.rtspplayer.domain.usecases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.room.Room;

import com.google.common.base.Strings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.HiltTestApplication;
import dagger.hilt.android.testing.UninstallModules;
import dagger.hilt.components.SingletonComponent;
import io.reactivex.rxjava3.observers.TestObserver;
import pl.huczeq.rtspplayer.domain.cameragenerator.ModelGeneratorForTests;
import pl.huczeq.rtspplayer.data.model.Camera;
import pl.huczeq.rtspplayer.data.model.CameraInstance;
import pl.huczeq.rtspplayer.data.model.CameraPattern;
import pl.huczeq.rtspplayer.data.repositories.base.CameraRepository;
import pl.huczeq.rtspplayer.data.sources.assets.UrlTemplateAssets;
import pl.huczeq.rtspplayer.data.sources.local.database.AppDatabase;
import pl.huczeq.rtspplayer.di.DatabaseModule;
import pl.huczeq.rtspplayer.domain.model.CameraGroupModel;

@RunWith(RobolectricTestRunner.class)
@HiltAndroidTest
@Config(application = HiltTestApplication.class)
@UninstallModules(DatabaseModule.class)
public class CreateCameraGroupUseCaseTest extends BaseUseCaseTestTemplate {

    @Module
    @InstallIn(SingletonComponent.class)
    public static class TestDatabase {

        @Provides
        @Singleton
        public AppDatabase appDatabase(@ApplicationContext Context context) {
            return Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                    .allowMainThreadQueries()
                    .build();
        }
    }

    @Inject
    CreateCameraGroupUseCase useCase;
    @Inject
    CameraRepository cameraRepository;
    @Inject
    UrlTemplateAssets urlTemplateAssets;

    CameraGroupModel cameraGroupModel;

    @Before
    public void prepare() {
        this.database.clearAllTables();
        this.cameraGroupModel = ModelGeneratorForTests.cameraGroupGeneratorWithoutUrlTemplate();
    }

    @Test
    public void execution_CameraWithoutUrlTemplateGiven_ShouldGenerateAndCreateNewCamera() throws InterruptedException {
        this.database.clearAllTables();

        TestObserver<Void> testObserver = new TestObserver<>();
        useCase.execute(this.cameraGroupModel, testObserver);

        testObserver.await()
                .assertComplete()
                .assertNoErrors();

        List<Camera> cameras = database.cameraDao().getAllCamerasSync();
        assertEquals(1, cameras.size());

        Camera dbCamera = cameras.get(0);
        CameraInstance cameraInstance = dbCamera.getCameraInstance();
        assertEquals(cameraGroupModel.getName(), cameraInstance.getName());
        assertEquals(cameraGroupModel.getUrl(), cameraInstance.getUrl());

        CameraPattern cameraPattern = dbCamera.getCameraPattern();
        assertEquals(cameraGroupModel.getName(), cameraPattern.getName());
        assertTrue(Strings.isNullOrEmpty(cameraPattern.getProducer()));
        assertTrue(Strings.isNullOrEmpty(cameraPattern.getModel()));
        assertEquals(cameraGroupModel.getUserName(), cameraPattern.getUserName());
        assertEquals(cameraGroupModel.getPassword(), cameraPattern.getPassword());
        assertEquals(cameraGroupModel.getAddressIp(), cameraPattern.getAddressIp());
        assertEquals(cameraGroupModel.getPort(), cameraPattern.getPort());
        assertEquals(cameraGroupModel.getChannel(), cameraPattern.getChannel());

        assertEquals(cameraGroupModel.getServerUrl(), cameraPattern.getServerUrl());
        assertEquals(cameraGroupModel.getUrl(), cameraPattern.getUrl());
    }
}
