package com.riku1227.viewrchat.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.riku1227.viewrchat.R
import com.riku1227.viewrchat.adapter.FriendsListRecyclerAdapter
import com.riku1227.viewrchat.system.ErrorHandling
import com.riku1227.viewrchat.view_model.FriendsListFragmentViewModel
import com.riku1227.vrchatlin.VRChatlin
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_friends_list.*

class FriendsListFragment : Fragment() {

    private lateinit var viewmodel: FriendsListFragmentViewModel

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var currentRefreshTime = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_friends_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewmodel = ViewModelProvider(this).get(FriendsListFragmentViewModel::class.java)

        if(viewmodel.friendsListRecyclerAdapter != null) {
            fragmentFriendsListRecycler.adapter = viewmodel.friendsListRecyclerAdapter
            val layoutManager = LinearLayoutManager(requireContext())
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            fragmentFriendsListRecycler.layoutManager = layoutManager
        } else {
            createFriendsList()
        }

        fragmentFriendsListSwipeRefreshLayout.setOnRefreshListener {
            val nowTime = System.currentTimeMillis() / 1000
            val diffTime = nowTime - currentRefreshTime
            if(diffTime >= 30) {
                createFriendsList()
            } else {
                Snackbar.make(fragmentFriendsListSwipeRefreshLayout, R.string.general_cooltime_now, Snackbar.LENGTH_SHORT).show()
                fragmentFriendsListSwipeRefreshLayout.isRefreshing = false
            }
        }
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private fun createFriendsList() {
        val disposable = VRChatlin.get(requireContext()).APIService().getFriends(n = 50, offline = false)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    currentRefreshTime = System.currentTimeMillis() / 1000

                    if(viewmodel.friendsListRecyclerAdapter == null) {
                        viewmodel.friendsListRecyclerAdapter = FriendsListRecyclerAdapter(requireContext(), it)
                        fragmentFriendsListRecycler.adapter = viewmodel.friendsListRecyclerAdapter
                        val layoutManager = LinearLayoutManager(requireContext())
                        layoutManager.orientation = LinearLayoutManager.VERTICAL
                        fragmentFriendsListRecycler.layoutManager = layoutManager
                    } else {
                        viewmodel.friendsListRecyclerAdapter?.let { adapter ->
                            adapter.friendsList = it
                            adapter.notifyDataSetChanged()
                            adapter.resetCount()
                            fragmentFriendsListSwipeRefreshLayout.isRefreshing = false
                        }
                    }
                },
                {
                    ErrorHandling.onNetworkError(it, requireContext(), fragment = this)
                }
            )

        compositeDisposable.add(disposable)
    }
}