package com.chat.ichat.screens.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.chat.ichat.application.SpotlightApplication;
import com.chat.ichat.core.RecyclerViewHelper;
import com.chat.ichat.core.lib.AndroidUtils;
import com.chat.ichat.db.BotDetailsStore;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.screens.discover_bots.DiscoverBotsActivity;
import com.chat.ichat.screens.people_nearby.PeopleNearbyActivity;
import com.chat.ichat.screens.settings.SettingsActivity1;
import com.chat.ichat.screens.web_view.WebViewActivity;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.chat.ichat.MessageController;
import com.chat.ichat.UserSessionManager;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.config.AnalyticsConstants;
import com.chat.ichat.core.Logger;
import com.chat.ichat.core.NotificationController;
import com.chat.ichat.core.lib.ImageUtils;
import com.chat.ichat.R;
import com.chat.ichat.core.BaseActivity;

import com.chat.ichat.db.MessageStore;
import com.chat.ichat.models.ContactResult;
import com.chat.ichat.models.MessageResult;
import com.chat.ichat.models.UserSession;
import com.chat.ichat.screens.message.MessageActivity;
import com.chat.ichat.screens.new_chat.NewChatActivity;
import com.chat.ichat.screens.search.SearchActivity;
import com.chat.ichat.screens.settings.SettingsActivity;

