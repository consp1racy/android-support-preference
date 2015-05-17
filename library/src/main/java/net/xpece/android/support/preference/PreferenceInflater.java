/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.xpece.android.support.preference;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * The {@link PreferenceInflater} is used to inflate preference hierarchies from
 * XML files.
 */
class PreferenceInflater extends GenericInflater<android.preference.Preference, PreferenceGroup> {
    private static final String TAG = "PreferenceInflater";
    private static final String INTENT_TAG_NAME = "intent";
    private static final String EXTRA_TAG_NAME = "extra";

    private PreferenceManager mPreferenceManager;

    public PreferenceInflater(Context context, PreferenceManager preferenceManager) {
        super(context);
        init(preferenceManager);
    }

    PreferenceInflater(GenericInflater<android.preference.Preference, PreferenceGroup> original, PreferenceManager preferenceManager, Context newContext) {
        super(original, newContext);
        init(preferenceManager);
    }

    @Override
    public GenericInflater<android.preference.Preference, PreferenceGroup> cloneInContext(Context newContext) {
        return new PreferenceInflater(this, mPreferenceManager, newContext);
    }

    private void init(PreferenceManager preferenceManager) {
        mPreferenceManager = preferenceManager;
        setDefaultPackage("android.preference.");
    }

    @Override
    protected boolean onCreateCustomFromTag(XmlPullParser parser, android.preference.Preference parentPreference,
                                            AttributeSet attrs) throws XmlPullParserException {
        final String tag = parser.getName();

        if (tag.equals(INTENT_TAG_NAME)) {
            Intent intent = null;

            try {
                intent = Intent.parseIntent(getContext().getResources(), parser, attrs);
            } catch (IOException e) {
                XmlPullParserException ex = new XmlPullParserException(
                    "Error parsing preference");
                ex.initCause(e);
                throw ex;
            }

            if (intent != null) {
                parentPreference.setIntent(intent);
            }

            return true;
        } else if (Build.VERSION.SDK_INT >= 11 && tag.equals(EXTRA_TAG_NAME)) {
            getContext().getResources().parseBundleExtra(EXTRA_TAG_NAME, attrs,
                parentPreference.getExtras());
            try {
                Util.skipCurrentTag(parser);
            } catch (IOException e) {
                XmlPullParserException ex = new XmlPullParserException(
                    "Error parsing preference");
                ex.initCause(e);
                throw ex;
            }
            return true;
        }

        return false;
    }

    @Override
    protected android.preference.Preference onCreateItem(String name, AttributeSet attrs) throws ClassNotFoundException {
        switch (name) {
            case "SeekBarDialogPreference":
                // is not public
                return new SeekBarDialogPreference(mContext, attrs);
            case "MultiCheckPreference":
                // is not public
                return new MultiCheckPreference(mContext, attrs);
            case "RingtonePreference":
                // we need custom theme colors
                return new RingtonePreference(mContext, attrs);
            case "EditTextPreference":
                // in favor of one dialog theme
                return new EditTextPreference(mContext, attrs);
            case "ListPreference":
                // in favor of one dialog theme
                return new ListPreference(mContext, attrs);
            case "MultiSelectListPreference":
                // in favor of one dialog theme
                return new MultiSelectListPreference(mContext, attrs);
        }
        if (Build.VERSION.SDK_INT < 21) {
            // using native counterparts on lollipop when feasible
            switch (name) {
                case "Preference":
                    return new Preference(mContext, attrs);
                case "CheckBoxPreference":
                    return new CheckBoxPreference(mContext, attrs);
                case "SwitchPreference":
                    return new SwitchPreference(mContext, attrs);
            }
        }
        return super.onCreateItem(name, attrs);
    }

    @Override
    protected PreferenceGroup onMergeRoots(PreferenceGroup givenRoot, boolean attachToGivenRoot,
                                           PreferenceGroup xmlRoot) {
        // If we were given a Preferences, use it as the root (ignoring the root
        // Preferences from the XML file).
        if (givenRoot == null) {
//            xmlRoot.onAttachedToHierarchy(mPreferenceManager);
            PreferenceCompat.onAttachedToHierarchy(xmlRoot, mPreferenceManager);
            return xmlRoot;
        } else {
            return givenRoot;
        }
    }

}
