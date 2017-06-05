package com.chat.ichat.screens.invite_friends;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chat.ichat.R;
import com.chat.ichat.models.ContactResult;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
/**
 * Created by vidhun on 03/06/17.
 */
public class InviteFriendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ClickListener clickListener;
    private List<ContactResult> contacts;
    private List<Boolean> checked;
    public InviteFriendsAdapter(Context context, List<ContactResult> contactResults, ClickListener clickListener) {
        this.context = context;
        this.clickListener = clickListener;
        this.contacts = contactResults;
        this.checked = new ArrayList<>();
        for (int i = 0; i < contacts.size(); i++) {
            checked.add(false);
        }
    }

    public void setAllSelected(boolean isSelected) {
        for (int i = 0; i < checked.size(); i++) {
            checked.set(i, isSelected);
        }
        this.notifyDataSetChanged();
    }

    public List<ContactResult> getSelected() {
        List<ContactResult> c = new ArrayList<>();
        for (int i = 0; i < checked.size(); i++)
            if(checked.get(i))
                c.add(contacts.get(i));
        return c;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View inviteContacts = inflater.inflate(R.layout.item_invite_contact_check, parent, false);
        viewHolder = new InviteContactsViewHolder(inviteContacts);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((InviteContactsViewHolder) holder).renderItem(contacts.get(position).getContactName(), contacts.get(position).getCountryCode()+contacts.get(position).getPhoneNumber(), checked.get(position), position);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    class InviteContactsViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_chatItem_contactName)
        TextView name;
        @Bind(R.id.tv_chatItem_number)
        TextView number;
        @Bind(R.id.checkbox)
        CheckBox checkbox;
        @Bind(R.id.layout)
        LinearLayout layout;

        InviteContactsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
        void renderItem(String contactName, String phone, boolean isChecked, int position) {
            name.setText(contactName);
            number.setText(phone);
            checkbox.setChecked(isChecked);

            checkbox.setOnCheckedChangeListener((buttonView, isChecked1) -> {
                checked.set(position, isChecked1);
                clickListener.onCheckedChange(number.getText().toString(), name.getText().toString());
            });

            this.setIsRecyclable(false);
        }
    }

    interface ClickListener {
        void onCheckedChange(String phone, String countryCode);
    }
}
