package com.chat.ichat.screens.message.gif;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.chat.ichat.R;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.application.SpotlightApplication;
import com.chat.ichat.core.Logger;
import com.chat.ichat.core.lib.AndroidUtils;
import com.chat.ichat.screens.message.MessageContract;
import com.chat.ichat.screens.message.MessageEditText;
import com.chat.ichat.screens.message.audio.ComposerViewHelper;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
/**
 * Created by vidhun on 22/05/17.
 */
public class GifViewHelper{
    Context mContext;

    private ViewGroup gifLayout;
    private View gifPickerLayout;
    private Window window;

    private boolean isGifViewInflated = false;
    private boolean isGifState = true;

    private ComposerViewHelper composerViewHelper;
    private SharedPreferences sharedPreferences;
    private RelativeLayout fullGifLayout;
    private ImageView fullGifImage;
    private ImageView back;
    private ProgressBar progressBar;
    private MessageEditText search;
    private ImageButton trendingButton;
    private ImageButton categoryButton;
    private View gifsTab;
    private GifApi gifApi;
    private boolean isSearchFocused = false;

    private GridView gifsGrid;
    private TextView error;
    public GifViewHelper(Context mContext, final ViewGroup viewGroup, Window window) {
        this.mContext = mContext;
        this.gifLayout = viewGroup;
        this.window = window;
        this.gifPickerLayout = createCustomView();
        composerViewHelper = new ComposerViewHelper(mContext, viewGroup);
        this.sharedPreferences = SpotlightApplication.getContext().getSharedPreferences("gif_preferences", Context.MODE_PRIVATE);
        this.gifApi = ApiManager.getRetrofitClient().create(GifApi.class);
    }

