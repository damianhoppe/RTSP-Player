package pl.huczeq.rtspplayer.domain.usecases;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.room.Room;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
import pl.huczeq.rtspplayer.data.JsonToDatabaseMigration;
import pl.huczeq.rtspplayer.data.model.Camera;
import pl.huczeq.rtspplayer.data.model.CameraInstance;
import pl.huczeq.rtspplayer.data.model.CameraPattern;
import pl.huczeq.rtspplayer.data.repositories.base.UrlTemplateRepository;
import pl.huczeq.rtspplayer.data.sources.local.database.AppDatabase;
import pl.huczeq.rtspplayer.data.sources.local.JsonDataFileLoader;
import pl.huczeq.rtspplayer.di.DatabaseModule;

@RunWith(RobolectricTestRunner.class)
@HiltAndroidTest
@Config(application = HiltTestApplication.class)
@UninstallModules(DatabaseModule.class)
public class DataMigrationUseCaseTest extends BaseUseCaseTestTemplate {

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
    DataMigrationUseCase dataMigrationUseCase;
    @Inject
    UrlTemplateRepository urlTemplateRepository;
    JsonDataFileLoader.Camera camera;

    File jsonDataFile;

    @Before
    public void prepare() {
        this.database.clearAllTables();
        this.jsonDataFile = new File(RuntimeEnvironment.getApplication().getFilesDir(), JsonDataFileLoader.FILE_NAME);
        this.camera = buildExampleCamera();
    }

    @Test
    public void execution_ExistingFileJsonGiven_ShouldLoadData() throws IOException, InterruptedException {
        this.database.clearAllTables();
        createJsonDataFile();

        TestObserver<Void> testObserver = new TestObserver<>();
        dataMigrationUseCase.execute(null, testObserver);

        testObserver.await()
                .assertComplete()
                .assertNoErrors();

        List<Camera> cameras = database.cameraDao().getAllCamerasSync();
        assertEquals(1, cameras.size());

        Camera dbCamera = cameras.get(0);
        CameraInstance cameraInstance = dbCamera.getCameraInstance();

        assertEquals(camera.getName(), cameraInstance.getName());
        assertEquals(camera.getUrl(), cameraInstance.getUrl());
        assertEquals(camera.getPreviewImg(), cameraInstance.getPreviewImg());

        CameraPattern cameraPattern = dbCamera.getCameraPattern();
        assertEquals(camera.getName(), cameraPattern.getName());
        assertEquals(camera.getProducer(), cameraPattern.getProducer());
        assertEquals(camera.getModel(), cameraPattern.getModel());
        assertEquals(camera.getUserName(), cameraPattern.getUserName());
        assertEquals(camera.getPassword(), cameraPattern.getPassword());
        assertEquals(camera.getAddressIp(), cameraPattern.getAddressIp());
        assertEquals(camera.getPort(), cameraPattern.getPort());
        assertEquals(camera.getChannel(), cameraPattern.getChannel());
        assertEquals(camera.getStream(), cameraPattern.getStream());
        assertEquals(camera.getServerUrl(), cameraPattern.getServerUrl());
        assertEquals(camera.getUrl(), cameraPattern.getUrl());
    }

    @Test
    public void execution_ExistingJsonFileGiven_ShouldDeleteFileAfterImport() throws IOException, InterruptedException {
        createJsonDataFile();

        TestObserver<Void> testObserver = new TestObserver<>();
        dataMigrationUseCase.execute(null, testObserver);

        testObserver.await()
                .assertComplete()
                .assertNoErrors();

        File dataFile = new File(RuntimeEnvironment.getApplication().getFilesDir(), JsonDataFileLoader.FILE_NAME);
        assertFalse(dataFile.exists());
    }

    @Test
    public void execution_NoJsonFileGiven_ShouldNotThrowAnyException() throws InterruptedException {
        File dataFile = new File(RuntimeEnvironment.getApplication().getFilesDir(), JsonDataFileLoader.FILE_NAME);
        if(dataFile.exists())
            dataFile.delete();
        if(dataFile.exists())
            throw new RuntimeException("Datafile should be deleted.");

        TestObserver<Void> testObserver = new TestObserver<>();
        dataMigrationUseCase.execute(null, testObserver);

        testObserver.await()
                .assertComplete()
                .assertNoErrors();
    }

    private JsonDataFileLoader.Camera buildExampleCamera() {
        JsonDataFileLoader.Camera camera = new JsonDataFileLoader.Camera();
        camera.setName("1");
        camera.setUrl("rtsp://username@password@127.0.0.1:554/Streaming/channels/001");
        camera.setProducer("Default");
        camera.setModel("Default");
        camera.setUserName("username");
        camera.setPassword("password");
        camera.setAddressIp("127.0.0.1");
        camera.setPort("554");
        camera.setChannel("1");
        camera.setStream("0");
        camera.setServerUrl("/Streaming/channels/001");
        camera.setPreviewImg("");
        return camera;
    }

    private void createJsonDataFile() throws IOException {
        String dataFileText = "{\"camerasData\":[" +
                "{" +
                    "\"name\":\"" + camera.getName() + "\"," +
                    "\"url\":\"" + camera.getUrl() + "\"," +
                    "\"producer\":\"" + camera.getProducer() + "\"," +
                    "\"model\":\"" + camera.getModel() + "\"," +
                    "\"userName\":\"" + camera.getUserName() + "\"," +
                    "\"password\":\"" + camera.getPassword() + "\"," +
                    "\"addressIp\":\"" + camera.getAddressIp() + "\"," +
                    "\"port\":\"" + camera.getPort() + "\"," +
                    "\"channel\":\"" + camera.getChannel() + "\"," +
                    "\"stream\":\"" + camera.getStream() + "\"," +
                    "\"previewImg\":\"" + camera.getPreviewImg() + "\"," +
                    "\"serverUrl\":\"" + camera.getServerUrl() + "\"" +
                "}" +
            "]}";
        if(jsonDataFile.exists())
            jsonDataFile.delete();
        jsonDataFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(jsonDataFile);
        OutputStreamWriter writer = new OutputStreamWriter(fos);
        writer.write(dataFileText);
        writer.flush();
        writer.close();
    }
}
