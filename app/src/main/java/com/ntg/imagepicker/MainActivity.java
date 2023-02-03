package com.ntg.imagepicker;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;

import com.ntg.imagepicker.Images.DownloadImage;
import com.ntg.imagepicker.Images.GetPermissions;

import java.io.File;

public class MainActivity extends AppCompatActivity implements
        ActivityResultCallback<ActivityResult> {

    DownloadImage.OnImageReady onImageReady;
    ActivityResultLauncher<Intent> launcher;

    Button button;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        imageView = findViewById(R.id.imageView);


        button.setOnClickListener(v -> {
            if (!GetPermissions.checkPermeationREAD_EXTERNAL_STORAGE(this))
                return;


            pickImage(new DownloadImage.OnImageReady() {

                @Override
                public void file(File image, File compressImage) {
                    //image =  الصورة بيالحجم الكامل
                    //compress = الصورة مضغوطة
                }

                @Override
                public void base64(String imageBase64, String compressImageBase64) {

                }

                @Override
                public void bytes(byte[] imageBytes, byte[] compressImageBytes) {
                    Bitmap bitmap = BitmapFactory
                            .decodeByteArray(compressImageBytes, 0, compressImageBytes.length);

                    imageView.setImageBitmap(bitmap);
                }

            });
        });

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this);
    }

    //region image

    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

    public void pickImage(DownloadImage.OnImageReady onImageReady) {

        this.onImageReady = onImageReady;
        if (GetPermissions.checkPermeationREAD_EXTERNAL_STORAGE(this))
            launcher.launch(galleryIntent);

    }

    @Override
    public void onActivityResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {

            if (result.getData() == null || result.getData().getData() == null)
                return;

            DownloadImage.pickImgFromGallery(this, result.getData().getData(), onImageReady);
        }
    }

    //endregion

}

