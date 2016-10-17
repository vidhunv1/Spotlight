package com.stairway.spotlight.screens.register.verifyotp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.stairway.data.manager.Logger;
import com.stairway.data.source.user.UserSessionResult;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseFragment;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.screens.home.HomeActivity;
import com.stairway.spotlight.screens.register.verifyotp.di.VerifyOtpViewModule;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * Created by vidhun on 22/07/16.
 */
public class VerifyOtpFragment extends BaseFragment implements VerifyOtpContract.View{

    @Bind(R.id.et_otp_otp)
    EditText otpEditText;

    @Bind(R.id.tv_otp_mobilenumber)
    TextView mobileNumberTextView;

    @Bind(R.id.btn_otp_continue)
    Button continueButton;

    @Inject
    VerifyOtpPresenter verifyOtpPresenter;

    ComponentContainer componentContainer;

    int OTP_LENGTH = 4;

    public VerifyOtpFragment() {
        return;
    }

    public static VerifyOtpFragment getInstance(String mobile, String countryCode) {
        Bundle bundle = new Bundle();
        bundle.putString("MOBILE", mobile);
        bundle.putString("COUNTRY_CODE", countryCode);

        VerifyOtpFragment verifyOtpFragment = new VerifyOtpFragment();
        verifyOtpFragment.setArguments(bundle);

        return verifyOtpFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_otp, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String mobile = getArguments().getString("MOBILE").toString();
        String countryCode = getArguments().getString("COUNTRY_CODE").toString();

        mobileNumberTextView.setText(countryCode+" "+mobile);
    }

    @Override
    public void onStart() {
        super.onStart();
        continueButton.setAlpha(0.2f);
    }

    @Override
    public void onResume() {
        super.onResume();
        verifyOtpPresenter.attachView(this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void navigateToHome(UserSessionResult userSessionResult) {
        Logger.d("Navigate to home");
        componentContainer.initUserSession(userSessionResult);
        startActivity(HomeActivity.callingIntent(getActivity()));
        getActivity().finish();
    }

    @Override
    public void invalidOtpError() {
        Toast.makeText(getActivity(), "Invalid OTP", Toast.LENGTH_LONG).show();
        otpEditText.setText("");
    }

    @OnTextChanged(R.id.et_otp_otp)
    public void onMobileTextChanged() {
        if(otpEditText.getText().toString().length()==OTP_LENGTH) {
            continueButton.setAlpha(1);
        } else {
            continueButton.setAlpha(.2f);
        }
    }

    @OnClick(R.id.btn_otp_continue)
    public void onContinueClicked() {
        if(otpEditText.getText().toString().length() == OTP_LENGTH) {
            verifyOtpPresenter.registerUser(getArguments().getString("COUNTRY_CODE").toString(), getArguments().getString("MOBILE").toString(), otpEditText.getText().toString());
        }
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
        this.componentContainer = componentContainer;
        componentContainer.getAppComponent().plus(new VerifyOtpViewModule(getContext())).inject(this);
    }
}
