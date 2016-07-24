# Change log

**0.10.0** *Pending release*
- Depends on support libs 24.1.1 to avoid issues in 21.1.0.
- `Fixes.updateLayoutInflaterFactory(getLayoutInflater())` is back.
  - `CheckedTextView`s are once more incorrectly tinted since support libs 24.1.0. This will fix it.
  - Call in your activity after `super.onCreate()` and before `setContentView(...)`.
- Uses appcompat-v7 `ColorStateList` inflater so theme references are resolved below API 21.

**0.9.1** 2016-07-22
- Public `XpPreferenceManager.setDefaultValues`.
  - Use this instead of `PreferenceManager.setDefaultValues`.
  - Takes into account preference classes with custom packages.
- `ListPreference` exposes `buildAdapter` method.
  - Now you can easily supply your own `SpinnerAdapter` inside a `ListPreference` subclass.
  - *Framework expects only one view type!*
- Depends on support libs 24.1.0 which are broken.

**0.9.0** 2016-06-18
- Supports support libs 24.0.0.
- Cleanup.
- `asp_preferenceIconTint` is back along with `asp_preferenceDialogIconTint`.
  - You *have to* define these attributes in your theme. 
  - Recommended colors are either `?colorAccent` or `?colorControlNormal`.

```
<item name="asp_preferenceIconTint">?colorAccent</item>
<item name="asp_preferenceDialogIconTint">?asp_preferenceIconTint</item>
```

**0.8.1** *Legacy*
- (2016-05-06) `support-spinner` only release.
  - Added `CheckedTypedItemAdapter` which is more versatile than `CheckedItemAdapter`.
  - *FIXED:* No more crashes when using empty adapter.
  - *FIXED:* Spinner no longer leaks popup window.
- (2016-05-11) `support-preference` only release.
  - Fixed crash in `RingtonePreference`.
    - If ringtones cannot be queried the preference tries to open system ringtone picker.
    - If system ringtone picker fails to open only "Silent" and "Default" options are shown if enabled.
  - Fixed crash in `EditTextPreference`.
    - `EditText` attributes are no longer passed on from `EditTextPreference` XML.
    - Use `EditTextPreference.setOnEditTextCreatedListener(OnEditTextCreatedListener)` for setup.
  - Fixed crash in `PreferenceScreenNavigationStrategy.ReplaceRoot`.
    - `onPreferenceScreenClick` can now be called before `onCreateView`. 

**0.8.0** *Deprecated*
- *NEW!* Very much material `XpAppCompatSpinner` is now available as a standalone lib;rary.
- *FIXED:* Simple menu:
  - Correct vertical position when using asymmetric top and bottom padding on anchor.
  - Correct horizontal size when using `setDropDownMaxWidth(MATCH_PARENT)`.
  - Correct gravity in RTL configurations.
- Dropping `preferenceTint` attribute. `colorAccent` will be used for icons by default.
- `ReplaceRoot` subscreen navigation strategy now remembers precise scroll position.
- UI tweaks.

**0.7.0** *Deprecated*
- `SeekBar*Preference` support `app:asp_min` attribute.
  - Preference stores a value between `app:asp_min` and `android:max`.
- `SeekBarPreference` supports `app:asp_info` attribute for a short arbitrary text such as numeric value.
- `SeekBarPreference` supports custom `OnSeekBarChangeListener`.
- UI tweaks:
  - Better aligned `CheckBox` and `Switch` widgets to 16dp right keyline.
  - Using native `Switch` on API >= 21.
    - Supports animation on toggle.
    - *May change back any time!*
  - Better aligned `SeekBar` in `SeekBar*Preference`.
- *FIXED:* `SeekBarDialogPreference` dialog now never shows icon in title region.
- *FIXED:* `SeekBarDialogPreference` dialog now supports `android:dialogMessage`.
- *FIXED:* `SeekBarPreference`'s `SeekBar` now shows up in proper state on Android 2.
- `ReplaceRoot` subscreen navigation strategy:
  - Remembers first visible child upon re-entering previous screen.
  - Is now deprecated. Please use `ReplaceFragment` instead.
