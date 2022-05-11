/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bytenine.tinnitusrelief.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.media.MediaController2;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bytenine.tinnitusrelief.R;
import com.bytenine.tinnitusrelief.client.MediaBrowserHelper;
import com.bytenine.tinnitusrelief.service.MusicService;
import com.bytenine.tinnitusrelief.service.MusicService.MediaSessionCallback;
import com.bytenine.tinnitusrelief.service.PlayerAdapter;
import com.bytenine.tinnitusrelief.service.contentcatalogs.MusicLibrary;
import com.bytenine.tinnitusrelief.service.players.MediaPlayerAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    private ImageView mAlbumArt;
    private TextView mTitleTextView;
    private TextView mArtistTextView;
    private ImageView mMediaControlsImage;
    private ImageView mItemFavImage;
    private MediaSeekBar mSeekBarAudio;

    private MediaBrowserHelper mMediaBrowserHelper;

    private boolean mIsPlaying;
    private boolean mAllSelected;
    ArrayList<Audio> audioList;
    public List<MediaBrowserCompat.MediaItem> mediaList;
    public List<MediaBrowserCompat.MediaItem> tempMediaList;
    private ImageView mAllImage;
    private ImageView mFavImage;
    BottomNavigationView bottomNavigation;
    MediaControllerCompat mediaController;
    MediaController2.ControllerCallback mControllerCallback;
    public MediaControllerCompat mMediaController;
    int prog;
    public static boolean pauseIsPressed = false;
    public static boolean favIsPressed = false;
    Context context;
    SharedPreferences preferences;
    private String currentSong;
    private RecyclerView_Adapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setSelectedItemId(R.id.page_2);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        mTitleTextView = findViewById(R.id.song_title);
        mArtistTextView = findViewById(R.id.song_artist);
        mAlbumArt = findViewById(R.id.album_art);
        mMediaControlsImage = findViewById(R.id.button_play);
        mAllImage = findViewById(R.id.all_button);
        mFavImage = findViewById(R.id.fav_button);
        mItemFavImage = findViewById(R.id.button_fav);


        final ClickListener clickListener = new ClickListener();
        findViewById(R.id.button_previous).setOnClickListener(clickListener);
        findViewById(R.id.button_play).setOnClickListener(clickListener);
        findViewById(R.id.button_next).setOnClickListener(clickListener);
        findViewById(R.id.all_button).setOnClickListener(clickListener);
        findViewById(R.id.fav_button).setOnClickListener(clickListener);
        findViewById(R.id.button_fav).setOnClickListener(clickListener);

        mAllSelected = true;



        mMediaBrowserHelper = new MediaBrowserConnection(this);
        mMediaBrowserHelper.registerCallback(new MediaBrowserListener());

        context = getApplicationContext();
        preferences = context.getSharedPreferences("TRS", MODE_PRIVATE);

        //loadAudio();
        //initRecyclerView();


    }

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            item -> {


                if (item.getItemId() == R.id.page_1)
                {
                    Log.e ("navbar_bottom", "item 1 clicked");

                    Intent intent = new Intent(this, Home.class);
                    startActivity(intent);


                    return true;
                }
                else if (item.getItemId() == R.id.page_2)
                {
                    Log.e ("navbar_bottom", "item 2 clicked");

                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);


                    return true;


                }
                else if (item.getItemId() == R.id.page_3)
                {
                    Log.e ("navbar_bottom", "item 3 clicked");

                    Intent intent = new Intent(this, Tools.class);
                    startActivity(intent);


                    return true;


                }
                else if (item.getItemId() == R.id.page_4)
                {
                    Log.e ("navbar_bottom", "item 4 clicked");

                    Intent intent = new Intent(this, Settings.class);
                    startActivity(intent);


                    return true;


                }
                else {
                    return false;
                }





            };



    @Override
    public void onStart() {
        super.onStart();
        mMediaBrowserHelper.onStart();
        bottomNavigation.setSelectedItemId(R.id.page_2);
        mSeekBarAudio = findViewById(R.id.seekbar_audio);
        prog = mSeekBarAudio.seekBarProgress;


        Log.e("seekbar","onStart");

    }

    @Override
    public void onStop() {
        super.onStop();
        //mSeekBarAudio.disconnectController();
        mMediaBrowserHelper.onStop();
        Log.e("seekbar","onStop");
    }

    /**
     * Convenience class to collect the click listeners together.
     * <p>
     * In a larger app it's better to split the listeners out or to use your favorite
     * library.
     */
    private class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_previous:
                    mMediaBrowserHelper.getTransportControls().skipToPrevious();
                    MediaPlayerAdapter.mCurrentMediaPlayedToCompletion = false;
                    break;
                case R.id.button_play:
                    if (mIsPlaying) {
                        mMediaBrowserHelper.getTransportControls().pause();
                        MediaPlayerAdapter.mCurrentMediaPlayedToCompletion = false;
                        pauseIsPressed = true;

                    } else {
                        mMediaBrowserHelper.getTransportControls().play();
                        MediaPlayerAdapter.mCurrentMediaPlayedToCompletion = false;
                        pauseIsPressed = false;
                    }
                    break;
                case R.id.button_next:
                    mMediaBrowserHelper.getTransportControls().skipToNext();
                    MediaPlayerAdapter.mCurrentMediaPlayedToCompletion = false;
                    break;

                case R.id.all_button:
                    mAllImage.setPressed(!mAllSelected);
                    mAllImage.setSelected(!mAllSelected);

                    mFavImage.setPressed(mAllSelected);
                    mFavImage.setSelected(mAllSelected);
                    favIsPressed = false;
                    try {
                        listRaw(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;

                case R.id.fav_button:

                    mFavImage.setPressed(mAllSelected);
                    mFavImage.setSelected(mAllSelected);

                    mAllImage.setPressed(mAllSelected);
                    mAllImage.setSelected(mAllSelected);
                    favIsPressed = true;
                    // load songlist, and reinit recyclerview.
                    try {
                        listRaw(false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case R.id.button_fav:

                    boolean currentSongIsFav = preferences.getBoolean(currentSong,false);
                    if (currentSongIsFav)
                    {
                        Log.e("******************","Fav pressed on fav song: " + currentSong);
                        // set song as not fav
                        preferences.edit().putBoolean(currentSong,false).apply();
                        mItemFavImage.setImageResource(R.drawable.fav_unselected);

                    }

                    else {
                        // set song as fav
                        Log.e("******************","Fav pressed on not fav song: " + currentSong);
                        preferences.edit().putBoolean(currentSong,true).apply();
                        mItemFavImage.setImageResource(R.drawable.fav_selected);
                    }


            }
        }
    }



    /**
     * Customize the connection to our {@link android.support.v4.media.MediaBrowserServiceCompat}
     * and implement our app specific desires.
     */
    private class MediaBrowserConnection extends MediaBrowserHelper {
        private MediaBrowserConnection(Context context) {
            super(context, MusicService.class);
        }

        @Override
        protected void onConnected(@NonNull MediaControllerCompat mediaController) {
            mMediaController = mediaController;
            mSeekBarAudio.setMediaController(mMediaController);

            //mMediaController.getTransportControls().seekTo(getProgress());
            //mMediaBrowserHelper.getTransportControls().
           // mMediaPlayer.getCurrentPosition();
         //   mMediaBrowserHelper.getTransportControls().prepare();

                 //  mSeekBarAudio.setProgress(10);



            Log.e("seekbar","onConnected setMediaController prog from seekbar: " + prog);
        }

        @Override
        protected void onChildrenLoaded(@NonNull String parentId,
                                        @NonNull List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);

            final MediaControllerCompat mediaController = getMediaController();

            // Queue up all media items for this simple sample.
            Log.e("onChildrenLoaded","mediaController.addQueueItem: " );
            for (final MediaBrowserCompat.MediaItem mediaItem : children) {
             /*   String title =  mediaItem.getDescription().getTitle() + ".mp3";
                boolean isSongFav = preferences.getBoolean(title,false);
                if (favIsPressed) {
                    if (isSongFav)
                        mediaController.addQueueItem(mediaItem.getDescription());
                }
                else {*/
                    mediaController.addQueueItem(mediaItem.getDescription());
               // }

            }

            // Call prepare now so pressing play just works.
            mediaController.getTransportControls().prepare();
        }
    }

    /**
     * Implementation of the {@link MediaControllerCompat.Callback} methods we're interested in.
     * <p>
     * Here would also be where one could override
     * {@code onQueueChanged(List<MediaSessionCompat.QueueItem> queue)} to get informed when items
     * are added or removed from the queue. We don't do this here in order to keep the UI
     * simple.
     */
    private class MediaBrowserListener extends MediaControllerCompat.Callback {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {
            mIsPlaying = playbackState != null &&
                    playbackState.getState() == PlaybackStateCompat.STATE_PLAYING;
            mMediaControlsImage.setPressed(mIsPlaying);
            mMediaControlsImage.setSelected(mIsPlaying);
           // int prog = mSeekBarAudio.getProgress();
            Log.e("onPlaybackStateChanged","progress: " + prog);
            Log.e("onPlaybackStateChanged","onPlaybackStateChanged");
            Log.e("onPlaybackStateChanged","pause is pressed: " + pauseIsPressed);

            boolean currentSongIsFav = preferences.getBoolean(currentSong,false);
            if (currentSongIsFav)
            {
                Log.e("******************","fav song: " + currentSong);
                // set song as not fav

                mItemFavImage.setImageResource(R.drawable.fav_selected);

            }

            else {
                // set song as fav
                Log.e("******************","not fav song: " + currentSong);

                mItemFavImage.setImageResource(R.drawable.fav_unselected);
            }

            boolean pause = preferences.getBoolean("pausePressed",false);
            boolean pause2 = PlayerAdapter.pauseIsPressed;
            Log.e("seekbar","pause is pressed: " + pause + " " + pause2);
            //TODO check if pause button is pressed, and skip playing next song
            if (null != playbackState) {
                Log.e("seekbar", "Palybackstate: " + playbackState);
                Log.e("seekbar", "MediaPlayerAdapter.mCurrentMediaPlayedToCompletion: " + MediaPlayerAdapter.mCurrentMediaPlayedToCompletion);

                if (MediaPlayerAdapter.mCurrentMediaPlayedToCompletion && !pause2) {
                    mMediaBrowserHelper.getTransportControls().skipToNext();
                    Log.e("seekbar", "Skipping to next song: " + prog + playbackState);
                    pause2 = true;
                }

            }

        }


        @Override
        public void onMetadataChanged(MediaMetadataCompat mediaMetadata) {
            if (mediaMetadata == null) {
                return;
            }
            mTitleTextView.setText(
                    mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            mArtistTextView.setText(
                    mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
            mAlbumArt.setImageBitmap(MusicLibrary.getAlbumBitmap(
                    MainActivity.this,
                    mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)));
            //check if fav
            Log.e("******onMetadataChanged", mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            currentSong = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE)+".mp3";
            if (preferences.getBoolean(mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE)+".mp3",false))
                mItemFavImage.setImageResource(R.drawable.fav_selected);
            else
                mItemFavImage.setImageResource(R.drawable.fav_unselected);

        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            Log.e ("onQueuechanged", "queue: " + queue.size());
            super.onQueueChanged(queue);
        }
    }
    private void initRecyclerView() {
        if (mediaList.size() > 0) {
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
            adapter = new RecyclerView_Adapter(mediaList, getApplication());
            Log.e("******************","adapter is initsialized");
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.addOnItemTouchListener(new CustomTouchListener(this, new onItemClickListener() {
                @Override
                public void onClick(View view, int index) {
                    playAudio(index);
                }
            }));

        }
    }
    private void loadAudio() {
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC; // + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
       // Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);
        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            audioList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

                // Save to audioList
                audioList.add(new Audio(data, title, album, artist));
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }
    public void listRaw(boolean allSongsPressed) throws IOException {
        audioList = new ArrayList<>();
        tempMediaList = new ArrayList<>();
        MusicLibrary.clearMusicList();


       // Field[] fields=R.raw.class.getDeclaredFields();
      //  for (int i = 0; i < fields.length; i++) {
       //     Field field = fields[i];
       //     Log.i("Raw Asset: ", field.getName());
          //  Audio song = new Audio("android.resource://" + "com.bytenine.tinnitusrelief" + "/raw/" +field.getName(),field.getName(),"album "+i,"artist "+i);
         //   audioList.add(i,song);



            AssetManager assetManager = MainActivity.this.getAssets();
            String[] files = assetManager.list("files");
            List<String> it = Arrays.asList(files);

        preferences = context.getSharedPreferences("TRS", 0);

            for (int i = 0; i < it.size(); i++) {
                String field = it.get(i);
            Audio song = new Audio("android.resource://" + "com.bytenine.tinnitusrelief" + "/assets/files/" +field,field,"album "+i,"artist "+i);




             //   Log.e("Files", i + " : " + field);

                String title = field.substring(0, field.length()-4);

                final AssetFileDescriptor afd=getAssets().openFd("files/"+field);


                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
                String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long timeInmillisec = Long.parseLong( time );
                long duration = timeInmillisec / 1000;
                long hours = duration / 3600;
                long minutes = (duration - hours * 3600) / 60;
                long seconds = duration - (hours * 3600 + minutes * 60);

                String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                String album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                String genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);

                boolean isSongFav = preferences.getBoolean(field,false);
                if (favIsPressed) {
                    if (isSongFav) {
                        MusicLibrary.createMediaMetadataCompat(
                                String.valueOf(i),
                                title,
                                artist,
                                album ,
                                genre,
                                duration,
                                TimeUnit.SECONDS,
                                field,
                                R.drawable.album_jazz_blues,
                                ""

                        );

                    } else {


                    }
                }
                else {
                    MusicLibrary.createMediaMetadataCompat(
                            String.valueOf(i),
                            title,
                            artist,
                            album ,
                            genre,
                            duration,
                            TimeUnit.SECONDS,
                            field,
                            R.drawable.album_jazz_blues,
                            ""

                    );
                }

            }

        context = getApplicationContext();

            if (null != context) {
                Log.e("*******","context not null");
            }
      //  MusicLibrary.clearMusicList();
        tempMediaList = MusicLibrary.getMediaItems(context);
        Log.e("*******","Size of MusicLibrary: " + MusicLibrary.getMediaItems(context).size());
        preferences = context.getSharedPreferences("TRS", 0);

        if (null !=  mMediaController.getQueue()) Log.e("*******", "Size of Queue: " + mMediaController.getQueue().size());
        //deletes whole queue
        if (null !=  mMediaController.getQueue()) mMediaController.removeQueueItem(mMediaController.getQueue().get(0).getDescription());
        if (null !=  mMediaController.getQueue()) Log.e("*******", "Size of Queue after: " + mMediaController.getQueue().size());



        for (int i = 0; i < tempMediaList.size(); i++) {
          //  mMediaController.removeQueueItem(tempMediaList.get(i).getDescription());
          //  if (null !=  mMediaController.getQueue()) Log.e("*******", "Size of Queue: " + mMediaController.getQueue().size());


                mMediaController.addQueueItem(tempMediaList.get(i).getDescription());

        }




        mMediaBrowserHelper.getTransportControls().prepare();
            mediaList = MusicLibrary.getMediaItems(context);
        Log.e("*******","Medialist size: " + mediaList.size());





          //  if (null != adapter) {
               // adapter.notifyDataSetChanged();
                initRecyclerView();



           //     Log.e("******************","adapter is not null");
            }
          //  else {
          //      Log.e("******************","adapter is null");
           // }

     //   }

    private void playAudio(int index) {
     /*   if (mIsPlaying) {
            mMediaBrowserHelper.getTransportControls().pause();
        } else {
            mMediaBrowserHelper.getTransportControls().play();
        }*/

        //String filename = audioList.get(index).getTitle()+".mp3";


     //   String stubSongId = "stub";
      //  Bundle extras = new Bundle();
        //extras.putInt("songPosition", 1);
       // gesPlaybackTransportControls.playFromMediaId(stubSongId, extras);


       // mMediaBrowserHelper.getTransportControls().prepare();
        //mMediaBrowserHelper.getTransportControls().playFromMediaId(String.valueOf(R.raw.busy_market_street_traffic_1_sidi_mokhtar_morocco), extras);
        Log.e("Files", index + " : pressed");
        mMediaBrowserHelper.getTransportControls().skipToQueueItem(index);
      //  mMediaBrowserHelper.getTransportControls().skipToNext();

       // com.bytenine.tinnitusrelief.service.players.MediaPlayerAdapter player  = new com.bytenine.tinnitusrelief.service.players.MediaPlayerAdapter(getApplicationContext(), listener );
       // palyer.playFromMedia(com.bytenine.tinnitusrelief.service.contentcatalogs.MusicLibrary.getMetadata(getApplicationContext(),audioList.get(1).getTitle()));
       // mediaController.getTransportControls().prepare();
       // MusicService.MediaPlayerListener
       // MediaController mediaCtrl = getMediaController();
        //mediaCtrl.getTransportControls().playFromMediaId(MusicLibrary.getMediaItems().get(1).getMediaId(),extras);

      //  mMediaBrowserHelper.getTransportControls().prepareFromMediaId(MusicLibrary.getMediaItems().get(1).getMediaId(),extras);
       // mMediaBrowserHelper.getTransportControls().playFromMediaId(MusicLibrary.getMediaItems().get(1).getMediaId(),extras);
      //  mMediaBrowserHelper.getTransportControls().play();
        // palyer.playFromMedia(MusicLibrary.getMetadata(getApplicationContext(),MusicLibrary.getMediaItems().get(1).getMediaId()));
       // player.playFromMedia(MusicLibrary.getMetadata(getApplicationContext(),MusicLibrary.getMediaItems().get(1).getMediaId()));

        // mMediaBrowserHelper.getTransportControls().stop();
        //mMediaBrowserHelper.getTransportControls().playFromMediaId (String.valueOf(index),null);
       // mPlayback
        //Uri mUri = Uri.parse("android.resource://" + context.getPackageName() + "/raw/" + name);
       /* int resID = getResources().getIdentifier(fullFileName, "raw", getPackageName());

        MediaPlayer mediaPlayer = MediaPlayer.create(PurchaseActivity.this, resID);
        mediaPlayer.start();*/
    }
}
