package com.chat.ichat.screens.message.persistent_menu;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.GridView;

import com.chat.ichat.R;
import com.chat.ichat.api.bot.PersistentMenu;
import com.chat.ichat.core.Logger;
import com.chat.ichat.screens.message.audio.ComposerViewHelper;

import java.util.List;

/**
 * Created by vidhun on 27/06/17.
 */
public class PersistentMenuViewHelper {
    Context mContext;

    private ViewGroup pmLayout;
    private View pmPickerLayout;
    private Window window;

    private boolean isPMViewInflated = false;
    private boolean isNotPMState = true;
    private Listener listener;
    private List<PersistentMenu> persistentMenus;
    PersistentMenuAdapter persistentMenuAdapter;

    ComposerViewHelper composerViewHelper;

    public PersistentMenuViewHelper(Context mContext, List<PersistentMenu> persistentMenuList, final ViewGroup viewGroup, Window window, Listener listener) {
        this.mContext = mContext;
        this.pmLayout = viewGroup;
        this.window = window;
        this.pmPickerLayout = createCustomView();
        composerViewHelper = new ComposerViewHelper(mContext, viewGroup);
        this.persistentMenus = persistentMenuList;
        this.listener = listener;
        isNotPMState = true;
        persistentMenuAdapter = new PersistentMenuAdapter(mContext, persistentMenuList, new PersistentMenuAdapter.ClickListener() {
            @Override
            public void onPostBackClicked(String title, String payload) {
                listener.onPostbackClicked(title, payload);
            }

            @Override
            public void onUrlClicked(String url) {
                listener.onUrlClicked(url);
            }
        });
    }

    public void addPMView() {
        isNotPMState = false;
        Activity currentActivity = (Activity) mContext;
        View view = currentActivity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        if(!isPMViewInflated) {
            this.isPMViewInflated = true;
            ViewGroup.LayoutParams layoutParams = pmLayout.getLayoutParams();
            Logger.d(this, "adding smiley view, Setting height: "+composerViewHelper.getLayoutHeightpx());
            layoutParams.height = composerViewHelper.getLayoutHeightpx();
            pmLayout.setLayoutParams(layoutParams);
            pmLayout.addView(pmPickerLayout);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
            GridView pmGrid = (GridView) pmPickerLayout.findViewById(R.id.grid_pm);
            pmGrid.setAdapter(persistentMenuAdapter);
        }
    }

    public void removePMPickerView() {
        isNotPMState = true;
        if(isPMViewInflated) {
            Log.d("DEF", "DEFLATING VIEW");
            isPMViewInflated = false;
            ViewGroup.LayoutParams layoutParams = pmLayout.getLayoutParams();
            layoutParams.height = 0;
            pmLayout.setLayoutParams(layoutParams);
            pmLayout.removeAllViews();
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    public void handleBackPress() {
        if(!isNotPMState()) {
            reset();
        } else {
            removePMPickerView();
        }
    }

    public void pmButtonToggle() {
        Activity currentActivity = (Activity) mContext;
        View view = currentActivity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        if(isNotPMState) {
            addPMView();
            isNotPMState = false;
        } else {
            isNotPMState = true;
        }
    }

    public boolean isNotPMState() {
        return isNotPMState;
    }

    public void reset() {
        isNotPMState = true;
        removePMPickerView();
    }

    public void hide() {
        isNotPMState = true;
    }

    private View createCustomView() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.pm_layout, null, false);
    }

    public interface Listener{
        void onPostbackClicked(String title, String payload);
        void onUrlClicked(String url);
    }
}
