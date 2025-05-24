// com.mulosbron.goldbazaar.util.ThemeManager.kt
package com.mulosbron.goldbazaar.util

import android.content.Context
import com.mulosbron.goldbazaar.R
import com.mulosbron.goldbazaar.util.ThemeManager.Palette.Companion.fromKey

object ThemeManager {

    enum class Palette(val key: String, val themeResId: Int) {
        PALETTE1("palette1", R.style.Theme_GoldBazaar_Palette1),
        PALETTE4("palette4", R.style.Theme_GoldBazaar_Palette4),
        PALETTE5("palette5", R.style.Theme_GoldBazaar_Palette5);

        companion object {
            fun fromKey(key: String?): Palette {
                return entries.firstOrNull { it.key == key } ?: PALETTE1
            }
        }
    }

    fun getCurrentPalette(context: Context): Palette {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return fromKey(prefs.getString("theme_palette", Palette.PALETTE1.key))
    }

    fun getCurrentThemeRes(context: Context): Int {
        return getCurrentPalette(context).themeResId
    }

    fun savePalette(context: Context, palette: Palette) {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit()
            .putString("theme_palette", palette.key)
            .apply()
    }
}
