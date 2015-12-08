# Material Preference

<img src="./sample/src/main/res/mipmap-xxhdpi/ic_launcher.png" align="right" style="margin-left: 1em;"/>

Material theme for preference widgets.

Backporting dat material look *and* functionality.

Available from API 7. Depends on appcompat-v7 and preference-v7 r23.1.1.

## Screenshots

Library version 0.5.1. Android version 4.4.

![Overview 1](./docs/device-2015-12-08-200222.png)&nbsp;
![EditTextPreference](./docs/device-2015-12-08-200302.png)&nbsp;
![MultiSelectListPreference](./docs/device-2015-12-08-201133.png)&nbsp;
![PreferenceScreen](./docs/device-2015-12-08-200351.png)&nbsp;
![Overview 2](./docs/device-2015-12-08-200423.png)&nbsp;
![RingtonePreference](./docs/device-2015-12-08-200503.png)&nbsp;
![SeekBarDialogPreference](./docs/device-2015-12-08-200524.png)&nbsp;
![ListPreference](./docs/device-2015-12-08-200544.png)&nbsp;
![Disabled preferences](./docs/device-2015-12-08-200704.png)&nbsp;

## Contents

- `Preference`
- `CheckBoxPreference`
- `SwitchPreference`
    - Using `SwitchCompat` available from API 7
- `DialogPreference`
    - Uses AppCompat Alert Dialog Material theme
- `EditTextPreference`
- `ListPreference`
- `MultiSelectListPreference`
    - Available since API 7
- `SeekBarDialogPreference` extends `DialogPreference`
    - Made public
- `SeekBarPreference`
    - Made public
    - According to http://www.google.com/design/spec/components/dialogs.html#dialogs-confirmation-dialogs
- `RingtonePreference`
    - Coerced Ringtone Picker Activity from AOSP
- `XpPreferenceFragment`
    - Handles proper Preference inflation and DialogPreference dialogs
- `SharedPreferencesCompat`
    - `getStringSet` and `putStringSet` methods allow persisting string sets even before API 11

## Features on top of preference-v7

- Material preference item layouts out of the box.
- Icon and dialog icon tinting and padding.
- `EditTextPreference` understands `EditText` XML attributes.
- Several preference widgets not publicly available in preference-v7 or SDK.
    - `RingtonePreference`, `SeekBarPreference`, `SeekBarDialogPreference`, `MultiSelectListPreference`
- Dividers.
- Subscreen navigation implementation.

## How to get the library?

```groovy
dependencies {
    compile 'net.xpece.android:support-preference:0.5.1'
}
```

## How to use the library?

### Basic setup

Your preference fragment needs to extend `XpPreferenceFragment`.

Setup your preference items in the following method:

```java
public void onCreatePreferences2(final Bundle savedInstanceState, final String rootKey) {
    // ...
}
```

Your settings activity theme needs to specify the following values:

```xml
<style name="AppTheme" parent="Theme.AppCompat.Light.NoActionBar">
    <!-- Used to theme preference list and items. -->
    <item name="preferenceTheme">@style/PreferenceThemeOverlay.Material</item>
    <!-- Default preference icon tint color. -->
    <item name="preferenceTint">@color/accent_state_list</item>
</style>
```

Styling `alertDialogTheme` is recommended for a proper color theme. See the sample project.

### Dividers

If you want to use dividers, override `onRecyclerViewCreated(RecyclerView)` in your fragment:

```java
@Override
public void onRecyclerViewCreated(RecyclerView list) {
    list.addItemDecoration(new PreferenceDividerDecoration(getContext()).drawBottom(true));
}
```

### Avoiding bugs

