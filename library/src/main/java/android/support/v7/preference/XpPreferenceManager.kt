package android.support.v7.preference

import android.content.Context
import android.os.Build
import android.support.v4.content.SharedPreferencesCompat
import java.lang.reflect.Method
import java.util.*

/**
 * @author Eugen on 6. 12. 2015.
 */
class XpPreferenceManager(context: Context, val mCustomDefaultPackages: Array<String>? = null) : PreferenceManager(context) {

    private val mAllDefaultPackages: Array<String> by lazy {
        if (mCustomDefaultPackages == null || mCustomDefaultPackages.size == 0) {
            DEFAULT_PACKAGES
        } else {
            val allDefaultPackagesSet = HashSet<String>(mCustomDefaultPackages.size + DEFAULT_PACKAGES.size)
            Collections.addAll(allDefaultPackagesSet, *mCustomDefaultPackages)
            Collections.addAll(allDefaultPackagesSet, *DEFAULT_PACKAGES)
            allDefaultPackagesSet.toTypedArray()
        }
    }

    private fun setNoCommit(noCommit: Boolean) {
        try {
            METHOD_SET_NO_COMMIT!!.invoke(this, noCommit)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun inflateFromResource(context: Context, resId: Int, rootPreferences: PreferenceScreen?): PreferenceScreen {
        this.setNoCommit(true)
        val inflater = PreferenceInflater(context, this)
        inflater.defaultPackages = mAllDefaultPackages
        val newRootPreferences = inflater.inflate(resId, rootPreferences) as PreferenceScreen
        newRootPreferences.onAttachedToHierarchy(this)
        this.setNoCommit(false)
        return newRootPreferences
    }

    companion object {

        private val METHOD_SET_NO_COMMIT: Method?

        private val DEFAULT_PACKAGES: Array<String>

        init {
            var setNoCommit: Method? = null
            try {
                setNoCommit = PreferenceManager::class.java.getDeclaredMethod("setNoCommit", Boolean::class.java)
                setNoCommit!!.isAccessible = true
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            }

            METHOD_SET_NO_COMMIT = setNoCommit

            if (Build.VERSION.SDK_INT < 14) {
                DEFAULT_PACKAGES = arrayOf("net.xpece.android.support.preference.", "android.support.v7.preference.")
            } else {
                DEFAULT_PACKAGES = arrayOf("net.xpece.android.support.preference.", "android.support.v14.preference.", "android.support.v7.preference.")
            }
        }

        @JvmStatic
        fun setDefaultValues(context: Context, resId: Int, readAgain: Boolean, customDefaultPackages: Array<String>? = null) {
            setDefaultValues(context, getDefaultSharedPreferencesName(context), getDefaultSharedPreferencesMode(), resId, readAgain, customDefaultPackages)
        }

        @JvmStatic
        fun setDefaultValues(context: Context, sharedPreferencesName: String, sharedPreferencesMode: Int, resId: Int, readAgain: Boolean, customDefaultPackages: Array<String>? = null) {
            val defaultValueSp = context.getSharedPreferences(KEY_HAS_SET_DEFAULT_VALUES, 0)
            if (readAgain || !defaultValueSp.getBoolean(KEY_HAS_SET_DEFAULT_VALUES, false)) {
                val pm = XpPreferenceManager(context, customDefaultPackages)
                pm.sharedPreferencesName = sharedPreferencesName
                pm.sharedPreferencesMode = sharedPreferencesMode
                pm.inflateFromResource(context, resId, null)
                val editor = defaultValueSp.edit().putBoolean(KEY_HAS_SET_DEFAULT_VALUES, true)
                SharedPreferencesCompat.EditorCompat.getInstance().apply(editor)
            }
        }

        @JvmStatic
        private fun getDefaultSharedPreferencesName(context: Context): String =
                context.packageName + "_preferences"

        @JvmStatic
        private fun getDefaultSharedPreferencesMode(): Int =
                Context.MODE_PRIVATE
    }
}
