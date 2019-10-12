package org.blwillia.redreader.activities;


import androidx.test.espresso.NoMatchingViewException;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.blwillia.redreader.R;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class Subreddit_EmptyString_Invalid {

	@Rule
	public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

	@Test
	public void subreddit_EmptyString_Invalid() {
		try {
			//Dismiss the popup to Login
			onView(allOf(withId(android.R.id.button2), withText("Be anonymous"))).perform(click());
		} catch (NoMatchingViewException e){
			// View not in hierarchy, user already approved, skipping.
		}

		//On the Recycler View list select "Custom Location" at position 2
		onView(withId(R.id.scrollbar_recyclerview_recyclerview)).perform(actionOnItemAtPosition(2, click()));

		//On popup window Click the Go button
		onView(withId(android.R.id.button1)).perform(click());

		//Check root view for toast message
		onView(withText("Invalid subreddit name.")).inRoot(withDecorView(not(is(mActivityTestRule.getActivity().getWindow().getDecorView())))).check(matches(isDisplayed()));
	}
}
