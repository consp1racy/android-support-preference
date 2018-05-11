package net.xpece.android.support.preference.sample;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.XpPreferenceManager;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import net.xpece.android.support.preference.ColorPreference;
import net.xpece.android.support.preference.PreferenceScreenNavigationStrategy;
import net.xpece.android.support.preference.XpColorPreferenceDialogFragment;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p></p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatActivity implements
    PreferenceFragmentCompat.OnPreferenceStartScreenCallback,
    PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback,
    PreferenceScreenNavigationStrategy.ReplaceFragment.Callbacks {

    Toolbar mToolbar;
    TextSwitcher mTitleSwitcher;

    private CharSequence mTitle;

    private SettingsFragment mSettingsFragment;

    private PreferenceScreenNavigationStrategy.ReplaceFragment mReplaceFragmentStrategy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        mReplaceFragmentStrategy = new PreferenceScreenNavigationStrategy.ReplaceFragment(this, R.anim.abc_fade_in, R.anim.abc_fade_out, R.anim.abc_fade_in, R.anim.abc_fade_out);

        if (savedInstanceState == null) {
            mSettingsFragment = SettingsFragment.newInstance(null);
            getSupportFragmentManager().beginTransaction().add(R.id.content, mSettingsFragment, "Settings").commit();
        } else {
            mSettingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag("Settings");
        }

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);

        // Cross-fading title setup.
        mTitle = getTitle();

        mTitleSwitcher = new TextSwitcher(mToolbar.getContext());
        mTitleSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @NonNull
            @Override
            public View makeView() {
                TextView tv = new AppCompatTextView(mToolbar.getContext());
                //noinspection deprecation
                tv.setTextAppearance(tv.getContext(), R.style.TextAppearance_AppCompat_Widget_ActionBar_Title);
                return tv;
            }
        });
        mTitleSwitcher.setCurrentText(mTitle);

        ab.setCustomView(mTitleSwitcher);
        ab.setDisplayShowCustomEnabled(true);
        ab.setDisplayShowTitleEnabled(false);

        // Add to hierarchy before accessing layout params.
        int margin = Util.dpToPxOffset(this, 16);
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mTitleSwitcher.getLayoutParams();
        lp.leftMargin = margin;
        lp.rightMargin = margin;

        mTitleSwitcher.setInAnimation(this, R.anim.abc_fade_in);
        mTitleSwitcher.setOutAnimation(this, R.anim.abc_fade_out);
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);

        if (!mTitle.equals(title)) {
            mTitle = title;

            // Only switch if the title differs. Used for the first hook.
            mTitleSwitcher.setText(title);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            case R.id.github: {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/consp1racy/android-support-preference"));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                return true;
            }
            case R.id.spinner: {
                Intent i = new Intent(this, SpinnerActivity.class);
                startActivity(i);
                return true;
            }
            case R.id.reset: {
                final Context context = this;
                final String[] customPackages = {BuildConfig.APPLICATION_ID};
                XpPreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply();
                XpPreferenceManager.setDefaultValues(context, R.xml.pref_general, true, customPackages);
                XpPreferenceManager.setDefaultValues(context, R.xml.pref_notification, true, customPackages);
                XpPreferenceManager.setDefaultValues(context, R.xml.pref_data_sync, true, customPackages);
                mSettingsFragment = SettingsFragment.newInstance(null);
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out)
                    .replace(R.id.content, mSettingsFragment, "Settings")
                    .commit();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceStartScreen(@NonNull final PreferenceFragmentCompat preferenceFragmentCompat, @NonNull final PreferenceScreen preferenceScreen) {
        mReplaceFragmentStrategy.onPreferenceStartScreen(getSupportFragmentManager(), preferenceFragmentCompat, preferenceScreen);
        return true;
    }

    @NonNull
    @Override
    public PreferenceFragmentCompat onBuildPreferenceFragment(@Nullable final String rootKey) {
        return SettingsFragment.newInstance(rootKey);
    }

    @Override
    public boolean onPreferenceDisplayDialog(@NonNull PreferenceFragmentCompat preferenceFragmentCompat, @NonNull Preference preference) {
        final String key = preference.getKey();
        DialogFragment f;
        if (preference instanceof ColorPreference) {
            f = XpColorPreferenceDialogFragment.newInstance(key);
        } else {
            return false;
        }

        f.setTargetFragment(preferenceFragmentCompat, 0);
        f.show(this.getSupportFragmentManager(), key);
        return true;
    }
}
