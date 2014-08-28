package com.clover.spika.enterprise.chat.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.models.Message;
import com.clover.spika.enterprise.chat.models.TreeNode;
import com.clover.spika.enterprise.chat.utils.Helper;

import java.util.ArrayList;
import java.util.List;

public class ThreadsAdapter extends BaseAdapter {

    private int mSelectedItem = -1;
    private Context mContext;
    private List<TreeNode> mMessageList = new ArrayList<TreeNode>();

    private ImageLoader imageLoader;

    public ThreadsAdapter(Context context) {
        this.mContext = context;
        this.imageLoader = ImageLoader.getInstance(context);
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

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_thread_message, parent, false);

            holder = new ViewHolder();
            holder.relativeLayoutHolder = (RelativeLayout) convertView.findViewById(R.id.relative_layout_thread_item_holder);

            holder.imageViewUser = (ImageView) convertView.findViewById(R.id.image_view_user);
            holder.textViewUser = (TextView) convertView.findViewById(R.id.text_view_user);
            holder.textViewMessage = (TextView) convertView.findViewById(R.id.text_view_message);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TreeNode node = mMessageList.get(position);

        imageLoader.displayImage(mContext, node.getMessage().getImage(), holder.imageViewUser);
        holder.textViewUser.setText(node.getMessage().getName());
        holder.textViewMessage.setText(node.getMessage().getText());

        if (node.getMessage().isMe()) {
            holder.textViewUser.setTypeface(holder.textViewUser.getTypeface(), Typeface.BOLD);
        } else {
            holder.textViewUser.setTypeface(holder.textViewUser.getTypeface(), Typeface.NORMAL);
        }

        setItemBackground(position, convertView);
        if (position == this.mSelectedItem) {
            holder.relativeLayoutHolder.setBackgroundResource(R.drawable.shape_selected_item);
            holder.textViewUser.setTextColor(Color.WHITE);
            holder.textViewMessage.setTextColor(Color.WHITE);
        } else {
            holder.relativeLayoutHolder.setBackgroundColor(Color.TRANSPARENT);
            holder.textViewUser.setTextColor(mContext.getResources().getColor(R.color.text_gray_image));
            holder.textViewMessage.setTextColor(mContext.getResources().getColor(R.color.black));
        }

        convertView.setPadding(node.getLevel() * 50, 0, 0, 0);

        return convertView;
    }

    private void setItemBackground(int position, View view) {
        if (position % 2 == 1) {
            view.setBackgroundColor(mContext.getResources().getColor(R.color.gray_in_adapter));
        } else {
            view.setBackgroundColor(Color.WHITE);
        }
    }

    private static final class ViewHolder {
        RelativeLayout relativeLayoutHolder;

        ImageView imageViewUser;
        TextView textViewMessage;
        TextView textViewUser;
    }
}
