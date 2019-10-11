package org.blwillia.redreader.activities;


import androidx.test.espresso.NoMatchingViewException;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.blwillia.redreader.R;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class Subreddit_Pinned_Unpinned {

	@Rule
	public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

	@Test
	public void subreddit_pinned() {
		try {
			//Dismiss the popup to Login
			onView(allOf(withId(android.R.id.button2), withText("Be anonymous"))).perform(click());
		} catch (NoMatchingViewException e){
			// View not in hierarchy, user already approved, skipping.
		}

		//On the Recycler View list select "Custom Location" at position 2
		onView(withId(R.id.scrollbar_recyclerview_recyclerview)).perform(actionOnItemAtPosition(2, click()));

		//On popup window insert text "abc"
		onView(allOf(withId(R.id.dialog_mainmenu_custom_value), isDisplayed())).perform(replaceText("abc"));

		//Click the Go button
		onView(withId(android.R.id.button1)).perform(click());

		//Click Menu Button on top right
		openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

		//Select Pin to Main Menu
		onView(allOf(withId(R.id.title), withText("Pin to Main Menu"))).perform(click());

		//Go back to the main list of subreddits
		onView(withId(R.id.actionbar_title_back_image)).perform(click());

		//Assert
		//Verify the new subreddit is pinned to the page
		onView(allOf(withId(R.id.list_item_text), withText("/r/abc"))).check(matches(isDisplayed()));

		//Start performing Unpin

		//On the Recycler View list select "Custom Location" at position 2
		onView(withId(R.id.scrollbar_recyclerview_recyclerview)).perform(actionOnItemAtPosition(2, click()));

		//On popup window insert text "abc"
		onView(allOf(withId(R.id.dialog_mainmenu_custom_value), isDisplayed())).perform(replaceText("abc"));

		//Dismiss the recent text popup by having espresso attempt to perform a click
		onView(withId(android.R.id.button1)).perform(click());
		//Actually click the button this time
		onView(withId(android.R.id.button1)).perform(click());

		//Click Menu Button on top right
		openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

		//Select Unpin from Main Menu
		onView(allOf(withId(R.id.title), withText("Unpin from Main Menu"))).perform(click());

		//Go back to the main list of subreddits
		onView(withId(R.id.actionbar_title_back_image)).perform(click());
	}
}
