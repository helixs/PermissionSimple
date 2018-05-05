package com.permission.library;

import android.content.Context;

import com.permission.library.callback.OnRequestPermissionListener;

/**
 * Created by helixs on 16/8/17.
 */
public interface IPer {

    void request(Context context, String[] permission, OnRequestPermissionListener listener);
}
