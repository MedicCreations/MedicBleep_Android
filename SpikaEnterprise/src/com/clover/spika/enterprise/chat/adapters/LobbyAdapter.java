package com.clover.spika.enterprise.chat.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.models.ChatsLobby;

public class LobbyAdapter extends BaseAdapter {

    private Context mContext;
    private List<ChatsLobby> data = new ArrayList<ChatsLobby>();

    private ImageLoader imageLoader;

    public LobbyAdapter(Context context, Collection<ChatsLobby> users, boolean isUsers) {
        this.mContext = context;
        this.data.addAll(users);

        imageLoader = new ImageLoader(context);
        if(isUsers){
        	imageLoader.setDefaultImage(R.drawable.default_user_image);
        }else{
        	imageLoader.setDefaultImage(R.drawable.default_group_image);
        }
    }

    public Context getContext() {
        return mContext;
    }
    
    public void setData(List<ChatsLobby> list){
    	data = list;
    	notifyDataSetChanged();
    }
    
    public void addData(List<ChatsLobby> list){
    	data.addAll(list);
    	notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public ChatsLobby getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolderCharacter holder;
        if (convertView == null) {

            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_lobby, parent, false);

            holder = new ViewHolderCharacter(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolderCharacter) convertView.getTag();
        }

        // set image to null
        holder.lobbyImage.setImageDrawable(null);

        if (position % 2 != 0) {
            holder.itemLayout.setBackgroundColor(getContext().getResources().getColor(R.color.gray_in_adapter));
        } else {
            holder.itemLayout.setBackgroundColor(Color.WHITE);
        }

        imageLoader.displayImage(getContext(), getItem(position).getImage(), holder.lobbyImage);
        holder.lobbyName.setText(getItem(position).getChatName());
        
        if(Integer.parseInt(getItem(position).getUnread()) > 0){
        	holder.unreadLayout.setVisibility(View.VISIBLE);
        	holder.unreadText.setText(getItem(position).getUnread());
        }else{
        	holder.unreadLayout.setVisibility(View.INVISIBLE);
        	holder.unreadText.setText("");
        }

        return convertView;
    }

    public class ViewHolderCharacter {

        public RelativeLayout itemLayout;
        public ImageView lobbyImage;

        public TextView lobbyName;
        
        public TextView unreadText;
        public RelativeLayout unreadLayout;

        public ViewHolderCharacter(View view) {

            itemLayout = (RelativeLayout) view.findViewById(R.id.itemLayout);
            lobbyImage = (ImageView) view.findViewById(R.id.lobbyImage);

            lobbyName = (TextView) view.findViewById(R.id.lobbyName);
            
            unreadText = (TextView) view.findViewById(R.id.unreadText);
            unreadLayout = (RelativeLayout) view.findViewById(R.id.unreadLayout);
        }

    }

}