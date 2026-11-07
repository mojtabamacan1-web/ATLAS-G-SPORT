package com.atlasg.sport;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.content.ComponentName;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.Looper;

import java.util.List;

public class MediaAccessService extends NotificationListenerService {

    public interface MediaUpdateListener {
        void onMediaUpdate(
                String title,
                String artist,
                String packageName,
                boolean isPlaying
        );
    }

    private static MediaAccessService instance;

    private static MediaUpdateListener savedListener;

    private MediaSessionManager sessionManager;
    private MediaController activeController;
    private MediaController.Callback controllerCallback;

    private MediaUpdateListener uiListener;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private final MediaSessionManager.OnActiveSessionsChangedListener
            sessionsListener = controllers -> refreshSessions();

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        uiListener = savedListener;

        sessionManager = (MediaSessionManager)
                getSystemService(MEDIA_SESSION_SERVICE);
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();

        try {
            sessionManager.addOnActiveSessionsChangedListener(
                    sessionsListener,
                    new ComponentName(this, MediaAccessService.class)
            );

            refreshSessions();

            if (uiListener != null) {
                publishCurrentMedia();
            }

        } catch (SecurityException e) {
            publishEmpty("Notification access required");
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification notification) {
        refreshSessions();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification notification) {
        refreshSessions();
    }

    private void refreshSessions() {

        if (sessionManager == null) return;

        try {
            List<MediaController> controllers =
                    sessionManager.getActiveSessions(
                            new ComponentName(
                                    this,
                                    MediaAccessService.class
                            )
                    );

            MediaController selected = null;

            if (controllers != null) {
                for (MediaController controller : controllers) {

                    PlaybackState state =
                            controller.getPlaybackState();

                    if (state != null &&
                            state.getState() ==
                                    PlaybackState.STATE_PLAYING) {

                        selected = controller;
                        break;
                    }

                    if (selected == null) {
                        selected = controller;
                    }
                }
            }

            setActiveController(selected);

        } catch (SecurityException e) {
            publishEmpty("Notification access required");
        }
    }

    private void setActiveController(MediaController controller) {

        if (activeController != null &&
                controllerCallback != null) {

            activeController.unregisterCallback(controllerCallback);
        }

        activeController = controller;
        controllerCallback = null;

        if (activeController == null) {
            publishEmpty("No active music player");
            return;
        }

        controllerCallback = new MediaController.Callback() {

            @Override
            public void onMetadataChanged(MediaMetadata metadata) {
                publishCurrentMedia();
            }

            @Override
            public void onPlaybackStateChanged(PlaybackState state) {
                publishCurrentMedia();
            }

            @Override
            public void onSessionDestroyed() {
                activeController = null;
                controllerCallback = null;
                refreshSessions();
            }
        };

        activeController.registerCallback(controllerCallback);

        publishCurrentMedia();
    }

    private void publishCurrentMedia() {

        if (activeController == null) {
            publishEmpty("No active music player");
            return;
        }

        MediaMetadata metadata = activeController.getMetadata();

        String title = "Unknown track";
        String artist = "Unknown artist";

        if (metadata != null) {

            String t = metadata.getString(
                    MediaMetadata.METADATA_KEY_TITLE
            );

            String a = metadata.getString(
                    MediaMetadata.METADATA_KEY_ARTIST
            );

            if (t != null && !t.isEmpty()) {
                title = t;
            }

            if (a != null && !a.isEmpty()) {
                artist = a;
            }
        }

        PlaybackState state = activeController.getPlaybackState();

        boolean playing = state != null &&
                state.getState() == PlaybackState.STATE_PLAYING;

        String packageName = activeController.getPackageName();

        notifyUi(title, artist, packageName, playing);
    }

    private void publishEmpty(String message) {
        notifyUi(message, "", "", false);
    }

    private void notifyUi(
            String title,
            String artist,
            String packageName,
            boolean playing
    ) {

        handler.post(() -> {

            if (uiListener != null) {
                uiListener.onMediaUpdate(
                        title,
                        artist,
                        packageName,
                        playing
                );
            }
        });
    }

    public static void setUiListener(MediaUpdateListener listener) {

        savedListener = listener;

        if (instance != null) {
            instance.uiListener = listener;
            instance.publishCurrentMedia();
        }
    }

    public static void playPause() {

        if (instance == null ||
                instance.activeController == null) {
            return;
        }

        PlaybackState state =
                instance.activeController.getPlaybackState();

        if (state != null &&
                state.getState() == PlaybackState.STATE_PLAYING) {

            instance.activeController
                    .getTransportControls().pause();

        } else {

            instance.activeController
                    .getTransportControls().play();
        }
    }

    public static void next() {

        if (instance != null &&
                instance.activeController != null) {

            instance.activeController
                    .getTransportControls().skipToNext();
        }
    }

    public static void previous() {

        if (instance != null &&
                instance.activeController != null) {

            instance.activeController
                    .getTransportControls().skipToPrevious();
        }
    }

    @Override
    public void onDestroy() {

        if (sessionManager != null) {
            try {
                sessionManager.removeOnActiveSessionsChangedListener(
                        sessionsListener
                );
            } catch (Exception ignored) {
            }
        }

        if (activeController != null &&
                controllerCallback != null) {

            activeController.unregisterCallback(controllerCallback);
        }

        activeController = null;
        controllerCallback = null;
        instance = null;

        super.onDestroy();
    }
}
