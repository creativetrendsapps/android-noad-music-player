package com.droidheat.musicplayer;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    SongsManager songsManager;
    private ArrayList<SongModel> data;
    private String listOrigin;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView txtHeader, txtSubHeader, textTrackID, duration;
        ImageView imageOverflow;
        RelativeLayout view;

        ViewHolder(View v) {
            super(v);
            txtHeader = v.findViewById(R.id.text);
            txtSubHeader = v.findViewById(R.id.text1);
            textTrackID = v.findViewById(R.id.trackId);
            duration = v.findViewById(R.id.textTime);
            imageOverflow = v.findViewById(R.id.albumArtImageView);
            view = v.findViewById(R.id.click);
        }
    }

    private Activity activity;

    // Provide a suitable constructor (depends on the kind of dataset)
    RecyclerViewAdapter(ArrayList<SongModel> myDataSet, Activity act, String type) {
        data = myDataSet;
        activity = act;
        listOrigin = type;
        songsManager = new SongsManager(act);
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_no_art, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        SongModel tempValues = data.get(position);

        String duration = tempValues.getDuration();
        String artist = tempValues.getArtist();
        String songName = tempValues.getFileName();
        String title = tempValues.getTitle();

        holder.textTrackID.setText((holder.getAdapterPosition() + 1) + "");

        String finalTitle;
        if (title != null) {
            finalTitle = title;
        } else {
            finalTitle = songName;
        }

        holder.txtHeader.setText(finalTitle);
        if (listOrigin.equals("artists")) {
            holder.txtSubHeader.setText(tempValues.getAlbum().trim());
        } else {
            holder.txtSubHeader.setText(artist);
        }
        holder.duration.setText(duration);

        final PopupMenu pop = new PopupMenu(activity, holder.imageOverflow);
        int[] j;
        switch (listOrigin) {
            case "albums":
                j = new int[6];
                break;
            case "artists":
                j = new int[6];
                break;
            case "mostplayed":
                j = new int[7];
                break;
            default:
                if (isInteger(listOrigin) || listOrigin.equals("favourites")) {
                    // Playlist or Favorites
                    j = new int[8];
                    j[j.length - 1] = R.id.remove_musicUtils;
                } else {
                    // Recently Added
                    j = new int[7];
                }
                break;
        }
        j[0] = R.id.play_next_musicUtils;
        j[1] = R.id.shuffle_play_musicUtils;
        j[2] = R.id.add_to_queue_musicUtils;
        j[3] = R.id.add_to_playlist_musicUtils;
        switch (listOrigin) {
            case "albums":
                j[4] = R.id.goto_artist_musicUtils;
                j[5] = R.id.info_musicUtils;
                break;
            case "artists":
                j[4] = R.id.goto_album_musicUtils;
                j[5] = R.id.info_musicUtils;
                break;
            default:
                j[4] = R.id.goto_album_musicUtils;
                j[5] = R.id.goto_artist_musicUtils;
                j[6] = R.id.info_musicUtils;
                break;
        }
        songsManager.generateMenu(pop, j);
        final SongModel finalTempValues = tempValues;
        pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.info_musicUtils:
                        songsManager.info(data.get(holder.getAdapterPosition())).show();
                        return true;
                    case R.id.remove_musicUtils:
                        ArrayList<SongModel> aaa;
                        if (!listOrigin.equals("favourites")) {
                            aaa = new ArrayList<>(songsManager.playlistSongs(Integer.parseInt(listOrigin)));
                            aaa.remove(holder.getAdapterPosition());
                            songsManager.removePlaylistSong(Integer.parseInt(listOrigin)
                                    , aaa);
                        } else {
                            aaa = new ArrayList<>(songsManager.favouriteSongs());
                            aaa.remove(holder.getAdapterPosition());
                            songsManager.updateFavouritesList(aaa);
                        }
                        data.remove(holder.getAdapterPosition());
                        notifyDataSetChanged();
                        return true;
                    case R.id.play_next_musicUtils:
                        songsManager.playNext(data.get(holder.getAdapterPosition()));
                        return true;
                    case R.id.add_to_queue_musicUtils:
                        songsManager.addToQueue(data.get(holder.getAdapterPosition()));
                        return true;
                    case R.id.add_to_playlist_musicUtils:
                        songsManager.addToPlaylist(data.get(holder.getAdapterPosition()));
                        return true;
                    case R.id.shuffle_play_musicUtils:
                        songsManager.shufflePlay(holder.getAdapterPosition(), data);
                        return true;
                    case R.id.goto_album_musicUtils:
                        Intent intent = new Intent(activity, GlobalDetailActivity.class);
                        intent.putExtra("name", finalTempValues.getAlbum());
                        intent.putExtra("field", "albums");
                        activity.startActivity(intent);
                        return true;
                    case R.id.goto_artist_musicUtils:
                        Intent intent1 = new Intent(activity, GlobalDetailActivity.class);
                        intent1.putExtra("name", finalTempValues.getArtist());
                        intent1.putExtra("field", "artists");
                        activity.startActivity(intent1);
                        return true;
                    default:
                        return false;
                }
            }
        });

        holder.imageOverflow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                pop.show();
            }
        });
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                songsManager.play(holder.getAdapterPosition(), data);
            }
        });
    }

    private boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

}