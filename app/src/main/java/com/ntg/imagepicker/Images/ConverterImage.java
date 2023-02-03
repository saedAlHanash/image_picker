package com.ntg.imagepicker.Images;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import id.zelory.compressor.Compressor;

public class ConverterImage {

    /**
     * convert any image type Base64 to Bitmap
     *
     * @param base64_Image string type Base64
     * @return Bitmap for image
     */
    public static Bitmap convertBase64ToBitmap(String base64_Image) {
        byte[] decodedString = Base64.decode(base64_Image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    /**
     * convert any Bitmap to image type Base64
     *
     * @param bitmap image bitmap
     * @return string Base64
     */
    public static String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
    }

    /**
     * convert any image with type URI to Base64
     *
     * @param context      needed to convert URI to Bitmap
     * @param selectedFile an image with type URI
     * @return string type Base64
     */
    public static String convertUriToBase64(Context context, Uri selectedFile) {
        Bitmap bitmap;
        String encodedString;


        if (selectedFile != null) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), selectedFile);

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            //  bitmap = Bitmap.createScaledBitmap(bitmap, 500, 500, true);
            bitmap = getResizedBitmap(bitmap, 300);

            bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);

            byte[] byteArray = outputStream.toByteArray();

            encodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);
        } else {
            return "";
        }
        return encodedString;
    }

    public static String bytesToBase64(byte[] bytes) {
       return Base64.encodeToString(bytes,Base64.DEFAULT);
    }


    public Bitmap compress(Bitmap yourBitmap) {
        //converted into webp into lowest quality
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        yourBitmap.compress(Bitmap.CompressFormat.WEBP, 0, stream);//0=lowest, 100=highest quality
        byte[] byteArray = stream.toByteArray();

        //convert your byteArray into bitmap
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    public static String resizeBase64Image(String base64image) {
        byte[] encodeByte = Base64.decode(base64image.getBytes(), Base64.DEFAULT);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        Bitmap image = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length, options);


        if (image.getHeight() <= 400 && image.getWidth() <= 400) {
            return base64image;
        }
        image = Bitmap.createScaledBitmap(image, 200, 200, false);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);

        byte[] b = baos.toByteArray();
        System.gc();
        return Base64.encodeToString(b, Base64.NO_WRAP);

    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            Log.e("SAED_", "getRealPathFromURI: ", e);
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }

    public static File convertBitmapToFile(Context mContext, Bitmap bitmap) {
        try {
            File f = new File(mContext.getCacheDir(), "image");
            f.createNewFile();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();


            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            return compressImageFile(mContext, f);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @NotNull
    public static File compressImageFile(Context mContext, @NotNull File file) {
        try {
            return new Compressor(mContext).setQuality(50).compressToFile(file);
        } catch (IOException e) {
            e.printStackTrace();
            return file;
        }
    }

    /**
     * reduces the size of the image
     *
     * @param image
     * @param maxSize
     * @return
     */
    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public static byte[] getBytes(InputStream inputStream) {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;

        while (true) {
            try {
                if ((len = inputStream.read(buffer)) == -1)
                    break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            byteBuffer.write(buffer, 0, len);
        }

        return byteBuffer.toByteArray();
    }

    public static String writeByteAsFile(byte[] bytes, Context context) throws IOException {

        Pair<File, String> pair = createImageFile(context);

        FileUtils.writeByteArrayToFile(new File(pair.second), bytes);

        return pair.second;
    }

    public static void writeFile(File file, Context context) throws IOException {

        // Create an image file name
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HH-mm-ss", Locale.ENGLISH).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );


        FileUtils.writeByteArrayToFile(image, FileUtils.readFileToByteArray(file));

    }

    public static Pair<File, String> createImageFile(Context context) throws IOException {

        String path;
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HH-mm-ss", Locale.ENGLISH).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        path = image.getAbsolutePath();
        return new Pair<>(image, path);
    }

    public static byte[] getByteImage(Context context, Uri uri) throws FileNotFoundException {
        InputStream iStream = context.getContentResolver().openInputStream(uri);
        return getBytes(iStream);
    }

    // file to byte[], old and classic way, before Java 7
    public static byte[] readFileToBytes(@NotNull File file) throws IOException {

        byte[] bytes = new byte[(int) file.length()];
        int check = -1;
        try (FileInputStream fis = new FileInputStream(file)) {

            check = fis.read(bytes);
        }

        return bytes;
    }


}
