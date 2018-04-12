package net.xpece.android.support.preference;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.Collection;
import java.util.HashMap;
import java.util.Stack;

/**
 * Created by Eugen on 08.12.2015.
 */
public abstract class PreferenceScreenNavigationStrategy {
    static final String TAG = PreferenceScreenNavigationStrategy.class.getSimpleName();

    public static final String DEFAULT_ROOT_KEY = TAG + ".ROOT";

    private PreferenceScreenNavigationStrategy() {}

    /**
     * This will replace the whole preference fragment while putting it on the backstack.
     * Supports transition animations.
     * <p/>
     * Create this inside your activity or calling fragment and call appropriate methods.
     * <p/>
     * This class uses fragment framework so it does support transition animations and
     * saved states.
     */
    public static class ReplaceFragment extends PreferenceScreenNavigationStrategy {

        private final int mAnimEnter, mAnimExit, mAnimPopEnter, mAnimPopExit;
        private final boolean mCustomAnimations;
        private final Callbacks mCallbacks;

        /**
         * @param callbacks Callbacks responsible for creating a new preference fragment based on a root preference key.
         * @param animEnter Enter animation resource ID.
         * @param animExit Exit animation resource ID.
         * @param animPopEnter Enter animation resource ID when popped from backstack.
         * @param animPopExit Enter animation resource ID when popped from backstack.
         */
        public ReplaceFragment(Callbacks callbacks, final int animEnter, final int animExit, final int animPopEnter, final int animPopExit) {
            mCallbacks = callbacks;
            mAnimEnter = animEnter;
            mAnimExit = animExit;
            mAnimPopEnter = animPopEnter;
            mAnimPopExit = animPopExit;
            mCustomAnimations = true;
        }

        /**
         * @param callbacks Callbacks responsible for creating a new preference fragment based on a root preference key.
         */
        public ReplaceFragment(Callbacks callbacks) {
            mCallbacks = callbacks;
            mAnimEnter = 0;
            mAnimExit = 0;
            mAnimPopEnter = 0;
            mAnimPopExit = 0;
            mCustomAnimations = false;
        }

        public static boolean onCreatePreferences(PreferenceFragmentCompat f, String rootKey) {
            if (rootKey != null && !PreferenceScreenNavigationStrategy.DEFAULT_ROOT_KEY.equals(rootKey)) {
                f.setPreferenceScreen((PreferenceScreen) f.findPreference(rootKey));
                return true;
            }
            return false;
        }

        private PreferenceFragmentCompat buildFragment(String rootKey) {
            return mCallbacks.onBuildPreferenceFragment(rootKey);
        }

        /**
         * Call this inside {@link android.support.v7.preference.PreferenceFragmentCompat.OnPreferenceStartScreenCallback#onPreferenceStartScreen(PreferenceFragmentCompat, PreferenceScreen)}.
         *
         * @param fragmentManager Fragment manager form activity or calling fragment
         * @param preferenceFragmentCompat The old preference fragment about to be replaced.
         * @param preferenceScreen The new root of preference hierarchy.
         */
        public void onPreferenceStartScreen(final FragmentManager fragmentManager, final PreferenceFragmentCompat preferenceFragmentCompat, final PreferenceScreen preferenceScreen) {
            final String key = preferenceScreen.getKey();
            PreferenceFragmentCompat f = buildFragment(key);
            FragmentTransaction ft = fragmentManager.beginTransaction();
            if (mCustomAnimations) {
                ft.setCustomAnimations(mAnimEnter, mAnimExit, mAnimPopEnter, mAnimPopExit);
            }
            ft.replace(preferenceFragmentCompat.getId(), f, preferenceFragmentCompat.getTag())
                .addToBackStack(key)
                .commit();
        }

        public interface Callbacks {
            PreferenceFragmentCompat onBuildPreferenceFragment(String rootKey);
        }
    }

    /**
     * This will replace just the preference hierarchy within a fragment while keeping track of navigated preference screens.
     * <p/>
     * Create this inside your preference fragment and call appropriate methods.
     * <p/>
     * This class does not support transition animations and remembers scroll position in a limited fashion.
     */
    @Deprecated
    public static class ReplaceRoot extends PreferenceScreenNavigationStrategy {
        private final PreferenceFragmentCompat mFragment;

        private Callbacks mCallbacks;

        private PreferenceScreen mRoot;
        private final Stack<String> mStack = new Stack<>();
        private final HashMap<String, Tuple<Integer>> mScrollPositions = new HashMap<>();

        public ReplaceRoot(PreferenceFragmentCompat fragment, Callbacks callbacks) {
            mFragment = fragment;
            mCallbacks = callbacks;
        }

        public Callbacks getCallbacks() {
            return mCallbacks;
        }

        /**
         * Set callbacks invoked when navigation to preference screen occurs.
         *
         * @param callbacks
         */
        public void setCallbacks(Callbacks callbacks) {
            mCallbacks = callbacks;
        }

