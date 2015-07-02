package com.medicbleep.app.chat;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.medicbleep.app.chat.adapters.InviteRemoveAdapter;
import com.medicbleep.app.chat.api.robospice.UserSpice;
import com.medicbleep.app.chat.caching.GlobalCaching.OnGlobalSearchDBChanged;
import com.medicbleep.app.chat.caching.GlobalCaching.OnGlobalSearchNetworkResult;
import com.medicbleep.app.chat.caching.robospice.GlobalCacheSpice;
import com.medicbleep.app.chat.extendables.BaseActivity;
import com.medicbleep.app.chat.listeners.OnChangeListener;
import com.medicbleep.app.chat.listeners.OnSearchListener;
import com.medicbleep.app.chat.models.Chat;
import com.medicbleep.app.chat.models.GlobalModel;
import com.medicbleep.app.chat.models.GlobalModel.Type;
import com.medicbleep.app.chat.models.User;
import com.medicbleep.app.chat.services.robospice.CustomSpiceListener;
import com.medicbleep.app.chat.utils.Const;
import com.medicbleep.app.chat.utils.Helper;
import com.medicbleep.app.chat.utils.Utils;
import com.medicbleep.app.chat.views.pulltorefresh.PullToRefreshBase;
import com.medicbleep.app.chat.views.pulltorefresh.PullToRefreshListView;
import com.octo.android.robospice.persistence.exception.SpiceException;

