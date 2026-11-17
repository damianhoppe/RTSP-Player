package pl.huczeq.rtspplayer.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class UrlUtilTest {

    @Test
    public void maskCredentials_UrlWithUsernameAndPassword_ShouldMaskCredentials() {
        String url = "rtsp://user:password@192.168.1.100:554/stream";
        String result = UrlUtils.maskCredentials(url);
        assertEquals("rtsp://****@192.168.1.100:554/stream", result);
    }

    @Test
    public void maskCredentials_UrlWithUsernameOnly_ShouldMaskCredentials() {
        String url = "rtsp://user@192.168.1.100:554/stream";
        String result = UrlUtils.maskCredentials(url);
        assertEquals("rtsp://****@192.168.1.100:554/stream", result);
    }

    @Test
    public void maskCredentials_UrlWithoutCredentials_ShouldNotModify() {
        String url = "rtsp://192.168.1.100:554/stream";
        String result = UrlUtils.maskCredentials(url);
        assertEquals(url, result);
    }

    @Test
    public void maskCredentials_UrlWithAtSignInPassword_ShouldMaskEntireCredentials() {
        String url = "rtsp://user:p@ssword@example.com/stream";
        String result = UrlUtils.maskCredentials(url);
        assertEquals("rtsp://****@example.com/stream", result);
    }

    @Test
    public void maskCredentials_WithoutProtocolAndCredentials_ShouldNotModifyUrl() {
        String url = "192.168.1.100:554/stream";
        String result = UrlUtils.maskCredentials(url);
        assertEquals(url, result);
    }

    @Test
    public void maskCredentials_WithoutProtocolAndWithCredentials_ShouldMaskCredentials() {
        String url = "admin:password@192.168.1.100:554/stream";
        String result = UrlUtils.maskCredentials(url);
        assertEquals(url, result);
    }
}
