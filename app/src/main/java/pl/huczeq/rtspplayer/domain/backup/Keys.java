package pl.huczeq.rtspplayer.domain.backup;

public final class Keys {
    public static final String VERSION = "version";
    public static final String SETTINGS = "settings";

    public static final class V1 {
        public static final String CAMERAS = "camerasData";
        public static final class Camera {
            public static final String NAME = "name";
            public static final String URL = "url";
            public static final String USERNAME = "userName";
            public static final String PASSWORD = "password";
            public static final String ADDRESS_IP = "addressIp";
            public static final String PORT = "port";
            public static final String CHANNEL = "channel";
            public static final String STREAM = "stream";
            public static final String PRODUCER = "producer";
            public static final String MODEL = "model";
            public static final String SERVER_URL = "serverUrl";
        }
    }

    public static final class V2 {
        public static final String CAMERA_GROUPS = "cameraGroups";
        public static final String CAMERA_GROUPS_VERSION = "cameraGroupsVersion";

        public static final class CameraGroup {
            public static final String CAMERA_PATTERN = "cameraPattern";
            public static final String CAMERA_INSTANCES = "cameraInstances";
        }

        public static final class Camera {
            public static final String NAME = "name";
            public static final String URL = "url";
        }

        public static final class CameraPattern {
            public static final String USERNAME = "username";
            public static final String PASSWORD = "password";
            public static final String IP_ADDRESS = "ipaddress";
            public static final String PORT = "port";
            public static final String CHANNEL = "channel";
            public static final String STREAM = "stream";
            public static final String PRODUCER = "producer";
            public static final String MODEL = "model";
            public static final String SERVER_URL = "serverUrl";
        }

        public static final class CameraInstance {
            public static final String VARIABLES_DATA = "variablesData";
        }

        public static final class Settings {
            public static final String APP_START_CAMERA_INDEX = "appStartCameraIndex";
        }
    }
}
