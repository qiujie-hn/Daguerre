package com.jay.daguerre.internal;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.jay.daguerre.MimeType;

import java.util.ArrayList;

/**
 * Created by jay on 2017/11/23 下午3:29
 */
final class Media {
    static class Resource implements Parcelable {
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

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.id);
            dest.writeString(this.data);
            dest.writeString(this.displayName);
            dest.writeString(this.mineType);
            dest.writeString(this.bucketDisplayName);
            dest.writeByte(this.isChecked ? (byte) 1 : (byte) 0);
        }

        protected Resource(Parcel in) {
            this.id = in.readString();
            this.data = in.readString();
            this.displayName = in.readString();
            this.mineType = in.readString();
            this.bucketDisplayName = in.readString();
            this.isChecked = in.readByte() != 0;
        }

        public static final Creator<Resource> CREATOR = new Creator<Resource>() {
            @Override
            public Resource createFromParcel(Parcel source) {
                return new Resource(source);
            }

            @Override
            public Resource[] newArray(int size) {
                return new Resource[size];
            }
        };
    }

    static class Album {
        String name;
        Resource cover;
        ArrayList<Resource> resources = new ArrayList<>();
        int resourceCount;
    }
}