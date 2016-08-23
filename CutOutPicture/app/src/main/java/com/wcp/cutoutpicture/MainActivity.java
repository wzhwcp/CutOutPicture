package com.wcp.cutoutpicture;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.wcp.cutoutpicture.view.CutOutLayout;

public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        final CutOutLayout cutOutLayout = (CutOutLayout) findViewById(R.id.cutout_layout);
        cutOutLayout.setShowPic(BitmapFactory.decodeResource(getResources(),R.drawable.beautiful));

        (findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView imageView = (ImageView) findViewById(R.id.output_bitmap);
                imageView.setImageBitmap(cutOutLayout.getCutOutBitmap());
                cutOutLayout.setVisibility(View.GONE);
            }
        });

    }
}
