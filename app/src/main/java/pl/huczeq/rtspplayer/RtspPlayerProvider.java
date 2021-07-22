package pl.huczeq.rtspplayer;

import javax.inject.Inject;

import pl.huczeq.rtspplayer.Settings;
import pl.huczeq.rtspplayer.player.RtspPlayer;
import pl.huczeq.rtspplayer.player.exo.ExoPlayerImpl;
import pl.huczeq.rtspplayer.player.vlc.VlcPlayerImpl;

public class RtspPlayerProvider {

    @Inject
    public VlcPlayerImpl.Factory vlcFactory;
    @Inject
    public ExoPlayerImpl.Factory exoFactory;
    @Inject
    public Settings settings;

    @Inject
    public RtspPlayerProvider() {}

    public RtspPlayer build(String... args) {
        if(settings.useExoPlayer())
            return exoFactory.build(args);
        return vlcFactory.build(args);
    }
}
