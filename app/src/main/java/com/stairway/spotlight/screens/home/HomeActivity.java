package com.stairway.spotlight.screens.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.stairway.data.config.Logger;
import com.stairway.data.source.message.MessageResult;
import com.stairway.data.source.user.UserApi;
import com.stairway.data.source.user.UserSessionResult;
import com.stairway.data.source.user.gson_models.User;
import com.stairway.data.source.user.gson_models.UserResponse;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.FCMRegistrationIntentService;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stairway.spotlight.screens.home.di.HomeViewModule;
import com.stairway.spotlight.screens.message.MessageActivity;
import com.stairway.spotlight.screens.new_chat.NewChatActivity;
import com.stairway.spotlight.screens.search.SearchActivity;

import org.jivesoftware.smackx.chatstates.ChatState;
import java.util.List;

import javax.inject.Inject;
import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscriber;

import static com.stairway.spotlight.core.FCMRegistrationIntentService.SENT_TOKEN_TO_SERVER;

public class HomeActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, HomeContract.View, ChatListAdapter.ChatClickListener {
	private ActionBarDrawerToggle toggle;

	@Bind(R.id.fab)
	FloatingActionButton fab;

	@Bind(R.id.drawer_layout)
	DrawerLayout drawer;

	@Bind(R.id.nav_view)
	NavigationView navigationView;

	@Bind(R.id.rv_chat_list)
	RecyclerView chatList;

	@Bind(R.id.cl_home)
	CoordinatorLayout homeLayout;

	@Bind(R.id.tb_home)
	Toolbar toolbar;

	@Inject
	HomePresenter presenter;

	UserApi userApi;
	UserSessionResult userSession;

	private List<ChatListItemModel> chats;

	private PopupWindow addContactPopupWindow;
	private View addContactPopupView;

	private ChatListAdapter chatListAdapter;
	public static Intent callingIntent(Context context) {
		Intent intent = new Intent(context, HomeActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		ButterKnife.bind(this);

		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		chatList.setLayoutManager(new LinearLayoutManager(this));

		fab.setOnClickListener(view -> startActivity(NewChatActivity.callingIntent(this)));

		toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		toggle.setDrawerIndicatorEnabled(false);
		Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_menu, this.getTheme());

		toggle.setHomeAsUpIndicator(drawable);
		toggle.setToolbarNavigationClickListener(v -> {
			if (drawer.isDrawerVisible(GravityCompat.START)) {
				drawer.closeDrawer(GravityCompat.START);
			} else {
				drawer.openDrawer(GravityCompat.START);
			}
		});

		drawer.addDrawerListener(toggle);
		toggle.syncState();

		navigationView.setNavigationItemSelectedListener(this);

//		navigationView.getHeaderView(0).setOnClickListener(v -> {
//			startActivity(ProfileActivity.callingIntent(this));
//			drawer.closeDrawer(GravityCompat.START);
//		});
		uploadFCMToken();
	}

	@Override
	protected void onResume() {
		super.onResume();
		presenter.attachView(this);
		presenter.initChatList();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			toggle.setDrawerIndicatorEnabled(true);
			fab.setVisibility(View.VISIBLE);
//            setChatFragment();
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.home_toolbar, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			onBackPressed();
			return true;
		} else if(id == R.id.action_search) {
			startActivity(SearchActivity.callingIntent(this, chats));
			this.overridePendingTransition(0, 0);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch(id) {
			case R.id.nav_settings:
				break;

			case R.id.nav_manage:
				break;

			case R.id.nav_add_contact:
				showAddContactPopup();
				break;
			case android.R.id.home:
				onBackPressed();
				break;
		}
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	private void showAddContactPopup() {
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

		addContactPopupView = inflater.inflate(R.layout.add_contact_popup,null);
		addContactPopupWindow = new PopupWindow(
				addContactPopupView,
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT,
				true
		);
		if(Build.VERSION.SDK_INT>=21)
			addContactPopupWindow.setElevation(5.0f);
		addContactPopupWindow.showAtLocation(homeLayout, Gravity.CENTER,0,0);
		addContactPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);

		RelativeLayout outLayout = (RelativeLayout) addContactPopupView.findViewById(R.id.fl_add_contact);
		outLayout.setOnClickListener(v -> {
			addContactPopupWindow.dismiss();
			hideSoftInput();
		});

		EditText enterId = (EditText) addContactPopupView.findViewById(R.id.et_add_contact);
		showSoftInput(enterId);

		Button addButton = (Button) addContactPopupView.findViewById(R.id.btn_add_contact);
		addButton.setOnClickListener(v -> presenter.addContact(enterId.getText().toString(), userSession.getAccessToken()));
	}


