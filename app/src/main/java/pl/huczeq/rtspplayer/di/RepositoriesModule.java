package pl.huczeq.rtspplayer.di;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import pl.huczeq.rtspplayer.data.repositories.base.CameraRepository;
import pl.huczeq.rtspplayer.data.repositories.CameraRepositoryImpl;
import pl.huczeq.rtspplayer.data.repositories.base.CameraThumbnailRepository;
import pl.huczeq.rtspplayer.data.repositories.CameraThumbnailRepositoryImpl;
import pl.huczeq.rtspplayer.data.repositories.base.UrlTemplateRepository;
import pl.huczeq.rtspplayer.data.repositories.UrlTemplateRepositoryImpl;

@Module
@InstallIn(SingletonComponent.class)
public abstract class RepositoriesModule {

    @Binds
    public abstract CameraRepository camerasRepository(CameraRepositoryImpl camerasRepository);

    @Binds
    public abstract CameraThumbnailRepository thumbnailsRepository(CameraThumbnailRepositoryImpl thumbnailsRepository);

    @Binds
    public abstract UrlTemplateRepository urlTemplatesRepository(UrlTemplateRepositoryImpl urlTemplatesRepository);
}
