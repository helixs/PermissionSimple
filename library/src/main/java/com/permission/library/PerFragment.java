package com.permission.library;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AlertDialog;

import com.permission.library.callback.OnRequestPermissionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by helixs on 2016/12/6.
 */
@SuppressLint("ValidFragment")
public class PerFragment extends Fragment implements PerInterface {

    private final static String DEF_RATIONALE_TITLE = "权限申请";
    private final static String DEF_RATIONALE = "需要申请以下权限才可以正常使用此功能";

    private final static int REQUEST_CODE = 6;
    private static final String TAG = "PerFragment";

    private PerChecker.CheckResult mPermissionCheckResult;
    private boolean mIsShowRationale;
    private OnRequestPermissionListener mRequestListener;
    private RationaleDialogFactory mDialogFactory;
    private Dialog mRationaleDialog;

    static PerFragment createPermissionFragment(PerChecker.CheckResult result, boolean isShowRationale, OnRequestPermissionListener listener, RationaleDialogFactory factory) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("result", result);
        bundle.putBoolean("isShowRationale", isShowRationale);
        PerFragment perFragment = new PerFragment(listener, factory);
        perFragment.setArguments(bundle);
        return perFragment;
    }

    static PerFragment createPermissionFragment(PerChecker.CheckResult result, boolean isShowRationale, OnRequestPermissionListener listener) {
        return createPermissionFragment(result, isShowRationale, listener, new RationaleDialog());
    }

    public PerFragment() {
    }

    private PerFragment(OnRequestPermissionListener listener, RationaleDialogFactory factory) {
        mRequestListener = listener;
        mDialogFactory = factory;
    }

    private PerFragment(OnRequestPermissionListener listener) {
        this(listener, new RationaleDialog());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        mPermissionCheckResult = (PerChecker.CheckResult) arguments.getSerializable("result");
        mIsShowRationale = arguments.getBoolean("isShowRationale", false);
        request();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        getActivity().onStateNotSaved();
        if (requestCode == REQUEST_CODE) {
            handleResult(permissions, grantResults);
        }
    }

    private void request() {
        if (mPermissionCheckResult.isNeedShowRationale() && mIsShowRationale) {
            if (mRationaleDialog == null) {
                if (mDialogFactory != null) {
                    mRationaleDialog = mDialogFactory.createDialog(this);
                    mRationaleDialog.show();
                }
            }
        } else {
            ArrayList<String> permissions = mPermissionCheckResult.getPermissions();
            requestPermissions(permissions.toArray(new String[permissions.size()]));
        }
    }

    @Override
    public void requestPermissions(String[] permissions) {
        requestPermissions(permissions, REQUEST_CODE);
    }

    @Override
    public String[] getPermissions() {
        ArrayList<String> permissions = mPermissionCheckResult.getPermissions();
        return permissions.toArray(new String[permissions.size()]);
    }

    @Override
    public Context context() {
        return getContext();
    }

    @Override
    public void dismiss() {
        finish();
    }

    private void handleResult(String[] permissions, int[] grantResults) {
        if (permissions.length > 0) {
            int length = permissions.length;
            ArrayMap<String, Boolean> resultMap = new ArrayMap<>(length);
            for (int i = 0; i < length; i++) {
                String permission = permissions[i];
                int code = grantResults[i];
                resultMap.put(permission, code == PackageManager.PERMISSION_GRANTED);
            }
            if (resultMap.containsValue(false)) {
                List<String> refusedPermission = new ArrayList<>();
                for (Map.Entry<String, Boolean> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Boolean value = entry.getValue();
                    if (!value) {
                        refusedPermission.add(key);
                    }
                }
                if (mRequestListener != null) {
                    mRequestListener.refused(refusedPermission);
                }
            } else {
                if (mRequestListener != null) {
                    mRequestListener.onAllowed();
                }
            }
        }
        if (mRequestListener != null) {
            mRequestListener.complete();
        }
        finish();
    }

    private void finish() {
        getActivity().getSupportFragmentManager().popBackStackImmediate();
        PerRequester.getDefault().destroyListener();
    }

    private static class RationaleDialog implements RationaleDialogFactory {

        @Override
        public Dialog createDialog(final PerInterface permission) {
            AlertDialog.Builder builder = new AlertDialog.Builder(permission.context());
            builder.setTitle(DEF_RATIONALE_TITLE).setMessage(DEF_RATIONALE);
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    permission.requestPermissions(permission.getPermissions());
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    permission.dismiss();
                }
            });
            return builder.create();
        }
    }
}
