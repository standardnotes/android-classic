package org.standardnotes.notes;

import android.app.Activity;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class TestHelper {

    public static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    public static boolean isScreenSw600dpAndLandscape(Activity activity) {
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        float widthDp = displayMetrics.widthPixels / displayMetrics.density;
        return widthDp >= 600 && activity.getResources().getConfiguration()
                .orientation == Configuration.ORIENTATION_LANDSCAPE;
    }
}
