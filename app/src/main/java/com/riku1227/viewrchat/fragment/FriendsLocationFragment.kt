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
import com.riku1227.viewrchat.adapter.FriendsLocationRecyclerAdapter
import com.riku1227.viewrchat.system.ErrorHandling
import com.riku1227.viewrchat.view_model.FriendsLocationFragmentViewModel
import com.riku1227.vrchatlin.VRChatlin
import com.riku1227.vrchatlin.model.VRChatUser
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_friends_location.*


class FriendsLocationFragment : Fragment() {

    private lateinit var viewModel: FriendsLocationFragmentViewModel

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    private var currentRefreshTime = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_friends_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(FriendsLocationFragmentViewModel::class.java)

        if(viewModel.friendsLocationRecyclerAdapter != null) {
            if(viewModel.friendsLocationRecyclerAdapter!!.itemCount > 0) {
                fragmentFriendsLocationSwipeRefreshLayout.visibility = View.VISIBLE
                fragmentFriendsLocationNoneUserRoot.visibility = View.GONE
                fragmentFriendsLocationRecycler.adapter = viewModel.friendsLocationRecyclerAdapter
                val layoutManager = LinearLayoutManager(requireContext())
                layoutManager.orientation = LinearLayoutManager.VERTICAL
                fragmentFriendsLocationRecycler.layoutManager = layoutManager
            } else {
                fragmentFriendsLocationSwipeRefreshLayout.visibility = View.GONE
                fragmentFriendsLocationNoneUserRoot.visibility = View.VISIBLE
            }
        } else {
            createFriendsLocationList()
        }

        fragmentFriendsLocationSwipeRefreshLayout.setOnRefreshListener {
            val nowTime = System.currentTimeMillis() / 1000
            val diffTime = nowTime - currentRefreshTime
            if(diffTime >= 30) {
                createFriendsLocationList()
            } else {
                Snackbar.make(fragmentFriendsLocationSwipeRefreshLayout, R.string.general_cooltime_now, Snackbar.LENGTH_SHORT).show()
                fragmentFriendsLocationSwipeRefreshLayout.isRefreshing = false
            }
        }
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    private fun createFriendsLocationList() {
        val getFriendsDisposable = VRChatlin.get(requireContext()).APIService().getFriends(offline = false, n = 50)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    val locationMap = mutableMapOf<String, ArrayList<VRChatUser>>()
                    val locationList = arrayListOf<String>()
                    for (user in it) {
                        user.location?.let {  userLocation ->
                            if(userLocation != "private" && userLocation != "offline") {
                                if(locationMap[userLocation] == null) {
                                    locationMap[userLocation] = arrayListOf(user)
                                    locationList.add(userLocation)
                                } else {
                                    locationMap[userLocation]!!.add(user)
                                }
                            }
                        }
                    }

                    currentRefreshTime = System.currentTimeMillis() / 1000

                    if (viewModel.friendsLocationRecyclerAdapter == null) {
                        viewModel.friendsLocationRecyclerAdapter = FriendsLocationRecyclerAdapter(requireContext(), this, compositeDisposable, locationMap, locationList, it.size)

                        if(locationList.size > 0) {
                            fragmentFriendsLocationSwipeRefreshLayout.visibility = View.VISIBLE
                            fragmentFriendsLocationNoneUserRoot.visibility = View.GONE
                            fragmentFriendsLocationRecycler.adapter = viewModel.friendsLocationRecyclerAdapter
                            val layoutManager = LinearLayoutManager(requireContext())
                            layoutManager.orientation = LinearLayoutManager.VERTICAL
                            fragmentFriendsLocationRecycler.layoutManager = layoutManager
                        } else {
                            fragmentFriendsLocationRecycler.visibility = View.GONE
                            fragmentFriendsLocationNoneUserRoot.visibility = View.VISIBLE
                        }
                    } else {
                        viewModel.friendsLocationRecyclerAdapter?.let { friendsLocationAdapter  ->
                            friendsLocationAdapter.locationMap = locationMap
                            friendsLocationAdapter.locationList = locationList
                            friendsLocationAdapter.currentCount = it.size

                            friendsLocationAdapter.notifyDataSetChanged()
                            fragmentFriendsLocationSwipeRefreshLayout.isRefreshing = false
                        }

                        if(locationList.size > 0) {
                            fragmentFriendsLocationSwipeRefreshLayout.visibility = View.VISIBLE
                            fragmentFriendsLocationNoneUserRoot.visibility = View.GONE
                        } else {
                            fragmentFriendsLocationSwipeRefreshLayout.visibility = View.GONE
                            fragmentFriendsLocationNoneUserRoot.visibility = View.VISIBLE
                        }
                    }
                },
                {
                    ErrorHandling.onNetworkError(it, requireContext(), fragment = this)
                }
            )
        compositeDisposable.add(getFriendsDisposable)
    }
}