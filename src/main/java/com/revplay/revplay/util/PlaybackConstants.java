package com.revplay.revplay.util;

public final class PlaybackConstants {

    private PlaybackConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final int MAX_QUEUE_SIZE = 500;
    public static final int RECENT_PLAYS_LIMIT = 50;
    public static final int DEFAULT_TOP_CONTENT_LIMIT = 10;
    public static final int MAX_POSITION_SHIFT = 100;

    public static final String CONTENT_TYPE_SONG = "SONG";
    public static final String CONTENT_TYPE_EPISODE = "EPISODE";
}
