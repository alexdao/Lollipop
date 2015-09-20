package com.lumivote.lollipop.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.clarifai.api.ClarifaiClient;
import com.clarifai.api.RecognitionRequest;
import com.clarifai.api.RecognitionResult;
import com.clarifai.api.Tag;
import com.lumivote.lollipop.R;
import com.lumivote.lollipop.TinyDB;
import com.lumivote.lollipop.api.UploadRESTAdapter;
import com.lumivote.lollipop.bus.BusProvider;
import com.lumivote.lollipop.bus.ImageUploadEvent;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final String APP_ID = "Jnd1zR_3ZtsW9xsr0EW87IKQyIIuxeTRHbcNBAt1";
    static final String APP_SECRET = "3ga_7Lvnfv9hWl5s4BnaSwuT_dE2DsXi6QK01e2X";
    String mCurrentPhotoPath;
    File mCurrentImage;

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.tabLayout)
    TabLayout tabLayout;
    @Bind(R.id.viewpager)
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        initTabLayout();
    }

    private void initTabLayout() {
        TabsFragmentPagerAdapter pagerAdapter = new TabsFragmentPagerAdapter(this.getSupportFragmentManager(),
                this);
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_clear) {
            TinyDB tinyDB = new TinyDB(this);
            tinyDB.putList(getString(R.string.photoPaths), new ArrayList<String>());
            tinyDB.putList(getString(R.string.photoDates), new ArrayList<String>());
            tinyDB.putList(getString(R.string.photoTags), new ArrayList<String>());
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void handleImageUploadEvent(ImageUploadEvent event) {
        TinyDB tinyDB = new TinyDB(this);
        String tag = event.getTag();
        ArrayList<String> photoTags = tinyDB.getList(getString(R.string.photoTags));
        photoTags.add(tag);
        tinyDB.putList(getString(R.string.photoTags), photoTags);
    }

    public static class TabsFragmentPagerAdapter extends FragmentPagerAdapter {

        final int PAGE_COUNT = 3;
        private String tabTitles[] = new String[3];
        private Context context;

        public TabsFragmentPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
            tabTitles[0] = context.getString(R.string.camera_tab_title);
            tabTitles[1] = context.getString(R.string.result_tab_title);
            tabTitles[2] = context.getString(R.string.history_tab_title);
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return CameraFragment.newInstance(position + 1);
            } else if (position == 1) {
                return ResultFragment.newInstance(position + 1);
            } else {
                return HistoryFragment.newInstance(position + 1);
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

    }

    public void fetchTag(View view) {
        ClarifaiClient clarifai = new ClarifaiClient(APP_ID, APP_SECRET);
        List<RecognitionResult> results =
                clarifai.recognize(new RecognitionRequest("http://www.clarifai.com/static/img_ours/metro-north.jpg"));
        Tag mostRelevantTag = null;
        for (Tag tag : results.get(0).getTags()) {
            if (mostRelevantTag == null || mostRelevantTag.getProbability() < tag.getProbability())
                mostRelevantTag = tag;
        }

        TinyDB tinyDB = new TinyDB(this);
        ArrayList<String> photoTags = tinyDB.getList(getString(R.string.photoTags));
        photoTags.add(mostRelevantTag.getName());
        tinyDB.putList(getString(R.string.photoTags), photoTags);
    }

    public void takePicture(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("Error creating file", ex.getMessage());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                galleryAddPic();
                startActivityForResult(intent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String formattedTime = new SimpleDateFormat("MM-dd-yyyy HH:mm").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        mCurrentImage = image;
        mCurrentPhotoPath = image.getAbsolutePath();

        //posting image to server
        UploadRESTAdapter client = UploadRESTAdapter.getInstance();
        client.uploadImage(mCurrentPhotoPath);

        TinyDB tinyDB = new TinyDB(this);
        ArrayList<String> photoPaths = tinyDB.getList(getString(R.string.photoPaths));
        photoPaths.add(mCurrentPhotoPath);
        tinyDB.putList(getString(R.string.photoPaths), photoPaths);

        ArrayList<String> photoDates = tinyDB.getList(getString(R.string.photoDates));
        photoDates.add(formattedTime);
        tinyDB.putList(getString(R.string.photoDates), photoDates);

        /**
         ArrayList<String> photoTags = tinyDB.getList(getString(R.string.photoTags));
         photoTags.add("Pink eye");
         tinyDB.putList(getString(R.string.photoTags), photoTags);*/
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
        Log.v("Broadcast sent!", "yes");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Bundle extras = data.getExtras();
            //Bitmap imageBitmap = (Bitmap) extras.get("data");
            //mImageView.setImageBitmap(imageBitmap);
            Log.v("Image taken!", "yes");
        }
    }
}
