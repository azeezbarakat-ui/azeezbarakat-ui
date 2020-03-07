package com.crazy_iter.eresta;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.fenchtose.nocropper.CropperView;

public class CropImageActivity extends AppCompatActivity {

    Button cropBTN, cancelBTN;
    ImageView snapIV, rotateIV, backIV;
    CropperView cropCV;
    Bitmap bitmap;
    boolean isSnappedCenter = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);

        initViews();

        backIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        cropBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cropImage();
            }
        });

        cancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(CropImageActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        });

        snapIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSnappedCenter) {
                    cropCV.cropToCenter();
                } else {
                    cropCV.fitToCenter();
                }

                isSnappedCenter = !isSnappedCenter;
            }
        });

        rotateIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cropCV.setImageBitmap(rotateBitmap(bitmap));
            }
        });

    }

    private float currentAngle = 0F;
    private Bitmap rotateBitmap(Bitmap bitmap) {
        if (currentAngle == 270F) {
            currentAngle = 0F;
        } else {
            currentAngle += 90F;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(currentAngle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void cropImage() {
        bitmap = cropCV.getCroppedBitmap();
        if (bitmap != null) {
//            cropCV.setImageBitmap(bitmap);
            Intent data = new Intent();
            data.setData(StaticsData.INSTANCE.bitmapToUri(this, bitmap));
            setResult(RESULT_OK, data);
            finish();
        }

    }

    private void initViews() {
        backIV = findViewById(R.id.cropBackIV);
        cropBTN = findViewById(R.id.cropBTN);
        cancelBTN = findViewById(R.id.cancelCropBTN);
        snapIV = findViewById(R.id.cropIV);
        rotateIV = findViewById(R.id.rotateIV);
        cropCV = findViewById(R.id.cropCV);
        cropCV.setGestureEnabled(true);
        byte[] imageBytes = getIntent().getByteArrayExtra(StaticsData.INSTANCE.getIMAGE());
        if (imageBytes == null) {
            onBackPressed();
        } else {
            bitmap = StaticsData.INSTANCE.bytesToBitmap(imageBytes);
            cropCV.setImageBitmap(bitmap);
        }
    }

}
