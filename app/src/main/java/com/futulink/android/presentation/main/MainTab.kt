package com.futulink.android.presentation.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.futulink.android.R

/**
 * The two bottom-bar destinations. With exactly two static tabs an enum plus rememberSaveable
 * is enough; a navigation framework would add dependencies without adding behaviour.
 */
enum class MainTab(
    @param:StringRes val labelResId: Int,
    @param:DrawableRes val iconResId: Int,
) {
    TEST(R.string.tab_test, R.drawable.ic_wifi),
    STATISTICS(R.string.tab_statistics, R.drawable.ic_history),
}
