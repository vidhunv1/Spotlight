package com.chat.ichat.screens.message;

import com.chat.ichat.core.Logger;
import com.chat.ichat.db.ContactStore;

import rx.Observable;

/**
 * Created by vidhun on 08/01/17.
 */
public class GetNameUseCase {
    private ContactStore contactStore;

    public GetNameUseCase(ContactStore contactStore) {
        this.contactStore = contactStore;
    }

    public Observable<String> execute(String username){
        Logger.d(this, "getting name for:"+username);
        return Observable.create(subscriber -> {
        });
    }
}