    private void addGifView() {
        if(!isGifViewInflated) {
            this.isGifViewInflated = true;
            ViewGroup.LayoutParams layoutParams = gifLayout.getLayoutParams();
            layoutParams.height = composerViewHelper.getLayoutHeightpx();
            gifLayout.setLayoutParams(layoutParams);
            gifLayout.addView(gifPickerLayout);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

            gifsGrid = (GridView) gifLayout.findViewById(R.id.grid_gifs);
            error = (TextView) gifLayout.findViewById(R.id.error);
            fullGifLayout = (RelativeLayout) gifLayout.findViewById(R.id.full_gif_view);
            fullGifImage = (ImageView) gifLayout.findViewById(R.id.iv_full_gif);
            back = (ImageView) gifLayout.findViewById(R.id.back);
            search = (MessageEditText) gifLayout.findViewById(R.id.gif_search);
            gifsTab = gifLayout.findViewById(R.id.gifs_tab);
            progressBar = (ProgressBar) gifLayout.findViewById(R.id.progress);
            progressBar.setVisibility(View.VISIBLE);

            back.setOnClickListener(v -> {
                fullGifLayout.setVisibility(View.GONE);
            });

            fullGifLayout.setVisibility(View.GONE);
            error.setVisibility(View.GONE);

            trendingButton = (ImageButton) gifLayout.findViewById(R.id.trending);
            categoryButton = (ImageButton) gifLayout.findViewById(R.id.category);

            int lastSelected = sharedPreferences.getInt("last_selected", 0);

            if(lastSelected == 1) {
                trendingButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_trending_up));
                categoryButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_view_compact_selected));
                setCategoryGif();
            } else {
                trendingButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_trending_up_selected));
                categoryButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_view_compact));
                setTrendingGifs();
            }

            trendingButton.setOnClickListener(v -> {
                trendingButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_trending_up_selected));
                categoryButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_view_compact));
                sharedPreferences.edit().putInt("last_selected", 0).apply();
                setTrendingGifs();
            });

            categoryButton.setOnClickListener(v -> {
                trendingButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_trending_up));
                categoryButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_view_compact_selected));
                sharedPreferences.edit().putInt("last_selected", 1).apply();
                setCategoryGif();
            });

            search.setText("");

            search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus) {
                        isSearchFocused = true;
                        refreshView();
                    }
                }
            });

            search.setOnEditTextImeBackListener(() -> {
                if(isSearchFocused) {
                    isSearchFocused = false;
                } else {
                    removeGifPickerView();
                }
            });

            search.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    search(s);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
    }

    public void refreshView() {
        if(!isGifViewInflated) {
            Logger.d(this, "!isGifViewInflated");
            addGifView();
        } else if(isSearchFocused) {
            Logger.d(this, "isSearchFocused");
            ViewGroup.LayoutParams layoutParams = gifLayout.getLayoutParams();
            layoutParams.height = composerViewHelper.getLayoutHeightpx() + (int)AndroidUtils.px(125);
            gifLayout.setLayoutParams(layoutParams);
            gifLayout.requestFocus();
        } else {
            Logger.d(this, "else");
            ViewGroup.LayoutParams layoutParams = gifLayout.getLayoutParams();
            layoutParams.height = composerViewHelper.getLayoutHeightpx();
            gifLayout.setLayoutParams(layoutParams);
        }
    }

    public void setCategoryGif() {
        gifsGrid.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        gifApi.getTenorTags()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<TenorTagsResponse>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        error.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        error.setText("Check your phone's Internet connection and try again.");
                    }

                    @Override
                    public void onNext(TenorTagsResponse tenorTagsResponse) {
                        gifsGrid.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        GifCategoryAdapter g = new GifCategoryAdapter(mContext, tenorTagsResponse, searchQuery -> {
                            search.setText("");
                            search.append(searchQuery);
                            search(searchQuery);
                        });
                        gifsGrid.setAdapter(g);
                    }
                });

    }

    public void setTrendingGifs() {
        gifsGrid.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        gifApi.getTrendingGifs()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<GiphyGifResponse>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        error.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        error.setText("Check your phone's Internet connection and try again.");
                    }

                    @Override
                    public void onNext(GiphyGifResponse giphyGifResponse) {
                        gifsGrid.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        GiphyGifAdapter g = new GiphyGifAdapter(mContext, giphyGifResponse, id -> {
                            for (GiphyGifResponse.Data data : giphyGifResponse.getData()) {
                                if(data.getId().equals(id)) {
                                    fullGifLayout.setVisibility(View.VISIBLE);
                                    Glide.with(mContext).load(data.getHighGifUrl().replace("https://", "http://"))
                                            .bitmapTransform(new FitCenter(mContext))
                                            .placeholder(0xFF000000)
                                            .crossFade()
                                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                            .into(fullGifImage);
                                }
                            }
                        });
                        gifsGrid.setAdapter(g);
                    }
                });
    }

    public void search(CharSequence s) {
        if(s.length()>0) {
            gifsTab.setVisibility(View.GONE);
            gifsGrid.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            gifApi.getTenorGifs(s)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<TenorGifResponse>() {
                        @Override
                        public void onCompleted() {}

                        @Override
                        public void onError(Throwable e) {
                            error.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            error.setText("Check your phone's Internet connection and try again.");
                        }

                        @Override
                        public void onNext(TenorGifResponse tenorGifResponse) {
                            gifsGrid.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            gifsTab.setVisibility(View.VISIBLE);
                            TenorGifAdapter tenorGifAdapter = new TenorGifAdapter(mContext, tenorGifResponse, position -> {
                                fullGifLayout.setVisibility(View.VISIBLE);
                                Glide.with(mContext).load(tenorGifResponse.getResults().get(position).getGif().getUrl().replace("https://", "http://"))
                                        .bitmapTransform(new FitCenter(mContext))
                                        .placeholder(0xFF000000)
                                        .crossFade()
                                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                        .into(fullGifImage);
                            });
                            gifsGrid.setAdapter(tenorGifAdapter);
                        }
                    });
        } else {
            gifsTab.setVisibility(View.VISIBLE);
            int lastSelected = sharedPreferences.getInt("last_selected", 0);
            if(lastSelected == 1) {
                trendingButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_trending_up));
                categoryButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_view_compact_selected));
                setCategoryGif();
            } else {
                trendingButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_trending_up_selected));
                categoryButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_view_compact));
                setTrendingGifs();
            }
        }
    }

    public void removeGifPickerView() {
        if(isGifViewInflated) {
            isGifViewInflated = false;
            ViewGroup.LayoutParams layoutParams = gifLayout.getLayoutParams();
            layoutParams.height = 0;
            gifLayout.setLayoutParams(layoutParams);
            gifLayout.removeAllViews();
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    public void GifButtonToggle() {
        Activity currentActivity = (Activity) mContext;
        View view = currentActivity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        if(isGifState) {
            addGifView();
            isGifState = false;
        } else {
            isSearchFocused = false;
            refreshView();
            isGifState = true;
        }
    }

    public boolean isGifState() {
        return isGifState;
    }

    public void reset() {
        isGifState = true;
        removeGifPickerView();
    }

    private View createCustomView() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.gif_layout, null, false);
    }
}