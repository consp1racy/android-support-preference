package net.xpece.android.support.preference;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import java.util.Collection;
import java.util.Stack;

/**
 * Created by Eugen on 08.12.2015.
 */
public abstract class PreferenceScreenNavigationStrategy {
    private static final String TAG = PreferenceScreenNavigationStrategy.class.getSimpleName();

    public static final String DEFAULT_ROOT_KEY = TAG + ".ROOT";

    private PreferenceScreenNavigationStrategy() {}

    public abstract void onCreate(Bundle savedInstanceState);

    public abstract void onSaveInstanceState(Bundle outState);

    public abstract void onPreferenceScreenClick(PreferenceScreen preference);

    public abstract boolean onBackPressed();

    public static class ReplaceRoot extends PreferenceScreenNavigationStrategy {
        private final PreferenceFragmentCompat mFragment;

        private Callbacks mCallbacks;

        private PreferenceScreen mRoot;
        private final Stack<String> mStack = new Stack<>();

        public ReplaceRoot(PreferenceFragmentCompat fragment, Callbacks callbacks) {
            mFragment = fragment;
            mCallbacks = callbacks;
        }

        public Callbacks getCallbacks() {
            return mCallbacks;
        }

        public void setCallbacks(Callbacks callbacks) {
            mCallbacks = callbacks;
        }

        public void onCreate(Bundle savedInstanceState) {
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
                if (mStack.size() > 1) {
                    // We're deeper than root preference screen. Load appropriate screen.
                    String key = mStack.peek(); // Get screen key.
                    PreferenceScreen preference = (PreferenceScreen) mRoot.findPreference(key);
                    navigateToPreferenceScreen(preference);
                }
            }
        }

        public void onSaveInstanceState(Bundle outState) {
            outState.putSerializable(TAG + ".mStack", mStack);
        }


        public void navigateToPreferenceScreen(PreferenceScreen preference) {
            if (preference.getKey() == null) {
                throw new IllegalArgumentException("PreferenceScreen needs a non-null key.");
            }

            mFragment.setPreferenceScreen(preference);
            if (mCallbacks != null) {
                mCallbacks.onNavigateToPreferenceScreen(preference);
            }
        }

        public void onPreferenceScreenClick(PreferenceScreen preference) {
            mStack.push(preference.getKey()); // Store new screen key.
            navigateToPreferenceScreen(preference);
        }

        public boolean onBackPressed() {
            if (mStack.size() > 1) {
                mStack.pop(); // Pop the screen we're leaving.
                String key = mStack.peek(); // Lookup new screen key.
                PreferenceScreen preference = (PreferenceScreen) mRoot.findPreference(key);
                navigateToPreferenceScreen(preference);
                return true;
            }
            return false;
        }
    }

    public interface Callbacks {
        void onNavigateToPreferenceScreen(PreferenceScreen preferenceScreen);
    }
}
