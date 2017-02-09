package com.stairway.spotlight.screens.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.squareup.otto.Subscribe;
import com.stairway.spotlight.MessageController;
import com.stairway.spotlight.api.ApiManager;
import com.stairway.spotlight.api.user.UserApi;
import com.stairway.spotlight.api.user.UserRequest;
import com.stairway.spotlight.api.user.UserResponse;
import com.stairway.spotlight.api.user._User;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.core.lib.ImageUtils;
import com.stairway.spotlight.db.ContactStore;
import com.stairway.spotlight.db.MessageStore;
import com.stairway.spotlight.models.AccessToken;
import com.stairway.spotlight.AccessTokenManager;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.EventBus;
import com.stairway.spotlight.core.FCMRegistrationIntentService;

import com.stairway.spotlight.models.ContactResult;
import com.stairway.spotlight.models.MessageResult;
import com.stairway.spotlight.screens.message.MessageActivity;
import com.stairway.spotlight.screens.new_chat.NewChatActivity;
import com.stairway.spotlight.screens.search.SearchActivity;

import org.jivesoftware.smackx.chatstates.ChatState;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.stairway.spotlight.core.FCMRegistrationIntentService.SENT_TOKEN_TO_SERVER;

public class HomeActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, HomeContract.View, ChatListAdapter.ChatClickListener {
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

	private ActionBarDrawerToggle toggle;

	private HomePresenter presenter;
	private List<ChatItem> chats;
	private ChatListAdapter chatListAdapter;

	public static Intent callingIntent(Context context) {
		return new Intent(context, HomeActivity.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Logger.d(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		ButterKnife.bind(this);
		chatList.setLayoutManager(new LinearLayoutManager(this));
		RecyclerView.ItemAnimator animator = chatList.getItemAnimator();
		if (animator instanceof SimpleItemAnimator)
			((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
		chatListAdapter = new ChatListAdapter(this, this);
		chatList.setAdapter(chatListAdapter);

		this.presenter = new HomePresenter(MessageController.getInstance());

		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

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

		fab.setOnClickListener(view -> startActivity(NewChatActivity.callingIntent(this, true)));
	}

	@Override
	protected void onStart() {
		super.onStart();
		presenter.attachView(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		presenter.detachView();
	}

	@Override
	protected void onResume() {
		super.onResume();
		presenter.loadChatList();
	}

	@Override
	protected void onPause() {
		super.onPause();
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
	public void setDeliveryStatus(int status, int chatId) {}

	@Override
	public void displayChatList(List<ChatItem> chats) {
		this.chats = chats;
		chatListAdapter.setChatList(chats);
	}

	@Override
	public void addNewMessage(MessageResult messageResult) {
		ChatItem item = new ChatItem(messageResult.getChatId(), messageResult.getChatId(), messageResult.getMessage(), messageResult.getTime(), 1);
		chatListAdapter.newChatMessage(item);
		Logger.d(this, "new notification: "+item);
	}

	@Override
	public void showChatState(String from, ChatState chatState) {
		if(chatState == ChatState.composing || chatState == ChatState.active)
			chatListAdapter.setChatState(from, "Typing...");
		if(chatState == ChatState.paused || chatState == ChatState.inactive)
			chatListAdapter.resetChatState(from);
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch(id) {
			case R.id.nav_settings:
				break;

			case R.id.nav_manage:
				break;

			case R.id.nav_contacts:
				startActivity(NewChatActivity.callingIntent(this, false));
				break;
			case android.R.id.home:
				onBackPressed();
				break;
		}
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	@Override
	public void onChatItemClicked(String userName) {
		Logger.d(this, "onChatItemClicked");
		//get chat name
		Activity activity = this;
		ContactStore.getInstance().getContactByUserName(userName)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Subscriber<ContactResult>() {
					@Override
					public void onCompleted() {}
					@Override
					public void onError(Throwable e) {}

					@Override
					public void onNext(ContactResult contactResult) {
						String name = "";
							name = contactResult.getContactName();
						startActivity(MessageActivity.callingIntent(activity, contactResult.getUsername(), name));
					}
				});
	}

	@Override
	public void onMessageReceived(MessageResult messageId) {
		Logger.d(this, "Message:::"+messageId);
		addNewMessage(messageId);
	}

	@Override
	public void onChatStateReceived(String from, ChatState chatState) {
		showChatState(from, chatState);
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

	private void uploadFCMToken() {
		//Upload to token to server if FCM token not updated
//		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//		if(! sharedPreferences.getBoolean(SENT_TOKEN_TO_SERVER, false)) {
//			String fcmToken = sharedPreferences.getString(FCMRegistrationIntentService.FCM_TOKEN, "");
//			Logger.d(this, "FCM TOKEN:"+fcmToken);
//			_User updateUser = new _User();
//			updateUser.setNotificationToken(fcmToken);
//			UserRequest userRequest = new UserRequest();
//			userRequest.setUser(updateUser);
//			ApiManager.getUserApi().updateUser(userRequest)
//
//					.subscribe(new Subscriber<UserResponse>() {
//				@Override
//				public void onCompleted() {}
//				@Override
//				public void onError(Throwable e) {
//					sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
//				}
//				@Override
//				public void onNext(UserResponse userResponse) {
//					sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, true).apply();
//				}
//			});
//		}
	}
}