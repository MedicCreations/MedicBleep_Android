package jp.co.vector.chat.adapters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import jp.co.vector.chat.ChatActivity;
import jp.co.vector.chat.PhotoActivity;
import jp.co.vector.chat.ProfileActivity;
import jp.co.vector.chat.extendables.BaseActivity;
import jp.co.vector.chat.extendables.BaseAsyncTask;
import jp.co.vector.chat.lazy.ImageLoader;
import jp.co.vector.chat.model.Message;
import jp.co.vector.chat.networking.NetworkManagement;
import jp.co.vector.chat.utils.Const;
import jp.co.vector.chat.utils.Helper;
import jp.co.vector.chat.utils.MessageSorting;
import jp.co.vector.chat.view.AppDialog;
import jp.co.vector.chat.view.ViewHolderChatMsg;

import org.json.JSONObject;

import jp.co.vector.chat.R;
import android.content.Context;
import android.content.Intent;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class MessagesAdapter extends BaseAdapter {

    private Context cntx;
    private List<Message> data;
    private List<String> myIds = new ArrayList<String>();
    private SparseIntArray dateSeparator = new SparseIntArray();

    ImageLoader imageLoader;

    int radius = 0;

    private String groupId = "";

    private boolean endOfSearch = false;
    private boolean isScrolling = false;
    private int totalItem = 0;

    public MessagesAdapter(Context context, List<Message> arrayList) {
	this.cntx = context;
	this.data = arrayList;

	imageLoader = new ImageLoader(context);
    }

    @Override
    public int getCount() {
	return data.size();
    }

    @Override
    public Message getItem(int position) {
	return data.get(position);
    }

    @Override
    public long getItemId(int position) {
	return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

	final ViewHolderChatMsg holder;
	if (convertView == null) {

	    convertView = LayoutInflater.from(cntx).inflate(R.layout.chat_item, null);

	    holder = new ViewHolderChatMsg(convertView);
	    convertView.setTag(holder);
	} else {
	    holder = (ViewHolderChatMsg) convertView.getTag();
	}

	// set image to null
	holder.meIcon.setImageDrawable(null);
	holder.youIcon.setImageDrawable(null);
	holder.imagePreviewYou.setImageDrawable(null);
	holder.imagePreviewMe.setImageDrawable(null);
	holder.loading_bar_img.setBackgroundDrawable(null);

	holder.defaultMsgLayoutMe.setVisibility(View.GONE);
	holder.imageMsgLayoutMe.setVisibility(View.GONE);
	holder.imageMsgLayoutYou.setVisibility(View.GONE);
	holder.settingsLayoutMe.setVisibility(View.GONE);
	holder.meItemMsgLayout.setVisibility(View.GONE);
	holder.youItemMsgLayout.setVisibility(View.GONE);

	holder.loading_bar.setVisibility(View.GONE);

	// Assign values
	final Message msg = (Message) getItem(position);

	final boolean me = isMe(msg.getCharacter().getCharacterId());

	if (!isScrolling) {
	    if (me) {
		imageLoader.displayImage(msg.getCharacter().getImage_name(), holder.meIcon, true);
	    } else {
		imageLoader.displayImage(msg.getCharacter().getImage_name(), holder.youIcon, true);
	    }
	}

	if (me) {

	    holder.meItemMsgLayout.setVisibility(View.VISIBLE);

	    if (msg.getType() == 0) {
		holder.defaultMsgLayoutMe.setVisibility(View.VISIBLE);
		holder.mePersonName.setText(msg.getCharacter().getUsername());
		holder.meMsgContent.setText(msg.getText());
	    } else if (msg.getType() == 1) {
		holder.imageMsgLayoutMe.setVisibility(View.VISIBLE);
		holder.mePersonNameImage.setText(msg.getCharacter().getUsername());

		if (!isScrolling) {
		    imageLoader.displayImage(msg.getFile(), holder.imagePreviewMe, false);
		}

		holder.imagePreviewMe.setOnClickListener(new OnClickListener() {

		    @Override
		    public void onClick(View v) {
			Intent intent = new Intent(cntx, PhotoActivity.class);
			intent.putExtra(Const.IMAGE_NAME, msg.getFile());
			cntx.startActivity(intent);
		    }
		});
	    }

	    holder.settingsLayoutMe.setVisibility(View.VISIBLE);

	    holder.timeMe.setText(getTime(msg.getCreated()));

	    if (msg.getIs_rated() != null) {
		holder.likeMe.setTextColor(cntx.getResources().getColor(R.color.text_blue));
		holder.likeMe.setBackgroundResource(R.layout.tab_mask_blue);
	    } else {
		holder.likeMe.setTextColor(cntx.getResources().getColor(R.color.white));
		holder.likeMe.setBackgroundResource(R.layout.tab_mask);
	    }

	    holder.likeNbrMe.setText("(" + msg.getRating() + ")");

	    holder.likeMe.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View v) {
		    if (cntx instanceof ChatActivity) {
			likeMessage(msg.getMessageId());
		    }
		}
	    });

	    holder.meIcon.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View v) {
		    Intent intent = new Intent(cntx, ProfileActivity.class);
		    intent.putExtra(Const.USER_IMAGE_NAME, msg.getCharacter().getImage_name());
		    intent.putExtra(Const.USER_NICKNAME, msg.getCharacter().getUsername());

		    cntx.startActivity(intent);
		}
	    });
	} else {

	    holder.youItemMsgLayout.setVisibility(View.VISIBLE);

	    if (msg.getType() == 0) {
		holder.defaultMsgLayoutYou.setVisibility(View.VISIBLE);
		holder.imageMsgLayoutYou.setVisibility(View.GONE);
		holder.youMsgContent.setText(msg.getText());
		holder.youPersonName.setText(msg.getCharacter().getUsername());
	    } else if (msg.getType() == 1) {
		holder.defaultMsgLayoutYou.setVisibility(View.GONE);
		holder.imageMsgLayoutYou.setVisibility(View.VISIBLE);
		holder.youPersonNameImage.setText(msg.getCharacter().getUsername());

		if (!isScrolling) {
		    imageLoader.displayImage(msg.getFile(), holder.imagePreviewYou, false);
		}

		holder.imagePreviewYou.setOnClickListener(new OnClickListener() {

		    @Override
		    public void onClick(View v) {
			Intent intent = new Intent(cntx, PhotoActivity.class);
			intent.putExtra(Const.IMAGE_NAME, msg.getFile());
			cntx.startActivity(intent);
		    }
		});
	    }

	    holder.timeYou.setText(getTime(msg.getCreated()));

	    holder.reportMsgYou.setVisibility(View.VISIBLE);

	    if (msg.getIs_reported() != null) {
		holder.reportMsgYou.setTextColor(cntx.getResources().getColor(R.color.text_blue));
		holder.reportMsgYou.setBackgroundResource(R.layout.tab_mask_blue);
	    } else {
		holder.reportMsgYou.setTextColor(cntx.getResources().getColor(R.color.white));
		holder.reportMsgYou.setBackgroundResource(R.layout.tab_mask);
	    }

	    holder.reportMsgYou.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View v) {
		    if (cntx instanceof ChatActivity) {
			reportMessage(msg.getMessageId());
		    }
		}
	    });

	    if (msg.getIs_rated() != null) {
		holder.likeYou.setTextColor(cntx.getResources().getColor(R.color.text_blue));
		holder.likeYou.setBackgroundResource(R.layout.tab_mask_blue);
	    } else {
		holder.likeYou.setTextColor(cntx.getResources().getColor(R.color.white));
		holder.likeYou.setBackgroundResource(R.layout.tab_mask);
	    }

	    holder.likeNbrYou.setText("(" + msg.getRating() + ")");

	    holder.likeYou.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View v) {
		    if (cntx instanceof ChatActivity) {
			likeMessage(msg.getMessageId());
		    }
		}
	    });

	    holder.youIcon.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View v) {
		    Intent intent = new Intent(cntx, ProfileActivity.class);
		    intent.putExtra(Const.USER_IMAGE_NAME, msg.getCharacter().getImage_name());
		    intent.putExtra(Const.USER_NICKNAME, msg.getCharacter().getUsername());

		    cntx.startActivity(intent);
		}
	    });
	}

	if (dateSeparator.get(getDayTimeStamp(msg.getCreated())) != position) {
	    holder.dateSeparator.setVisibility(View.GONE);
	} else {
	    holder.dateSeparator.setVisibility(View.VISIBLE);
	    holder.dateSeparator.setBackgroundColor(cntx.getResources().getColor(R.color.transparent));
	    holder.sectionDate.setText(getSectionDate(msg.getCreated()));
	}

	if (position == (0) && !endOfSearch) {
	    holder.loading_bar.setVisibility(View.VISIBLE);

	    Helper.startPaggingAnimation(cntx, holder.loading_bar_img);

	    if (cntx instanceof ChatActivity) {
		((ChatActivity) cntx).getMessages(false, false, true, false, false, false);
	    }
	}

	return convertView;
    }

    private boolean isMe(String msgFromId) {

	try {

	    if (msgFromId.equals(BaseActivity.getPreferences().getCustomString(Const.USER_ID))) {
		return true;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return false;
    }

    private String getTime(String createdString) {

	long created = Long.parseLong(createdString) * 1000;

	Date date = new Date(created);
	SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.US);

	String time = dateFormat.format(date);

	return time;
    }

    private String getSectionDate(String createdString) {
	long created = Long.parseLong(createdString) * 1000;

	Date date = new Date(created);
	SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd. - EEEE", Locale.US);

	String rez = dateFormat.format(date);

	return rez;
    }

    private void addSeparatorDate() {

	dateSeparator.clear();

	for (int i = 0; i < data.size(); i++) {

	    int key = getDayTimeStamp(data.get(i).getCreated());
	    int current = dateSeparator.get(key, -1);

	    if (current == -1) {
		dateSeparator.put(key, i);
	    }
	}
    }

    private Integer getDayTimeStamp(String created) {
	try {
	    String sDate = getSectionDate(created);
	    SimpleDateFormat format = new SimpleDateFormat("MM.dd. - EEEE", Locale.US);
	    Date oDate = format.parse(sDate);
	    Integer iDate = (int) oDate.getTime();

	    return iDate;
	} catch (ParseException e) {
	    e.printStackTrace();
	}

	return 0;
    }

    public void addItems(List<Message> newItems, boolean isNew) {

	if (isNew) {
	    for (int i = 0; i < newItems.size(); i++) {
		boolean isFound = false;
		for (int j = 0; j < data.size(); j++) {
		    if (newItems.get(i).getMessageId().equals(data.get(j).getMessageId())) {
			isFound = true;
			if (Long.parseLong(newItems.get(i).getModified()) > Long.parseLong(data.get(j).getModified())) {
			    data.set(j, newItems.get(i));
			}
		    }
		}

		if (!isFound) {
		    data.add(newItems.get(i));
		}
	    }
	} else {
	    data.addAll(newItems);
	}

	Collections.sort(data, new MessageSorting());
	addSeparatorDate();
	this.notifyDataSetChanged();
    }

    public void removeMessage(String msgId) {
	boolean isFound = false;
	int position = 0;

	for (int i = 0; i < data.size(); i++) {
	    if (data.get(i).getMessageId().equals(msgId)) {
		isFound = true;
		position = i;

		break;
	    }
	}

	if (isFound) {
	    data.remove(position);
	    setTotalItem(--totalItem);
	    notifyDataSetChanged();
	}
    }

    public void addTempMsg(Message msg) {
	data.add(msg);
	addSeparatorDate();
	this.notifyDataSetChanged();
    }

    public void clearItems() {
	data.clear();
	notifyDataSetChanged();
    }

    public List<Message> getData() {
	return this.data;
    }

    public void setEndOfSearch(boolean value) {
	this.endOfSearch = value;
    }

    public int getTotalItem() {
	return totalItem;
    }

    public void setTotalItem(int totalItem) {
	this.totalItem = totalItem;

	if (getCount() >= totalItem) {
	    setEndOfSearch(true);
	} else {
	    setEndOfSearch(false);
	}
    }

    public List<String> getMyIds() {
	return myIds;
    }

    public void setMyIds(List<String> myIds) {
	this.myIds = myIds;
    }

    public String getGroupId() {
	return groupId;
    }

    public void setGroupId(String groupId) {
	this.groupId = groupId;
    }

    public void likeMessage(final String msgId) {
	new BaseAsyncTask<Void, Void, Integer>(cntx, true) {

	    protected Integer doInBackground(Void... params) {

		try {

		    HashMap<String, String> getParams = new HashMap<String, String>();
		    getParams.put(Const.MODULE, String.valueOf(Const.M_CHAT));
		    getParams.put(Const.FUNCTION, Const.F_RATE_MESSAGE);
		    getParams.put(Const.TOKEN, BaseActivity.getPreferences().getToken());

		    JSONObject reqData = new JSONObject();
		    reqData.put(Const.MESSAGE_ID, msgId);

		    JSONObject result = NetworkManagement.httpPostRequest(getParams, reqData);

		    if (result != null) {
			return result.getInt(Const.CODE);
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}

		return Const.E_FAILED;
	    };

	    protected void onPostExecute(Integer result) {
		super.onPostExecute(result);

		if (result.equals(Const.E_SUCCESS)) {
		    AppDialog dialog = new AppDialog(context, false);
		    dialog.setSucceed();
		    if (cntx instanceof ChatActivity) {
			((ChatActivity) cntx).getMessages(false, false, false, true, true, true);
		    }
		} else {
		    AppDialog dialog = new AppDialog(cntx, false);
		    dialog.setFailed(Helper.errorDescriptions(cntx, result));
		}
	    };

	}.execute();
    }

    public void reportMessage(final String msgId) {
	new BaseAsyncTask<Void, Void, Integer>(cntx, true) {

	    protected Integer doInBackground(Void... params) {

		try {

		    HashMap<String, String> getParams = new HashMap<String, String>();
		    getParams.put(Const.MODULE, String.valueOf(Const.M_CHAT));
		    getParams.put(Const.FUNCTION, Const.F_REPORT_MESSAGE);
		    getParams.put(Const.TOKEN, BaseActivity.getPreferences().getToken());

		    JSONObject reqData = new JSONObject();
		    reqData.put(Const.MESSAGE_ID, msgId);

		    JSONObject result = NetworkManagement.httpPostRequest(getParams, reqData);

		    if (result != null) {
			return result.getInt(Const.CODE);
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}

		return Const.E_FAILED;
	    };

	    protected void onPostExecute(Integer result) {
		super.onPostExecute(result);

		if (result.equals(Const.E_SUCCESS)) {
		    AppDialog dialog = new AppDialog(context, false);
		    dialog.setSucceed();
		    if (cntx instanceof ChatActivity) {
			((ChatActivity) cntx).getMessages(false, false, false, true, true, true);
		    }
		} else {
		    AppDialog dialog = new AppDialog(cntx, false);
		    dialog.setFailed(Helper.errorDescriptions(cntx, result));
		}
	    };

	}.execute();
    }

    public boolean isScrolling() {
	return isScrolling;
    }

    public void setScrolling(boolean isScrolling) {
	this.isScrolling = isScrolling;
    }
}