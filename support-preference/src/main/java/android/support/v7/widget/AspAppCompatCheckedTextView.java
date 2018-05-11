/*
 * Copyright (C) 2014 The Android Open Source Project
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

package android.support.v7.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.util.AttributeSet;
import android.util.Log;

import javax.annotation.ParametersAreNonnullByDefault;

@Deprecated
@ParametersAreNonnullByDefault
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class AspAppCompatCheckedTextView extends AppCompatCheckedTextView {
    private static final String TAG = AspAppCompatCheckedTextView.class.getSimpleName();

    public AspAppCompatCheckedTextView(Context context) {
        super(context);
        Log.w(TAG, "This class is deprecated and will be removed.");
    }

    public AspAppCompatCheckedTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Log.w(TAG, "This class is deprecated and will be removed.");
    }

    public AspAppCompatCheckedTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.w(TAG, "This class is deprecated and will be removed.");
    }

    @Deprecated
    @RequiresApi(21)
    public AspAppCompatCheckedTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr);
        Log.w(TAG, "defStyleRes parameter is ignored.");
    }
}