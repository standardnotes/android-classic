package org.standardnotes.notes;

import android.support.test.espresso.IdlingResource;

public class ProgressIdlingResource implements IdlingResource {

    private IdlingResource.ResourceCallback resourceCallback;
    private LoginActivity loginActivity;
    private LoginActivity.ProgressListener progressListener;

    public ProgressIdlingResource(LoginActivity activity){
        loginActivity = activity;

        progressListener = new LoginActivity.ProgressListener() {

            @Override
            public void onProgressShown() {

            }

            @Override
            public void onProgressDismissed() {
                if (resourceCallback == null){
                    return ;
                }
                resourceCallback.onTransitionToIdle();
            }
        };

        loginActivity.setProgressListener(progressListener);
    }

    @Override
    public String getName() {
        return "My idling resource";
    }

    @Override
    public boolean isIdleNow() {
        return !loginActivity.isInProgress();
    }

    @Override
    public void registerIdleTransitionCallback(IdlingResource.ResourceCallback resourceCallback) {
        this.resourceCallback = resourceCallback;
    }

}