- `ColorPreference` dialog will automatically calculate column count if `app:asp_columnCount="-1"`.

**0.6.2** *Legacy*
- *FIXED:* Finally fixed simple menu position - no more arbitrary numbers + more effective.
- *FIXED:* Simple menu animation looks closer to platform default on Android 4.
  - Override `Animation.Asp.Popup` to change this behavior.
- *FIXED:* Better `SeekBarPreference` layout height on Android 6.

**0.6.1** *Deprecated*
- *FIXED:* Dialog preferences respect `alertDialogTheme` when inflating icons and layouts.
- *FIXED:* When using simple color for `preferenceTint` disabled state is computed automatically.

**0.6.0** *Deprecated*
- *NEW!* `ColorPreference` available as a separate module!
- *NEW!* `ReplaceFragment` subscreen navigation strategy allowing for fragment transition animations.
- Minor fixes.

**0.5.11** *Deprecated*
- *FIXED:* Focused `SeekBarPreference` can now be controlled by `+` and `-` keys.
- *FIXED:* Simple menu.
  - Popup will now display correctly for various item counts in various positions.
  - Popup is clipped to parent recycler view with a 16dp margin as suggested by MD.
  - Limit: Simple menu is now disabled before Android 4. Falls back to simple dialog.
  - Limit: Displaying popup will not make parent recycler view scroll.

**0.5.10** *Deprecated*
- *NEW!* Simple dialog variant of `ListPreference`. See above for instructions.
- *FIXED:* Simple menu.
  - Fixed simple menu height when using multiline items.
  - Centering multiline item over anchor view when using simple menu.
- *FIXED:* Recycling items with custom text appearance or text color.

**0.5.9** *Deprecated*
- *FIXED:* Simple menu.
  - Showing simple menu is restored after screen orientation change.
  - Aligning simple menu width to 56dp/64dp grid on phones/tablets (customizable).

**0.5.8** *Deprecated*
- *FIXED:* Simple menu.
  - Preselected option is now highlighted.
  - Fixed animation on Android 4 - fading in.
  - Fixed popup width on Android 4.
  - Two line items now have top and bottom padding.

**0.5.6** *Deprecated*
- *NEW!* Custom title and summary text styles.
  - `app:titleTextAppearance` and `app:titleTextColor` for titles.
  - `app:subtitleTextAppearance` and `app:subtitleTextColor` for summaries.
  - Analogous methods available in Java.
- *FIXED:* Simple menu.
  - Popup window adjusts its width according to its own content.
  - Added top and bottom padding to popup window.
  - Items can have up to two lines of text (increased form one).
  - Added persistent scrollbar if all items don't fit on screen.

**0.5.5** *Deprecated*
- ***Only supports appcompat-v7 with preference-v7 version 23.2.x!***

**0.5.4** *Legacy*
- ***Last version that supports appcompat-v7 with preference-v7 version 23.1.1!***
- *NEW!* Simple menu variant of `ListPreference`.
  - Via `app:asp_simpleMenu="true"`.
  - Described here https://www.google.com/design/spec/components/menus.html#menus-simple-menus
  - Does not have top and bottom padding yet.
  - Menu position is unpredictable when having more than 3 menu items and the preference is first or last in the list.
  - To be updated later.
- *FIXED:* Small icon is properly aligned in RTL configurations.

**0.5.3** *Deprecated*
- *FIXED:* Ringtone picker does not *need* `READ_EXTERNAL_STORAGE` permission even prior to Android 6.

**0.5.2** *Deprecated*
- *FIXED:* `PreferenceScreenNavigationStrategy.ReplaceRoot` no longer crashes on screen rotation.
- *FIXED:* Ringtone picker does not stop playback on screen rotation.

**0.5.1** *Deprecated*
- *NEW!* `PreferenceScreenNavigationStrategy` class.
- *FIXED:* Missing Proguard rules are now bundled with the library.
- *FIXED:* Incorrect icon size on Lollipop.

