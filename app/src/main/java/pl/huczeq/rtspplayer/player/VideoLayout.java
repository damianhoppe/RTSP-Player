package pl.huczeq.rtspplayer.player;

import lombok.Getter;

@Getter
public class VideoLayout {

    public static boolean isEmptyOrNull(VideoLayout videoLayout) {
        if(videoLayout == null)
            return true;
        return videoLayout.visibleWidth * videoLayout.visibleHeight == 0;
    }

    private int width;
    private int height;
    private int visibleWidth;
    private int visibleHeight;
    private float widthToHeightRatio;
    private float heightToWidthRatio;

    public VideoLayout(int width, int height) {
        this(width, height, width, height);
    }

    public VideoLayout(int width, int height, int visibleWidth, int visibleHeight) {
        this.width = width;
        this.height = height;
        this.visibleWidth = visibleWidth;
        this.visibleHeight = visibleHeight;
        if(visibleWidth * visibleHeight != 0) {
            this.widthToHeightRatio = (float) visibleWidth / visibleHeight;
            this.heightToWidthRatio = (float) visibleHeight / visibleWidth;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoLayout that = (VideoLayout) o;
        return width == that.width && height == that.height && visibleWidth == that.visibleWidth && visibleHeight == that.visibleHeight;
    }

    @Override
    public String toString() {
        return "VideoLayout{" +
                "width=" + width +
                ", height=" + height +
                ", visibleWidth=" + visibleWidth +
                ", visibleHeight=" + visibleHeight +
                ", aspectRatio=" + widthToHeightRatio +
                '}';
    }

    public String toShortString() {
        return "VideoLayout{" +
                "visibleWidth=" + visibleWidth +
                ", visibleHeight=" + visibleHeight +
                ", aspectRatio=" + widthToHeightRatio +
                '}';
    }
}
