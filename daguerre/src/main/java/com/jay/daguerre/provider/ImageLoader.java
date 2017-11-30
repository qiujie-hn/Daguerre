package com.jay.daguerre.provider;


import android.content.Context;
import android.widget.ImageView;

/**
 * Created by jay on 2017/11/23 下午4:12
 */
public interface ImageLoader {
    /**
     * 加载列表图片
     *
     * @param uri photo path
     */
    void loadImage(Context context, ImageView imageView, String uri, boolean isGif, boolean isVideo);

    /**
     * 加载预览大图
     *
     * @param uri photo path
     */
    void loadPreviewImage(Context context, ImageView imageView, String uri, boolean isGif, boolean isVideo);

    /**
     * 加载 Album 列表封面图片
     *
     * @param uri photo path
     */
    void loadAlbumImage(Context context, ImageView imageView, String uri);
}
