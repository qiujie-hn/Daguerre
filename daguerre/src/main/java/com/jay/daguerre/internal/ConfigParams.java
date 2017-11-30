package com.jay.daguerre.internal;

import com.jay.daguerre.provider.ImageLoader;

/**
 * Created by jay on 2017/11/24 下午5:09
 */
public class ConfigParams {

    private ImageLoader mImageLoader;
    private String[] mimeTypes;
    private int mimeType;

    private ConfigParams() {
    }

    public static ConfigParams getInstance() {
        return InstanceHolder.INSTANCE;
    }


    public ConfigParams setImageLoader(ImageLoader imageLoader) {
        mImageLoader = imageLoader;
        return this;
    }

    ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public void setMimeType(int mimeType, String[] mimeTypes) {
        this.mimeType = mimeType;
        this.mimeTypes = mimeTypes;
    }

    String[] getMimeTypes() {
        return mimeTypes;
    }

    public int getMimeType() {
        return mimeType;
    }

    private static final class InstanceHolder {
        private static final ConfigParams INSTANCE = new ConfigParams();
    }
}
