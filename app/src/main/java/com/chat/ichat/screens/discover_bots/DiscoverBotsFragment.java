package com.chat.ichat.screens.discover_bots;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chat.ichat.R;
import com.chat.ichat.api.bot.DiscoverBotsResponse;
import com.chat.ichat.config.AnalyticsConstants;
import com.chat.ichat.core.BaseFragment;
import com.chat.ichat.core.Logger;
import com.chat.ichat.screens.discover_category.DiscoverCategoryActivity;
import com.chat.ichat.screens.message.MessageActivity;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by vidhun on 26/07/17.
 */

public class DiscoverBotsFragment extends BaseFragment implements  DiscoverBotsContract.View, DiscoverBotsAdapter.ActionListener {
    @Bind(R.id.rv_chat_list)
    RecyclerView recyclerView;


    DiscoverBotsPresenter discoverBotsPresenter;
    private FirebaseAnalytics firebaseAnalytics;
    final ProgressDialog[] progressDialog = new ProgressDialog[1];

    private boolean isInitLoad = true;

    public static DiscoverBotsFragment getInstance() {
        DiscoverBotsFragment fragment = new DiscoverBotsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        discoverBotsPresenter = new DiscoverBotsPresenter();
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        discoverBotsPresenter.attachView(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.firebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!isInitLoad) {
//            progressDialog[0] = new ProgressDialog(getActivity()) {
//                @Override
//                public void onBackPressed() {
//
//                }
//            };
//            progressDialog[0].setMessage("loading please wait...");
//            progressDialog[0].show();
        } else {
            isInitLoad = false;
        }
        discoverBotsPresenter.discoverBots();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void showError(String title, String message) {
        if(progressDialog[0]!=null && progressDialog[0].isShowing()) {
            progressDialog[0].dismiss();
        }

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage("\n"+message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    @Override
    public void displayBots(DiscoverBotsResponse discoverBotsResponse) {
        Logger.d(this, "DiscoverBotsResponse: "+discoverBotsResponse.toString());
        if(progressDialog[0]!=null && progressDialog[0].isShowing())
            progressDialog[0].dismiss();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        DiscoverBotsAdapter discoverBotsAdapter = new DiscoverBotsAdapter(getActivity(), discoverBotsResponse, this);
        recyclerView.setAdapter(discoverBotsAdapter);
    }

    @Override
    public void navigateToMessage(String username, String coverPicture, String description, String botCategory) {
        if(progressDialog[0]!=null && progressDialog[0].isShowing())
            progressDialog[0].dismiss();
        startActivity(MessageActivity.callingIntent(getActivity(), username, coverPicture, description, botCategory));
    }

    @Override
    public void onContactItemClicked(String userId, String coverPicture, String botDescription, String category) {
        progressDialog[0] = new ProgressDialog(getActivity()) {
            @Override
            public void onBackPressed() {

            }};
        progressDialog[0].setMessage("loading please wait...");
        progressDialog[0].show();
        /*              Analytics           */
        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsConstants.Param.RECIPIENT_USER_ID, userId);
        firebaseAnalytics.logEvent(AnalyticsConstants.Event.DISCOVER_BOTS_CHAT_OPEN, bundle);
        discoverBotsPresenter.openContact(userId, coverPicture, botDescription, category);
    }

    @Override
    public void navigateToDiscoverCategory(List<DiscoverBotsResponse.Bots> botses) {
        if(progressDialog[0]!=null && progressDialog[0].isShowing())
            progressDialog[0].dismiss();
        startActivity(DiscoverCategoryActivity.callingIntent(getActivity(), botses));
    }
}
