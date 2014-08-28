package com.clover.spika.enterprise.chat.extendables;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.ChatApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.SettingsItem;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.clover.spika.enterprise.chat.views.RobotoThinTextView;
import com.clover.spika.enterprise.chat.views.RoundImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

public abstract class BaseChatActivity extends BaseActivity {

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
    private RobotoThinTextView screenTitle;
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

        imageLoader = ImageLoader.getInstance();

        Button file = (Button) findViewById(R.id.bntFile);
        file.setOnClickListener(thisClickListener);
        Button photo = (Button) findViewById(R.id.btnPhoto);
        photo.setOnClickListener(thisClickListener);
        Button gallery = (Button) findViewById(R.id.btnGallery);
        gallery.setOnClickListener(thisClickListener);
        Button video = (Button) findViewById(R.id.btnVideo);
        video.setOnClickListener(thisClickListener);
        Button location = (Button) findViewById(R.id.btnLocation);
        location.setOnClickListener(thisClickListener);
        Button record = (Button) findViewById(R.id.btnRecord);
        record.setOnClickListener(thisClickListener);

        chatListView = (ListView) findViewById(R.id.main_list_view);

        partnerIcon = (RoundImageView) findViewById(R.id.partnerIcon);
        screenTitle = (RobotoThinTextView) findViewById(R.id.screenTitle);
        ImageButton goBack = (ImageButton) findViewById(R.id.goBack);
        goBack.setOnClickListener(thisClickListener);
        ImageButton settingsBtn = (ImageButton) findViewById(R.id.settingsBtn);
        settingsBtn.setOnClickListener(thisClickListener);

        settingsListView = (ListView) findViewById(R.id.settings_list_view);
        settingsAdapter = new SettingsAdapter(this);
        settingsListView.setAdapter(settingsAdapter);
        settingsListView.setOnItemClickListener(thisItemClickListener);

        etMessage = (EditText) findViewById(R.id.etMessage);
        etMessage.setOnClickListener(thisClickListener);
        setEditTextEditorAction();

        rlDrawer = (RelativeLayout) findViewById(R.id.rlDrawer);
        rlDrawer.setSelected(false);

        footerMore = (ImageButton) findViewById(R.id.footerMore);
        footerMore.setOnClickListener(thisClickListener);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                Uri fileUri = data.getData();

                String fileName = null;
                String filePath = null;

                if (fileUri.getScheme().equals("content")) {


                    String[] proj = {MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.DISPLAY_NAME};
                    Cursor cursor = getContentResolver().query(fileUri, proj, null, null, null);

                    int column_index_name = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
                    int column_index_path = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
                    cursor.moveToFirst();

                    fileName = cursor.getString(column_index_name);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

                        try {
                            InputStream in = getContentResolver().openInputStream(fileUri);
                            File tempFile = Utils.getTempFile(this, fileName);
                            OutputStream out = new FileOutputStream(tempFile);

                            // Transfer bytes from in to out
                            byte[] buf = new byte[1024];
                            int len;
                            while ((len = in.read(buf)) > 0) {
                                out.write(buf, 0, len);
                            }
                            in.close();
                            out.close();

                            filePath = tempFile.getAbsolutePath();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        filePath = cursor.getString(column_index_path);
                    }

                } else if (fileUri.getScheme().equals("file")) {

                    File file = new File(URI.create(fileUri.toString()));
                    fileName = file.getName();
                    filePath = file.getAbsolutePath();
                }

                filePath = Utils.handleFileEncryption(filePath, BaseChatActivity.this);

