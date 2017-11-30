package com.jay.daguerre;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.StyleRes;
import android.support.v4.app.Fragment;

import com.jay.daguerre.internal.ConfigParams;
import com.jay.daguerre.internal.DaguerreActivity;
import com.jay.daguerre.provider.ImageLoader;

import java.util.ArrayList;

/**
 * Created by jay on 2017/11/23 下午3:03
 * daguerre main class
 */
public final class Daguerre {

    public final static String INTENT_EXTRA_KEY_MAX = "maxSelectable";
    public final static String INTENT_EXTRA_KEY_SPAN_COUNT = "spanCount";
    public final static String INTENT_EXTRA_KEY_THEME = "theme";
    public final static String INTENT_EXTRA_RESULT = "result";

    private Activity mActivity;
    private Fragment mFragment;
    private Intent mIntent;

    private Daguerre(Activity activity) {
        mActivity = activity;
        mIntent = new Intent(activity, DaguerreActivity.class);
    }

    private Daguerre(Fragment fragment) {
        mFragment = fragment;
        mIntent = new Intent(fragment.getContext(), DaguerreActivity.class);
    }

    public static Daguerre with(Activity activity) {
        return new Daguerre(activity);
    }

    public static Daguerre with(Fragment fragment) {
        return new Daguerre(fragment);
    }

    /**
     * 设置图片加载器
     *
     * @param imageLoader {@link ImageLoader}
     */
    public Daguerre setImageLoader(ImageLoader imageLoader) {
        ConfigParams.getInstance().setImageLoader(imageLoader);
        return this;
    }

    /**
     * 设置最大选择数据，默认为1
     *
     * @param max value
     */
    public Daguerre maxSelectable(int max) {
        mIntent.putExtra(INTENT_EXTRA_KEY_MAX, max);
        return this;
    }

    /**
     * 设置列表列数
     *
     * @param spanCount 列数
     */
    public Daguerre spanCount(int spanCount) {
        mIntent.putExtra(INTENT_EXTRA_KEY_SPAN_COUNT, spanCount);
        return this;
    }

    /**
     * 设置主题样式
     *
     * @param theme 主题样式资源
     */
    public Daguerre theme(@StyleRes int theme) {
        mIntent.putExtra(INTENT_EXTRA_KEY_THEME, theme);
        return this;
    }

    /**
     * 设置 mine 类型
     *
     * @param mimeTypes mine 类型，可传多个
     */
    public Daguerre mimeType(int mimeType, String... mimeTypes) {
        ConfigParams.getInstance().setMimeType(mimeType,mimeTypes);
        return this;
    }

    /**
     * @return 获得选择结果的第一个，一般单选操作时调用此方法
     */
    public static String obtainResult(Intent data) {
        ArrayList<String> result = data.getStringArrayListExtra(INTENT_EXTRA_RESULT);
        return result.get(0);
    }

    /**
     * @return 获得选择结果集
     */
    public static ArrayList<String> obtainResultSet(Intent data) {
        return data.getStringArrayListExtra(INTENT_EXTRA_RESULT);
    }

    /**
     * 启动库
     *
     * @param requestCode 请求码，用于返回给 {@link Activity#onActivityResult(int, int, Intent)} 判断操作类型
     */
    public void launch(int requestCode) {
        if (mActivity != null) {
            mActivity.startActivityForResult(mIntent, requestCode);
        } else if (mFragment != null) {
            mFragment.startActivityForResult(mIntent, requestCode);
        }
    }
}
