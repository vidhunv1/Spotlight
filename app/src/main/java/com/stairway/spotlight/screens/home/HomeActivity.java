package com.stairway.spotlight.screens.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

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
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stairway.spotlight.core.lib.AndroidUtils;
import com.stairway.spotlight.screens.home.di.HomeViewModule;
import com.stairway.spotlight.screens.launcher.LauncherActivity;
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
		RecyclerView.ItemAnimator animator = chatList.getItemAnimator();
		if (animator instanceof SimpleItemAnimator) {
			((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
		}

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

		uploadFCMToken();
	}

	/* Lifecycle */

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
	/* Lifecycle */

	/* Menu options */
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
	/* Menu options */

	/* Views */
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
		if(chatState == ChatState.composing || chatState == ChatState.active)
			chatListAdapter.setChatState(from, "Typing...");
		if(chatState == ChatState.paused || chatState == ChatState.inactive)
			chatListAdapter.resetChatState(from);
	}

	@Override
	public void showContactAddedSuccess(String name, String username, boolean isExistingContact) {
		addContactPopupWindow.dismiss();
		AndroidUtils.hideSoftInput(this);

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
			ProgressBar pb = (ProgressBar) addContactPopupView.findViewById(R.id.pb_add_contact);
			pb.setVisibility(View.GONE);
			showAlertDialog("Please enter a valid iChat ID.", R.layout.alert);
		}
	}
	/* Views */

	/* Events */
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch(id) {
			case R.id.nav_settings:
				break;

			case R.id.nav_manage:
				break;

			case R.id.nav_add_contact:
				new Handler().postDelayed(() -> showAddContactPopup(), 150);
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
		startActivity(MessageActivity.callingIntent(this, userName));
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
	/* Events */


	/* Helpers */
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

		FrameLayout outLayout = (FrameLayout) addContactPopupView.findViewById(R.id.fl_add_contact);
		outLayout.setOnClickListener(v -> {
			addContactPopupWindow.dismiss();
			AndroidUtils.hideSoftInput(this);
		});

		EditText enterId = (EditText) addContactPopupView.findViewById(R.id.et_add_contact);
		AndroidUtils.showSoftInput(this, enterId);

		Button addButton = (Button) addContactPopupView.findViewById(R.id.btn_add_contact);
		addButton.setOnClickListener(v -> {
			if(enterId.getText().length()>0) {
				presenter.addContact(enterId.getText().toString(), userSession.getAccessToken());
				ProgressBar pb = (ProgressBar) addContactPopupView.findViewById(R.id.pb_add_contact);
				pb.setVisibility(View.VISIBLE);
			}
		});

		// popup not working in older versions
		if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
			homeLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){
				public void onGlobalLayout(){
					int heightDiff = homeLayout.getRootView().getHeight()- homeLayout.getHeight();
					// IF height diff is more then 150, consider keyboard as visible.
					Logger.d(this, "DIFF: "+heightDiff);
					RelativeLayout content = (RelativeLayout) addContactPopupView.findViewById(R.id.rl_add_contact_content);
					FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)content.getLayoutParams();
					if(heightDiff>150) {
						int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -110, getResources().getDisplayMetrics());
						params.setMargins(0, px, 0, 0);
						content.setLayoutParams(params);
					} else {
						params.setMargins(0, 0, 0, 0);
						content.setLayoutParams(params);
					}
				}
			});
	}

	public void showAlertDialog(String message, int layout) {
		//TODO: Something wrong. 16?
		final int WIDTH = 294, HEIGHT = 98;

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		View dialogView = inflater.inflate(layout, null);

		dialogBuilder.setView(dialogView);
		AlertDialog alertDialog = dialogBuilder.create();
		alertDialog.show();

		int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, WIDTH+16, getResources().getDisplayMetrics());
		int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, HEIGHT+16, getResources().getDisplayMetrics());
		alertDialog.getWindow().setLayout(width, height);

		TextView messageText = (TextView) dialogView.findViewById(R.id.tv_alert_message);
		messageText.setText(message);
		Button ok = (Button) dialogView.findViewById(R.id.btn_alert_ok);
		ok.setOnClickListener(v -> alertDialog.dismiss());
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
	/* Helpers */

	@Override
	protected void injectComponent(ComponentContainer componentContainer) {
		if(componentContainer.userSessionComponent()==null) {
			startActivity(LauncherActivity.callingIntent(this));
			finish();
		} else {
			componentContainer.userSessionComponent().plus(new HomeViewModule()).inject(this);
			userSession = componentContainer.userSessionComponent().getUserSession();
		}
	}
}