                onFileSelected(RESULT_OK, fileName, filePath);
            } else {
                onFileSelected(RESULT_CANCELED, null, null);
            }
        }
    }

    protected void setTitle(String title) {
        screenTitle.setText(title);
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
            partnerIcon.setOnClickListener(thisClickListener);
            imageLoader.displayImage(this, chatImage, partnerIcon);
        } else {
            partnerIcon.setVisibility(View.INVISIBLE);
            partnerIcon.setOnClickListener(null);
        }
    }

    private void getFromPush(String msg, String chatIdPush, String chatName, String chatImage, String pushType) {
        if (chatIdPush.equals(chatId)) {
            onChatPushUpdated();
        } else {
            showPopUp(msg, chatIdPush, chatName, chatImage);
        }
    }

    protected void deleteMessage(String messageId) {
        new ChatApi().deleteMessage(messageId, this, new ApiCallback<BaseModel>() {
            @Override
            public void onApiResponse(Result<BaseModel> result) {
                if (result.isSuccess()) {
                    onMessageDeleted();
                }
            }
        });
    }

    @Override
    public void pushCall(String msg, String chatIdPush, String chatName, String chatImage, String pushType) {
        getFromPush(msg, chatIdPush, chatName, chatImage, pushType);
    }

    @Override
    public void onBackPressed() {
        if (rlDrawer.isSelected()) {
            forceClose();
        } else {
            super.onBackPressed();
        }
    }

    View.OnClickListener thisClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();

            if (id == R.id.bntFile) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, PICK_FILE_RESULT_CODE);

            } else if (id == R.id.btnPhoto) {
                Intent intent = new Intent(BaseChatActivity.this, CameraCropActivity.class);
                intent.putExtra(Const.INTENT_TYPE, Const.PHOTO_INTENT);
                intent.putExtra(Const.FROM_WAll, true);
                intent.putExtra(Const.CHAT_ID, chatId);
                intent.putExtra(CameraCropActivity.EXTRA_ROOT_ID, getRootId());
                intent.putExtra(CameraCropActivity.EXTRA_MESSAGE_ID, getMessageId());
                startActivity(intent);

            } else if (id == R.id.btnGallery) {
                Intent intent = new Intent(BaseChatActivity.this, CameraCropActivity.class);
                intent.putExtra(Const.INTENT_TYPE, Const.GALLERY_INTENT);
                intent.putExtra(Const.FROM_WAll, true);
                intent.putExtra(Const.CHAT_ID, chatId);
                intent.putExtra(CameraCropActivity.EXTRA_ROOT_ID, getRootId());
                intent.putExtra(CameraCropActivity.EXTRA_MESSAGE_ID, getMessageId());
                startActivity(intent);

            } else if (id == R.id.btnVideo) {
                AppDialog dialog = new AppDialog(BaseChatActivity.this, false);
                dialog.choseCamGallery(chatId);
                hideSettings();

            } else if (id == R.id.btnLocation) {
                Intent intent = new Intent(BaseChatActivity.this, LocationActivity.class);
                intent.putExtra(Const.CHAT_ID, chatId);
                startActivity(intent);

            } else if (id == R.id.btnRecord) {
                Intent intent = new Intent(BaseChatActivity.this, RecordAudioActivity.class);
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
                ProfileOtherActivity.openOtherProfile(BaseChatActivity.this, chatImage, chatName);

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
    };

    AdapterView.OnItemClickListener thisItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (parent.getAdapter() != null) {

                SettingsItem item = (SettingsItem) parent.getAdapter().getItem(position);
                if (!item.isDisabled()) {

                    if (position == SETTINGS_POSITION_MEMBERS) {
                        if (chatType == Const.C_PRIVATE) {
                            ProfileOtherActivity.openOtherProfile(BaseChatActivity.this, chatImage, chatName);
                        } else if (chatType == Const.C_GROUP || chatType == Const.C_TEAM) {
                            ChatMembersActivity.startActivity(chatId, BaseChatActivity.this);
                        }
                    } else if (position == SETTINGS_POSITION_INVITE) {
                        if (chatType == Const.C_GROUP || chatType == Const.C_PRIVATE) {
                            InvitePeopleActivity.startActivity(chatId, chatType, BaseChatActivity.this);
                        }
                    } else if (position == SETTINGS_POSITION_LEAVE) {
                        if (chatType == Const.C_GROUP) {
                            leaveChat();
                        }
                    }
                }
            }
        }
    };

    /**
     *
     */
    protected abstract void leaveChat();

    /**
     * Called when "enter" key has been pressed on keyboard
     * @param text text provided from EditText
     */
    protected abstract void onEditorSendEvent(String text);

    /**
     * Called when push (related to this chat) has been received
     */
    protected abstract void onChatPushUpdated();

    protected abstract void onMessageDeleted();

    /**
     * Called as a callback method after user has selected a file.
     * @param result same as result available in {@link android.app.Activity},
     *               {@link android.app.Activity#RESULT_OK} or {@link android.app.Activity#RESULT_CANCELED}
     * @param fileName name of the file selected by user, may be null oif result isn't successful
     * @param filePath a path to the file which user has selected, may be null if result isn't successful
     */
    protected abstract void onFileSelected(int result, String fileName, String filePath);

    protected abstract String getRootId();

    protected abstract String getMessageId();
}
