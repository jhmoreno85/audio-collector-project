package mx.jorge.watson.audio_collector.websocket;

import com.google.gson.JsonSyntaxException;

import mx.jorge.watson.audio_collector.Constants;
import mx.jorge.watson.audio_collector.utils.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpoint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author huerta.jorge at gmail.com
 */
@ApplicationScoped
@ServerEndpoint(value = "/v1/recognize", configurator = ServerWebSocketConfiguration.class)
public class ServerWebSocket {

    private static final Logger debugLog = LogManager.getLogger("debugLogger");

    private byte[] audioByteArr;
    private long recTimeInMillis;
    private long startTime;
    private String vgwSessionId;
    private AudioFormat audioFormat;
    private boolean isAudioCollected;

    @OnOpen
    public void open(Session session, EndpointConfig config) {
        this.audioFormat = new AudioFormat(
                AudioFormat.Encoding.ULAW,
                Constants.AUDIO_VGW_SAMPLE_RATE,
                Constants.AUDIO_VGW_SAMPLE_SIZE_IN_BITS,
                Constants.AUDIO_VGW_CHANNELS,
                Constants.AUDIO_VGW_FRAME_SIZE,
                Constants.AUDIO_VGW_FRAME_RATE,
                Constants.AUDIO_VGW_BIG_ENDIAN
        );

        this.recTimeInMillis = Constants.DEFAULT_RECORD_TIME_MILLIS;
        this.isAudioCollected = false;

        this.vgwSessionId = Optional.ofNullable(config)
                .map(EndpointConfig::getUserProperties)
                .map(userPropertiesMap -> userPropertiesMap.get("handshakeRequest"))
                .map(HandshakeRequest.class::cast)
                .map(HandshakeRequest::getHeaders)
                .map(headersMap -> headersMap.get(Constants.VGW_SESSION_ID_HEADER))
                .filter(values -> !values.isEmpty())
                .map(values -> values.get(0))
                .orElse(Constants.VGW_SESSION_ID_UNKNOWN);

        debugLog.debug("Session opened (websocket) ==> {}", session.getId());
        debugLog.debug("Session opened (vgw-session-id) ==> {}", this.vgwSessionId);
    }

    @OnMessage
    public void handleMessage(String message, Session session) {
        debugLog.debug("message received ==> {}", message);
        try {
            Map<String, Object> messageMap = Util.parseJSONStringToMap(message);
            if (messageMap.containsKey(Constants.ACTION_KEY)) {
                if (Constants.ACTION_START.equals(messageMap.get(Constants.ACTION_KEY))) {
                    debugLog.debug("ACTION - START");
                    this.recTimeInMillis = Util.getTimeToRecInMillis((String) messageMap.get("recTimeInMillis"));
                    debugLog.debug("Recording time in millis: {}", this.recTimeInMillis);
                } else if (Constants.ACTION_STOP.equals(messageMap.get(Constants.ACTION_KEY))) {
                    debugLog.debug("ACTION - END");
                }
                session.getBasicRemote().sendText(Constants.LISTENING_MESSAGE);
            }
        } catch (IOException | JsonSyntaxException e) {
            debugLog.error("An error has occurred within the message handler", e);
        }
    }

    @OnMessage
    public void handleBinaryStreamMessage(byte[] data, Session session) {
        String jsonResponse = Constants.KEEP_ALIVE_MESSAGE;
        if (this.isAudioCollected) {
            try {
                session.getBasicRemote().sendText(jsonResponse);
            } catch (IOException e) {
                debugLog.error("An error has occurred sending the text", e);
            }
        }

        if (null == this.audioByteArr || 0 == this.audioByteArr.length) {
            debugLog.debug("Collecting audio stream (vgw-session-id) => {}", this.vgwSessionId);
            this.audioByteArr = data;
            this.startTime = System.currentTimeMillis();
        } else {
            this.audioByteArr = Util.concatByteArray(this.audioByteArr, data);
        }

        if ((System.currentTimeMillis() - this.startTime) >= this.recTimeInMillis) {
            this.isAudioCollected = true;
            AudioInputStream audioInputStream = new AudioInputStream(
                    new ByteArrayInputStream(this.audioByteArr),
                    this.audioFormat,
                    this.audioByteArr.length);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                debugLog.debug("Composing audio and converting to wav (vgw-session-id) => {}", this.vgwSessionId);
                AudioSystem.write(
                        audioInputStream,
                        AudioFileFormat.Type.WAVE,
                        byteArrayOutputStream);
                String transcript = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
                jsonResponse = Util.parseMapToJSONString(Util.buildResponse(transcript));
            } catch (IOException e) {
                debugLog.error("An error has occurred during the audio recompose process", e);
            }
        }
        try {
            session.getBasicRemote().sendText(jsonResponse);
        } catch (IOException e) {
            debugLog.error("An error has occurred sending the text", e);
        }
    }

    @OnClose
    public void close(Session session) {
        debugLog.debug("Session closed (websocket) ==> {}", session.getId());
        debugLog.debug("Session closed (vgw-session-id) ==> {}", this.vgwSessionId);
    }

    @OnError
    public void onError(Throwable e) {
        debugLog.error("An error has occurred", e);
    }
}
