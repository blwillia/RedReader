package org.blwillia.redreader.activities;


import androidx.test.espresso.NoMatchingViewException;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.blwillia.redreader.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class Subreddit_Sort {

	@Rule
	public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

	@Test
	public void subreddit_Sort() {
		try {
			//Dismiss the popup to Login
			onView(allOf(withId(android.R.id.button2), withText("Be anonymous"))).perform(click());
		} catch (NoMatchingViewException e){
			// View not in hierarchy, user already approved, skipping.
		}

		//On the Recycler View list select "front page" at position 0
		onView(withId(R.id.scrollbar_recyclerview_recyclerview)).perform(actionOnItemAtPosition(0, click()));

		//Click on the sorting icon
		onView(withContentDescription("Sort Posts")).perform(click());

		//Select to sort by "hot"
		onView(allOf(withId(R.id.title), withText("Hot"))).perform(click());

		//Check header sub-text for sort order
		onView(withText("reddit.com/hot")).check(matches(isDisplayed()));

		//Click on the sorting icon
		onView(withContentDescription("Sort Posts")).perform(click());

		//Select to sort by "rising"
		onView(allOf(withId(R.id.title), withText("Rising"))).perform(click());

		//Check header sub-text for sort order
		onView(withText("reddit.com/rising")).check(matches(isDisplayed()));

		//Click on the sorting icon
		onView(withContentDescription("Sort Posts")).perform(click());

		//Select to sort by "new"
		onView(allOf(withId(R.id.title), withText("New"))).perform(click());

		//Check header sub-text for sort order
		onView(withText("reddit.com/new")).check(matches(isDisplayed()));
	}
}
