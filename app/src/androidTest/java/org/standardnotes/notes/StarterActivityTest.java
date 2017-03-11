package org.standardnotes.notes;


import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.not;
import static org.standardnotes.notes.TestHelper.childAtPosition;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class StarterActivityTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    static String email = UUID.randomUUID().toString();
    static boolean signedup = false;

    @Before
    public void signupin() {
        mActivityTestRule.launchActivity(new Intent());
        IdlingResource idlingResource = new ProgressIdlingResource(mActivityTestRule.getActivity());
        Espresso.registerIdlingResources(idlingResource);

        if (!SApplication.Companion.getInstance().getValueStore().getServer().contains("staging")) {
            throw new RuntimeException("These tests add lots of test users - don't run against a live server.");
        }

        ViewInteraction appCompatAutoCompleteTextView = onView(
                withId(R.id.email));
        appCompatAutoCompleteTextView.perform(scrollTo(), click());

        ViewInteraction appCompatAutoCompleteTextView3 = onView(
                withId(R.id.email));
        appCompatAutoCompleteTextView3.perform(scrollTo(), replaceText(email), closeSoftKeyboard());

        ViewInteraction appCompatEditText = onView(
                withId(R.id.password));
        appCompatEditText.perform(scrollTo(), replaceText("aaa"), closeSoftKeyboard());

        if (!signedup) {
            ViewInteraction appCompatButton = onView(
                    allOf(withText("Register"),
                            withParent(allOf(withId(R.id.email_login_form),
                                    withParent(withId(R.id.login_form))))));
            appCompatButton.perform(scrollTo(), click());

            ViewInteraction appCompatEditText2 = onView(
                    allOf(withId(R.id.confirm_password), isDisplayed()));
            appCompatEditText2.perform(replaceText("aaa"), closeSoftKeyboard());

            ViewInteraction appCompatButton2 = onView(
                    allOf(withId(android.R.id.button1), withText("OK")));
            appCompatButton2.perform(scrollTo(), click());
            signedup = true;
        } else {
            onView(
                    allOf(withText("Sign in"))).perform(click());
        }

        Espresso.unregisterIdlingResources(idlingResource);
    }

    @After
    public void logout() {
        ViewInteraction actionMenuItemView = onView(allOf(withId(R.id.settings), isDisplayed()));
        actionMenuItemView.perform(click());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.logout), withText("Log out")));
        appCompatButton.perform(scrollTo(), click());
    }

    @Test
    public void createNote() {
        ViewInteraction newNoteButton = onView(anyOf(withId(R.id.fab_new_note), withId(R.id.new_note)));
        newNoteButton.perform(click());

        ViewInteraction appCompatEditText = onView(
                withId(R.id.titleEdit));
        appCompatEditText.perform(scrollTo(), click());

        ViewInteraction appCompatEditText2 = onView(
                withId(R.id.titleEdit));
        appCompatEditText2.perform(scrollTo(), replaceText("Title1"), closeSoftKeyboard());

        ViewInteraction appCompatEditText14 = onView(
                allOf(withId(R.id.bodyEdit)));
        appCompatEditText14.perform(scrollTo(), replaceText("body1"), closeSoftKeyboard());

        if (!TestHelper.isScreenSw600dpAndLandscape(mActivityTestRule.getActivity())) {
            pressBack();
        }

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.list_note), isDisplayed()));
        recyclerView.perform(actionOnItemAtPosition(0, click()));

        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.tags), withContentDescription("Tags"), isDisplayed()));
        actionMenuItemView.perform(click());

        pressBack();

        appCompatEditText14.perform(scrollTo(), replaceText("body1a"), closeSoftKeyboard());

        if (!TestHelper.isScreenSw600dpAndLandscape(mActivityTestRule.getActivity())) {
            pressBack();
        } else {
            // Not sure why adding this delay works, not a good solution.
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        ViewInteraction textView = onView(
                allOf(withId(R.id.text), withText("body1a"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.list_note),
                                        0),
                                1),
                        isDisplayed()));
        textView.check(matches(withText("body1a")));

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.title), withText("Title1"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.list_note),
                                        0),
                                0),
                        isDisplayed()));
        textView2.check(matches(withText("Title1")));

        logout();
        signupin();

        textView2.check(matches(withText("Title1")));
        textView.check(matches(withText("body1a")));
    }

    @Test
    public void justSignInAndOut() {
    }

    @Test
    public void tagSomething() {
        ViewInteraction newNoteButton = onView(anyOf(withId(R.id.fab_new_note), withId(R.id.new_note)));
        newNoteButton.perform(click());

        ViewInteraction appCompatEditText = onView(
                withId(R.id.titleEdit));
        appCompatEditText.perform(scrollTo(), click());

        ViewInteraction appCompatEditText2 = onView(
                withId(R.id.titleEdit));
        appCompatEditText2.perform(scrollTo(), replaceText("Title2"), closeSoftKeyboard());

        ViewInteraction appCompatEditText14 = onView(
                allOf(withId(R.id.bodyEdit)));
        appCompatEditText14.perform(scrollTo(), replaceText("body2"), closeSoftKeyboard());

        ViewInteraction tagsAction = onView(
                allOf(withId(R.id.tags), withContentDescription("Tags"), isDisplayed()));
        tagsAction.perform(click());
        onView(withId(R.id.fab_new_tag)).perform(click());
        onView(
                allOf(withId(R.id.tag), isDisplayed())).perform(replaceText("tag1"), closeSoftKeyboard());
        onView(
                allOf(withId(android.R.id.button1), withText("OK"))).perform(scrollTo(), click());
        onView(withId(R.id.fab_new_tag)).perform(click());
        onView(
                allOf(withId(R.id.tag), isDisplayed())).perform(replaceText("tag2"), closeSoftKeyboard());
        onView(
                allOf(withId(android.R.id.button1), withText("OK"))).perform(scrollTo(), click());
        onView(
                withText("tag1")).perform(click());

        pressBack();

        if (!TestHelper.isScreenSw600dpAndLandscape(mActivityTestRule.getActivity())) {
            pressBack();
        }

        logout();
        signupin();

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.list_note), isDisplayed()));
        recyclerView.perform(actionOnItemAtPosition(0, click()));
        onView(allOf(withText("tag1"), withId(R.id.tagText))).check(matches(isDisplayed()));

        tagsAction.perform(click());
        onView(
                withText("tag1")).perform(click()); // deselect
        onView(
                withText("tag2")).perform(click()); // select
        pressBack();
        onView(allOf(withText("tag2"), withId(R.id.tagText))).check(matches(isDisplayed()));
        onView(withText("tag1")).check(matches(not(isDisplayed())));

        if (!TestHelper.isScreenSw600dpAndLandscape(mActivityTestRule.getActivity())) {
            pressBack();
        }

        logout();
        signupin();

        recyclerView.perform(actionOnItemAtPosition(0, click()));
        onView(allOf(withText("tag2"), withId(R.id.tagText))).check(matches(isDisplayed()));
        onView(withText("tag1")).check(matches(not(isDisplayed())));

        if (!TestHelper.isScreenSw600dpAndLandscape(mActivityTestRule.getActivity())) {
            pressBack();
        }
    }

    @Test
    public void openCloseOpenClose() {
        ViewInteraction newNoteButton = onView(anyOf(withId(R.id.fab_new_note), withId(R.id.new_note)));
        newNoteButton.perform(click());

        ViewInteraction appCompatEditText = onView(
                withId(R.id.titleEdit));
        appCompatEditText.perform(scrollTo(), click());

        ViewInteraction appCompatEditText2 = onView(
                withId(R.id.titleEdit));
        appCompatEditText2.perform(scrollTo(), replaceText("Title2"), closeSoftKeyboard());

        ViewInteraction appCompatEditText14 = onView(
                allOf(withId(R.id.bodyEdit)));
        appCompatEditText14.perform(scrollTo(), replaceText("body2"), closeSoftKeyboard());

        if (!TestHelper.isScreenSw600dpAndLandscape(mActivityTestRule.getActivity())) {
            pressBack();
        }

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.list_note), isDisplayed()));

        int i = 0;
        do {
            recyclerView.perform(actionOnItemAtPosition(0, click()));
            if (!TestHelper.isScreenSw600dpAndLandscape(mActivityTestRule.getActivity())) {
                pressBack();
            }
            i++;
        } while (i < 10);
    }
}
