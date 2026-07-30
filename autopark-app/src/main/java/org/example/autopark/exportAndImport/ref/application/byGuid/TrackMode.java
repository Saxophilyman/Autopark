package org.example.autopark.exportAndImport.ref.application.byGuid;

public enum TrackMode {
    SUMMARY,
    FULL;

    public static TrackMode fromBoolean(boolean withTrack) {
        return withTrack ? FULL : SUMMARY;
    }
}
