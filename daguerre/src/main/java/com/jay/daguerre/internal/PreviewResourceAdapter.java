package com.jay.daguerre.internal;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jay.daguerre.R;
import com.jay.daguerre.provider.ImageLoader;

import java.util.List;

/**
 * Created by jay on 2017/11/27 下午4:31
 */
class PreviewResourceAdapter extends BaseRecyclerAdapter<Media.Resource, PreviewResourceAdapter.ViewHolder> {


    PreviewResourceAdapter(Context context, List<Media.Resource> datas) {
        super(context, datas);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = mLayoutInflater.inflate(R.layout.daguerre_preview_resource_item, parent, false);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ImageLoader imageLoader = ConfigParams.getInstance().getImageLoader();
        if (imageLoader != null) {
            Media.Resource item = getItem(position);
            imageLoader.loadPreviewImage(mContext, holder.mImageView, item.data, item.isGif(), item.isVideo());
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mImageView;

        ViewHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.image);
        }
    }

}
