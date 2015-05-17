# Material Preference

Material themes for preference widgets and support preference widgets.

Available from API 7.

## Screenshots

All taken from a API 10 device.

![Overview 1](./docs/device-2015-05-16-022153.png)&nbsp;
![Overview 2](./docs/device-2015-05-16-022305.png)&nbsp;
![EditTextPreference](./docs/device-2015-05-16-022337.png)&nbsp;
![ListPreference](./docs/device-2015-05-16-022400.png)&nbsp;
![RingtonePreference](./docs/device-2015-05-16-022428.png)&nbsp;
![SeekBarDialogPreference](./docs/device-2015-05-17-164239.png)

## Contents

- `Preference`
  - Material theme
  - Icon capability from API 7

- `TwoStatePreference` extends `Preference`
  - Base for checkable preferences, now made public

- `CheckBoxPreference` extends `TwoStatePreference`

- `SwitchPreference` extends `TwoStatePreference`
  - Using `SwitchCompat` available from API 7

- `DialogPreference` extends `Preference`
  - Base for preferences modified via a dialog
  - Uses AppCompat Alert Dialog Material theme

- `EditTextPreference` extends `DialogPreference`

- `ListPreference` extends `DialogPreference`

- `MultiCheckPreference` extends `DialogPreference`

- `RingtonePreference` extends `Preference`
  - Extracted Ringtone Picker Activity from AOSP
  - Customizable AppCompat theme

- `SeekBarDialogPreference` extends `DialogPreference`

## How to get the library?

    repositories {
        maven { url "http://dl.bintray.com/consp1racy/maven" }
    }
  
    dependencies {
        compile 'net.xpece.android:support-preference:0.1.2'
    }

## Changelog

**0.1.2**
Czech strings
`SeekBar` in `SeekBarDialogActivity` uses `ColorFilter` to match theme

**0.1.1**
Initial release
Backported material style and icon capability for `Preference` children
Backported `SwitchPreference`
Material styled `RingtonePreference` picker dialog/activity
Some hidden preferences made public

## Known issues

- Doesn't work well with fragment headers. Use simple preference layout as much as possible.
- Multilingual strings for Ringtone picker activity are not pulled yet.
- `CheckBoxPreference` and `SwitchPreference` don't animate on Lollipop. Use native counterparts.
- `SeekBarDialogPreference` has no Material style for `SeekBar` yet.

Use this method to handle checkbox preferences until it's released:

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static void setChecked(Preference preference, boolean checked) {
        if (preference instanceof net.xpece.android.support.preference.TwoStatePreference) {
            ((net.xpece.android.support.preference.TwoStatePreference) preference).setChecked(checked);
        } else if (preference instanceof android.preference.CheckBoxPreference) {
            ((android.preference.CheckBoxPreference) preference).setChecked(checked);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
            && preference instanceof android.preference.TwoStatePreference) {
            ((android.preference.TwoStatePreference) preference).setChecked(checked);
        } else {
            Timber.e("#setChecked called on non-checkable preference!");
        }
    }