package org.blwillia.redreader.activities;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import androidx.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
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
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class Theme_Color_Changed {

	@Rule
	public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

	@Test
	public void theme_Color_Changed() {
		try {
			//Dismiss the popup to Login
			onView(allOf(withId(android.R.id.button2), withText("Be anonymous"))).perform(click());
		} catch (NoMatchingViewException e){
			// View not in hierarchy, user already approved, skipping.
		}

		//Click Menu Button on top right
		openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

		//Select Themes
		onView(allOf(withId(R.id.title), withText("Themes"))).perform(click());

		//Pick the "Blue" selection
		onData(anything())
				.inAdapterView(allOf(
						withClassName(is("com.android.internal.app.AlertController$RecycleListView")),
							childAtPosition(withClassName(is("android.widget.FrameLayout")),1)))
				.atPosition(4).perform(click());

		//Assert
		//Verify top bar background is the blue color.
		onView(withId(R.id.rr_actionbar_toolbar)).check(matches(checkBackgroundColor(Color.parseColor("#126291"))));

		//In the case that the app may be retaining the blue color between rounds of testing, do another change to revert it back to default color "Red"

		//Click Menu Button on top right
		openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

		//Select Themes
		onView(allOf(withId(R.id.title), withText("Themes"))).perform(click());

		//Pick the "Red" selection
		onData(anything())
				.inAdapterView(allOf(
						withClassName(is("com.android.internal.app.AlertController$RecycleListView")),
						childAtPosition(withClassName(is("android.widget.FrameLayout")),1)))
				.atPosition(0).perform(click());

		//Assert
		//Verify top bar background is the red color.
		onView(withId(R.id.rr_actionbar_toolbar)).check(matches(checkBackgroundColor(Color.parseColor("#d32f2f"))));
	}

	private static Matcher<View> checkBackgroundColor(final int color){
		return new TypeSafeMatcher<View>() {
			@Override
			protected boolean matchesSafely(View item) {
				int itemColor = Color.TRANSPARENT;
				Drawable background = item.getBackground();
				if (background instanceof ColorDrawable)
					itemColor = ((ColorDrawable) background).getColor();
				return itemColor == color;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("With background color: ");
			}
		};
	}

	private static Matcher<View> childAtPosition(final Matcher<View> parentMatcher, final int position) {

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
}
