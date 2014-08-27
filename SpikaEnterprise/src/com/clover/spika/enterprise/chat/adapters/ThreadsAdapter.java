package com.clover.spika.enterprise.chat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.models.Message;
import com.clover.spika.enterprise.chat.models.TreeNode;

import java.util.ArrayList;
import java.util.List;

public class ThreadsAdapter extends BaseAdapter {

    private Context mContext;
    private List<TreeNode> mMessageList = new ArrayList<TreeNode>();

    private ImageLoader imageLoader;

    public ThreadsAdapter(Context context, List<TreeNode> messages) {
        this.mContext = context;
        this.mMessageList.addAll(messages);
        this.imageLoader = ImageLoader.getInstance();

        for (TreeNode node : mMessageList) {
            Message.decryptContent(mContext, node.getMessage());
        }
    }

    public Context getContext() {
        return this.mContext;
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

        convertView.setPadding(node.getLevel() * 50, 0, 0, 0);

        return convertView;
    }

    private static final class ViewHolder {
        ImageView imageViewUser;
        TextView textViewMessage;
        TextView textViewUser;
    }
}
