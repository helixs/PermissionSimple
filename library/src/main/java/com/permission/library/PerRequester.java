package com.permission.library;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

import com.permission.library.callback.OnRequestPermissionListener;

/**
 * Created by helixs on 16/8/17.
 */
public class PerRequester implements IPer {

    private static PerRequester sInstance;

    private boolean mIsShowRationale;
    private RationaleDialogFactory mFactory;
    private OnRequestPermissionListener mListener;
    private String[] mTargetPermissions;

    private PerRequester() {
    }

    public static PerRequester getDefault() {
        if (sInstance == null) {
            sInstance = new PerRequester();
        }
        return sInstance;
    }

    public PerRequester showRationale(boolean isShowRationale) {
        this.mIsShowRationale = isShowRationale;
        return this;
    }

    public PerRequester setRationaleDialogFactory(RationaleDialogFactory factory) {
        mFactory = factory;
        return this;
    }

    public PerRequester callback(OnRequestPermissionListener listener) {
        mListener = listener;
        return this;
    }

    public PerRequester targetPermissions(String... permissions) {
        mTargetPermissions = permissions;
        return this;
    }

    public void apply(Context context) {
        request(context, mTargetPermissions, mListener);
    }

    @Override
    public void request(Context context, String[] permissions, OnRequestPermissionListener listener) {
        PerChecker.CheckResult result = PerChecker.check(context, permissions);
        if (result.isGranted()) {
            callListener(listener);
            return;
        }

        PerFragment perFragment =
                mFactory == null
                        ? PerFragment.createPermissionFragment(result, mIsShowRationale, listener) :
                        PerFragment.createPermissionFragment(result, mIsShowRationale, listener, mFactory);
        ((FragmentActivity) context)
                .getSupportFragmentManager()
                .beginTransaction()
                .add(perFragment, perFragment.getClass().getName())
                .addToBackStack("Permission")
                .commit();
    }

    void destroyListener() {
        if (mListener != null) {
            mListener = null;
        }
        if (mFactory != null) {
            mFactory = null;
        }
        if (mTargetPermissions != null) {
            mTargetPermissions = null;
        }
    }

    private void callListener(OnRequestPermissionListener listener) {
        if (listener != null) {
            listener.onAllowed();
            listener.complete();
        }
        destroyListener();
    }
}
