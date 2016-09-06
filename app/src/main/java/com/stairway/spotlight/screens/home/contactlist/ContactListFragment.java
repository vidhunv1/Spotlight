package com.stairway.spotlight.screens.home.contactlist;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stairway.data.manager.Logger;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseFragment;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.screens.home.contactlist.di.ContactListViewModule;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by vidhun on 01/09/16.
 */
public class ContactListFragment extends BaseFragment implements ContactListContract.View, ContactListAdapter.ContactClickListener{
    @Bind(R.id.rv_contact_list)
    RecyclerView contactList;

    @Inject
    ContactListPresenter contactListPresenter;

    public ContactListFragment() {
    }

    public static ContactListFragment getInstance() {
        ContactListFragment contactListFragment = new ContactListFragment();
        return contactListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_list, container, false);
        ButterKnife.bind(this, view);

        Logger.v("[ContactListFragment] onCreateView");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        contactList.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onResume() {
        super.onResume();
        contactListPresenter.attachView(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        contactListPresenter.detachView();
    }

    @Override
    public void displayContactList(ArrayList<ContactListItemModel> contactListItemModels) {
        contactList.setAdapter(new ContactListAdapter(getActivity(), contactListItemModels, this));
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
        componentContainer.userSessionComponent().plus(new ContactListViewModule()).inject(this);
    }

    @Override
    public void onContactItemClicked(String userId) {

    }
}
