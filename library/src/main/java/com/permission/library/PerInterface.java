package com.permission.library;

import android.content.Context;

/**
 * Created by helixs on 2016/12/7.
 */

public interface PerInterface {

    void requestPermissions(String[] permissions);

    String[] getPermissions();

    Context context();

    void dismiss();
}
