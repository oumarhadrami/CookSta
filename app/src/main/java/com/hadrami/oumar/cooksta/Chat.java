package com.hadrami.oumar.cooksta;

/**
 * Created by oumar on 20/12/2016.
 */

public class Chat {

    private String text;
    private String uid;

    public Chat() {
    }

    public Chat(String name, String uid, String message) {

        this.text = message;
        this.uid = uid;
    }


    public String getUid() {
        return uid;
    }

    public String getText() {
        return text;
    }
}