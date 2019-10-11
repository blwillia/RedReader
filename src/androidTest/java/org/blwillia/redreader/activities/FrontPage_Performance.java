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
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.core.internal.deps.guava.base.Preconditions.checkState;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class FrontPage_Performance {

	@Rule
	public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

	@Test
	public void frontPage_Performance() {
		try {
			//Dismiss the popup to Login
			onView(allOf(withId(android.R.id.button2), withText("Be anonymous"))).perform(click());
		} catch (NoMatchingViewException e){
			// View not in hierarchy, user already approved, skipping.
		}

		//On the Recycler View list select "front page" at position 0
		onView(withId(R.id.scrollbar_recyclerview_recyclerview)).perform(actionOnItemAtPosition(0, click()));

		long start = System.nanoTime();

		//Scroll down a certain number of times
		for (int i = 0; i < 50; i++) {
			onView(withId(R.id.scrollbar_recyclerview_recyclerview)).perform(swipeUp());
		}

		long finish = System.nanoTime();
		long timeElapsed = (finish - start) / 1000000000; //nano seconds to seconds

		//Verify test did not exceed a pre-defined expected time
		checkState(timeElapsed < 30);
	}
}
