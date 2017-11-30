package com.jay.daguerre.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.jay.daguerre.Daguerre;
import com.jay.daguerre.MimeType;
import com.jay.daguerre.provider.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PHOTO = 123;
    private static final int VIDEO = 124;
    private TextView mTextResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_photo).setOnClickListener(this);
        findViewById(R.id.btn_video).setOnClickListener(this);
        findViewById(R.id.btn_image_video).setOnClickListener(this);
        mTextResult = findViewById(R.id.text_result);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_photo:
                Daguerre.with(this)
                        .theme(R.style.DaguerreTheme) // 设置主题样式
                        .spanCount(3) // 设置列表列数，默认为3
                        .maxSelectable(3) // 设置最大选择数据，默认为1
                        .mimeType(MimeType.IMAGE, MimeType.JPEG) // 设置mimeType，默认会显示所有图片与视频
                        .setImageLoader(new MyImageLoader()) // 设置图片加载器，必须设置的参数，否则图片等资源无法显示
                        .launch(PHOTO);
                break;
            case R.id.btn_video:
                Daguerre.with(this)
                        .theme(R.style.DaguerreTheme) // 设置主题样式
                        .spanCount(2) // 设置列表列数，默认为3
                        .maxSelectable(1) // 设置最大选择数据，默认为1
                        .mimeType(MimeType.VIDEO, MimeType.MP4) // 设置mimeType，默认会显示所有图片与视频
                        .setImageLoader(new MyImageLoader()) // 设置图片加载器，必须设置的参数，否则图片等资源无法显示
                        .launch(VIDEO);
                break;
            case R.id.btn_image_video:
                Daguerre.with(this)
                        .theme(R.style.DaguerreTheme) // 设置主题样式
                        .spanCount(4) // 设置列表列数，默认为3
                        .maxSelectable(1) // 设置最大选择数据，默认为1
                        .mimeType(MimeType.IMAGE_AND_VIDEO, MimeType.MP4, MimeType.JPEG, MimeType.GIF) // 设置mimeType，默认会显示所有图片与视频
                        .setImageLoader(new MyImageLoader()) // 设置图片加载器，必须设置的参数，否则图片等资源无法显示
                        .launch(VIDEO);
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO && resultCode == RESULT_OK) {
            ArrayList<String> photos = Daguerre.obtainResultSet(data);
            mTextResult.setText("");
            for (String photo : photos) {
                mTextResult.append(photo);
                mTextResult.append("\n");
            }
        } else if (requestCode == VIDEO && resultCode == RESULT_OK) {
            String videoPath = Daguerre.obtainResult(data);
            mTextResult.setText(videoPath);
        }
    }

    class MyImageLoader implements ImageLoader {

        @Override
        public void loadImage(Context context, ImageView imageView, String uri, boolean isGif, boolean isVideo) {
            RequestOptions requestOptions = new RequestOptions()
                    .centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE)
                    .override(120, 120);
            Glide.with(context).load(uri).apply(requestOptions).into(imageView);
        }

        @Override
        public void loadPreviewImage(Context context, ImageView imageView, String uri, boolean isGif, boolean isVideo) {
            RequestOptions requestOptions = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.NONE);
            Glide.with(context).load(uri).apply(requestOptions).into(imageView);
        }

        @Override
        public void loadAlbumImage(Context context, ImageView imageView, String uri) {
            RequestOptions requestOptions = new RequestOptions()
                    .centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE)
                    .override(120, 120);
            Glide.with(context).load(uri).apply(requestOptions).into(imageView);
        }
    }
}
