# Material Preference

Material themes for preference widgets and support preference widgets.

Available from API 7. Depends on appcompat-v7-r22.1.1.

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

```groovy
repositories {
    maven { url "http://dl.bintray.com/consp1racy/maven" }
}

dependencies {
    compile 'net.xpece.android:support-preference:0.2.0'
}
```

## How to use the library?

For now: Please read known issues at the bottom of this page and carefully examine the sample project.

## Changelog

**0.2.0**
- No need for `net.xpece.android.support.preference.` prefix in XML files defining preferences, framework will choose automatically:
  - On Lollipop native `Preference`, `CheckBoxPreference`, `SwitchPreference` will be used.
  - Otherwise support version will be used.
  - Force either version by using fully qualified class name.
- Added `PreferenceCompat#setChecked(Preference, boolean)` helper method.

**0.1.2**
- Czech strings
- `SeekBar` in `SeekBarDialogActivity` uses `ColorFilter` to match theme

**0.1.1**
- Initial release
- Backported material style and icon capability for `Preference` children
- Backported `SwitchPreference`
- Material styled `RingtonePreference` picker dialog/activity
- Some hidden preferences made public

## Work TBD

- Try unifying view IDs inside layouts so they can be used interchangeably with native and custom preferences
  - This would make custom `Preference` and `CheckBoxPreference` obsolete if you target at least API 14.
- Steal `PreferenceInflater` from SDK so we don't need to duplicate preference XML files in `res/xml-v21`.

## Known issues

- Doesn't work well with fragment headers. Use simple preference layout as much as possible.
- Multilingual strings for Ringtone picker activity are not pulled yet.
- <s>`CheckBoxPreference` and `SwitchPreference` don't animate on Lollipop. Use native counterparts.</s>
- `SeekBarDialogPreference` has no Material style for `SeekBar` yet.

If you want Holo seek bar on Gingerbread, copy necessary resources from SDK to your project and override `android:seekBarStyle` in your theme appropriately.