In appcompat-v7 r23.1.1 library there is a bug which prevents tinting of checkmarks in lists.
Call `Fixes.updateLayoutInflaterFactory(getLayoutInflater())` right after
`super.onCreate(savedInstanceState)` in your Activity.

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Fixes.updateLayoutInflaterFactory(getLayoutInflater());
    setContentView(R.layout.activity_settings);
    //...
}
```

### Ringtone picker

If you'll be using the `RingtonePreference` your app needs to request
the `android.permission.READ_EXTERNAL_STORAGE` permission in its manifest.

### Handling PreferenceScreen icons

As `PreferenceScreen` class is final and hardwired into preference system
I was unable to automate icon tinting and padding. However you are able to do this yourself:

```java
Preference subs = findPreference("subs_screen");
PreferenceIconHelper subsHelper = new PreferenceIconHelper(subs);
subsHelper.setIconPaddingEnabled(true); // Call this BEFORE setIcon!
subsHelper.setIcon(R.drawable.some_icon);
subsHelper.setTintList(ContextCompat.getColorStateList(getPreferenceManager().getContext(), R.color.accent));
subsHelper.setIconTintEnabled(true);
```

### Subscreen navigation

One solution is implemented in `PreferenceScreenNavigationStrategy.ReplaceRoot` class.
Please review the sample project for an example solution.

### XML attributes

- `app:asp_tint`
- `app:asp_tintMode`
- `app:asp_tintEnabled`
- `app:asp_iconPaddingEnabled`
- `app:asp_dialogTint`
- `app:asp_dialogTintMode`
- `app:asp_dialogTintEnabled`
- `app:asp_dialogIconPaddingEnabled`

### Icon padding

Application icons (48dp x 48dp) require no extra padding.
For smaller icons extra padding of 4dp on each side is needed.
Achieve this by using `app:asp_iconPaddingEnabled`
and `app:asp_dialogIconPaddingEnabled` attributes. Icon padding is enabled by default.

### Proguard

Since version 0.5.1 Proguard rules are bundled with the library.

## Changelog

**0.5.1**
- *NEW!* `PreferenceScreenNavigationStrategy` class.
- *FIXED:* Missing Proguard rules are now bundled with the library.
- *FIXED:* Incorrect icon size on Lollipop.

**0.5.0**
- *NEW!* Based on preference-v7 instead of native preferences.
- Updated appcompat-v7 library to 23.1.1.
    - Material SeekBar style across all platforms.
- `RingtonePreference` is now `DialogFragment` based.
- Unmanaged preference icons (such as that of `PreferenceScreen`) can be tinted via `PreferenceIconHelper`.
- Default preference icon tint specified by `preferenceTint` theme attribute.
- Fixed divider color.
- Sample contains `PreferenceScreen` subscreen handling.

**0.4.3**
- No more `Resources.NotFoundException` in `RingtonePickerActivity`. Falls back to English.
- Updated appcompat-v7 library to 22.2.1.

**0.4.2**
- <s>`SeekBar` tinting can be turned off via `app:asp_tintSeekBar="false"`</s>
- Introduced missing `seekBarDialogPreference` style

**0.4.1**
- Ringtone picker strings are now taken dynamically from `android` and `com.android.providers.media` packages, falls back to English
    - These are accessible via `RingtonePickerActivity.get*String(Context)`

**0.4.0**
- *NEW!* Implemented SeekBarPreference according to http://www.google.com/design/spec/components/dialogs.html#dialogs-confirmation-dialogs
- *FIXED:* tinting/padding in DialogPreference and SeekBarDialogPreference
- <s>AppCompatPreferenceActivity and PreferenceFragment now implement Factory<Preference></s>
- *NEW!* app:asp_dialogIconPaddingEnabled attribute

**0.3.0**
- Removed `MultiCheckPreference` as it was only partially implemented and `MultiSelectListPreference` provides the same function.
- `MultiSelectListPreference` is now available since API 7 (formerly API 11). Uses `JSONArray` to persist `Set<String>`.
- API for persisting and accessing string sets since API 7 is available via `SharedPreferencesCompat`.
- `Preference`s now support `app:asp_iconPaddingEnabled` attribute which allows to better align non-launcher icons to 16dp keyline.
- <s>Custom preferences are now recycled which fixed animation issues on Lollipop.</s>
- Custom preferences are now always inflated on all platforms if using `AppCompatPreferenceActivity` and/or custom `PreferenceFragment`.
- Library no longer includes `android.permission.READ_EXTERNAL_STORAGE` permission (used to read ringtones). You have to do it yourself.
    - This is needed because the custom picker is part of the app and not provided by system.
    - <s>You are of course free to use `android.preference.RingtonePreference` when necessary.</s>

**0.2.2**
- optional tinting <s>via `app:asp_tintIcon="true"` and `app:asp_tintDialogIcon="true"` and `asp_tint` and `asp_tintMode`</s>.

**0.2.1**
- No need for `net.xpece.android.support.preference.` prefix in XML files defining preferences, framework will choose automatically:
    - <s>On Lollipop native `Preference`, `CheckBoxPreference`, `SwitchPreference` will be used.</s>
    - <s>Otherwise support version will be used.</s>
    - <s>Force either version by using fully qualified class name.</s>
    - <s>You need to use `AppCompatPreferenceActivity` or special `PreferenceFragment` both of which are provided.</s>
- <s>Added `PreferenceCompat#setChecked(Preference, boolean)` helper method.</s>

**0.1.2**
- Czech strings
- `SeekBar` in `SeekBarDialogActivity` uses `ColorFilter` to match theme

**0.1.1**
- Initial release
- Backported material style and icon capability for `Preference` children
- Backported `SwitchPreference`
- Material styled `RingtonePreference` picker dialog/activity

## Work to be done

- Additional ringtone preference which uses system dialog and requires no permission.
- Use weaving to keep original method names in PreferenceFragment (no "2" suffix).

## Known issues

- SwitchPreference does not animate its SwitchCompat widget when clicked.
- MultiSelectListPreference items may be incorrectly tinted on Android 2.
- SeekBarPreference's SeekBar may appear in disabled state until clicked on Android 2.

## Questions

- Why are some of your classes in `android.support.v7` packages?
    - I'm using their package private features to achieve consistent results.

## Credit

Most of this library is straight up pillaged latest SDK mixed with heavy reliance on appcompat-v7. Since version 0.5.0 the same applies to preference-v7. Kudos to the people who create and maintain these!