public class InvitePeopleActivity extends BaseActivity implements OnItemClickListener, OnSearchListener, OnChangeListener<GlobalModel>, OnGlobalSearchDBChanged,
		OnGlobalSearchNetworkResult {

	PullToRefreshListView mainList;
	InviteRemoveAdapter adapter;

	private String chatId = "";
	@SuppressWarnings("unused")
	private int chatType = 0;
	private int mCurrentIndex = 0;
	private String mSearchData = null;
	private int mTotalCount = 0;
    private String activeUserId = "";

	ImageButton goBack;
	TextView screenTitle;

	ImageButton inviteBtn;

	TextView invitedPeople;

	List<User> usersToAdd = new ArrayList<User>();

    private EditText etSearch;
    private boolean isDataFromNet = false;
    private List<GlobalModel> allData = new ArrayList<GlobalModel>();

	public static void startActivity(String chatId, int type, boolean isAdmin, String activeUserId, Context context) {
		Intent intent = new Intent(context, InvitePeopleActivity.class);
		intent.putExtra(Const.CHAT_ID, chatId);
		intent.putExtra(Const.TYPE, type);
		intent.putExtra(Const.IS_ADMIN, isAdmin);
        intent.putExtra(Const.USER_ID, activeUserId);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_invite_people);
		// setSearch(this);

		goBack = (ImageButton) findViewById(R.id.goBack);
		goBack.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		screenTitle = (TextView) findViewById(R.id.screenTitle);

		inviteBtn = (ImageButton) findViewById(R.id.inviteBtn);
		inviteBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				invitePeople();
			}
		});

		invitedPeople = (TextView) findViewById(R.id.invitedPeople);
		invitedPeople.setMovementMethod(new ScrollingMovementMethod());

		adapter = new InviteRemoveAdapter(spiceManager, this, new ArrayList<GlobalModel>(), this, null);

		mainList = (PullToRefreshListView) findViewById(R.id.main_list_view);
		mainList.setAdapter(adapter);
		mainList.setOnRefreshListener(refreshListener2);
		mainList.setOnItemClickListener(this);

		handleIntent(getIntent());

		setInitialTextToTxtUsers();

        etSearch = (EditText) findViewById(R.id.etSearchUsers);
        etSearch.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
                    onSearch(etSearch.getText().toString());
                }
                return false;
            }
        });

        etSearch.addTextChangedListener(textWatacher);
	}

    private TextWatcher textWatacher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @SuppressWarnings("rawtypes")
        @Override
        public void afterTextChanged(Editable s) {
            if(isDataFromNet){
                GlobalCacheSpice.GlobalSearch globalSearch = new GlobalCacheSpice.GlobalSearch(InvitePeopleActivity.this, spiceManager, 0, null, null, Type.USER,
                        null, true, true, activeUserId, InvitePeopleActivity.this, InvitePeopleActivity.this);
                spiceManager.execute(globalSearch, new CustomSpiceListener<List>() {

                    @SuppressWarnings("unchecked")
                    @Override
                    public void onRequestSuccess(List result) {
                        super.onRequestSuccess(result);
                        allData.clear();
                        allData.addAll(result);
                    }
                });
                isDataFromNet = false;
            }else {
                adapter.manageData(s.toString(), allData);
            }
        }
    };

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (intent != null && intent.getExtras() != null) {
			chatId = intent.getExtras().getString(Const.CHAT_ID);
			chatType = intent.getExtras().getInt(Const.TYPE);
            activeUserId = getIntent().getStringExtra(Const.USER_ID);
			getUsers(0, mSearchData, false);
		}
	}

	@SuppressWarnings("rawtypes")
	PullToRefreshBase.OnRefreshListener2 refreshListener2 = new PullToRefreshBase.OnRefreshListener2() {
		@Override
		public void onPullDownToRefresh(PullToRefreshBase refreshView) {
			// mCurrentIndex--; don't need this for now
		}

		@Override
		public void onPullUpToRefresh(PullToRefreshBase refreshView) {
			mCurrentIndex++;
			getUsers(mCurrentIndex, mSearchData, false);
		}
	};

	private void setData(List<GlobalModel> data, boolean toClearPrevious) {
		// -2 is because of header and footer view
		int currentCount = mainList.getRefreshableView().getAdapter().getCount() - 2 + data.size();

		if (toClearPrevious) {
			currentCount = data.size();
		}

		adapter.setData(data);

		if (toClearPrevious) {
			mainList.getRefreshableView().setSelection(0);
		}

		mainList.onRefreshComplete();

		if (currentCount >= mTotalCount) {
			mainList.setMode(PullToRefreshBase.Mode.DISABLED);
		} else if (currentCount < mTotalCount) {
			mainList.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
		}

        allData.clear();
        allData.addAll(adapter.getData());
	}

	private void getUsers(int page, String search, final boolean toClear) {

        if(!TextUtils.isEmpty(search)){
            isDataFromNet = true;
        }

		GlobalCacheSpice.GlobalSearch globalSearch = new GlobalCacheSpice.GlobalSearch(this, spiceManager, page, null, chatId, Type.USER, search, toClear, activeUserId, this, this);
		spiceManager.execute(globalSearch, new CustomSpiceListener<List>() {

			@SuppressWarnings("unchecked")
			@Override
			public void onRequestSuccess(List result) {
				super.onRequestSuccess(result);
				setData(result, toClear);
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		position = position - 1;

		if (position != -1 && position != adapter.getCount()) {
			User user = (User) adapter.getItem(position).getModel();
			ProfileOtherActivity.openOtherProfile(this, user.getId(), user.getImage(), user.getFirstName() + " " + user.getLastName(), user);
		}
	}

	@Override
	public void onSearch(String data) {
		mCurrentIndex = 0;
		if (TextUtils.isEmpty(data)) {
			mSearchData = null;
		} else {
			mSearchData = data;
		}
		getUsers(mCurrentIndex, mSearchData, true);
	}

	private void invitePeople() {

		StringBuilder users = new StringBuilder();

		List<String> usersId = adapter.getUsersSelected();

		if (usersId.isEmpty()) {
			return;
		}

		for (int i = 0; i < usersId.size(); i++) {
			users.append(usersId.get(i));

			if (i != (usersId.size() - 1)) {
				users.append(",");
			}
		}

		handleProgress(true);

		UserSpice.InviteUsers inviteUser = new UserSpice.InviteUsers(chatId, users.toString());
		spiceManager.execute(inviteUser, new CustomSpiceListener<Chat>() {

			@Override
			public void onRequestFailure(SpiceException arg0) {
				super.onRequestFailure(arg0);
				handleProgress(false);
				Utils.onFailedUniversal(null, InvitePeopleActivity.this);
			}

			@Override
			public void onRequestSuccess(Chat result) {
				super.onRequestSuccess(result);
				handleProgress(false);

				if (result.getCode() == Const.API_SUCCESS) {

					ChatActivity.startWithChatId(InvitePeopleActivity.this, result.chat, result.user);
					finish();

				} else {
					Utils.onFailedUniversal(Helper.errorDescriptions(InvitePeopleActivity.this, result.getCode()), InvitePeopleActivity.this);
				}
			}
		});
	}

	@Override
	public void onChange(GlobalModel obj, boolean isFromDetails) {

		boolean isFound = false;
		int j = 0;

		for (User user : usersToAdd) {
			if (user.getId() == ((User) obj.getModel()).getId()) {
				isFound = true;
				break;
			}
			j++;
		}

		if (isFound) {
			usersToAdd.remove(j);
		} else {
			usersToAdd.add((User) obj.getModel());
		}

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < usersToAdd.size(); i++) {
			builder.append(usersToAdd.get(i).getFirstName() + " " + usersToAdd.get(i).getLastName());
			if (i != (usersToAdd.size() - 1)) {
				builder.append(", ");
			}
		}

		String selectedUsers = getString(R.string.selected_users);
		Spannable span = new SpannableString(selectedUsers + builder.toString());
		span.setSpan(new ForegroundColorSpan(R.color.devil_gray), 0, selectedUsers.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		invitedPeople.setText(span);

	}

	private void setInitialTextToTxtUsers() {
		String selectedUsers = getString(R.string.selected_users);
		Spannable span = new SpannableString(selectedUsers);
		span.setSpan(new ForegroundColorSpan(R.color.devil_gray), 0, selectedUsers.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		invitedPeople.setText(span);
	}

	@Override
	public void onGlobalSearchNetworkResult(int totalCount) {
		mTotalCount = totalCount;
	}

	@Override
	public void onGlobalSearchDBChanged(List<GlobalModel> usableData, boolean isClear) {
		setData(usableData, isClear);
	}

}
