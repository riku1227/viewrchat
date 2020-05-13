package com.riku1227.viewrchat.navigator.nav_host


import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import com.riku1227.viewrchat.navigator.BottomNavigationViewNavigator

class BottomNavigationViewNavHost : NavHostFragment() {
    override fun createFragmentNavigator(): Navigator<out FragmentNavigator.Destination> {
        return BottomNavigationViewNavigator(requireContext(), childFragmentManager, id)
    }
}