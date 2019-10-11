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
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class Reddit_Comment_Viewable {

	@Rule
	public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

	@Test
	public void reddit_Comment_Viewable() {
		try {
			//Dismiss the popup to Login
			onView(allOf(withId(android.R.id.button2), withText("Be anonymous"))).perform(click());
		} catch (NoMatchingViewException e){
			// View not in hierarchy, user already approved, skipping.
		}

		//On the Recycler View list select "ask reddit"
		onView(withId(R.id.scrollbar_recyclerview_recyclerview)).perform(actionOnItemAtPosition(6, click()));

		//Pick a post in the middle of the view
		onView(withId(R.id.scrollbar_recyclerview_recyclerview)).perform(actionOnItemAtPosition(4, click()));

		//Verify the comment page appears
		onView(withClassName(endsWith("RedditPostHeaderView"))).check(matches(isDisplayed()));
	}
}
