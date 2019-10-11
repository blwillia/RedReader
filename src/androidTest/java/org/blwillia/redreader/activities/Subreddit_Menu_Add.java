package org.blwillia.redreader.activities;


import androidx.annotation.NonNull;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import androidx.test.runner.AndroidJUnit4;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.blwillia.redreader.R;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.core.internal.deps.guava.base.Preconditions.checkNotNull;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.not;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class Subreddit_Menu_Add {

	@Rule
	public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

	@Test
	public void subreddit_Menu_Add() {
		try {
			//Dismiss the popup to Login
			onView(allOf(withId(android.R.id.button2), withText("Be anonymous"))).perform(click());
		} catch (NoMatchingViewException e){
			// View not in hierarchy, user already approved, skipping.
		}

		//Click Menu Button on top right
		openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

		//Select Settings
		onView(allOf(withId(R.id.title), withText("Settings"))).perform(click());

		//Select "Menu"
		onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(5).perform(click());

		//Click the shortcuts
		onView(withText("Main Menu Shortcuts")).perform(click());

		//Tick Random Subreddit
		onView(withText("Random Subreddit")).perform(click());

		//Hit the OK button
		onView(allOf(withId(android.R.id.button1), withText("OK"))).perform(click());

		//Go back
		onView(withContentDescription("Navigate up")).perform(click());

		//Go back
		onView(withContentDescription("Navigate up")).perform(click());

		//Verify Random Subreddit is displayed
		onView(withText("Random Subreddit")).check(matches(isDisplayed()));

		//Remove Random Subreddit

		//Click Menu Button on top right
		openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

		//Select Settings
		onView(allOf(withId(R.id.title), withText("Settings"))).perform(click());

		//Select "Menu"
		onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(5).perform(click());

		//Click the shortcuts
		onView(withText("Main Menu Shortcuts")).perform(click());

		//Tick Random Subreddit
		onView(withText("Random Subreddit")).perform(click());

		//Hit the OK button
		onView(allOf(withId(android.R.id.button1), withText("OK"))).perform(click());

		//Go back
		onView(withContentDescription("Navigate up")).perform(click());

		//Go back
		onView(withContentDescription("Navigate up")).perform(click());

		//On the Recycler View list verify text at position 2
		onView(withId(R.id.scrollbar_recyclerview_recyclerview)).check(matches(atPosition(3, not(withText("Random Subreddit")))));
	}


	public static Matcher<View> atPosition(final int position, @NonNull final Matcher<View> itemMatcher) {
		checkNotNull(itemMatcher);
		return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
			@Override
			public void describeTo(Description description) {
				description.appendText("has item at position " + position + ": ");
				itemMatcher.describeTo(description);
			}

			@Override
			protected boolean matchesSafely(final RecyclerView view) {
				RecyclerView.ViewHolder viewHolder = view.findViewHolderForAdapterPosition(position);
				if (viewHolder == null) {
					// has no item on such position
					return false;
				}
				return itemMatcher.matches(viewHolder.itemView);
			}
		};
	}
}
