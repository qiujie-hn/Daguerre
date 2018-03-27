package com.jay.daguerre.internal;

import android.Manifest;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.jay.daguerre.Daguerre;
import com.jay.daguerre.MimeType;
import com.jay.daguerre.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by jay on 2017/11/23 下午3:09
 */
public class DaguerreActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        ActionMode.Callback,
        CompoundButton.OnCheckedChangeListener,
        ResourceItemAdapter.OnItemClickListener,
        AlbumsItemAdapter.OnItemClickListener {
    private static final int REQUEST_READ_EXTERNAL_STORAGE_PERMISSION = 123;
    private static final int REQUEST_CAMERA_IMAGE = 124;
    private static final int REQUEST_CAMERA_VIDEO = 125;
    private static final int REQUEST_CAMERA_APP = 126;

    private static final String _ID = MediaStore.Files.FileColumns._ID;
    private static final String DATA = MediaStore.Files.FileColumns.DATA;
    private static final String DISPLAY_NAME = MediaStore.Files.FileColumns.DISPLAY_NAME;
    private static final String MIME_TYPE = MediaStore.Files.FileColumns.MIME_TYPE;
    private static final String BUCKET_DISPLAY_NAME = "bucket_display_name";
    private static final String[] COLUMNS_NAME = {
            _ID,
            DATA,
            DISPLAY_NAME,
            MIME_TYPE, BUCKET_DISPLAY_NAME
    };

    private ArrayList<Media.Resource> mResources = Media.getResourceStoreInstance().getResources();
    private ArrayList<Media.Album> mAlbums = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private ResourceItemAdapter mAdapter;
    private ActionMode mActionModel;

    private ArrayList<String> mSelectResources = new ArrayList<>();
    /**
     * 最大选择数量
     */
    private int max = 1;
    private File mCameraOutPutFile;
    private RecyclerView mNavRecyclerView;
    private AlbumsItemAdapter mAlbumsItemAdapter;
    private DrawerLayout mDrawerLayout;
    private ContentLoadingProgressBar mContentLoadingProgressBar;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCameraOutPutFile != null) {
            outState.putString("take_photo_file", mCameraOutPutFile.getAbsolutePath());
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Intent intent = getIntent();
        int theme = intent.getIntExtra(Daguerre.INTENT_EXTRA_KEY_THEME, R.style.Daguerre_Activity_Theme);
        setTheme(theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.daguerre_activity);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        Toolbar toolbar = findViewById(android.support.v7.appcompat.R.id.action_bar);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, 0, 0);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mContentLoadingProgressBar = findViewById(R.id.content_progress_bar);

        if (savedInstanceState != null) {
            // 恢复数据
            String take_photo_file = savedInstanceState.getString("take_photo_file", null);
            if (!TextUtils.isEmpty(take_photo_file)) {
                mCameraOutPutFile = new File(take_photo_file);
            }
        }

        max = intent.getIntExtra(Daguerre.INTENT_EXTRA_KEY_MAX, 1);

        int spanCount = intent.getIntExtra(Daguerre.INTENT_EXTRA_KEY_SPAN_COUNT, 3);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(16, 16, 16, 16);
            }
        });

        mAdapter = new ResourceItemAdapter(this);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnCheckedChangeListener(this);
        mRecyclerView.setAdapter(mAdapter);


        mNavRecyclerView = findViewById(R.id.nav_recycler_view);
        mNavRecyclerView.setHasFixedSize(true);
        mNavRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mNavRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mAlbumsItemAdapter = new AlbumsItemAdapter(this);
        mAlbumsItemAdapter.setOnItemClickListener(this);
        mNavRecyclerView.setAdapter(mAlbumsItemAdapter);

        // 检测读取、写入权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
            startLoader();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // empty code
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_READ_EXTERNAL_STORAGE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 权限授权结果
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE_PERMISSION &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            startLoader();
        }
    }

    private void startLoader() {
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = null;
        String[] mimeTypes = ConfigParams.getInstance().getMimeTypes();
        if (mimeTypes != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mimeTypes.length; i++) {
                sb.append("mime_type=?");
                if (i + 1 < mimeTypes.length) {
                    sb.append(" or ");
                }
            }
            selection = sb.toString();
        }

        int mimeType = ConfigParams.getInstance().getMimeType();
        Uri uri;
        switch (mimeType) {
            case MimeType.IMAGE:
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                break;
            case MimeType.VIDEO:
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                break;
            default:
                uri = MediaStore.Files.getContentUri("external");
        }

        return new CursorLoader(
                this,
                uri,
                COLUMNS_NAME,
                selection,
                mimeTypes,
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mResources.clear();
        mAlbums.clear();

        Media.Album allAlbum = new Media.Album();
        allAlbum.name = getString(R.string.daguerre_all);

        if (data != null && data.getCount() > 0 && data.moveToFirst()) {
            do {
                String _id = data.getString(data.getColumnIndex(_ID));
                String _data = data.getString(data.getColumnIndex(DATA));
                String _display_name = data.getString(data.getColumnIndex(DISPLAY_NAME));
                String mime_type = data.getString(data.getColumnIndex(MIME_TYPE));
                String bucket_display_name = data.getString(data.getColumnIndex(BUCKET_DISPLAY_NAME));
                Media.Resource resource = new Media.Resource(_id, _data, _display_name, mime_type, bucket_display_name);
                mResources.add(resource);

                // 统计专辑列表
                Media.Album album = filterAlbumByBucketName(bucket_display_name);
                if (album == null) {
                    album = new Media.Album();
                    album.name = bucket_display_name;
                    album.cover = resource;
                    album.resourceCount++;
                    mAlbums.add(album);
                } else {
                    album.resourceCount++;
                }
                album.resources.add(resource);
                allAlbum.resources.add(resource);
            } while (data.moveToNext());

            mAdapter.setData(mResources);
            mAdapter.notifyDataSetChanged();
            setTitle(allAlbum.name);

            Media.Album firstAlbum = mAlbums.get(0);

            allAlbum.cover = firstAlbum.cover;
            allAlbum.resourceCount = data.getCount();
            mAlbums.add(0, allAlbum);
            mAlbumsItemAdapter.setData(mAlbums);
            mAlbumsItemAdapter.notifyDataSetChanged();
        }
        mContentLoadingProgressBar.hide();
    }

    /**
     * 根据 bucketName 进行筛选是否已经存在 Album
     *
     * @return 存在返回 {@link Media.Album}，反之返回null
     */
    private Media.Album filterAlbumByBucketName(String bucketName) {
        for (Media.Album album : mAlbums) {
            if (TextUtils.equals(album.name, bucketName)) {
                return album;
            }
        }
        return null;
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mResources.clear();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(View itemView) {
        int adapterPosition = mRecyclerView.getChildViewHolder(itemView).getAdapterPosition();
        Intent intent = new Intent(this, PreviewResourceActivity.class);
        intent.putExtra("position", adapterPosition);

        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(this, itemView.findViewById(R.id.image), "element");
        ActivityCompat.startActivity(this, intent, options.toBundle());
    }

    @Override
    public void onAlbumsItemClick(View itemView) {
        int adapterPosition = mNavRecyclerView.getChildViewHolder(itemView).getAdapterPosition();
        Media.Album album = mAlbums.get(adapterPosition);
        mResources = album.resources;
        mAdapter.setData(mResources);
        mAdapter.notifyDataSetChanged();

        setTitle(album.name);
        mDrawerLayout.closeDrawers();
        if (mActionModel != null) {
            mActionModel.finish();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        View itemView = (View) buttonView.getParent();
        int adapterPosition = mRecyclerView.getChildViewHolder(itemView).getAdapterPosition();

        Media.Resource resource = mAdapter.getItem(adapterPosition);
        String data = resource.data;
        resource.isChecked = isChecked;
        if (isChecked) {
            if (!mSelectResources.contains(data)) {
                if (mSelectResources.size() == max) {
                    buttonView.setChecked(false);
                    resource.isChecked = false;
                    Toast.makeText(this, getString(R.string.daguerre_max_select, max), Toast.LENGTH_SHORT).show();
                    return;
                }
                mSelectResources.add(data);
            }
            if (mActionModel == null) {
                startSupportActionMode(this);
            }
        } else {
            if (mSelectResources.contains(data))
                mSelectResources.remove(data);
        }
        if (mActionModel != null) {
            if (mSelectResources.isEmpty()) {
                mActionModel.finish();
            } else {
                mActionModel.setTitle(getString(R.string.daguerre_select, mSelectResources.size()));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        switch (ConfigParams.getInstance().getMimeType()) {
            case MimeType.IMAGE:
                getMenuInflater().inflate(R.menu.daguerre_action_camera, menu);
                break;
            case MimeType.VIDEO:
                getMenuInflater().inflate(R.menu.daguerre_action_video, menu);
                break;
            default:
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.menu_camera) {
            launchCameraApp(REQUEST_CAMERA_IMAGE);
            return true;
        } else if (itemId == R.id.menu_video) {
            launchCameraApp(REQUEST_CAMERA_VIDEO);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * start the camera app
     */
    private void launchCameraApp(int useType) {
        Intent cameraIntent = new Intent();
        if (useType == REQUEST_CAMERA_VIDEO) {
            cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            cameraIntent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
        } else {
            cameraIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            if (useType == REQUEST_CAMERA_VIDEO) {
                mCameraOutPutFile = createVideoFile();
            } else {
                mCameraOutPutFile = createPhotoFile();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 适配 7.0+ 系统
                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".daguerre.fileprovider", mCameraOutPutFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCameraOutPutFile));
            }
            if (useType == REQUEST_CAMERA_VIDEO) {
                cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            }
            startActivityForResult(cameraIntent, REQUEST_CAMERA_APP);
        } else {
            Toast.makeText(this, R.string.daguerre_not_found_camera_app, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * create a photo file storage path
     */
    private static File createPhotoFile() {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File dcimFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        return new File(dcimFile, "Camera/IMG_" + timeStamp + ".jpg");
    }

    private static File createVideoFile() {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File dcimFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        return new File(dcimFile, "Camera/VID_" + timeStamp + ".mp4");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CAMERA_APP) {
            // 拍照回来
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(mCameraOutPutFile)));
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mActionModel = mode;
        mode.getMenuInflater().inflate(R.menu.daguerre_action_mode, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (item.getItemId() == R.id.menu_done) {
            Intent intent = new Intent();
            intent.putExtra(Daguerre.INTENT_EXTRA_RESULT, mSelectResources);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mode.setTitle("");
        mActionModel = null;
        for (Media.Resource resource : mResources) {
            resource.isChecked = false;
        }
        mSelectResources.clear();
        mAdapter.notifyDataSetChanged();
    }
}