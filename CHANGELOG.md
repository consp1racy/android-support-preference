# Change log

## 3.0.0 2021-03-13

### Added
- `PreferenceScreenNavigationStrategy.ReplaceFragment` supports Up navigation.
- Support `useSimpleSummaryProvider` XML attribute.
- Better compatibility with AndroidX Preference 1.1.x.

### Changed
- Migrated to AndroidX.

### Deprecated
- Use `OnBindEditTextListener` instead of `OnEditTextCreatedListener`.
- `StyledContextProvider` and `XpPreferenceFragment.onProvideCustomStyledContext` no longer work.

### Removed
- Previously deprecated `PreferenceScreenNavigationStrategy.ReplaceRoot` is now gone.
- Previously deprecated classes in `android.support.*` packages are now gone.

### Fixed
- Prevent crashing on Android Q when picking ringtone (#120)
- Prefer custom long click instead of long-click-to-copy.
- Fix widget styles (e.g. check box tints) on Android 4.x.

## 2.3.2 2018-08-05

- *FIXED:* `PreferenceCategory` now has proper padding on API 16 (introduced in 2.3.0).
- *FIXED:* `DropDownPreference` is now properly styled when using `.Inset` theme (introduced in 2.3.1).

## 2.3.1 2018-07-29

### Support Preference

- *FIXED:* Added missing support for `DropDownPreference`.
  - Android Support Library uses an invisible `Spinner` to display its popup. This library uses
    `XpListPopupWindow` directly. If you want to use a custom layout with custom styled Spinner,
    extend and use Android Support Library `DropDownPreference`.

## 2.3.0 2018-07-27

### Support Preference

- *FIXED:* Properly disable long click listener so a selector doesn't appear on passive items.
- *FIXED:* `SeekBarPreference` now properly registers and unregisters its key listener.
- *FIXED:* `SeekBarPreference` info text view no longer leaks the activity context.
- *FIXED:* `MultiSelectListPreference` persistence:
  - Storing a new value now correctly overwrites any currently persisted value.
    Previously it could throw a `ClassCastException`.
  - Reading now correctly throws a `ClassCastException` if stored value is not a `Set<String>`.
  - `PreferenceDataStore` is now supported.
- *FIXED:* The library is now compatible with Android P preview.
  - `RingtonePreference` dialog shows ringtones.
  - `XpAppCompatSpinner` doesn't crash.
- *FIXED:* `SeekBarPreference` now treats null default value as zero.
- *FIXED:* `TwoStatePreference` now treats null default value as false.
- *FIXED:* `CheckBoxPreference` now correctly reverts check box state if preference change listener returned false.
- *FIXED:* Added missing implementation in deprecated method `XpPreferenceHelpers#setDialogIcon`.
- *NEW!* New API for using a long-lived themed context with the preference fragment.
  - This will prevent memory leaks when using retained preference fragments. Retained fragments
  don't reinflate the preference hierarchy on each configuration change which prevents jank.
  - Override `XpPreferenceFragment#onProvideCustomStyledContext` and return an application context
    with an activity theme overlay. Use utility methods in the `StyledContextProvider` class.
- *NEW!* Preference theme with text aligned with activity title.

```xml
<item name="preferenceTheme">@style/PreferenceThemeOverlay.Asp.Material.Inset</item>
<!-- Add if you use ColorPreference. -->
<item name="colorPreferenceStyle">
    @style/Preference.Asp.Material.Inset.DialogPreference.ColorPreference
</item>
```

### Support Spinner

- *FIXED:* Revised logic for sizing and positioning of popup windows. See behavior changes.
  Popups should now correctly show when not using ListPreference or underlined Spinner.
- *FIXED:* Drop down indicator now has consistent position in plain and underlined Spinner. 
  See behavior changes.

### API Changes

* While binary compatibility is maintained, nullability annotations were added *everywhere*.
  This will affect Kotlin users.
* In an effort to be compatible with upcoming AndroidX libraries **all classes of *this* library
  that were previously in `android.support.*` packages are deprecated** and replaced with classes
  in packages `net.xpece.android.support.*`. 
  * Behavior and API *should* remain unaffected.
  * Please migrate to the new classes, changing the import statements should suffice.
  * Old classes will be removed in upcoming major release to ensure compatibility with AndroidX.

### Behavior Changes

* Popup grow animation has been replaced with Material fade in animation on Android 4.
  This looks better since the popup and selected item are now displayed precisely over
  the emitting view. If you want the previous behavior put this code in your `styles.xml`:
   
```xml
<style name="Base.Widget.Asp.ListPopupWindow" parent="Widget.AppCompat.ListPopupWindow">
    <item name="android:popupAnimationStyle">@style/Animation.Asp.Popup.Holo</item>
</style>
``` 

* Since the logic for sizing and positioning popup windows now actually works,
  you may need to revise any hacks you used to precisely position your `XpListPopupWindow`s.
* Notes on drop down indicator of `XpAppCompatSpinner`:
  * The indicator is 10dp wide.
  * The indicator is now 8dp from text and 6dp from view bounds (was 15dp and 7dp respectively).
  * The underlined background includes extra 4dp inset on left and right sides.
  * If you want the previous behavior (bigger space between text and indicator) set both
    `android:paddingEnd` and `android:paddingRight` to 32dp (plain) or 36dp (underlined).
  
## 2.2.0 (2018-04-12)
- *FIXED:* `MultiSelectListPreference` no longer crashes on saved state restoration.

### API Changes

* Nullable parameter in `Preference.onSaveInstanceState(@NonNull Parcelable savedState)`
  * No included preferences return `null` here.
* Non-null parameter in `Preference.onRestoreInstanceState(@NonNull Parcelable savedState)`

## 2.1.2 (2018-04-02)
- *FIXED:* Popup window exit transition now plays on API 24-25.
- Catch more error cases when loading ringtone picker dialog.

## 2.1.1 (2018-03-06)
- Catch more error cases when loading ringtone picker dialog.

## 2.1.0 (2018-03-05)
- Support library 27.0.0+ and min SDK 14 are now required.
  - You will get unexpected runtime exceptions if your project does not comply.
- *FIXED:* Version 2.0.x didn't publish javadoc and sources artifacts. We're back on track.
- `Fixes.updateLayoutInflaterFactory` is now deprecated and does nothing. It's no longer needed.
- *NEW!* Use `XpSupportPreferencePlugins.registerErrorInterceptor` to report internal non-fatal
  errors e.g. to Crashlytics. Read more in [README.md](README.md#reporting-errors).
  [Issue #93](https://github.com/consp1racy/android-support-preference/issues/93)
- Ringtone picker preference
  - *NEW:* Show 'Unknown' ringtone when previously selected external ringtone is no longer available.
    [Issue #97](https://github.com/consp1racy/android-support-preference/issues/97)
  - *NEW:* `RingtonePreference.get*String` values copied from Android Oreo in all available locales.
    [Issue #96](https://github.com/consp1racy/android-support-preference/issues/96) 
  - *FIXED:* Ringtone picker should no longer crash on orientation change or state restoration
    when the dialog fragment is restored before preference fragment.
    [Issue #87](https://github.com/consp1racy/android-support-preference/issues/87)
  - *FIXED:* Ringtone picker should no longer crash while listing ringtones on some devices.
    [Issue #92](https://github.com/consp1racy/android-support-preference/issues/92)

## 2.0.2 (2018-03-03)
- All libraries
  - Libraries do not automatically transitively pull support libraries 27.1.0. [Issue #91](https://github.com/consp1racy/android-support-preference/issues/91)
- `RingtonePreference`
  - `SortCursor` does not crash when sorting by null columns. [Issue #88](https://github.com/consp1racy/android-support-preference/issues/88)

## 2.0.1 (2018-03-01)
- Requires compile SDK 27 and at least support library 27.0.0.
- Changes in support-preference library
  - *FIXED:* `ListPreference` in simple menu mode doesn't crash with support libraries 27.1.0.
  - `app:asp_min` is now deprecated in favor of `app:min`. `app:asp_min` takes precedence if both are specified.
- Changes in support-spinner library
  - *NEW!* `XpAppCompatSpinner` uses `app:asp_simpleMenuMaxItemCount` attribute to limit popup menu height.
  - *NEW!* Supports `android:popupEnterTransition` and `android:popupExitTransition` on API 23+.
  - *FIXED:* Popup transitions on API 23+ are now smooth.
  - *FIXED:* Make best effort to position selected item over emitting widget when popup menu scrolls.
  - *FIXED:* `XpAppCompatSpinner` width is now better calculated from all items. Don't use with large data sets!

## <s>2.0.0 (2018-03-01)</s>

## 1.3.2 (2018-01-08)
- *FIXED:* Fixed behavior when `android:selectable="false"`. See #84.

## 1.3.1 (2017-10-04)
- *FIXED:* `RingtonePreference.canShowSelectedRingtoneTitle` should no longer crash 
  when no ringtone is selected

## 1.3.0 (2017-09-24)
- `SeekBarPreference` info text view can now be baseline aligned via `app:asp_infoAnchor` attribute 
  to either `@android:id/title`  or `@android:id/summary` (default).
- Added support for padding around dividers in `PreferenceDividerDecoration`.
- Added support for preference categories without a title.
- Changes to ringtone picker:
  - `XpRingtonePreferenceDialogFragment` doesn't crash when trying to play default ringtone
    from external storage without the `READ_EXTERNAL_STORAGE` permission; instead is silent.
  - Added `RingtonePreference.OnFailedToReadRingtoneListener` to notify you 
    about such potential case. Valid courses of actions are:
    - Ignore the error and show the picker anyway using 
      `RingtonePreference.showDialogFragment(XpPreferenceFragment)` (default),
    - Open the system picker and process its result using 
      `RingtonePreference.buildRingtonePickerIntent()` and 
      `RingtonePreference.onActivityResult(Intent)` respectively.<br> 
      The system picker can access external storage but does not share your app's theme.

## 1.2.8 (2017-09-19)
- Added `XpAppCompatSpinner.setEntries` method.
- Preference text views now respect forced RTL for non-RTL content.

## 1.2.7 (2017-03-23)
- These libraries no longer contribute to the problem described here: http://stackoverflow.com/questions/42949974
- Ensured compatibility with support libraries back to v23.2.0.

## 1.2.6 (2017-03-15)
- *FIXED:* `RingtonePreference` no longer crashes, falls back to system Ringtone Picker Activity.

## 1.2.5 (2017-02-15)
- *FIXED:* Using `XpPreferenceHelpers` no longer causes memory leaks
  - `OnPreferenceLongClickListener` *MUST NOT* hold reference to outer class!

## 1.2.4 (2017-01-21)
- Added missing `CheckBoxPreference` constructor.
- Added methods controlling icon tint enabled state on `Preference`s.
- Popups now have basic epicenter awareness on API 24.
- Fixed potential crashes while using older versions of the support library.

## 1.2.2 (2017-01-18)
- `SeekBarDialogPreference` supports `app:min` attribute (since support libs 25.1.0)
- Updated consumer proguard rules.

## 1.2.1 (2016-12-14)
- Better compatibility with `SeekBarPreference` introduced in preference-v7 library 25.1.0.

## 1.2.0 (2016-11-27)
- *NEW!* Arbitrary `Preference`s now support long click listeners.
  - `PreferenceScreen`s now support long click listeners.
  - Use `XpPreferenceHelpers.setOnPreferenceLongClickListener`.
  - Breaking change in `OnPreferenceLongClickListener` API.
- *FIXED:*
  - Custom text appearance is now applied to explicitly referenced preferences.
  - `XpPreferenceHelpers` now applies custom text appearance to preferences created in Java.
  - `SwitchPreference` supports `Switch` with `@android:id/switch_widget` introduced in Android N. 
  - `AspAppCompatCheckedTextView` (part of `Fixes` for AppCompat v24.1.x) uses correct style.

## 1.1.1 (2016-11-27) *Unreleased*
- `SwitchCompat` is now used on all platforms and animates on all supported platforms.
  - Resolves [Crashes with native Switch](https://github.com/consp1racy/android-support-preference/issues/52).

## 1.1.0 (2016-11-27) *Unreleased*
- *NEW!* Arbitrary preferences support tinted icons and text appearance.
  - `PreferenceScreen`s support tinted icons and text appearance.
  - Tinted icons and text appearance accessible at runtime from `XpPreferenceHelpers` class.
- Fixed `NoSuchMethodError` introduced in last release when using AppCompat older than 25.

## 1.0.4
- 2016-10-17 `support-spinner` only release
  - Added support for an `onClickListener`.

## 1.0.3 (2016-09-06)
- *NEW!* `OnPreferenceLongClickListener` support for all preferences in `net.xpece.android.support.preference` package
  - <s>Not supported by `PreferenceScreen`.</s>

## 1.0.2
- 2016-08-01 `support-preference` only release 
  - *FIXED:* class resolution when using custom packages.
  - Renamed `XpAppCompat*` widgets (used by `Fixes`) to `AspAppCompat*` to avoid conflicts with my other library.
- 2016-08-18 `support-spinner` only release
  - `XpAppCompatSpinner` now supports support libs 24.2.0.
  - Minimum SDK raised from 7 to 9.

## 1.0.1 (2016-07-29) *Deprecated*
- `SpinnerAdapter ListPreference.buildAdapter(Context)` is deprecated in favor of:
  - `SpinnerAdapter buildSimpleMenuAdapter(Context)` - Used in simple menus.
  - `SpinnerAdapter buildSimpleDialogAdapter(Context)` - Used in simple dialogs.
  - Override the following methods to expose your data set:
    - `CharSequence[] getEntries()` - Item captions.
    - `CharSequence[] getEntryValues()` - Persisted item values.

## 1.0.0 (2016-07-24) *Deprecated*
- Depends on support libs 24.1.1 to avoid issues in 21.1.0.
- `Fixes.updateLayoutInflaterFactory(getLayoutInflater())` is back.
  - `CheckedTextView`s are once more incorrectly tinted since support libs 24.1.0. This will fix it.
  - Call in your activity after `super.onCreate()` and before `setContentView(...)`.
- Uses appcompat-v7 `ColorStateList` inflater so theme references are resolved below API 21.

## 0.9.1 (2016-07-22) *Deprecated*
- Public `XpPreferenceManager.setDefaultValues`.
  - Use this instead of `PreferenceManager.setDefaultValues`.
  - Takes into account preference classes with custom packages.
- <s>`ListPreference` exposes `buildAdapter` method.</s>
  - Now you can easily supply your own `SpinnerAdapter` inside a `ListPreference` subclass.
  - *Framework expects only one view type!*
- Depends on support libs 24.1.0 which are broken.

## 0.9.0 (2016-06-18) *Deprecated*
- Supports support libs 24.0.0.
- Cleanup.
- `asp_preferenceIconTint` is back along with `asp_preferenceDialogIconTint`.
  - You *have to* define these attributes in your theme. 
  - Recommended colors are either `?colorAccent` or `?colorControlNormal`.

```
<item name="asp_preferenceIconTint">?colorAccent</item>
<item name="asp_preferenceDialogIconTint">?asp_preferenceIconTint</item>
```

## 0.8.1 *Legacy*
- 2016-05-06 `support-spinner` only release.
  - Added `CheckedTypedItemAdapter` which is more versatile than `CheckedItemAdapter`.
  - *FIXED:* No more crashes when using empty adapter.
  - *FIXED:* Spinner no longer leaks popup window.
- 2016-05-11 `support-preference` only release.
  - Fixed crash in `RingtonePreference`.
    - If ringtones cannot be queried the preference tries to open system ringtone picker.
    - If system ringtone picker fails to open only "Silent" and "Default" options are shown if enabled.
  - Fixed crash in `EditTextPreference`.
    - `EditText` attributes are no longer passed on from `EditTextPreference` XML.
    - Use `EditTextPreference.setOnEditTextCreatedListener(OnEditTextCreatedListener)` for setup.
  - Fixed crash in `PreferenceScreenNavigationStrategy.ReplaceRoot`.
    - `onPreferenceScreenClick` can now be called before `onCreateView`. 

## 0.8.0 *Deprecated*
- *NEW!* Very much material `XpAppCompatSpinner` is now available as a standalone lib;rary.
- *FIXED:* Simple menu:
  - Correct vertical position when using asymmetric top and bottom padding on anchor.
  - Correct horizontal size when using `setDropDownMaxWidth(MATCH_PARENT)`.
  - Correct gravity in RTL configurations.
- Dropping `preferenceTint` attribute. `colorAccent` will be used for icons by default.
- `ReplaceRoot` subscreen navigation strategy now remembers precise scroll position.
- UI tweaks.

## 0.7.0 *Deprecated*
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

## 0.6.2 *Legacy*
- *FIXED:* Finally fixed simple menu position - no more arbitrary numbers + more effective.
- *FIXED:* Simple menu animation looks closer to platform default on Android 4.
  - Override `Animation.Asp.Popup` to change this behavior.
- *FIXED:* Better `SeekBarPreference` layout height on Android 6.

## 0.6.1 *Deprecated*
- *FIXED:* Dialog preferences respect `alertDialogTheme` when inflating icons and layouts.
- *FIXED:* When using simple color for `preferenceTint` disabled state is computed automatically.

## 0.6.0 *Deprecated*
- *NEW!* `ColorPreference` available as a separate module!
- *NEW!* `ReplaceFragment` subscreen navigation strategy allowing for fragment transition animations.
- Minor fixes.

## 0.5.11 *Deprecated*
- *FIXED:* Focused `SeekBarPreference` can now be controlled by `+` and `-` keys.
- *FIXED:* Simple menu.
  - Popup will now display correctly for various item counts in various positions.
  - Popup is clipped to parent recycler view with a 16dp margin as suggested by MD.
  - Limit: Simple menu is now disabled before Android 4. Falls back to simple dialog.
  - Limit: Displaying popup will not make parent recycler view scroll.

## 0.5.10 *Deprecated*
- *NEW!* Simple dialog variant of `ListPreference`. See above for instructions.
- *FIXED:* Simple menu.
  - Fixed simple menu height when using multiline items.
  - Centering multiline item over anchor view when using simple menu.
- *FIXED:* Recycling items with custom text appearance or text color.

## 0.5.9 *Deprecated*
- *FIXED:* Simple menu.
  - Showing simple menu is restored after screen orientation change.
  - Aligning simple menu width to 56dp/64dp grid on phones/tablets (customizable).

## 0.5.8 *Deprecated*
- *FIXED:* Simple menu.
  - Preselected option is now highlighted.
  - Fixed animation on Android 4 - fading in.
  - Fixed popup width on Android 4.
  - Two line items now have top and bottom padding.

## 0.5.6 *Deprecated*
- *NEW!* Custom title and summary text styles.
  - `app:titleTextAppearance` and `app:titleTextColor` for titles.
  - `app:subtitleTextAppearance` and `app:subtitleTextColor` for summaries.
  - Analogous methods available in Java.
- *FIXED:* Simple menu.
  - Popup window adjusts its width according to its own content.
  - Added top and bottom padding to popup window.
  - Items can have up to two lines of text (increased form one).
  - Added persistent scrollbar if all items don't fit on screen.

## 0.5.5 *Deprecated*
- ***Only supports appcompat-v7 with preference-v7 version 23.2.x!***

## 0.5.4 *Legacy*
- ***Last version that supports appcompat-v7 with preference-v7 version 23.1.1!***
- *NEW!* Simple menu variant of `ListPreference`.
  - Via `app:asp_simpleMenu="true"`.
  - Described here https://www.google.com/design/spec/components/menus.html#menus-simple-menus
  - Does not have top and bottom padding yet.
  - Menu position is unpredictable when having more than 3 menu items and the preference is first or last in the list.
  - To be updated later.
- *FIXED:* Small icon is properly aligned in RTL configurations.

## 0.5.3 *Deprecated*
- *FIXED:* Ringtone picker does not *need* `READ_EXTERNAL_STORAGE` permission even prior to Android 6.

## 0.5.2 *Deprecated*
- *FIXED:* `PreferenceScreenNavigationStrategy.ReplaceRoot` no longer crashes on screen rotation.
- *FIXED:* Ringtone picker does not stop playback on screen rotation.

## 0.5.1 *Deprecated*
- *NEW!* `PreferenceScreenNavigationStrategy` class.
- *FIXED:* Missing Proguard rules are now bundled with the library.
- *FIXED:* Incorrect icon size on Lollipop.

## 0.5.0 *Deprecated*
- ***Only supports appcompat-v7 with preference-v7 version 23.1.1!***
- *NEW!* Based on preference-v7 instead of native preferences.
- Updated appcompat-v7 library to 23.1.1.
    - Material SeekBar style across all platforms.
- `RingtonePreference` is now `DialogFragment` based.
- Unmanaged preference icons (such as that of `PreferenceScreen`) can be tinted via `PreferenceIconHelper`.
- Default preference icon tint specified by `preferenceTint` theme attribute.
- Fixed divider color.
- Sample contains `PreferenceScreen` subscreen handling.

## 0.4.3 *Legacy*
- ***Last fully supported appcompat-v7 version is 23.0.1. After that ringtone picker crashes!***
- No more `Resources.NotFoundException` in `RingtonePickerActivity`. Falls back to English.
- Updated appcompat-v7 library to 22.2.1.

## 0.4.2 *Deprecated*
- <s>`SeekBar` tinting can be turned off via `app:asp_tintSeekBar="false"`</s>
- Introduced missing `seekBarDialogPreference` style

## 0.4.1 *Deprecated*
- Ringtone picker strings are now taken dynamically from `android` and `com.android.providers.media` packages, falls back to English
    - <s>These are accessible via `RingtonePickerActivity.get*String(Context)`</s>

## 0.4.0 *Deprecated*
- *NEW!* Implemented SeekBarPreference according to http://www.google.com/design/spec/components/dialogs.html#dialogs-confirmation-dialogs
- *FIXED:* tinting/padding in DialogPreference and SeekBarDialogPreference
- <s>AppCompatPreferenceActivity and PreferenceFragment now implement Factory<Preference></s>
- *NEW!* app:asp_dialogIconPaddingEnabled attribute

## 0.3.0 *Deprecated*
- Removed `MultiCheckPreference` as it was only partially implemented and `MultiSelectListPreference` provides the same function.
- `MultiSelectListPreference` is now available since API 7 (formerly API 11). Uses `JSONArray` to persist `Set<String>`.
- API for persisting and accessing string sets since API 7 is available via `SharedPreferencesCompat`.
- `Preference`s now support `app:asp_iconPaddingEnabled` attribute which allows to better align non-launcher icons to 16dp keyline.
- <s>Custom preferences are now recycled which fixed animation issues on Lollipop.</s>
- Custom preferences are now always inflated on all platforms if using `AppCompatPreferenceActivity` and/or custom `PreferenceFragment`.
- Library no longer includes `android.permission.READ_EXTERNAL_STORAGE` permission (used to read ringtones). You have to do it yourself.
    - This is needed because the custom picker is part of the app and not provided by system.
    - <s>You are of course free to use `android.preference.RingtonePreference` when necessary.</s>

## 0.2.2 *Deprecated*
- optional tinting <s>via `app:asp_tintIcon="true"` and `app:asp_tintDialogIcon="true"` and `asp_tint` and `asp_tintMode`</s>.

## 0.2.1 *Deprecated*
- No need for `net.xpece.android.support.preference.` prefix in XML files defining preferences, framework will choose automatically:
    - <s>On Lollipop native `Preference`, `CheckBoxPreference`, `SwitchPreference` will be used.</s>
    - <s>Otherwise support version will be used.</s>
    - <s>Force either version by using fully qualified class name.</s>
    - <s>You need to use `AppCompatPreferenceActivity` or special `PreferenceFragment` both of which are provided.</s>
- <s>Added `PreferenceCompat#setChecked(Preference, boolean)` helper method.</s>

## 0.1.2 *Deprecated*
- Czech strings
- `SeekBar` in `SeekBarDialogActivity` uses `ColorFilter` to match theme

## 0.1.1 *Deprecated*
- Initial release
- Backported material style and icon capability for `Preference` children
- Backported `SwitchPreference`
- Material styled `RingtonePreference` picker dialog/activity
