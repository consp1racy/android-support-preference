-keepclassmembernames class androidx.preference.PreferenceManager {
    void setNoCommit(boolean);
}

-keepclassmembernames class androidx.preference.PreferenceFragmentCompat {
    androidx.preference.PreferenceManager mPreferenceManager;
}

-dontwarn net.xpece.android.support.preference.SeekBarPreference
-dontwarn net.xpece.android.support.preference.SeekBarDialogPreference

-dontwarn net.xpece.android.support.widget.XpListPopupWindow
-dontwarn net.xpece.android.support.widget.AbstractXpListPopupWindow
