package com.clothapp.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.clothapp.ImageFragment;
import com.clothapp.R;
import com.clothapp.profile.UserProfileActivity;
import com.clothapp.resources.CircleTransform;
import com.clothapp.resources.Image;
import com.parse.GetCallback;
import com.parse.GetFileCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class TopRatedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    public static List<Image> itemList;

    private final static String username = ParseUser.getCurrentUser().getUsername();

    public TopRatedAdapter(List<Image> itemList) {
        TopRatedAdapter.itemList = itemList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_home_top_rated_item, parent, false);
        return new TopRatedItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        final TopRatedItemViewHolder holder = (TopRatedItemViewHolder) viewHolder;

        Image image = itemList.get(position);

        holder.setUsername(image.getUser());
        holder.setItemName(image.getVestitiToString());
        holder.setHashtags(image.getHashtagToString());
        holder.setLikeCount(image.getNumLike());
        holder.setPhoto(image.getFile());

        List likeUsers = image.getLike();

        if (likeUsers != null && likeUsers.contains(username)) {
            holder.setHeartImage(true);
        } else {
            holder.setHeartImage(false);
        }

        // Doesn't work since a holder may be bound to multiple images...
        /*ParseQuery<ParseObject> query = ParseQuery.getQuery("UserPhoto");
        query.whereEqualTo("username", image.getUser());

        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject photo, ParseException e) {
                if (e == null) {
                    ParseFile thumbnail = photo.getParseFile("thumbnail");
                    thumbnail.getFileInBackground(new GetFileCallback() {
                        @Override
                        public void done(File file, ParseException e) {
                            holder.setProfilePhoto(file);
                        }
                    });
                } else {
                    Log.d("TopRatedAdapter", "Error: " + e.getMessage());
                }
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    class TopRatedItemViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtUsername;
        private final TextView txtItemName;
        private final TextView txtHashtags;
        private final TextView txtLikeCount;

        private final ImageView imgPhoto;
        private ImageView imgProfilePhoto;
        private ImageView imgHeart;
        private ImageView imgShare;
        private ImageView imgProfileIcon;

        public TopRatedItemViewHolder(final View parent) {
            super(parent);

            txtUsername = (TextView) parent.findViewById(R.id.fragment_home_top_rated_item_username);
            txtItemName = (TextView) parent.findViewById(R.id.fragment_home_top_rated_item_item_name);
            txtHashtags = (TextView) parent.findViewById(R.id.fragment_home_top_rated_item_hashtags);
            txtLikeCount = (TextView) parent.findViewById(R.id.fragment_home_top_rated_item_like_count);

            imgPhoto = (ImageView) parent.findViewById(R.id.fragment_home_top_rated_item_image);
            imgHeart = (ImageView) parent.findViewById(R.id.fragment_home_top_rated_item_like);
            imgProfilePhoto = (ImageView) parent.findViewById(R.id.fragment_home_top_rated_item_profile_image);
            imgProfileIcon = (ImageView) parent.findViewById(R.id.fragment_home_top_rated_item_profile);

            setupPhotoOnClickListener();
            setupHeartImageOnClickListener();
            setupProfileIconOnClickListener();
        }

        public void setUsername(String username) {
            txtUsername.setText(username);
        }

        public void setItemName(String itemName) {
            txtItemName.setText(itemName);
        }

        public void setHashtags(String hashtags) {
            txtHashtags.setText(hashtags);
        }

        public void setPhoto(File file) {
            Glide.with(HomeActivity.context)
                    .load(file)
                    .centerCrop()
                    .into(imgPhoto);
        }

        public void setProfilePhoto(File file) {
            Glide.with(HomeActivity.context)
                    .load(file)
                    .centerCrop()
                    .transform(new CircleTransform(HomeActivity.context))
                    .into(imgProfilePhoto);
        }

        public void setHeartImage(boolean red) {
            if (red) imgHeart.setColorFilter(Color.rgb(181, 47, 41));
            else imgHeart.setColorFilter(Color.rgb(205, 205, 205));
        }

        public void setLikeCount(int value) {
            txtLikeCount.setText(value + "");
        }

        private void setupPhotoOnClickListener() {
            imgPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeActivity.context, ImageFragment.class);
                    intent.putExtra("classe", "TopRatedPhotos");
                    intent.putExtra("position", TopRatedItemViewHolder.this.getAdapterPosition());
                    HomeActivity.activity.startActivity(intent);
                }
            });
        }

        private void setupHeartImageOnClickListener() {
            imgHeart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Log.d("TopRatedAdapter", "Clicked on photo with position: " + TopRatedItemViewHolder.this.getAdapterPosition());

                    Image image = TopRatedAdapter.itemList.get(TopRatedItemViewHolder.this.getAdapterPosition());

                    final boolean add = !image.getLike().contains(username);
                    if (add) {
                        // Log.d("TopRatedAdapter", "Adding...");
                        image.addLike(username);
                    } else {
                        // Log.d("TopRatedAdapter", "Removing...");
                        image.remLike(username);
                    }

                    notifyDataSetChanged();

                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Photo");

                    query.getInBackground(image.getObjectId(), new GetCallback<ParseObject>() {
                        public void done(ParseObject photo, ParseException e) {
                            if (e == null) {
                                if (add) {
                                    photo.addUnique("like", username);
                                    photo.put("nLike", photo.getInt("nLike") + 1);
                                    photo.saveInBackground();
                                } else {
                                    photo.removeAll("like", Collections.singletonList(username));
                                    photo.put("nLike", photo.getInt("nLike") - 1);
                                    photo.saveInBackground();
                                }
                            } else {
                                Log.d("TopRatedAdapter", "Error: " + e.getMessage());
                            }
                        }
                    });
                }
            });
        }

        private void setupProfileIconOnClickListener() {
            imgProfileIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Image image = TopRatedAdapter.itemList.get(TopRatedItemViewHolder.this.getAdapterPosition());
                    Intent intent = new Intent(HomeActivity.activity, UserProfileActivity.class);
                    intent.putExtra("user", image.getUser());
                    HomeActivity.activity.startActivity(intent);
                }
            });
        }

    }
}