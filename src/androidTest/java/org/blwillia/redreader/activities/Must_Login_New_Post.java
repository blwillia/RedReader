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
public class Must_Login_New_Post {

	@Rule
	public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

	@Test
	public void must_Login_New_Post() {
		try {
			//Dismiss the popup to Login
			onView(allOf(withId(android.R.id.button2), withText("Be anonymous"))).perform(click());
		} catch (NoMatchingViewException e){
			// View not in hierarchy, user already approved, skipping.
		}

		//On the Recycler View list select "art" at position 4
		onView(withId(R.id.scrollbar_recyclerview_recyclerview)).perform(actionOnItemAtPosition(4, click()));

		//Click Menu Button on top right
		openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

		//Select Submit Post
		onView(allOf(withId(R.id.title), withText("Submit Post"))).perform(click());

		//Check root view for toast message
		onView(withText("You must be logged in to do that.")).inRoot(withDecorView(not(is(mActivityTestRule.getActivity().getWindow().getDecorView())))).check(matches(isDisplayed()));
	}
}
