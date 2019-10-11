package org.blwillia.redreader.activities;


import androidx.annotation.NonNull;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.blwillia.redreader.R;

import java.util.ArrayList;
import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.core.internal.deps.guava.base.Preconditions.checkNotNull;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class Refresh_New {

	public static String textViewString = "";

	@Rule
	public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

	@Test
	public void refresh_New() throws InterruptedException {
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

		//Select to sort by "new"
		onView(allOf(withId(R.id.title), withText("New"))).perform(click());

		//Get first post and store the header in a static class string
		onView(withId(R.id.scrollbar_recyclerview_recyclerview)).check(matches(atPosition(1, storeViewTextInClassString())));

		//Wait 2seconds before refreshing list.  Espresso is too fast and 2 seconds is generally enough time.
		Thread.sleep(2000);

		//Click refresh
		onView(withContentDescription("Refresh Posts")).perform(click());

		//Get the updated first post and compare that it is different from the one 2 seconds ago
		onView(withId(R.id.scrollbar_recyclerview_recyclerview)).check(matches(atPosition(1, not(withText(textViewString)))));
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

	private static Matcher<View> storeViewTextInClassString(){
		return new TypeSafeMatcher<View>() {
			@Override
			protected boolean matchesSafely(View item) {
				List<View> allViews = getAllChildrenBFS(item);
				//Better way to do this is to pass in the id and do a query but this works for now.
				String itemText = ((AppCompatTextView)allViews.get(12)).getText().toString();
				textViewString = itemText;
				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("Stored text in class. This is a safe way to interact with ViewInteraction objects.");
			}
		};
	}

	private static List<View> getAllChildrenBFS(View v) {
		List<View> visited = new ArrayList<>();
		List<View> unvisited = new ArrayList<>();
		unvisited.add(v);

		while (!unvisited.isEmpty()) {
			View child = unvisited.remove(0);
			visited.add(child);
			if (!(child instanceof ViewGroup)) continue;
			ViewGroup group = (ViewGroup) child;
			final int childCount = group.getChildCount();
			for (int i=0; i<childCount; i++) unvisited.add(group.getChildAt(i));
		}

		return visited;
	}
}
