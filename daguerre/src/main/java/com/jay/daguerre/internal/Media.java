package com.jay.daguerre.internal;

import com.jay.daguerre.MimeType;

import java.util.ArrayList;

/**
 * Created by jay on 2017/11/23 下午3:29
 */
final class Media {
    static class Resource{
        String id;
        String data = ""; //file path
        //        long size;
        String displayName = ""; // xxx.jpg
        String mineType; // image/jpeg
        //        String title;
//        long dateAdded; // 时间戳
//        long dateModified; // 时间戳
//        String description;
//        String picasaId;
//        String isPrivate;
//        String latitude;
//        String longitude;
//        String dateTaken;
//        int orientation;
//        String miniThumbMagic;
//        String bucketId;
        String bucketDisplayName = "";
        //        int width;
//        int height;
        boolean isChecked;

        public Resource(String id, String data, String displayName, String mineType, String bucketDisplayName) {
            this.id = id;
            this.data = data;
            this.displayName = displayName;
            this.mineType = mineType;
            this.bucketDisplayName = bucketDisplayName;
        }

        public boolean isVideo() {
            return mineType != null && mineType.contains("video");
        }

        public boolean isGif() {
            return mineType != null && mineType.equals(MimeType.GIF);
        }

    }

    static class Album {
        String name;
        Resource cover;
        ArrayList<Resource> resources = new ArrayList<>();
        int resourceCount;
    }

    static ResourceStore getResourceStoreInstance() {
        return ResourceStore.instance;
    }

    static class ResourceStore{

        private ArrayList<Media.Resource> mResources = new ArrayList<>();
        static ResourceStore instance = new ResourceStore();

        private ResourceStore() {

        }

        ArrayList<Resource> getResources() {
            return mResources;
        }
    }
}