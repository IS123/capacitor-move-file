package com.vholodivskyi.capacitorMoveFile;

import android.net.Uri;

public interface FileSaveCallback {
    void onSuccess(Uri fileUri);

    void onError(Exception e);
}