import org.jivesoftware.smackx.chatstates.ChatState;
import org.joda.time.DateTime;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeActivity extends BaseActivity implements HomeContract.View, ChatListAdapter.ChatClickListener{
	@Bind(R.id.fab)
	FloatingActionButton fab;

	@Bind(R.id.rv_chat_list)
	RecyclerView chatList;

	@Bind(R.id.cl_home)
	CoordinatorLayout homeLayout;

	@Bind(R.id.tb_home)
	Toolbar toolbar;

	private ActionBarDrawerToggle toggle;

	final ProgressDialog[] progressDialog = new ProgressDialog[1];

	private HomePresenter presenter;
	private List<ChatItem> chats;
	private ChatListAdapter chatListAdapter;

	private UserSession userSession;

	private static final String KEY_CHAT_USER_NAME = "HomeActivity.CHAT_USERNAME";
	private static final String KEY_ENTRY = "HomeActivity.Entry";

	private FirebaseAnalytics firebaseAnalytics;
	public static final String APP_PREFS_FILE = "app_prefs";
	public static final String KEY_LAST_SYNC = "last_sync";
	public static final String KEY_SHOW_PEOPLE_NEARBY_NOTE = "show_people_nearby_note";

	private SharedPreferences sharedPreferences;
	private RecyclerView.LayoutManager layoutManager;

	public static Intent callingIntent(Context context, int entry, String chatUserName) {
		Intent intent = new Intent(context, HomeActivity.class);
		intent.putExtra(KEY_CHAT_USER_NAME, chatUserName);
		intent.putExtra(KEY_ENTRY, entry);
		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		ButterKnife.bind(this);
		this.sharedPreferences = SpotlightApplication.getContext().getSharedPreferences(APP_PREFS_FILE, Context.MODE_PRIVATE);

		Intent receivedIntent = getIntent();
		if(receivedIntent.getIntExtra(KEY_ENTRY, 0) == 1) {
			startActivity(MessageActivity.callingIntent(this, receivedIntent.getStringExtra(KEY_CHAT_USER_NAME)));
		}

		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		this.presenter = new HomePresenter(MessageController.getInstance(), ApiManager.getAppApi(), MessageStore.getInstance(), ContactStore.getInstance(), ApiManager.getUserApi(), BotDetailsStore.getInstance(), ApiManager.getBotApi());
		userSession = UserSessionManager.getInstance().load();
		Logger.d(this, "AccessToken: "+userSession.getAccessToken());

		layoutManager = new LinearLayoutManager(this);
		chatList.setLayoutManager(layoutManager);
		RecyclerView.ItemAnimator animator = chatList.getItemAnimator();
		if (animator instanceof SimpleItemAnimator)
			((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
		chatListAdapter = new ChatListAdapter(this, this);
		chatList.setAdapter(chatListAdapter);

		fab.setOnClickListener(view -> {
			startActivity(SearchActivity.callingIntent(this));
		});

		try {
			presenter.init(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
		} catch (PackageManager.NameNotFoundException e) {
		}
		this.firebaseAnalytics = FirebaseAnalytics.getInstance(this);

		Long lastSync = sharedPreferences.getLong(KEY_LAST_SYNC, -1);
		Logger.d(this, "LastSync: "+lastSync);
		DateTime lastSyncTime =  new DateTime(lastSync);
		if(lastSync == -1 || lastSyncTime.plusMinutes(15).getMillis() <= DateTime.now().getMillis()) {
			Logger.d(this, "Syncing...");
			presenter.performSync();
		}
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
		userSession = UserSessionManager.getInstance().load();

		/*              Analytics           */
		firebaseAnalytics.setCurrentScreen(this, AnalyticsConstants.Event.HOME_SCREEN, null);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	public void showContactAddedSuccess(String name, String username, boolean isExistingContact) {
		AndroidUtils.hideSoftInput(this);
		firebaseAnalytics.logEvent(AnalyticsConstants.Event.POPUP_ADD_CONTACT_SUCCESS_SHOW, null);

		if(progressDialog[0].isShowing()) {
			progressDialog[0].dismiss();
		}

		String message;
		if(isExistingContact) {
			message = "<b>" + name + "</b> is already in your contacts on iChat.";
		} else {
			message = "<b>" + name + "</b> is added to your contacts on iChat.";
		}

		AlertDialog alertDialog = new AlertDialog.Builder(HomeActivity.this).create();
		alertDialog.setMessage(Html.fromHtml(message));
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> {
			firebaseAnalytics.logEvent(AnalyticsConstants.Event.POPUP_ADD_CONTACT_SUCCESS_OK, null);
			dialog.dismiss();
		});
		alertDialog.setOnDismissListener(dialog -> {
			firebaseAnalytics.logEvent(AnalyticsConstants.Event.POPUP_ADD_CONTACT_SUCCESS_DISMISS, null);
        });
		alertDialog.show();

		presenter.loadChatList();
	}

	@Override
	public void showInvalidIDError() {
		AndroidUtils.hideSoftInput(this);

		firebaseAnalytics.logEvent(AnalyticsConstants.Event.POPUP_ADD_CONTACT_ERROR_SHOW, null);

		if(progressDialog[0].isShowing()) {
			progressDialog[0].dismiss();
		}

		AlertDialog alertDialog = new AlertDialog.Builder(HomeActivity.this).create();
		alertDialog.setMessage("Please enter a valid iChat ID.");
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> {
			firebaseAnalytics.logEvent(AnalyticsConstants.Event.POPUP_ADD_CONTACT_ERROR_OK, null);
			dialog.dismiss();
		});
		alertDialog.setOnDismissListener(dialog -> firebaseAnalytics.logEvent(AnalyticsConstants.Event.POPUP_ADD_CONTACT_ERROR_DISMISS, null));
		alertDialog.show();
	}

	@Override
	public void showUpdate(int versionCode, String versionName, boolean isMandatory) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("Update Available");
		alertDialog.setMessage("New version contains improvements and bug fixes.\nPlease update to version "+versionName+".");
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "UPDATE", (dialog, which) -> {
			final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
			try {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
			} catch (android.content.ActivityNotFoundException anfe) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
			}
			dialog.dismiss();
		});
		alertDialog.show();
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
		} else if(id == R.id.action_settings) {
			startActivity(SettingsActivity1.callingIntent(this));
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
		Logger.d(this, "DisplayChatList: "+chats.get(0).toString());
		this.chats = chats;
		chatListAdapter.setChatList(chats);
		chatList.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				final View child = RecyclerViewHelper.findOneVisibleChild(chatList, 0, layoutManager.getChildCount(), true, false);
				int pos = child == null ? RecyclerView.NO_POSITION : chatList.getChildAdapterPosition(child);
				if(pos < chats.size()) {
					Bundle bundle = new Bundle();
					bundle.putString(AnalyticsConstants.Param.RECIPIENT_NAME, chats.get(pos).getChatName());
					bundle.putString(AnalyticsConstants.Param.RECIPIENT_USER_ID, chats.get(pos).getChatId());
					firebaseAnalytics.logEvent(AnalyticsConstants.Event.HOME_SCROLL, bundle);
				}
			}
		});

	}

	@Override
	public void removeChatItem(String chatId) {
		chatListAdapter.removeChat(chatId);
	}

	@Override
	public void showChatState(String from, ChatState chatState) {
		if(chatState == ChatState.composing || chatState == ChatState.active) {
			chatListAdapter.setChatState(from, "Typing...");
			final Handler handler = new Handler();
			handler.postDelayed(() -> chatListAdapter.resetChatState(from), 10000);
		} else if(chatState == ChatState.gone || chatState == ChatState.inactive) {
			Logger.d(this, "ChatState: "+chatState.name());
			chatListAdapter.resetChatState(from);
		}
	}

	@Override
	public void onChatItemClicked(String userName) {
		/*              Analytics           */
		Bundle bundle = new Bundle();
		bundle.putString(AnalyticsConstants.Param.RECIPIENT_USER_NAME, userName);
		firebaseAnalytics.logEvent(AnalyticsConstants.Event.HOME_CHAT_OPEN, bundle);

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
			bundle.putString(AnalyticsConstants.Param.RECIPIENT_USER_NAME, username);
			presenter.deleteChat(username);
			chatActionsDialog.dismiss();
			firebaseAnalytics.logEvent(AnalyticsConstants.Event.HOME_CHAT_CLEAR, bundle);
		});

		LinearLayout llDelteChat = (LinearLayout)chatActionsView.findViewById(R.id.delete_chat);
		llDelteChat.setOnClickListener(v -> {
			Bundle bundle = new Bundle();
			bundle.putString(AnalyticsConstants.Param.RECIPIENT_USER_NAME, username);
			presenter.deleteChat(username);
			chatActionsDialog.dismiss();
			firebaseAnalytics.logEvent(AnalyticsConstants.Event.HOME_CHAT_DELETE, bundle);
		});

		/*              Analytics           */
		Bundle bundle = new Bundle();
		bundle.putString(AnalyticsConstants.Param.RECIPIENT_USER_NAME, username);
		firebaseAnalytics.logEvent(AnalyticsConstants.Event.HOME_CHAT_OPEN, bundle);
	}

	@Override
	public void onMessageReceived(MessageResult messageResult, ContactResult from) {
		Logger.d(this, "MessageReceived");
		messageResult.setName(from.getContactName());
		ChatItem item = new ChatItem(messageResult.getChatId(), messageResult.getName(), messageResult.getMessage(), messageResult.getTime(), messageResult.getMessageStatus(), messageResult.getReceiptId(), messageResult.getMessageId(), 1);
		item.setProfileDP(from.getProfileDP());
		this.chats = chatListAdapter.newChatMessage(item);
		chatList.scrollToPosition(0);
		NotificationController.getInstance().showNotificationAndAlert(true);
	}

	@Override
	public void onMessageStatusReceived(String messageId, String chatId, String deliveryReceiptId, MessageResult.MessageStatus messageStatus) {
		super.onMessageStatusReceived(messageId, chatId, deliveryReceiptId, messageStatus);
		Logger.d(this, "deliveryStatus: "+messageStatus.name());
		chatListAdapter.updateDeliveryStatus(messageId, deliveryReceiptId, messageStatus);
	}

	@Override
	public void onChatStateReceived(String from, ChatState chatState) {
		showChatState(from, chatState);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void onSyncSuccess() {
		Logger.d(this, "OnSyncSuccess");
		sharedPreferences.edit().putLong(KEY_LAST_SYNC, DateTime.now().getMillis()).apply();
	}
}