        /**
         * Call this in {@link android.support.v4.app.Fragment#onCreate(Bundle)}.
         *
         * @param savedInstanceState
         */
        public void onCreatePreferences(Bundle savedInstanceState) {
            mRoot = mFragment.getPreferenceScreen();
            if (mRoot.getKey() == null) {
                mRoot.setKey(DEFAULT_ROOT_KEY); // Non-null key!
            }

            if (savedInstanceState == null) {
                mStack.clear();
                mStack.push(mRoot.getKey()); // Store root key.
            } else {
                //noinspection unchecked
                Collection<String> savedStack = (Collection<String>) savedInstanceState.getSerializable(TAG + ".mStack");
                mStack.clear();
                if (savedStack != null) {
                    mStack.addAll(savedStack);
                }
                //noinspection unchecked
                HashMap<String, Tuple<Integer>> savedScrollPositions = (HashMap<String, Tuple<Integer>>) savedInstanceState.getSerializable(TAG + ".mScrollPositions");
                if (savedScrollPositions != null) {
                    mScrollPositions.putAll(savedScrollPositions);
                }
                if (mStack.size() > 1) {
                    // We're deeper than root preference screen. Load appropriate screen.
                    String key = mStack.peek(); // Get screen key.
                    PreferenceScreen preference = (PreferenceScreen) mRoot.findPreference(key);
                    navigateToPreferenceScreen(preference);
                }
            }
        }

        /**
         * Call this in {@link android.support.v4.app.Fragment#onSaveInstanceState(Bundle)}.
         *
         * @param outState
         */
        public void onSaveInstanceState(@NonNull final Bundle outState) {
            outState.putSerializable(TAG + ".mStack", mStack);
            outState.putSerializable(TAG + ".mScrollPositions", mScrollPositions);
        }

        private void navigateToPreferenceScreen(PreferenceScreen preference, boolean forward) {
            if (preference.getKey() == null) {
                throw new IllegalArgumentException("PreferenceScreen needs a non-null key.");
            }

            if (forward) {
                String key = mFragment.getPreferenceScreen().getKey();
                RecyclerView list = mFragment.getListView();
                if (list != null) {
                    final View firstChild = list.getChildAt(0);
                    if (firstChild != null) {
                        int position = list.getChildAdapterPosition(firstChild);
                        int offset = firstChild.getTop();
                        mScrollPositions.put(key, new Tuple<>(position, offset));
                    }
                }
            }

            mFragment.setPreferenceScreen(preference);

            if (!forward) {
                String key = preference.getKey();
                if (mScrollPositions.containsKey(key)) {
                    Tuple<Integer> scroll = mScrollPositions.get(key);
                    final int position = scroll.first;
                    final int offset = scroll.second;
                    onRestoreScrollPosition(position, offset);
                }
            }

            if (mCallbacks != null) {
                mCallbacks.onNavigateToPreferenceScreen(preference);
            }
        }

        protected void onRestoreScrollPosition(final int position, final int offset) {
            final RecyclerView list = mFragment.getListView();
            if (list == null) return;

            if (list.getLayoutManager() instanceof LinearLayoutManager) {
                LinearLayoutManager layout = (LinearLayoutManager) list.getLayoutManager();
                layout.scrollToPositionWithOffset(position, offset);
            } else {
                list.scrollToPosition(position);
                list.scrollBy(0, offset); // Is not working, whatever...
            }
        }

        private void navigateToPreferenceScreen(PreferenceScreen preference) {
            if (preference.getKey() == null) {
                throw new IllegalArgumentException("PreferenceScreen needs a non-null key.");
            }

            mFragment.setPreferenceScreen(preference);

            if (mCallbacks != null) {
                mCallbacks.onNavigateToPreferenceScreen(preference);
            }
        }

        /**
         * Call this when the preference screen representative item has been clicked
         * (perhaps in {@link PreferenceFragmentCompat#onPreferenceTreeClick(android.support.v7.preference.Preference)}.
         *
         * @param preference
         */
        public void onPreferenceScreenClick(PreferenceScreen preference) {
            mStack.push(preference.getKey()); // Store new screen key.
                navigateToPreferenceScreen(preference, true);
        }

        /**
         * Provide a mechanism for preference fragment to react to back button presses and call this.
         *
         * @return Whether the event has been consumed.
         */
        public boolean onBackPressed() {
            if (mStack.size() > 1) {
                mStack.pop(); // Pop the screen we're leaving.
                String key = mStack.peek(); // Lookup new screen key.
                PreferenceScreen preference = (PreferenceScreen) mRoot.findPreference(key);
                navigateToPreferenceScreen(preference, false);
                return true;
            }
            return false;
        }

        public interface Callbacks {
            void onNavigateToPreferenceScreen(PreferenceScreen preferenceScreen);
        }
    }
}
