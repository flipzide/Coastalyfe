package com.ondemandbay.taxianytime;

import android.content.BroadcastReceiver;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.ondemandbay.taxianytime.R;
import com.ondemandbay.taxianytime.fragments.RegisterFragment;
import com.ondemandbay.taxianytime.fragments.SignInFragment;
import com.ondemandbay.taxianytime.utils.AndyUtils;
import com.ondemandbay.taxianytime.utils.Const;
import com.ondemandbay.taxianytime.utils.PreferenceHelper;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;

/**
 * @author Elluminati elluminati.in
 */
public class RegisterActivity extends ActionBarBaseActivitiy {

    Permission[] permissions = new Permission[]{Permission.EMAIL};
    SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
            .setPermissions(permissions).build();
    public PreferenceHelper phelper;
    public SignInFragment signInFrag = new SignInFragment();
    public RegisterFragment regFrag = new RegisterFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        SimpleFacebook.setConfiguration(configuration);
        phelper = new PreferenceHelper(this);
        setIcon(R.drawable.back);

        if (getIntent().getBooleanExtra("isSignin", false)) {
            gotSignInFragment();
        } else {
            goToRegisterFragment();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnActionNotification:
                onBackPressed();
                break;

            default:
                break;
        }
    }

//    public void registerGcmReceiver(BroadcastReceiver mHandleMessageReceiver) {
//        if (mHandleMessageReceiver != null) {
//            AndyUtils.showCustomProgressDialog(this,
//                    getString(R.string.progress_loading), false, null);
//            new GCMRegisterHendler(RegisterActivity.this,
//                    mHandleMessageReceiver);
//
//        }
//    }
//
//    public void unregisterGcmReceiver(BroadcastReceiver mHandleMessageReceiver) {
//        if (mHandleMessageReceiver != null) {
//
//            if (mHandleMessageReceiver != null) {
//                unregisterReceiver(mHandleMessageReceiver);
//            }
//        }
//    }

    private void gotSignInFragment() {
        clearBackStack();
        addFragment(signInFrag, false, Const.FRAGMENT_SIGNIN);
    }

    private void goToRegisterFragment() {
        clearBackStack();
        addFragment(regFrag, false, Const.FRAGMENT_REGISTER);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.uberorg.ActionBarBaseActivitiy#isValidate()
     */
    @Override
    protected boolean isValidate() {
        return false;
    }

    public void showKeyboard(View v) {
        InputMethodManager inputManager = (InputMethodManager) this
                .getSystemService(INPUT_METHOD_SERVICE);

        // check if no view has focus:
        // View view = activity.getCurrentFocus();
        // if (view != null) {
        inputManager.showSoftInput(v, InputMethodManager.SHOW_FORCED);
        // }
    }

    private void requestRequiredPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                .READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission
                    .WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE}, Const
                    .PERMISSION_STORAGE_REQUEST_CODE);

            return;
        } else if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA
            }, Const.PERMISSION_CAMERA_REQUEST_CODE);

            return;
        } else {
            if (getIntent().getBooleanExtra("isSignin", false)) {
                gotSignInFragment();
            } else {
                goToRegisterFragment();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case Const.PERMISSION_STORAGE_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    regFrag.choosePhotoFromGallary();
                }

                break;
            case Const.PERMISSION_CAMERA_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    regFrag.takePhotoFromCamera();
                }
                break;

            case Const.PERMISSION_GET_ACCOUNT_REQUEST_CODE:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if(currentFragment.equals(Const.FRAGMENT_REGISTER)){
                            regFrag.checkPermissionForGoogleAccount();
                        }else {
                            signInFrag.checkPermissionForGoogleAccount();
                        }
                    }
                }
                break;
        }
    }
}