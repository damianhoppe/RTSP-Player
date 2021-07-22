package pl.huczeq.rtspplayer.data.database;

import androidx.lifecycle.LiveData;

import java.util.List;

public class CamerasStats {

    private int cameraInstancesCount;
    private int cameraPatternsCount;
    private int camerasCount;
    private int groupsCount;

    public CamerasStats() {
        this(0,0,0);
    }

    public CamerasStats(List<Integer> list) {
        if(list == null || list.size() < 3) {
            this.cameraInstancesCount = 0;
            this.cameraPatternsCount = 0;
            this.camerasCount = 0;
            this.groupsCount = 0;
        }else {
            this.cameraInstancesCount = list.get(0);
            this.cameraPatternsCount = list.get(1);
            this.camerasCount = list.get(2);
            this.groupsCount = this.cameraPatternsCount - this.camerasCount;
        }
    }

    public CamerasStats(int cameraInstancesCount, int cameraPatternsCount, int camerasCount) {
        this.cameraInstancesCount = cameraInstancesCount;
        this.cameraPatternsCount = cameraPatternsCount;
        this.camerasCount = camerasCount;
        this.groupsCount = this.cameraPatternsCount - this.camerasCount;
    }

    public int getCameraInstancesCount() {
        return cameraInstancesCount;
    }

    public int getCameraPatternsCount() {
        return cameraPatternsCount;
    }

    public int getCamerasCount() {
        return camerasCount;
    }

    public int getGroupsCount() {
        return groupsCount;
    }
}