	@Override
	public void showContactAddedSuccess(String name, String username, boolean isExistingContact) {
		addContactPopupWindow.dismiss();
		hideSoftInput();

		String message;
		if(isExistingContact)
			message = name+" is already in your contacts on iChat.";
		else
			message = name+" is added to your contacts on iChat.";

		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		View addedContactView = inflater.inflate(R.layout.added_contact_popup, null);
		PopupWindow addedPopupWindow = new PopupWindow(
				addedContactView,
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT,
				true
		);
		if(Build.VERSION.SDK_INT>=21)
			addedPopupWindow.setElevation(5.0f);
		addedPopupWindow.showAtLocation(homeLayout, Gravity.CENTER,0,0);

		RelativeLayout out = (RelativeLayout) addedContactView.findViewById(R.id.fl_added_contact);
		out.setOnClickListener(view -> {
			addedPopupWindow.dismiss();
		});

		Button sendMessage = (Button) addedContactView.findViewById(R.id.btn_send_message);
		sendMessage.setOnClickListener(v1 -> {
			addedPopupWindow.dismiss();
			startActivity(MessageActivity.callingIntent(this, username));
		});

		TextView resultMessage = (TextView) addedContactView.findViewById(R.id.tv_add_result_message);
		resultMessage.setText(message);
	}

	@Override
	public void showInvalidIDError() {
		if(addContactPopupWindow.isShowing()) {
			TextInputLayout til = (TextInputLayout) addContactPopupView.findViewById(R.id.ti_add_contact);
			til.setErrorEnabled(true);
			til.setError("Please enter a valid ID.");
		}
	}

	@Override
	public void setDeliveryStatus(int status, int chatId) {}

	@Override
	public void displayChatList(List<ChatListItemModel> chats) {
		this.chats = chats;
		chatListAdapter = new ChatListAdapter(this, chats, this);
		chatList.setAdapter(chatListAdapter);
		}

	@Override
	public void addNewMessage(MessageResult messageResult) {
		ChatListItemModel item = new ChatListItemModel(messageResult.getChatId(), messageResult.getChatId(), messageResult.getMessage(), messageResult.getTime(), 1);
		chatListAdapter.newChatMessage(item);
		Logger.d(this, "new notification: "+item);
	}

	@Override
	public void showChatState(String from, ChatState chatState) {
		if(chatState == ChatState.composing)
			chatListAdapter.setChatState(from, "Typing...");
		else
			chatListAdapter.resetChatState(from);
	}

	@Override
	public void onChatItemClicked(String userName) {
		startActivity(MessageActivity.callingIntent(this, userName));
	}

	@Override
	protected void injectComponent(ComponentContainer componentContainer) {
		componentContainer.userSessionComponent().plus(new HomeViewModule()).inject(this);
		userSession = componentContainer.userSessionComponent().getUserSession();
	}

	@Override
	public void onMessageReceived(MessageResult messageId) {
		addNewMessage(messageId);
	}

	@Override
	public void onChatStateReceived(String from, ChatState chatState) {
		showChatState(from, chatState);
	}

	private void uploadFCMToken() {
		//Upload to token to server if FCM token not updated
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		if(! sharedPreferences.getBoolean(SENT_TOKEN_TO_SERVER, false)) {
			String fcmToken = sharedPreferences.getString(FCMRegistrationIntentService.FCM_TOKEN, "");
			Logger.d(this, "FCM TOKEN:"+fcmToken);
			User updateUser = new User();
			updateUser.setNotificationToken(fcmToken);
			userApi = new UserApi();
			userApi.updateUser(updateUser, userSession.getAccessToken()).subscribe(new Subscriber<UserResponse>() {
				@Override
				public void onCompleted() {}
				@Override
				public void onError(Throwable e) {
					sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
				}
				@Override
				public void onNext(UserResponse userResponse) {
					sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, true).apply();
				}
			});
		}
	}

	private void showSoftInput(EditText editText) {
		editText.setOnFocusChangeListener((v, hasFocus) -> {
			if(hasFocus) {
				InputMethodManager inputMgr = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
				inputMgr.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
			}
		});
		editText.requestFocus();
	}

	private void hideSoftInput() {
		View view = this.getCurrentFocus();
		view.clearFocus();
		if (view != null) {
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}
}
