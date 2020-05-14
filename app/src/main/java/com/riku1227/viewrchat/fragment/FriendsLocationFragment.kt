package com.riku1227.viewrchat.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
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

    private lateinit var viewmodel: FriendsLocationFragmentViewModel

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_friends_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewmodel = ViewModelProvider(this).get(FriendsLocationFragmentViewModel::class.java)

        if(viewmodel.friendsLocationRecyclerAdapter != null) {
            fragmentFriendsLocationRecycler.adapter = viewmodel.friendsLocationRecyclerAdapter
            val layoutManager = LinearLayoutManager(requireContext())
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            fragmentFriendsLocationRecycler.layoutManager = layoutManager
        } else {
            val dispo = VRChatlin.get(requireContext()).APIService().getFriends(offline = false, n = 50)
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

                        if(locationList.size > 0) {
                            fragmentFriendsLocationRecycler.visibility = View.VISIBLE
                            fragmentFriendsLocationNoneUserRoot.visibility = View.GONE
                            viewmodel.friendsLocationRecyclerAdapter = FriendsLocationRecyclerAdapter(requireContext(), this, locationMap, locationList, it.size)
                            fragmentFriendsLocationRecycler.adapter = viewmodel.friendsLocationRecyclerAdapter
                            val layoutManager = LinearLayoutManager(requireContext())
                            layoutManager.orientation = LinearLayoutManager.VERTICAL
                            fragmentFriendsLocationRecycler.layoutManager = layoutManager
                        } else {
                            fragmentFriendsLocationRecycler.visibility = View.GONE
                            fragmentFriendsLocationNoneUserRoot.visibility = View.VISIBLE
                        }
                    },
                    {
                        ErrorHandling.onNetworkError(it, requireContext(), fragment = this)
                    }
                )

            compositeDisposable.add(dispo)
        }
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }
}