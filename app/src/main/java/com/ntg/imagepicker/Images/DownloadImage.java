package com.ntg.imagepicker.Images;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import id.zelory.compressor.BuildConfig;

public class DownloadImage {


    public static void getImageFile(@NotNull Activity context, @NotNull String url) {

        if (!GetPermissions.checkPermeationREAD_EXTERNAL_STORAGE(context))
            return;

//        Glide.with(context)
//                .asBitmap()
//                .load(url)
//
//                .into(new SimpleTarget<Bitmap>() {
//                    @Override
//                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
//                        saveImage(resource, context);
//                    }
//                });

        new Thread(() -> {
            try {
                saveImage(Glide.with(context).asBitmap().load(url).submit().get(), context);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();


    }

    private static void saveImage(Bitmap resource, Activity activity) {

        String savedImagePath;
        String imageFileName = Calendar.getInstance().getTimeInMillis() + ".jpg";


        final File storageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Pics");

        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }

        if (success) {
            File imageFile = new File(storageDir, imageFileName);
            savedImagePath = imageFile.getAbsolutePath();

            try {

                OutputStream fOut = new FileOutputStream(imageFile);
                resource.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            activity.runOnUiThread(() -> {
                // Add the image to the system gallery
                galleryAddPic(savedImagePath, activity);
                Toast.makeText(activity, "تم الحفظ", Toast.LENGTH_LONG).show();
            });
        }
    }


    // Add the image to the system gallery
    private static void galleryAddPic(String imagePath, Activity activity) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        activity.sendBroadcast(mediaScanIntent);
    }

    public static void write(String fileName, Bitmap bitmap, Activity context) {
        FileOutputStream outputStream;
        try {
            outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (Exception error) {
            error.printStackTrace();
        }
    }


    public static ActivityResultCallback<ActivityResult> pickImgFromGallery(Activity activity,
                                                                            OnImageReady onImageReady) {

        return result -> {

            if (result.getResultCode() == Activity.RESULT_OK) {
                if (result.getData() == null)
                    return;

                Uri uri = result.getData().getData();
                if (uri == null)
                    return;

                new Thread(() -> {

                    final byte[] bytes, compressBytes;
                    final File file, compressFile;
                    final String base64, compressBase64;

                    try {

                        bytes = ConverterImage.getByteImage(activity, uri);
                        String path = ConverterImage.writeByteAsFile(bytes, activity);

                        file = new File(path);
                        compressFile = ConverterImage.compressImageFile(activity, file);

                        compressBytes = ConverterImage.readFileToBytes(compressFile);

                        base64 = ConverterImage.bytesToBase64(bytes);
                        compressBase64 = ConverterImage.bytesToBase64(compressBytes);

                        if (onImageReady != null)
                            activity.runOnUiThread(() -> {
                                onImageReady.bytes(bytes, compressBytes);
                                onImageReady.file(file, compressFile);
                                onImageReady.base64(base64, compressBase64);
                            });

                        String log = " " +
                                "\n byte size= " + (bytes.length / 1024.0F) + " KB" +
                                "\n compress byte size= " + (compressBytes.length / 1024.0F) + " KB" +
                                "\n file size = " + (file.length() / 1024.0F) + " KB" +
                                "\n compress file size= " + (compressFile.length() / 1024.0F) + " KB" +
                                "\n base 64 size = " + (base64.length() / 1024.0F) + " KB" +
                                "\n compress base 64 size = " + (compressBase64.length() / 1024.0F) + " KB" +
                                "\n";

                        Log.d(TAG, "pickImgFromGallery: " + log);

                    } catch (IOException ignored) {

                    }

                }).start();
            }

        };
    }

    public static void pickImgFromGallery(Activity activity, Uri uri, OnImageReady onImageReady) {

        if (uri == null)
            return;

        new Thread(() -> {

            final byte[] bytes, compressBytes;
            final File file, compressFile;
            final String base64, compressBase64;

            try {

                bytes = ConverterImage.getByteImage(activity, uri);
                String path = ConverterImage.writeByteAsFile(bytes, activity);

                file = new File(path);
                compressFile = ConverterImage.compressImageFile(activity, file);

                compressBytes = ConverterImage.readFileToBytes(compressFile);

                base64 = ConverterImage.bytesToBase64(bytes);
                compressBase64 = ConverterImage.bytesToBase64(compressBytes);

                if (onImageReady != null)
                    activity.runOnUiThread(() -> {
                        onImageReady.bytes(bytes, compressBytes);
                        onImageReady.file(file, compressFile);
                        onImageReady.base64(base64, compressBase64);
                    });

                String log = " " +
                        "\n byte size= " + (bytes.length / 1024.0F) + " KB" +
                        "\n compress byte size= " + (compressBytes.length / 1024.0F) + " KB" +
                        "\n file size = " + (file.length() / 1024.0F) + " KB" +
                        "\n compress file size= " + (compressFile.length() / 1024.0F) + " KB" +
                        "\n base 64 size = " + (base64.length() / 1024.0F) + " KB" +
                        "\n compress base 64 size = " + (compressBase64.length() / 1024.0F) + " KB" +
                        "\n";

                Log.d(TAG, "pickImgFromGallery: " + log);

            } catch (IOException ignored) {

            }

        }).start();

    }

    public static void pickImgFromGallery(Activity activity, String path, OnImageReady onImageReady) {

        if (path == null)
            return;

        new Thread(() -> {

            final byte[] bytes, compressBytes;
            final File file, compressFile;
            final String base64, compressBase64;

            try {

                file = new File(path);
                bytes = ConverterImage.readFileToBytes(file);
                compressFile = ConverterImage.compressImageFile(activity, file);

                compressBytes = ConverterImage.readFileToBytes(compressFile);

                base64 = ConverterImage.bytesToBase64(bytes);
                compressBase64 = ConverterImage.bytesToBase64(compressBytes);

                if (onImageReady != null)
                    activity.runOnUiThread(() -> {
                        onImageReady.bytes(bytes, compressBytes);
                        onImageReady.file(file, compressFile);
                        onImageReady.base64(base64, compressBase64);
                    });

                String log = " " +
                        "\n byte size= " + (bytes.length / 1024.0F) + " KB" +
                        "\n compress byte size= " + (compressBytes.length / 1024.0F) + " KB" +
                        "\n file size = " + (file.length() / 1024.0F) + " KB" +
                        "\n compress file size= " + (compressFile.length() / 1024.0F) + " KB" +
                        "\n base 64 size = " + (base64.length() / 1024.0F) + " KB" +
                        "\n compress base 64 size = " + (compressBase64.length() / 1024.0F) + " KB" +
                        "\n";

                Log.d(TAG, "pickImgFromGallery: " + log);

            } catch (IOException ignored) {

            }

        }).start();

    }

    private static final String TAG = "DownloadImage__";

    public interface OnImageReady extends Serializable {

        void file(File image, File compressImage);

        void base64(String imageBase64, String compressImageBase64);

        void bytes(byte[] imageBytes, byte[] compressImageBytes);
    }
}
