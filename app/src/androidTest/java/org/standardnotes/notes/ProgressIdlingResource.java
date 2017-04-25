package org.standardnotes.notes;

import android.support.test.espresso.IdlingResource;

public class ProgressIdlingResource implements IdlingResource {

    private IdlingResource.ResourceCallback resourceCallback;
    private AccountActivity accountActivity;
    private AccountActivity.ProgressListener progressListener;

    public ProgressIdlingResource(AccountActivity activity){
        accountActivity = activity;

        progressListener = new AccountActivity.ProgressListener() {

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

        accountActivity.setProgressListener(progressListener);
    }

    @Override
    public String getName() {
        return "My idling resource";
    }

    @Override
    public boolean isIdleNow() {
        return !accountActivity.isInProgress();
    }

    @Override
    public void registerIdleTransitionCallback(IdlingResource.ResourceCallback resourceCallback) {
        this.resourceCallback = resourceCallback;
    }

}