**0.5.0** *Deprecated*
- ***Only supports appcompat-v7 with preference-v7 version 23.1.1!***
- *NEW!* Based on preference-v7 instead of native preferences.
- Updated appcompat-v7 library to 23.1.1.
    - Material SeekBar style across all platforms.
- `RingtonePreference` is now `DialogFragment` based.
- Unmanaged preference icons (such as that of `PreferenceScreen`) can be tinted via `PreferenceIconHelper`.
- Default preference icon tint specified by `preferenceTint` theme attribute.
- Fixed divider color.
- Sample contains `PreferenceScreen` subscreen handling.

**0.4.3** *Legacy*
- ***Last fully supported appcompat-v7 version is 23.0.1. After that ringtone picker crashes!***
- No more `Resources.NotFoundException` in `RingtonePickerActivity`. Falls back to English.
- Updated appcompat-v7 library to 22.2.1.

**0.4.2** *Deprecated*
- <s>`SeekBar` tinting can be turned off via `app:asp_tintSeekBar="false"`</s>
- Introduced missing `seekBarDialogPreference` style

**0.4.1** *Deprecated*
- Ringtone picker strings are now taken dynamically from `android` and `com.android.providers.media` packages, falls back to English
    - <s>These are accessible via `RingtonePickerActivity.get*String(Context)`</s>

**0.4.0** *Deprecated*
- *NEW!* Implemented SeekBarPreference according to http://www.google.com/design/spec/components/dialogs.html#dialogs-confirmation-dialogs
- *FIXED:* tinting/padding in DialogPreference and SeekBarDialogPreference
- <s>AppCompatPreferenceActivity and PreferenceFragment now implement Factory<Preference></s>
- *NEW!* app:asp_dialogIconPaddingEnabled attribute

**0.3.0** *Deprecated*
- Removed `MultiCheckPreference` as it was only partially implemented and `MultiSelectListPreference` provides the same function.
- `MultiSelectListPreference` is now available since API 7 (formerly API 11). Uses `JSONArray` to persist `Set<String>`.
- API for persisting and accessing string sets since API 7 is available via `SharedPreferencesCompat`.
- `Preference`s now support `app:asp_iconPaddingEnabled` attribute which allows to better align non-launcher icons to 16dp keyline.
- <s>Custom preferences are now recycled which fixed animation issues on Lollipop.</s>
- Custom preferences are now always inflated on all platforms if using `AppCompatPreferenceActivity` and/or custom `PreferenceFragment`.
- Library no longer includes `android.permission.READ_EXTERNAL_STORAGE` permission (used to read ringtones). You have to do it yourself.
    - This is needed because the custom picker is part of the app and not provided by system.
    - <s>You are of course free to use `android.preference.RingtonePreference` when necessary.</s>

**0.2.2** *Deprecated*
- optional tinting <s>via `app:asp_tintIcon="true"` and `app:asp_tintDialogIcon="true"` and `asp_tint` and `asp_tintMode`</s>.

**0.2.1** *Deprecated*
- No need for `net.xpece.android.support.preference.` prefix in XML files defining preferences, framework will choose automatically:
    - <s>On Lollipop native `Preference`, `CheckBoxPreference`, `SwitchPreference` will be used.</s>
    - <s>Otherwise support version will be used.</s>
    - <s>Force either version by using fully qualified class name.</s>
    - <s>You need to use `AppCompatPreferenceActivity` or special `PreferenceFragment` both of which are provided.</s>
- <s>Added `PreferenceCompat#setChecked(Preference, boolean)` helper method.</s>

**0.1.2** *Deprecated*
- Czech strings
- `SeekBar` in `SeekBarDialogActivity` uses `ColorFilter` to match theme

**0.1.1** *Deprecated*
- Initial release
- Backported material style and icon capability for `Preference` children
- Backported `SwitchPreference`
- Material styled `RingtonePreference` picker dialog/activity
