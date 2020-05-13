package com.riku1227.viewrchat.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.riku1227.viewrchat.R
import com.riku1227.viewrchat.adapter.FriendsListRecyclerAdapter
import com.riku1227.viewrchat.view_model.FriendsListFragmentViewModel
import com.riku1227.vrchatlin.VRChatlin
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_friends_list.*

class FriendsListFragment : Fragment() {

    private lateinit var viewmodel: FriendsListFragmentViewModel

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

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
            val disposable = VRChatlin.get(requireContext()).APIService().getFriends(n = 50, offline = false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        viewmodel.friendsListRecyclerAdapter = FriendsListRecyclerAdapter(requireContext(), it)
                        fragmentFriendsListRecycler.adapter = viewmodel.friendsListRecyclerAdapter
                        val layoutManager = LinearLayoutManager(requireContext())
                        layoutManager.orientation = LinearLayoutManager.VERTICAL
                        fragmentFriendsListRecycler.layoutManager = layoutManager
                    },
                    {}
                )

            compositeDisposable.add(disposable)
        }
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }
}