package com.stairway.spotlight.screens.home;

import android.app.Activity;
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
import android.widget.TextView;

import com.stairway.spotlight.MessageController;
import com.stairway.spotlight.UserSessionManager;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.core.NotificationController;
import com.stairway.spotlight.core.lib.ImageUtils;
import com.stairway.spotlight.db.ContactStore;
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
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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

	private UserSession userSession;

	public static Intent callingIntent(Context context) {
		return new Intent(context, HomeActivity.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Logger.d(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		ButterKnife.bind(this);

		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		this.presenter = new HomePresenter(MessageController.getInstance());
		userSession = UserSessionManager.getInstance().load();

		View header = navigationView.getHeaderView(0);
		TextView profileIdView = (TextView)header.findViewById(R.id.tv_profile_id);
		TextView profileNameView = (TextView)header.findViewById(R.id.tv_profile_name);
		ImageView profileDp = (ImageView)header.findViewById(R.id.profile_image);
		profileNameView.setText(userSession.getName());
		profileIdView.setText("ID: "+userSession.getUserId());

		profileDp.setImageDrawable(ImageUtils.getDefaultProfileImage(userSession.getName(), userSession.getUserId(), 18));

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
		if(fab!=null) {
			fab.setVisibility(View.VISIBLE);
		}
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
			case R.id.nav_settings:
				new Handler().postDelayed(() -> startActivity(SettingsActivity.callingIntent(homeActivity)), 250);
				drawer.closeDrawer(GravityCompat.START, true);
				break;
			case R.id.nav_faq:
				break;
			case R.id.nav_contacts:
				new Handler().postDelayed(() -> startActivity(NewChatActivity.callingIntent(homeActivity, false)), 250);
				drawer.closeDrawer(GravityCompat.START, true);
				break;
			case android.R.id.home:
				onBackPressed();
				drawer.closeDrawer(GravityCompat.START);
				break;
		}
		return true;
	}

	@Override
	public void onChatItemClicked(String userName) {
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
	public void onChatItemLongClicked(String username) {
		BottomSheetDialog chatActionsDialog = new BottomSheetDialog(this);
		View chatActionsView = this.getLayoutInflater().inflate(R.layout.bottomsheet_chat_actions, null);
		chatActionsDialog.setContentView(chatActionsView);
		chatActionsDialog.show();
	}

	@Override
	public void onMessageReceived(MessageResult messageId) {
		addNewMessage(messageId);
		chatList.scrollToPosition(0);
		NotificationController.getInstance().showNotification(true);
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