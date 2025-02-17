package com.android.client;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

public class UnityPlayerActivity extends com.unity3d.player.UnityPlayerActivity {


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        AndroidSdk.displayInNotch(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        AndroidSdk.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AndroidSdk.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AndroidSdk.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        AndroidSdk.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AndroidSdk.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        AndroidSdk.onNewIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        AndroidSdk.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        AndroidSdk.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }






}
