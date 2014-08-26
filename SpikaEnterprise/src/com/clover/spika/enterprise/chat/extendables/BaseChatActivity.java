package com.clover.spika.enterprise.chat.extendables;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.CameraCropActivity;
import com.clover.spika.enterprise.chat.ChatMembersActivity;
import com.clover.spika.enterprise.chat.InvitePeopleActivity;
import com.clover.spika.enterprise.chat.LocationActivity;
import com.clover.spika.enterprise.chat.ProfileOtherActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.RecordAudioActivity;
import com.clover.spika.enterprise.chat.adapters.SettingsAdapter;
import com.clover.spika.enterprise.chat.animation.AnimUtils;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.models.SettingsItem;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.views.RoundImageView;

public abstract class BaseChatActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    protected static final int PICK_FILE_RESULT_CODE = 987;

    protected static final int SETTINGS_POSITION_MEMBERS = 0;
    protected static final int SETTINGS_POSITION_INVITE = 1;
    protected static final int SETTINGS_POSITION_LEAVE = 2;

    protected String chatImage = null;
    protected String chatId = null;
    private int drawerDuration = 300;
    private int drawerHeight = 200;
    protected String chatName = null;
    protected int chatType = 0;

    private ListView settingsListView;
    private ImageButton footerMore;
    private RelativeLayout rlDrawer;
    private RelativeLayout chatLayout;
    private RoundImageView partnerIcon;
    protected EditText etMessage;
    protected ListView chatListView;

    private SettingsAdapter settingsAdapter;

    private Animation animShowSettings;
    private Animation animHideSettings;
    private Animation animHideSettingsHack;

    private ImageLoader imageLoader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        imageLoader = new ImageLoader(this);

        Button file = (Button) findViewById(R.id.bntFile);
        file.setOnClickListener(this);
        Button photo = (Button) findViewById(R.id.btnPhoto);
        photo.setOnClickListener(this);
        Button gallery = (Button) findViewById(R.id.btnGallery);
        gallery.setOnClickListener(this);
        Button video = (Button) findViewById(R.id.btnVideo);
        video.setOnClickListener(this);
        Button location = (Button) findViewById(R.id.btnLocation);
        location.setOnClickListener(this);
        Button record = (Button) findViewById(R.id.btnRecord);
        record.setOnClickListener(this);

        chatListView = (ListView) findViewById(R.id.main_list_view);

        partnerIcon = (RoundImageView) findViewById(R.id.partnerIcon);
        ImageButton goBack = (ImageButton) findViewById(R.id.goBack);
        goBack.setOnClickListener(this);
        ImageButton settingsBtn = (ImageButton) findViewById(R.id.settingsBtn);
        settingsBtn.setOnClickListener(this);

        settingsListView = (ListView) findViewById(R.id.settings_list_view);
        settingsAdapter = new SettingsAdapter(this);
        settingsListView.setAdapter(settingsAdapter);
        settingsListView.setOnItemClickListener(this);

        etMessage = (EditText) findViewById(R.id.etMessage);
        etMessage.setOnClickListener(this);
        setEditTextEditorAction();

        rlDrawer = (RelativeLayout) findViewById(R.id.rlDrawer);
        rlDrawer.setSelected(false);

        footerMore = (ImageButton) findViewById(R.id.footerMore);
        footerMore.setOnClickListener(this);

        chatLayout = (RelativeLayout) findViewById(R.id.chatLayout);

        animShowSettings = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in);
        animShowSettings.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                settingsListView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }
        });

        animHideSettings = AnimationUtils.loadAnimation(this, R.anim.anim_fade_out);
        animHideSettings.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                settingsListView.setVisibility(View.GONE);
            }
        });

        animHideSettingsHack = AnimationUtils.loadAnimation(this, R.anim.anim_fade_out_hack);
        animHideSettingsHack.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                settingsListView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        settingsAnimationHack();
        forceClose();
    }

    protected ImageLoader getImageLoader() {
        return imageLoader;
    }

    /* Animation expand started form 0,0 */
    private void settingsAnimationHack() {
        settingsListView.startAnimation(animHideSettingsHack);
    }

    private void showSettings() {
        settingsListView.startAnimation(animShowSettings);
    }

    private void hideSettings() {
        if (settingsListView.getVisibility() == View.VISIBLE) {
            settingsListView.startAnimation(animHideSettings);
        }
    }

    private void rlDrawerManage() {
        if (!rlDrawer.isSelected()) {
            rlDrawer.setVisibility(View.VISIBLE);
            AnimUtils.translationY(rlDrawer, Helper.dpToPx(this, drawerHeight), 0, drawerDuration, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    rlDrawer.setSelected(true);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) chatListView.getLayoutParams();
                    params.bottomMargin = Helper.dpToPx(BaseChatActivity.this, drawerHeight);
                    chatListView.setLayoutParams(params);

                    footerMore.setImageDrawable(getResources().getDrawable(R.drawable.hide_more_btn_off));
                    hideKeyboard(etMessage);
                }
            });
            AnimUtils.translationY(chatLayout, 0, -Helper.dpToPx(this, drawerHeight), drawerDuration, null);
        } else {
            AnimUtils.translationY(rlDrawer, 0, Helper.dpToPx(this, drawerHeight), drawerDuration, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    rlDrawer.setVisibility(View.GONE);
                    rlDrawer.setSelected(false);

                    footerMore.setImageDrawable(getResources().getDrawable(R.drawable.more_button_selector));
                }
            });
            AnimUtils.translationY(chatLayout, -Helper.dpToPx(this, drawerHeight), 0, drawerDuration, null);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) chatListView.getLayoutParams();
            params.bottomMargin = 0;
            chatListView.setLayoutParams(params);
            AnimUtils.translationY(chatListView, -Helper.dpToPx(this, drawerHeight), 0, drawerDuration, null);
        }
    }

    private void forceClose() {
        if (rlDrawer.isSelected()) {
            rlDrawerManage();
            hideKeyboard(etMessage);
        }
    }

    private void setEditTextEditorAction() {
        etMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) || actionId == EditorInfo.IME_ACTION_DONE) {
                    if (!TextUtils.isEmpty(etMessage.getText().toString())) {
                        onEditorSendEvent(etMessage.getText().toString());
                        return true;
                    }
                }
                return false;
            }
        });
    }

    protected void disableSettingsItems(int chatType) {
        settingsAdapter.disableItem(chatType);
    }

    protected void loadImage() {
        if (!TextUtils.isEmpty(chatImage)) {
            partnerIcon.setVisibility(View.VISIBLE);
            partnerIcon.setOnClickListener(this);
            imageLoader.displayImage(this, chatImage, partnerIcon);
        } else {
            partnerIcon.setVisibility(View.INVISIBLE);
            partnerIcon.setOnClickListener(null);
        }
    }

    @Override
    public void onBackPressed() {
        if (rlDrawer.isSelected()) {
            forceClose();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.bntFile) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, PICK_FILE_RESULT_CODE);
        } else if (id == R.id.btnPhoto) {
            Intent intent = new Intent(this, CameraCropActivity.class);
            intent.putExtra(Const.INTENT_TYPE, Const.PHOTO_INTENT);
            intent.putExtra(Const.FROM_WAll, true);
            intent.putExtra(Const.CHAT_ID, chatId);
            startActivity(intent);
        } else if (id == R.id.btnGallery) {
            Intent intent = new Intent(this, CameraCropActivity.class);
            intent.putExtra(Const.INTENT_TYPE, Const.GALLERY_INTENT);
            intent.putExtra(Const.FROM_WAll, true);
            intent.putExtra(Const.CHAT_ID, chatId);
            startActivity(intent);
        } else if (id == R.id.btnVideo) {
            AppDialog dialog = new AppDialog(this, false);
            dialog.choseCamGallery(chatId);
            hideSettings();
        } else if (id == R.id.btnLocation) {
            Intent intent = new Intent(this, LocationActivity.class);
            intent.putExtra(Const.CHAT_ID, chatId);
            startActivity(intent);
        } else if (id == R.id.btnRecord) {
            Intent intent = new Intent(this, RecordAudioActivity.class);
            intent.putExtra(Const.CHAT_ID, chatId);
            startActivity(intent);

        } else if (id == R.id.etMessage) {
            showKeyboard(etMessage);
            forceClose();
            hideSettings();
        } else if (id == R.id.footerMore) {

            rlDrawerManage();
            hideSettings();
        } else if (id == R.id.partnerIcon) {
            ProfileOtherActivity.openOtherProfile(this, chatImage, chatName);
        } else if (id == R.id.goBack) {
            finish();
        } else if (id == R.id.settingsBtn) {
            if (settingsListView.getVisibility() == View.GONE) {
                showSettings();
            } else {
                hideSettings();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getAdapter() != null) {

            SettingsItem item = (SettingsItem) parent.getAdapter().getItem(position);
            if (!item.isDisabled()) {

                if (position == SETTINGS_POSITION_MEMBERS) {
                    if (chatType == Const.C_PRIVATE) {
                        ProfileOtherActivity.openOtherProfile(this, chatImage, chatName);
                    } else if (chatType == Const.C_GROUP || chatType == Const.C_TEAM) {
                        ChatMembersActivity.startActivity(chatId, this);
                    }
                } else if (position == SETTINGS_POSITION_INVITE) {
                    if (chatType == Const.C_GROUP || chatType == Const.C_PRIVATE) {
                        InvitePeopleActivity.startActivity(chatId, chatType, this);
                    }
                } else if (position == SETTINGS_POSITION_LEAVE) {
                    if (chatType == Const.C_GROUP) {
                        leaveChat();
                    }
                }
            }
        }
    }

    protected abstract void leaveChat();

    protected abstract void onEditorSendEvent(String text);
}
