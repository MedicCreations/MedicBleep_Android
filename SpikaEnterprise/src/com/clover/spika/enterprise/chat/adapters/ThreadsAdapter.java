package com.clover.spika.enterprise.chat.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.LocationActivity;
import com.clover.spika.enterprise.chat.PhotoActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.models.Message;
import com.clover.spika.enterprise.chat.models.TreeNode;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;

import java.util.ArrayList;
import java.util.List;

public class ThreadsAdapter extends BaseAdapter {

    private static final int VIEW_TYPE_MESSAGE = 0;
    private static final int VIEW_TYPE_PHOTO = 1;
    private static final int VIEW_TYPE_LOCATION = 2;
    private static final int VIEW_TYPE_DELETED = 3;

    private static final int[] VIEW_TYPES = {
            VIEW_TYPE_MESSAGE,
            VIEW_TYPE_PHOTO,
            VIEW_TYPE_LOCATION,
            VIEW_TYPE_DELETED
    };

    private static final int INDENTATION_PADDING = 50;

    private int mSelectedItem = -1;
    private int mMaxIndentLevel = 0;
    private Context mContext;
    private List<TreeNode> mMessageList = new ArrayList<TreeNode>();

    private ImageLoader imageLoader;

    public ThreadsAdapter(Context context) {
        if (context instanceof Activity) {
            this.mContext = context;
            this.imageLoader = ImageLoader.getInstance();

            DisplayMetrics dm = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
            int maxIndent = dm.widthPixels / 2;
            mMaxIndentLevel = maxIndent / INDENTATION_PADDING;
        } else {
            throw new IllegalArgumentException("Context must be an Activity context to proceed.");
        }
    }

    public void updateContent(List<TreeNode> collection) {
        this.mMessageList.clear();
        this.mMessageList.addAll(collection);

        String thisUserId = Helper.getUserId(mContext);
        for (TreeNode node : mMessageList) {
            Message.decryptContent(mContext, node.getMessage());
            node.getMessage().setMe(node.getMessage().getUser_id().equals(thisUserId));
        }

        notifyDataSetChanged();
    }

    public Context getContext() {
        return this.mContext;
    }

    public void setSelectedItem(int position) {
        this.mSelectedItem = position;
        notifyDataSetInvalidated();
    }

    private int getIndentPadding(int level) {
        level = level > mMaxIndentLevel ? mMaxIndentLevel : level;
        return INDENTATION_PADDING  * level;
    }

