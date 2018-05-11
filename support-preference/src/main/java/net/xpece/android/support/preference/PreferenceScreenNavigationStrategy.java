package net.xpece.android.support.preference;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by Eugen on 08.12.2015.
 */
@ParametersAreNonnullByDefault
public abstract class PreferenceScreenNavigationStrategy {
    static final String TAG = PreferenceScreenNavigationStrategy.class.getSimpleName();

    public static final String DEFAULT_ROOT_KEY = TAG + ".ROOT";

    protected PreferenceScreenNavigationStrategy() {}

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

        public static boolean onCreatePreferences(PreferenceFragmentCompat f, @Nullable String rootKey) {
            if (rootKey != null && !PreferenceScreenNavigationStrategy.DEFAULT_ROOT_KEY.equals(rootKey)) {
                f.setPreferenceScreen((PreferenceScreen) f.findPreference(rootKey));
                return true;
            }
            return false;
        }

        private PreferenceFragmentCompat buildFragment(@Nullable String rootKey) {
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
            @NonNull
            PreferenceFragmentCompat onBuildPreferenceFragment(@Nullable String rootKey);
        }
    }
}
