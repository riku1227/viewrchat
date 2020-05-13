package com.riku1227.viewrchat.view_model

import androidx.lifecycle.ViewModel
import com.riku1227.viewrchat.adapter.FriendsListRecyclerAdapter

class FriendsListFragmentViewModel : ViewModel() {
    var friendsListRecyclerAdapter: FriendsListRecyclerAdapter? = null
}