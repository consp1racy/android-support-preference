-keepclassmembernames class android.support.v7.preference.PreferenceManager {
    void setNoCommit(boolean);
}

-keepclassmembernames class android.support.v7.preference.PreferenceFragmentCompat {
    android.support.v7.preference.PreferenceManager mPreferenceManager;
}
