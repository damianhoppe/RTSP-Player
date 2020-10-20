package pl.huczeq.rtspplayer.data;

import java.util.ArrayList;

import pl.huczeq.rtspplayer.data.objects.Camera;

public class Data {

    private ArrayList<Camera> cameraList;

    public Data() {
        cameraList = new ArrayList<>();
    }

    public ArrayList<Camera> getCameraList() {
        return cameraList;
    }

    public void addCamera(Camera camera) {
        this.cameraList.add(camera);
    }

    public void deleteCamera(Camera camera) {
        this.cameraList.remove(camera);
    }
}
