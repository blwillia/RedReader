/*******************************************************************************
 * This file is part of RedReader.
 *
 * RedReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RedReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RedReader.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.blwillia.redreader.reddit.api;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import org.blwillia.redreader.account.RedditAccount;
import org.blwillia.redreader.activities.BugReportActivity;
import org.blwillia.redreader.cache.CacheManager;
import org.blwillia.redreader.cache.CacheRequest;
import org.blwillia.redreader.common.AndroidCommon;
import org.blwillia.redreader.common.General;
import org.blwillia.redreader.common.RRError;
import org.blwillia.redreader.common.TimestampBound;
import org.blwillia.redreader.common.UnexpectedInternalStateException;
import org.blwillia.redreader.common.collections.WeakReferenceListManager;
import org.blwillia.redreader.io.RawObjectDB;
import org.blwillia.redreader.io.RequestResponseHandler;
import org.blwillia.redreader.io.WritableHashSet;
import org.blwillia.redreader.reddit.APIResponseHandler;
import org.blwillia.redreader.reddit.RedditAPI;
import org.blwillia.redreader.reddit.RedditSubredditHistory;
import org.blwillia.redreader.reddit.RedditSubredditManager;
import org.blwillia.redreader.reddit.things.RedditSubreddit;

import java.util.ArrayList;
import java.util.HashSet;

public class RedditSubredditSubscriptionManager {

	private static final String TAG = "SubscriptionManager";

	public enum SubredditSubscriptionState { SUBSCRIBED, SUBSCRIBING, UNSUBSCRIBING, NOT_SUBSCRIBED }

	private final SubredditSubscriptionStateChangeNotifier notifier = new SubredditSubscriptionStateChangeNotifier();
	private final WeakReferenceListManager<SubredditSubscriptionStateChangeListener> listeners
			= new WeakReferenceListManager<>();

	private static RedditSubredditSubscriptionManager singleton;
	private static RedditAccount singletonAccount;

	private final RedditAccount user;
	private final Context context;

	private static RawObjectDB<String, WritableHashSet> db = null;

	private WritableHashSet subscriptions;
	private final HashSet<String> pendingSubscriptions = new HashSet<>(), pendingUnsubscriptions = new HashSet<>();

	public static synchronized RedditSubredditSubscriptionManager getSingleton(final Context context, final RedditAccount account) {

		if(db == null) {
			db = new RawObjectDB<>(context, "rr_subscriptions.db", WritableHashSet.class);
		}

		if(singleton == null || !account.equals(RedditSubredditSubscriptionManager.singletonAccount)) {
			singleton = new RedditSubredditSubscriptionManager(account, context);
			RedditSubredditSubscriptionManager.singletonAccount = account;
		}

		return singleton;
	}

	private RedditSubredditSubscriptionManager(RedditAccount user, Context context) {

		this.user = user;
		this.context = context;

		subscriptions = db.getById(user.getCanonicalUsername());

		if(subscriptions != null) {
			addToHistory(user, subscriptions.toHashset());
		}
	}

	public void addListener(SubredditSubscriptionStateChangeListener listener) {
		listeners.add(listener);
	}

	public synchronized boolean areSubscriptionsReady() {
		return subscriptions != null;
	}

	public synchronized SubredditSubscriptionState getSubscriptionState(final String subredditCanonicalId) {

		if(pendingSubscriptions.contains(subredditCanonicalId)) return SubredditSubscriptionState.SUBSCRIBING;
		else if(pendingUnsubscriptions.contains(subredditCanonicalId)) return SubredditSubscriptionState.UNSUBSCRIBING;
		else if(subscriptions.toHashset().contains(subredditCanonicalId)) return SubredditSubscriptionState.SUBSCRIBED;
		else return SubredditSubscriptionState.NOT_SUBSCRIBED;
	}

	private synchronized void onSubscriptionAttempt(final String subredditCanonicalId) {
		pendingSubscriptions.add(subredditCanonicalId);
		listeners.map(notifier, SubredditSubscriptionChangeType.SUBSCRIPTION_ATTEMPTED);
	}

	private synchronized void onUnsubscriptionAttempt(final String subredditCanonicalId) {
		pendingUnsubscriptions.add(subredditCanonicalId);
		listeners.map(notifier, SubredditSubscriptionChangeType.UNSUBSCRIPTION_ATTEMPTED);
	}

	private synchronized void onSubscriptionChangeAttemptFailed(final String subredditCanonicalId) {
		pendingUnsubscriptions.remove(subredditCanonicalId);
		pendingSubscriptions.remove(subredditCanonicalId);
		listeners.map(notifier, SubredditSubscriptionChangeType.LIST_UPDATED);
	}

	private synchronized void onSubscriptionAttemptSuccess(final String subredditCanonicalId) {
		pendingSubscriptions.remove(subredditCanonicalId);
		subscriptions.toHashset().add(subredditCanonicalId);
		listeners.map(notifier, SubredditSubscriptionChangeType.LIST_UPDATED);
	}

	private synchronized void onUnsubscriptionAttemptSuccess(final String subredditCanonicalId) {
		pendingUnsubscriptions.remove(subredditCanonicalId);
		subscriptions.toHashset().remove(subredditCanonicalId);
		listeners.map(notifier, SubredditSubscriptionChangeType.LIST_UPDATED);
	}

	private static void addToHistory(final RedditAccount account, final HashSet<String> newSubscriptions)
	{
		for(final String sub : newSubscriptions)
		{
			try
			{
				RedditSubredditHistory.addSubreddit(account, sub);
			}
			catch(RedditSubreddit.InvalidSubredditNameException e)
			{
				Log.e(TAG, "Invalid subreddit name " + sub, e);
			}
		}
	}

	private synchronized void onNewSubscriptionListReceived(final HashSet<String> newSubscriptions, final long timestamp) {

		pendingSubscriptions.clear();
		pendingUnsubscriptions.clear();

		subscriptions = new WritableHashSet(newSubscriptions, timestamp, user.getCanonicalUsername());

		// TODO threaded? or already threaded due to cache manager
		db.put(subscriptions);

		addToHistory(user, newSubscriptions);

		listeners.map(notifier, SubredditSubscriptionChangeType.LIST_UPDATED);
	}

	public synchronized ArrayList<String> getSubscriptionList() {
		return new ArrayList<>(subscriptions.toHashset());
	}

	public void triggerUpdate(final RequestResponseHandler<HashSet<String>, SubredditRequestFailure> handler, TimestampBound timestampBound) {

		if(subscriptions != null && timestampBound.verifyTimestamp(subscriptions.getTimestamp())) {
			return;
		}

		new RedditAPIIndividualSubredditListRequester(context, user).performRequest(
				RedditSubredditManager.SubredditListType.SUBSCRIBED,
				timestampBound,
				new RequestResponseHandler<WritableHashSet, SubredditRequestFailure>() {

					// TODO handle failed requests properly -- retry? then notify listeners
					@Override
					public void onRequestFailed(SubredditRequestFailure failureReason) {
						if(handler != null) handler.onRequestFailed(failureReason);
					}

					@Override
					public void onRequestSuccess(WritableHashSet result, long timeCached) {
						final HashSet<String> newSubscriptions = result.toHashset();
						onNewSubscriptionListReceived(newSubscriptions, timeCached);
						if(handler != null) handler.onRequestSuccess(newSubscriptions, timeCached);
					}
				}
		);

	}

	public void subscribe(final String subredditCanonicalId, final AppCompatActivity activity) {

		RedditAPI.subscriptionAction(
				CacheManager.getInstance(context),
				new SubredditActionResponseHandler(activity, RedditAPI.SUBSCRIPTION_ACTION_SUBSCRIBE, subredditCanonicalId),
				user,
				subredditCanonicalId,
				RedditAPI.SUBSCRIPTION_ACTION_SUBSCRIBE,
				context
		);

		onSubscriptionAttempt(subredditCanonicalId);
	}

	public void unsubscribe(final String subredditCanonicalId, final AppCompatActivity activity) {

		RedditAPI.subscriptionAction(
				CacheManager.getInstance(context),
				new SubredditActionResponseHandler(activity, RedditAPI.SUBSCRIPTION_ACTION_UNSUBSCRIBE, subredditCanonicalId),
				user,
				subredditCanonicalId,
				RedditAPI.SUBSCRIPTION_ACTION_UNSUBSCRIBE,
				context
		);

		onUnsubscriptionAttempt(subredditCanonicalId);
	}

	private class SubredditActionResponseHandler extends APIResponseHandler.ActionResponseHandler {

		private final @RedditAPI.RedditSubredditAction int action;
		private final AppCompatActivity activity;
		private final String canonicalName;

		protected SubredditActionResponseHandler(AppCompatActivity activity,
												 @RedditAPI.RedditSubredditAction int action,
												 String canonicalName) {
			super(activity);
			this.activity = activity;
			this.action = action;
			this.canonicalName = canonicalName;
		}

		@Override
		protected void onSuccess(@Nullable final String redirectUrl) {

			switch(action) {
				case RedditAPI.SUBSCRIPTION_ACTION_SUBSCRIBE:
					onSubscriptionAttemptSuccess(canonicalName);
					break;
				case RedditAPI.SUBSCRIPTION_ACTION_UNSUBSCRIBE:
					onUnsubscriptionAttemptSuccess(canonicalName);
					break;
			}

			triggerUpdate(null, TimestampBound.NONE);
		}

		@Override
		protected void onCallbackException(Throwable t) {
			BugReportActivity.handleGlobalError(context, t);
		}

		@Override
		protected void onFailure(@CacheRequest.RequestFailureType int type, Throwable t, Integer status, String readableMessage) {
			onSubscriptionChangeAttemptFailed(canonicalName);
			if(t != null) t.printStackTrace();

			final RRError error = General.getGeneralErrorForFailure(context, type, t, status, null);
			AndroidCommon.UI_THREAD_HANDLER.post(new Runnable() {
				@Override
				public void run() {
					General.showResultDialog(activity, error);
				}
			});
		}

		@Override
		protected void onFailure(APIFailureType type) {
			onSubscriptionChangeAttemptFailed(canonicalName);
			final RRError error = General.getGeneralErrorForFailure(context, type);
			AndroidCommon.UI_THREAD_HANDLER.post(new Runnable() {
				@Override
				public void run() {
					General.showResultDialog(activity, error);
				}
			});
		}
	}

	public Long getSubscriptionListTimestamp() {
		return subscriptions != null ? subscriptions.getTimestamp() : null;
	}

	public interface SubredditSubscriptionStateChangeListener {
		void onSubredditSubscriptionListUpdated(RedditSubredditSubscriptionManager subredditSubscriptionManager);

		void onSubredditSubscriptionAttempted(RedditSubredditSubscriptionManager subredditSubscriptionManager);

		void onSubredditUnsubscriptionAttempted(RedditSubredditSubscriptionManager subredditSubscriptionManager);
	}

	private enum SubredditSubscriptionChangeType {LIST_UPDATED, SUBSCRIPTION_ATTEMPTED, UNSUBSCRIPTION_ATTEMPTED}

	private class SubredditSubscriptionStateChangeNotifier
			implements WeakReferenceListManager.ArgOperator<SubredditSubscriptionStateChangeListener, SubredditSubscriptionChangeType> {

		public void operate(SubredditSubscriptionStateChangeListener listener, SubredditSubscriptionChangeType changeType) {

			switch(changeType) {
				case LIST_UPDATED:
					listener.onSubredditSubscriptionListUpdated(RedditSubredditSubscriptionManager.this);
					break;
				case SUBSCRIPTION_ATTEMPTED:
					listener.onSubredditSubscriptionAttempted(RedditSubredditSubscriptionManager.this);
					break;
				case UNSUBSCRIPTION_ATTEMPTED:
					listener.onSubredditUnsubscriptionAttempted(RedditSubredditSubscriptionManager.this);
					break;
				default:
					throw new UnexpectedInternalStateException("Invalid SubredditSubscriptionChangeType " + changeType.toString());
			}
		}
	}
}
