package com.inc.os_i.camrecharge;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 1;
    private static final String FILE_PROVIDER_AUTHORITY = "com.inc.os_i.camrecharge.fileprovider";
    private static final String KEY_IMG_PATH = "tempImagePathKey";

    private String mTempPhotoPath;
    private Bitmap mResultsBitmap;

    private Button mBtn_take_photo, mBtn_clear_photo;
    private ImageView mCardImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         mBtn_take_photo = findViewById(R.id.btn_photo);
         mBtn_clear_photo = findViewById(R.id.btn_cancel);
         mCardImageView = findViewById(R.id.image_view);

        mBtn_take_photo.setVisibility(View.VISIBLE);
        mBtn_clear_photo.setVisibility(View.GONE);

        mBtn_take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rechargeShot();
            }
        });

        mBtn_clear_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteImage();
            }
        });
        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            mTempPhotoPath = savedInstanceState.getString(KEY_IMG_PATH);
        }

    }

    /**
     * OnClick method for Launching the camera app
     */
    public void rechargeShot() {
        // Check for the external storage permission
        // ContextCompat - Helper to access features in context
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // If you do not have permission, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            // Launch the camera if the permission exists
            launchCamera();
        }
    }


    private void launchCamera() {

        // Create the capture image intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the temporary File where the photo should go
            File photoFile = null;
            try {
                photoFile = BitmapUtils.createTempImageFile(this);
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                // Get the path of the temporary file
                mTempPhotoPath = photoFile.getAbsolutePath();

                // Get the content URI for the image file
                Uri photoURI = FileProvider.getUriForFile(this,
                        FILE_PROVIDER_AUTHORITY,
                        photoFile);

                // Add the URI so the camera can store the image
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Launch the camera activity
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save the user's current game state
        outState.putString(KEY_IMG_PATH, mTempPhotoPath);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If the image capture activity was called and was successful
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Process the image and set it to the TextView
            processAndSetImage();
        } else {

            // Otherwise, delete the temporary image file
            BitmapUtils.deleteImageFile(this, mTempPhotoPath);
        }
    }

    /**
     * Method for processing the captured image and setting it to the TextView for loading
     * the card
     */
    private void processAndSetImage() {

        // Toggle Visibility of the views
        mBtn_take_photo.setVisibility(View.GONE);
        mBtn_clear_photo.setVisibility(View.VISIBLE);

        // Resample the saved image to fit the ImageView
        mResultsBitmap = BitmapUtils.resamplePic(this, mTempPhotoPath);

        // Set the new bitmap to the ImageView
        mCardImageView.setImageBitmap(mResultsBitmap);
    }

    /**
     * OnClick method for the save button.
     * @param view The save button.
     *
     */
    public void saveMe(View view) {
        // Delete the temporary image file
        BitmapUtils.deleteImageFile(this, mTempPhotoPath);

        // Save the image
        BitmapUtils.saveImage(this, mResultsBitmap);
    }

    /**
     * OnClick method for the share button, saves and shares the new bitmap.
     *
     * @param view The share button.
     */
    public void shareMe(View view) {
        // Delete the temporary image file
        BitmapUtils.deleteImageFile(this, mTempPhotoPath);
        // Save the image
        BitmapUtils.saveImage(this, mResultsBitmap);
        // Share the image
        BitmapUtils.shareImage(this, mTempPhotoPath);
    }

    /**
     * OnClick for the clear button, resets the app to original state.
     *  The delete button.
     */
    public void deleteImage() {
        // Clear the image and toggle the view visibility
        mCardImageView.setImageResource(0);
        mBtn_take_photo.setVisibility(View.VISIBLE);
        mBtn_clear_photo.setVisibility(View.GONE);
        // Delete the temporary image file
        try {
            BitmapUtils.deleteImageFile(this, mTempPhotoPath);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "No image to delete.", Toast.LENGTH_SHORT).show();
        }

    }
}
