package com.riku1227.viewrchat.navigator

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigator

@Navigator.Name("bottom_navigation_view_navigator")
class BottomNavigationViewNavigator (
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val containerId: Int
): FragmentNavigator(context, fragmentManager, containerId) {
    private var currentTag = ""
    override fun navigate(
        destination: Destination,
        args: Bundle?,
        navOptions: NavOptions?,
        navigatorExtras: Navigator.Extras?
    ): NavDestination? {
        if(fragmentManager.isStateSaved) {
            return null
        }

        var className = destination.className
        if(className[0] == '.') {
            className = context.packageName + className
        }

        val currentFragment = fragmentManager.primaryNavigationFragment
        val fragmentTransaction = fragmentManager.beginTransaction()
        currentFragment?.let {
            fragmentTransaction.hide(it)
        }

        val tag = destination.id.toString()
        if(currentTag.isEmpty() || currentTag != tag) {
            var fragment = fragmentManager.findFragmentByTag(tag)

            if(fragment == null) {
                fragment = fragmentManager.fragmentFactory.instantiate(context.classLoader, className)
                fragmentTransaction.add(containerId, fragment, tag)
            }
            fragment.arguments = args

            fragmentTransaction.show(fragment)
            fragmentTransaction.setPrimaryNavigationFragment(fragment)
            fragmentTransaction.commit()
            currentTag = tag
        }

        return destination
    }
}