package com.vholodivskyi.capacitorMoveFile;

import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Function;

import javax.security.auth.callback.Callback;

@CapacitorPlugin(name = "MoveFile")
public class MoveFile extends Plugin{

    private void moveFileToDownloads(Context context, String sourcePath, String fileName, String fileType, FileSaveCallback callback) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, fileType);
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri uri = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        }

        FileInputStream fis = null;
        OutputStream os = null;

        try  {
            fis = new FileInputStream(new File(sourcePath));
            os = context.getContentResolver().openOutputStream(uri);

            if (os != null) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                os.flush();

                if (callback != null) {
                    callback.onSuccess(uri);
                }
            }
        } catch (Exception e) {
            if (callback != null) {
                callback.onError(e);
            }
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    Log.e("MoveFile", "Error closing FileInputStream", e);
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.e("MoveFile", "Error closing OutputStream", e);
                }
            }
//			File sourceFile = new File(sourcePath);
//			if (sourceFile.delete()) {
//				Log.d("MoveFile", "Source file deleted: " + sourcePath);
//			} else {
//				Log.e("MoveFile", "Failed to delete source file: " + sourcePath);
//			}
        }
    }

    private void scanFile(Context context, String path) {
        MediaScannerConnection.scanFile(context, new String[]{path}, null,
                (path1, uri) -> Log.d("MoveFile", "File scanned successfully: " + path1));
    }

    @PluginMethod()
    public  void moveFile(PluginCall call) {
        String sourcePath = call.getString("sourcePath");
        String fileName = call.getString("fileName");
        String fileType = call.getString("fileType");
        Context context = getContext();
        try {
            this.moveFileToDownloads(context, sourcePath, fileName, fileType, new FileSaveCallback() {
                @Override
                public void onSuccess(Uri fileUri) {
                    Log.d("MoveFile", "File moved to Downloads: " + fileUri.toString());
                    scanFile(context, fileUri.toString());
                    JSObject ret = new JSObject();
                    ret.put("uri", fileUri);
                    call.resolve(ret);
                }

                @Override
                public void onError(Exception e) {
                    Log.e("MoveFile", "Error moving file to Downloads", e);
                }
            });
        } catch (Exception e) {
            Log.e("MoveFile", "Error moving file to Downloads", e);
            call.reject(e.getLocalizedMessage(), null, e);
        }
    }

}
