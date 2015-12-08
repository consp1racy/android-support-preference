package net.xpece.android.support.preference;

import android.content.Context;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.view.LayoutInflaterFactory;
import android.support.v7.widget.XpAppCompatCheckedTextView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import java.lang.reflect.Field;

/**
 * @author Eugen on 7. 12. 2015.
 */
public class Fixes {
    private Fixes() {}

    public static void updateLayoutInflaterFactory(LayoutInflater layoutInflater) {
        final LayoutInflater.Factory existingFactory = layoutInflater.getFactory();
        try {
            Field field = LayoutInflater.class.getDeclaredField("mFactorySet");
            field.setAccessible(true);
            field.setBoolean(layoutInflater, false);
            LayoutInflaterCompat.setFactory(layoutInflater, new LayoutInflaterFactory() {
                private LayoutInflaterFactory fixedFactory = new FixedFactory();

                @Override
                public View onCreateView(View parent, String name, final Context context, AttributeSet attrs) {
                    View view = fixedFactory.onCreateView(parent, name, context, attrs);
                    if (view == null) {
                        if (existingFactory != null) {
                            view = existingFactory.onCreateView(name, context, attrs);
                        }
                    }
                    return view;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class FixedFactory implements LayoutInflaterFactory {

        @Override
        public View onCreateView(final View parent, final String name, final Context context, final AttributeSet attrs) {
            if ("CheckedTextView".equals(name)) {
                return new XpAppCompatCheckedTextView(context, attrs);
            }
            return null;
        }
    }
}
