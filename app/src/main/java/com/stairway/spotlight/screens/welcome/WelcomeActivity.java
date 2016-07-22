package com.stairway.spotlight.screens.welcome;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.screens.register.RegisterActivity;
import com.stairway.spotlight.screens.welcome.di.WelcomeModule;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WelcomeActivity extends BaseActivity implements WelcomeContract.View {

    @Inject WelcomePresenter welcomePresenter;

    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, WelcomeActivity.class);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);
        welcomePresenter.attachView(this);
    }

    @OnClick(R.id.btn_welcome_signup)
    public void signUpClicked() {
        startActivity(RegisterActivity.callingIntent(this));
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
        componentContainer.getAppComponent().plus(new WelcomeModule()).inject(this);
    }
}
