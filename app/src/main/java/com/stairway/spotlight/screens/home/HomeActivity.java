package com.stairway.spotlight.screens.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetDialog;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.stairway.spotlight.MessageController;
import com.stairway.spotlight.UserSessionManager;
import com.stairway.spotlight.config.AnalyticsContants;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.core.NotificationController;
import com.stairway.spotlight.core.lib.ImageUtils;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseActivity;

import com.stairway.spotlight.models.ContactResult;
import com.stairway.spotlight.models.MessageResult;
import com.stairway.spotlight.models.UserSession;
import com.stairway.spotlight.screens.message.MessageActivity;
import com.stairway.spotlight.screens.new_chat.NewChatActivity;
import com.stairway.spotlight.screens.search.SearchActivity;
import com.stairway.spotlight.screens.settings.SettingsActivity;

import org.jivesoftware.smackx.chatstates.ChatState;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

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

	@Bind(R.id.header)
	View header;

	private ActionBarDrawerToggle toggle;

	private HomePresenter presenter;
	private List<ChatItem> chats;
	private ChatListAdapter chatListAdapter;

	private UserSession userSession;

	private static final String KEY_CHAT_USER_NAME = "HomeActivity.CHAT_USERNAME";
	private static final String KEY_ENTRY = "HomeActivity.Entry";

	private FirebaseAnalytics firebaseAnalytics;
	private final String SCREEN_NAME = "home";

	public static Intent callingIntent(Context context, int entry, String chatUserName) {
		Intent intent = new Intent(context, HomeActivity.class);
		intent.putExtra(KEY_CHAT_USER_NAME, chatUserName);
		intent.putExtra(KEY_ENTRY, entry);
		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Logger.d(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		ButterKnife.bind(this);

		Intent receivedIntent = getIntent();
		if(receivedIntent.getIntExtra(KEY_ENTRY, 0) == 1) {
			startActivity(MessageActivity.callingIntent(this, receivedIntent.getStringExtra(KEY_CHAT_USER_NAME)));
		}

		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		this.presenter = new HomePresenter(MessageController.getInstance());
		userSession = UserSessionManager.getInstance().load();

		TextView profileIdView = (TextView)header.findViewById(R.id.tv_profile_id);
		TextView profileNameView = (TextView)header.findViewById(R.id.tv_profile_name);
		ImageView profileDp = (ImageView)header.findViewById(R.id.profile_image);
		profileNameView.setText(userSession.getName());
		profileIdView.setText("ID: "+userSession.getUserId());

		if(userSession.getProfilePicPath()!=null && !userSession.getProfilePicPath().isEmpty()) {
			Glide.with(this).load(userSession.getProfilePicPath())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.skipMemoryCache(true)
					.into(profileDp);
		} else {
			profileDp.setImageDrawable(ImageUtils.getDefaultProfileImage(userSession.getName(), userSession.getUserId(), 18));
		}

		chatList.setLayoutManager(new LinearLayoutManager(this));
		RecyclerView.ItemAnimator animator = chatList.getItemAnimator();
		if (animator instanceof SimpleItemAnimator)
			((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
		chatListAdapter = new ChatListAdapter(this, this);
		chatList.setAdapter(chatListAdapter);


		toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

		toggle.setDrawerIndicatorEnabled(false);
		Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_menu, this.getTheme());
		toggle.setHomeAsUpIndicator(drawable);
		toggle.setToolbarNavigationClickListener(v -> {
			if (drawer.isDrawerVisible(GravityCompat.START)) {
				drawer.closeDrawer(GravityCompat.START);
			} else {
				drawer.openDrawer(GravityCompat.START);
				firebaseAnalytics.logEvent(AnalyticsContants.Event.OPEN_DRAWER_MENU, null);
			}
		});
		drawer.addDrawerListener(toggle);
		toggle.syncState();
		navigationView.setNavigationItemSelectedListener(this);

		fab.setOnClickListener(view -> startActivity(NewChatActivity.callingIntent(this, true)));

		chatList.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				if(dy > 0){
					fab.hide();
				} else{
					fab.show();
				}
				super.onScrolled(recyclerView, dx, dy);
			}
		});

		this.firebaseAnalytics = FirebaseAnalytics.getInstance(this);
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
		NotificationController.getInstance().showNotificationAndAlert(false);
		if(fab!=null) {
			fab.setVisibility(View.VISIBLE);
		}
		presenter.loadChatList();

		/*              Analytics           */
		firebaseAnalytics.setCurrentScreen(this, SCREEN_NAME, null);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@OnClick(R.id.nav_item_contacts)
	public void onNavigationContactsClicked() {
		final Activity homeActivity = this;
		new Handler().postDelayed(() -> startActivity(NewChatActivity.callingIntent(homeActivity, false)), 250);
		drawer.closeDrawer(GravityCompat.START, true);
	}

	@OnClick(R.id.nav_item_settings)
	public void onNavigationSettingsClicked() {
		final Activity homeActivity = this;
		new Handler().postDelayed(() -> startActivity(SettingsActivity.callingIntent(homeActivity)), 250);
		drawer.closeDrawer(GravityCompat.START, true);
	}

	@OnClick(R.id.nav_item_faq)
	public void onNavigationFAQClicked() {}

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

			/*              Analytics           */
			firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, null);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void showError(String title, String message) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage("\n"+message);
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
		alertDialog.show();
	}

	@Override
	public void setDeliveryStatus(int status, int chatId) {}

	@Override
	public void displayChatList(List<ChatItem> chats) {
		this.chats = chats;
		chatListAdapter.setChatList(chats);
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
		final Activity homeActivity = this;
		switch(id) {
			case android.R.id.home:
				onBackPressed();
				drawer.closeDrawer(GravityCompat.START);
				break;
		}
		return true;
	}

	@Override
	public void onChatItemClicked(String userName) {

		/*              Analytics           */
		Bundle bundle = new Bundle();
		bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, userName);
		firebaseAnalytics.logEvent(AnalyticsContants.Event.OPEN_CHAT, bundle);

		startActivity(MessageActivity.callingIntent(this, userName));
	}

	@Override
	public void onChatItemLongClicked(String username) {
		BottomSheetDialog chatActionsDialog = new BottomSheetDialog(this);
		View chatActionsView = this.getLayoutInflater().inflate(R.layout.bottomsheet_chat_actions, null);
		chatActionsDialog.setContentView(chatActionsView);
		chatActionsDialog.show();

		LinearLayout llClearHistory = (LinearLayout)chatActionsView.findViewById(R.id.clear_history);
		llClearHistory.setOnClickListener(v -> {
			Bundle bundle = new Bundle();
			bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, username);
			firebaseAnalytics.logEvent(AnalyticsContants.Event.CLEAR_HISTORY_CHAT, bundle);
		});

		LinearLayout llDelteChat = (LinearLayout)chatActionsView.findViewById(R.id.delete_chat);
		llDelteChat.setOnClickListener(v -> {
			Bundle bundle = new Bundle();
			bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, username);
			firebaseAnalytics.logEvent(AnalyticsContants.Event.DELETE_CHAT, bundle);
		});

		/*              Analytics           */
		Bundle bundle = new Bundle();
		bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, username);
		firebaseAnalytics.logEvent(AnalyticsContants.Event.SELECT_CHAT, bundle);
	}

	@Override
	public void onMessageReceived(MessageResult messageResult, ContactResult from) {
		Logger.d(this, "MessageReceived");
		messageResult.setName(from.getContactName());
		ChatItem item = new ChatItem(messageResult.getChatId(), messageResult.getName(), messageResult.getMessage(), messageResult.getTime(), messageResult.getMessageStatus(), messageResult.getReceiptId(), 1);
		this.chats = chatListAdapter.newChatMessage(item);
		chatList.scrollToPosition(0);
		NotificationController.getInstance().showNotificationAndAlert(true);
	}

	@Override
	public void onMessageStatusReceived(String chatId, String deliveryReceiptId, MessageResult.MessageStatus messageStatus) {
		super.onMessageStatusReceived(chatId, deliveryReceiptId, messageStatus);

		chatListAdapter.updateDeliveryStatus(deliveryReceiptId, messageStatus);
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
			super.onBackPressed();
		}
	}
}