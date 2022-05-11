package com.bytenine.tinnitusrelief.ui;

import android.content.Context;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bytenine.tinnitusrelief.R;
import com.bytenine.tinnitusrelief.service.contentcatalogs.MusicLibrary;

import java.util.Collections;
import java.util.List;

/**
 * Created by Valdio Veliu on 16-07-08.
 */
public class RecyclerView_Adapter extends RecyclerView.Adapter<ViewHolder> {

    List<MediaBrowserCompat.MediaItem> list = Collections.emptyList();
    Context context;

    public RecyclerView_Adapter(List<MediaBrowserCompat.MediaItem> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        ViewHolder holder = new ViewHolder(v);
        return holder;

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
        holder.title.setText(list.get(position).getDescription().getTitle());

        Long duration = MusicLibrary.getMetadata(context.getApplicationContext(), list.get(position).getMediaId()).getBundle().getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        duration = duration / 1000;
        long hours = duration / 3600;
        long minutes = (duration - hours * 3600) / 60;
        long seconds = duration - (hours * 3600 + minutes * 60);
        String min = "";
        String sec = "";

        if (minutes < 10) min = "0"+String.valueOf(minutes);
        else min = String.valueOf(minutes);
        if (seconds < 10) sec = "0"+String.valueOf(seconds);
        else sec = String.valueOf(seconds);


        String durationInMinutes = min+":"+sec;
        holder.duration.setText(String.valueOf(durationInMinutes));
    }

    @Override
    public int getItemCount() {
        //returns the number of elements the RecyclerView will display
        return list.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

}

class ViewHolder extends RecyclerView.ViewHolder {

    TextView title;
    TextView duration;
    ImageView play_pause;
   // ImageView fav;

    ViewHolder(View itemView) {
        super(itemView);
        title = (TextView) itemView.findViewById(R.id.title);
        duration = (TextView) itemView.findViewById(R.id.duration);
        play_pause = (ImageView) itemView.findViewById(R.id.play_pause);
        // to add fav in itemview list
       // fav = (ImageView) itemView.findViewById(R.id.button_fav);
    }
}