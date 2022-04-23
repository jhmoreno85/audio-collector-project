package mx.jorge.watson.audio_collector;

/**
 * @author huerta.jorge at gmail.com
 */
public final class Constants {

    private Constants() {
        throw new IllegalStateException("Constants class");
    }

    public static final String VGW_SESSION_ID_HEADER = "vgw-session-id";
    public static final String VGW_SESSION_ID_UNKNOWN = "unknown";
    public static final String ACTION_KEY = "action";
    public static final String ACTION_START = "start";
    public static final String ACTION_STOP = "stop";
    public static final String LISTENING_MESSAGE = "{\"state\":\"listening\"}";
    public static final String KEEP_ALIVE_MESSAGE = "{\"result_index\":0,\"results\":[{\"alternatives\":[{\"transcript\":\"keep alive\"}],\"final\":false}]}";
    public static final int DEFAULT_RECORD_TIME_MILLIS = 10000;

    public static final float AUDIO_VGW_SAMPLE_RATE = 8000;
    public static final int AUDIO_VGW_SAMPLE_SIZE_IN_BITS = 8;
    public static final int AUDIO_VGW_CHANNELS = 1;
    public static final int AUDIO_VGW_FRAME_SIZE = 2;
    public static final float AUDIO_VGW_FRAME_RATE = 8000;
    public static final boolean AUDIO_VGW_BIG_ENDIAN = false;

}
