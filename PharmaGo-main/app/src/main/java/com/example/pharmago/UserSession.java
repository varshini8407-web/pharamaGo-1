package com.example.pharmago;

import android.content.Context;

/**
 * Root package bridge for UserSession.
 */
public class UserSession extends com.example.pharmago.utils.UserSession {

    protected UserSession(Context context) {
        super(context);
    }

    public static synchronized com.example.pharmago.utils.UserSession getInstance(Context context) {
        return com.example.pharmago.utils.UserSession.getInstance(context);
    }
}
