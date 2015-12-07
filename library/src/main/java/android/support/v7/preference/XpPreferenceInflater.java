//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.support.v7.preference;

import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.InflateException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;

class XpPreferenceInflater {
    private static final String TAG = "PreferenceInflater";
    private static final Class<?>[] CONSTRUCTOR_SIGNATURE = new Class[]{Context.class, AttributeSet.class};
    private static final HashMap<String, Constructor> CONSTRUCTOR_MAP = new HashMap<>();
    private final Context mContext;
    private final Object[] mConstructorArgs = new Object[2];
    private PreferenceManager mPreferenceManager;
    private String[] mDefaultPackages;
    private static final String INTENT_TAG_NAME = "intent";
    private static final String EXTRA_TAG_NAME = "extra";

    public XpPreferenceInflater(Context context, PreferenceManager preferenceManager) {
        this.mContext = context;
        this.init(preferenceManager);
    }

    private void init(PreferenceManager preferenceManager) {
        this.mPreferenceManager = preferenceManager;
        if (Build.VERSION.SDK_INT >= 14) {
            setDefaultPackages(new String[]{"net.xpece.android.support.preference.", "android.support.v14.preference.", "android.support.v7.preference."});
        } else {
            setDefaultPackages(new String[]{"net.xpece.android.support.preference.", "android.support.v7.preference."});
        }
    }

    public void setDefaultPackages(String[] defaultPackage) {
        this.mDefaultPackages = defaultPackage;
    }

    public String[] getDefaultPackages() {
        return this.mDefaultPackages;
    }

    public Context getContext() {
        return this.mContext;
    }

    public Preference inflate(int resource, @Nullable PreferenceGroup root) {
        XmlResourceParser parser = this.getContext().getResources().getXml(resource);

        Preference var4;
        try {
            var4 = this.inflate(parser, root);
        } finally {
            parser.close();
        }

        return var4;
    }

    public Preference inflate(XmlPullParser parser, @Nullable PreferenceGroup root) {
        Object[] var3 = this.mConstructorArgs;
        synchronized(this.mConstructorArgs) {
            AttributeSet attrs = Xml.asAttributeSet(parser);
            this.mConstructorArgs[0] = this.mContext;

            PreferenceGroup result;
            InflateException ex;
            try {
                int e;
                do {
                    e = parser.next();
                } while(e != 2 && e != 1);

                if(e != 2) {
                    throw new InflateException(parser.getPositionDescription() + ": No start tag found!");
                }

                Preference ex1 = this.createItemFromTag(parser.getName(), attrs);
                result = this.onMergeRoots(root, (PreferenceGroup)ex1);
                this.rInflate(parser, result, attrs);
            } catch (InflateException var9) {
                throw var9;
            } catch (XmlPullParserException var10) {
                ex = new InflateException(var10.getMessage());
                ex.initCause(var10);
                throw ex;
            } catch (IOException var11) {
                ex = new InflateException(parser.getPositionDescription() + ": " + var11.getMessage());
                ex.initCause(var11);
                throw ex;
            }

            return result;
        }
    }

    @NonNull
    private PreferenceGroup onMergeRoots(PreferenceGroup givenRoot, @NonNull PreferenceGroup xmlRoot) {
        if(givenRoot == null) {
            xmlRoot.onAttachedToHierarchy(this.mPreferenceManager);
            return xmlRoot;
        } else {
            return givenRoot;
        }
    }

    private Preference createItem(@NonNull String name, @Nullable String[] prefixes, AttributeSet attrs) throws ClassNotFoundException, InflateException {
        Constructor constructor = (Constructor)CONSTRUCTOR_MAP.get(name);

        try {
            if(constructor == null) {
                ClassLoader e = this.mContext.getClassLoader();
                Class var17 = null;
                if(prefixes != null && prefixes.length != 0) {
                    ClassNotFoundException notFoundException = null;
                    String[] arr$ = prefixes;
                    int len$ = prefixes.length;

                    for(int i$ = 0; i$ < len$; ++i$) {
                        String prefix = arr$[i$];

                        try {
                            var17 = e.loadClass(prefix + name);
                            break;
                        } catch (ClassNotFoundException var13) {
                            notFoundException = var13;
                        }
                    }

                    if(var17 == null) {
                        if(notFoundException == null) {
                            throw new InflateException(attrs.getPositionDescription() + ": Error inflating class " + name);
                        }

                        throw notFoundException;
                    }
                } else {
                    var17 = e.loadClass(name);
                }

                constructor = var17.getConstructor(CONSTRUCTOR_SIGNATURE);
                constructor.setAccessible(true);
                CONSTRUCTOR_MAP.put(name, constructor);
            }

            Object[] var16 = this.mConstructorArgs;
            var16[1] = attrs;
            return (Preference)constructor.newInstance(var16);
        } catch (ClassNotFoundException var14) {
            throw var14;
        } catch (Exception var15) {
            InflateException ie = new InflateException(attrs.getPositionDescription() + ": Error inflating class " + name);
            ie.initCause(var15);
            throw ie;
        }
    }

    protected Preference onCreateItem(String name, AttributeSet attrs) throws ClassNotFoundException {
        return this.createItem(name, this.mDefaultPackages, attrs);
    }

    private Preference createItemFromTag(String name, AttributeSet attrs) {
        InflateException ie;
        try {
            Preference e;
            if(-1 == name.indexOf(46)) {
                e = this.onCreateItem(name, attrs);
            } else {
                e = this.createItem(name, (String[])null, attrs);
            }

            return e;
        } catch (InflateException var5) {
            throw var5;
        } catch (ClassNotFoundException var6) {
            ie = new InflateException(attrs.getPositionDescription() + ": Error inflating class (not found)" + name);
            ie.initCause(var6);
            throw ie;
        } catch (Exception var7) {
            ie = new InflateException(attrs.getPositionDescription() + ": Error inflating class " + name);
            ie.initCause(var7);
            throw ie;
        }
    }

    private void rInflate(XmlPullParser parser, Preference parent, AttributeSet attrs) throws XmlPullParserException, IOException {
        int depth = parser.getDepth();

        int type;
        while(((type = parser.next()) != 3 || parser.getDepth() > depth) && type != 1) {
            if(type == 2) {
                String name = parser.getName();
                if("intent".equals(name)) {
                    Intent item;
                    try {
                        item = Intent.parseIntent(this.getContext().getResources(), parser, attrs);
                    } catch (IOException var11) {
                        XmlPullParserException ex1 = new XmlPullParserException("Error parsing preference");
                        ex1.initCause(var11);
                        throw ex1;
                    }

                    parent.setIntent(item);
                } else if("extra".equals(name)) {
                    this.getContext().getResources().parseBundleExtra("extra", attrs, parent.getExtras());

                    try {
                        skipCurrentTag(parser);
                    } catch (IOException var10) {
                        XmlPullParserException ex = new XmlPullParserException("Error parsing preference");
                        ex.initCause(var10);
                        throw ex;
                    }
                } else {
                    Preference item1 = this.createItemFromTag(name, attrs);
                    ((PreferenceGroup)parent).addItemFromInflater(item1);
                    this.rInflate(parser, item1, attrs);
                }
            }
        }

    }

    private static void skipCurrentTag(XmlPullParser parser) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();

        int type;
        do {
            type = parser.next();
        } while(type != 1 && (type != 3 || parser.getDepth() > outerDepth));

    }
}
