package com.permission.library;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.permission.library.callback.RequestPermissionCallback;

/**
 * Created by helixs on 2017/4/11.
 */

public class PerHelper {

    public static void callPhone(final Context context, final String phone) {
        PerRequester.getDefault().targetPermissions(Manifest.permission.CALL_PHONE)
                .callback(new RequestPermissionCallback() {
                    @SuppressWarnings("MissingPermission")
                    @Override
                    public void onAllowed() {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
                        context.startActivity(intent);
                    }
                }).apply(context);
    }
}
