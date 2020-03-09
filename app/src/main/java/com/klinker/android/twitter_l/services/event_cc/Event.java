package com.klinker.android.twitter_l.services.event_cc;

public class Event {
    private final String title;
    private final String id;
    private final int trustworthiness;

    public Event(String title, String id, int trustworthiness) {
        this.title = title;
        this.id = id;
        this.trustworthiness = trustworthiness;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public int getTrustworthiness() {
        return trustworthiness;
    }

    public String toString() {
        return title + " (" + trustworthiness + ")";
    }
}
