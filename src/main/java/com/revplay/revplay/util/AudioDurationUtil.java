package com.revplay.revplay.util;

import com.mpatric.mp3agic.Mp3File;

import java.io.File;

public final class AudioDurationUtil {

    private AudioDurationUtil() {}

    public static int readMp3DurationSeconds(File file) {
        try {
            Mp3File mp3 = new Mp3File(file);
            long seconds = mp3.getLengthInSeconds();
            if (seconds <= 0 || seconds > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Invalid mp3 duration");
            }
            return (int) seconds;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to read mp3 duration");
        }
    }
}