    @Override
    public int getItemViewType(int position) {
        switch (getItem(position).getMessage().getType()) {
            case Const.MSG_TYPE_DELETED:
                return VIEW_TYPE_DELETED;

            case Const.MSG_TYPE_PHOTO:
                return VIEW_TYPE_PHOTO;

            case Const.MSG_TYPE_LOCATION:
                return VIEW_TYPE_LOCATION;

            case Const.MSG_TYPE_DEFAULT:
            default:
                return VIEW_TYPE_MESSAGE;
        }
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPES.length;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) != VIEW_TYPE_DELETED;
    }

    @Override
    public int getCount() {
        return mMessageList.size();
    }

    @Override
    public TreeNode getItem(int position) {
        return mMessageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mMessageList.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        int type = getItemViewType(position);
        if (convertView == null) {
            holder = new ViewHolder();

            switch (type) {
                case VIEW_TYPE_DELETED:
                    convertView = inflateDeleted(holder, parent);
                    break;

                case VIEW_TYPE_PHOTO:
                    convertView = inflatePhoto(holder, parent);
                    break;

                case VIEW_TYPE_LOCATION:
                    convertView = inflateMedia(holder, parent);
                    break;

                case VIEW_TYPE_MESSAGE:
                default:
                    convertView = inflateMessage(holder, parent);
                    break;
            }

            holder.relativeLayoutHolder = (RelativeLayout) convertView.findViewById(R.id.relative_layout_thread_item_holder);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        setItemBackground(position, convertView);

        TreeNode node = mMessageList.get(position);

        switch (type) {
            case VIEW_TYPE_DELETED:
                break;

            case VIEW_TYPE_PHOTO:
                populatePhoto(holder, node, position);
                break;

            case VIEW_TYPE_LOCATION:
                populateLocation(holder, node, position);
                break;

            case VIEW_TYPE_MESSAGE:
            default:
                populateMessage(holder, node, position);
                break;
        }

        convertView.setPadding(getIndentPadding(node.getLevel()), 0, 0, 0);

        return convertView;
    }

    private void setItemBackground(int position, View view) {
        if (position % 2 == 1) {
            view.setBackgroundColor(mContext.getResources().getColor(R.color.gray_in_adapter));
        } else {
            view.setBackgroundColor(Color.WHITE);
        }
    }

    private View inflateMessage(final ViewHolder holder, final ViewGroup parent) {
        View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_thread_message, parent, false);

        holder.imageViewUser = (ImageView) convertView.findViewById(R.id.image_view_user);
        holder.textViewUser = (TextView) convertView.findViewById(R.id.text_view_user);
        holder.textViewMessage = (TextView) convertView.findViewById(R.id.text_view_message);

        return convertView;
    }

    private View inflateDeleted(final ViewHolder holder, final ViewGroup parent) {
        View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_thread_deleted, parent, false);

        holder.textViewMessage = (TextView) convertView.findViewById(R.id.text_view_message);

        return convertView;
    }

    private View inflatePhoto(final ViewHolder holder, final ViewGroup parent) {
        View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_thread_photo, parent, false);

        holder.imageViewUser = (ImageView) convertView.findViewById(R.id.image_view_user);
        holder.textViewUser = (TextView) convertView.findViewById(R.id.text_view_user);
        holder.imageViewPhoto = (ImageView) convertView.findViewById(R.id.image_view_photo);
        holder.imageViewPhoto.setOnClickListener(mOnClickPhoto);

        return convertView;
    }

    private View inflateMedia(final ViewHolder holder, final ViewGroup parent) {
        View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_thread_media, parent, false);

        holder.imageViewUser = (ImageView) convertView.findViewById(R.id.image_view_user);
        holder.textViewUser = (TextView) convertView.findViewById(R.id.text_view_user);
        holder.textViewMessage = (TextView) convertView.findViewById(R.id.text_view_message);
        holder.imageViewIcon = (ImageView) convertView.findViewById(R.id.image_view_icon);
        holder.imageViewIcon.setOnClickListener(mOnClickLocation);

        return convertView;
    }

    private void populateMessage(ViewHolder holder, TreeNode node, int position) {
        imageLoader.displayImage(mContext, node.getMessage().getImage(), holder.imageViewUser);
        holder.textViewUser.setText(node.getMessage().getName());
        holder.textViewMessage.setText(node.getMessage().getText());

        if (node.getMessage().isMe()) {
            holder.textViewUser.setTypeface(null, Typeface.BOLD);
        } else {
            holder.textViewUser.setTypeface(null, Typeface.NORMAL);
        }

        if (position == this.mSelectedItem) {
            holder.relativeLayoutHolder.setBackgroundResource(R.drawable.shape_selected_item);
            holder.textViewUser.setTextColor(Color.WHITE);
            holder.textViewMessage.setTextColor(Color.WHITE);
        } else {
            holder.relativeLayoutHolder.setBackgroundColor(Color.TRANSPARENT);
            holder.textViewUser.setTextColor(mContext.getResources().getColor(R.color.text_gray_image));
            holder.textViewMessage.setTextColor(mContext.getResources().getColor(R.color.black));
        }
    }

    private void populatePhoto(ViewHolder holder, TreeNode node, int position) {
        imageLoader.displayImage(mContext, node.getMessage().getImage(), holder.imageViewUser);
        holder.textViewUser.setText(node.getMessage().getName());

        if (node.getMessage().isMe()) {
            holder.textViewUser.setTypeface(null, Typeface.BOLD);
        } else {
            holder.textViewUser.setTypeface(null, Typeface.NORMAL);
        }

        imageLoader.displayImage(mContext, node.getMessage().getThumb_id(), holder.imageViewPhoto);
        holder.imageViewPhoto.setTag(R.id.tag_file_id, node.getMessage().getFile_id());

        if (position == this.mSelectedItem) {
            holder.relativeLayoutHolder.setBackgroundResource(R.drawable.shape_selected_item);
            holder.textViewUser.setTextColor(Color.WHITE);
        } else {
            holder.relativeLayoutHolder.setBackgroundColor(Color.TRANSPARENT);
            holder.textViewUser.setTextColor(mContext.getResources().getColor(R.color.text_gray_image));
        }
    }

    private void populateLocation(ViewHolder holder, TreeNode node, int position) {
        imageLoader.displayImage(mContext, node.getMessage().getImage(), holder.imageViewUser);
        holder.textViewUser.setText(node.getMessage().getName());
        holder.textViewMessage.setText(
                "\"" + node.getMessage().getLatitude() + ", " + node.getMessage().getLongitude() +  "\"");

        if (node.getMessage().isMe()) {
            holder.textViewUser.setTypeface(null, Typeface.BOLD);
        } else {
            holder.textViewUser.setTypeface(null, Typeface.NORMAL);
        }

        holder.imageViewIcon.setImageResource(R.drawable.icon_location);
        holder.imageViewIcon.setTag(R.id.tag_latitude, node.getMessage().getLatitude());
        holder.imageViewIcon.setTag(R.id.tag_longitude, node.getMessage().getLongitude());

        if (position == this.mSelectedItem) {
            holder.relativeLayoutHolder.setBackgroundResource(R.drawable.shape_selected_item);
            holder.textViewUser.setTextColor(Color.WHITE);
            holder.textViewMessage.setTextColor(Color.WHITE);
        } else {
            holder.relativeLayoutHolder.setBackgroundColor(Color.TRANSPARENT);
            holder.textViewUser.setTextColor(mContext.getResources().getColor(R.color.text_gray_image));
            holder.textViewMessage.setTextColor(mContext.getResources().getColor(R.color.black));
        }
    }

    private View.OnClickListener mOnClickPhoto = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag(R.id.tag_file_id) != null) {
                String fileId = (String) v.getTag(R.id.tag_file_id);
                Intent photoIntent = new Intent(mContext, PhotoActivity.class);
                photoIntent.putExtra(Const.IMAGE, fileId);
                mContext.startActivity(photoIntent);
            }
        }
    };

    private View.OnClickListener mOnClickLocation = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag(R.id.tag_latitude) != null && v.getTag(R.id.tag_longitude) != null) {
                Intent locationIntent = new Intent(mContext, LocationActivity.class);
                locationIntent.putExtra(Const.LATITUDE, Double.parseDouble((String) v.getTag(R.id.tag_latitude)));
                locationIntent.putExtra(Const.LONGITUDE, Double.parseDouble((String) v.getTag(R.id.tag_longitude)));
                mContext.startActivity(locationIntent);
            }
        }
    };

    private static final class ViewHolder {
        RelativeLayout relativeLayoutHolder;

        ImageView imageViewUser;
        ImageView imageViewPhoto;
        ImageView imageViewIcon;
        TextView textViewMessage;
        TextView textViewUser;
    }
}
