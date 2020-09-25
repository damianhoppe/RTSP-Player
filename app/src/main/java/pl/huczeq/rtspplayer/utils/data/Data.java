package pl.huczeq.rtspplayer.utils.data;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class Data {

    private ArrayList<Camera> cameraList;

    public Data() {
        cameraList = new ArrayList<Camera>();
    }

    public ArrayList<Camera> getCameraList() {
        return cameraList;
    }
}
