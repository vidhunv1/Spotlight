package com.stairway.spotlight.screens.register;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.BaseFragment;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.screens.register.signup.SignUpFragment;

import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity implements BaseFragment.BackHandlerInterface{

    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, RegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.register_FragmentContainer, SignUpFragment.getInstance());
        fragmentTransaction.commit();
    }

    @Override
    public void setSelectedFragment(BaseFragment backHandledFragment) {

    }

    @Override
    public void removeSelectedFragment(BaseFragment backHandledFragment) {

    }
}
