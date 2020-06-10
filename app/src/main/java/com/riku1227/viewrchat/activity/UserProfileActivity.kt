package com.riku1227.viewrchat.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.riku1227.viewrchat.R
import com.riku1227.viewrchat.ViewRChat
import com.riku1227.viewrchat.adapter.UserLanguagesRecyclerAdapter
import com.riku1227.viewrchat.adapter.UserLinksRecyclerAdapter
import com.riku1227.viewrchat.system.CacheSystem
import com.riku1227.viewrchat.system.ErrorHandling
import com.riku1227.viewrchat.util.SettingsUtil
import com.riku1227.viewrchat.util.VRCUtil
import com.riku1227.viewrchat.view_model.UserProfileActivityViewModel
import com.riku1227.vrchatlin.model.VRChatUser
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_user_profile.*
import java.util.*

class UserProfileActivity : AppCompatActivity() {

    private lateinit var viewModel: UserProfileActivityViewModel
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {

        SettingsUtil.initBlackTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        setSupportActionBar(userProfileActivityToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this).get(UserProfileActivityViewModel::class.java)

        if(viewModel.vrcUser == null) {
            val id = intent.getStringExtra("user_id")
            if(id != null) {
                val disposable = CacheSystem.loadVRChatUser(baseContext, id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            viewModel.vrcUser = it
                            setup(it)
                        },
                        {
                            ErrorHandling.onNetworkError(it, applicationContext, activity = this)
                        }
                    )
                compositeDisposable.add(disposable)
            }
        } else {
            setup(viewModel.vrcUser!!)
        }
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun setup(vrcUser: VRChatUser) {
        title = vrcUser.displayName

        userInfoCard.visibility = View.VISIBLE
        userStatusCard.visibility = View.VISIBLE

        userProfileActivityAvatarThumbnail.outlineProvider = ViewRChat.imageRadiusOutlineProvider
        val disposable = CacheSystem.loadImage(baseContext, CacheSystem.CacheType.USER_AVATAR_IMAGE, vrcUser.id, vrcUser.currentAvatarThumbnailImageUrl)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    Picasso.get()
                        .load(it)
                        .centerCrop()
                        .fit()
                        .into(userProfileActivityAvatarThumbnail)
                },
                {
                    ErrorHandling.onNetworkError(it, baseContext, this)
                }
            )
        compositeDisposable.add(disposable)

        userProfileActivityUserName.text = vrcUser.displayName
        userProfileActivityStatusIcon.setColorFilter(VRCUtil.getStatusIconColor(baseContext, vrcUser.status))
        userProfileActivityUserStatus.text = if(vrcUser.statusDescription.isNullOrEmpty()) {
            vrcUser.status?.toUpperCase(Locale.US)
        } else {
            vrcUser.statusDescription
        }
        userProfileActivityLastPlatform.text = VRCUtil.getLastLoginPlatform(vrcUser.last_platform)
        userProfileActivityTrustRank.text = VRCUtil.getTrustRank(vrcUser.tags)

        if(!vrcUser.bio.isNullOrEmpty()) {
            userProfileActivityUserBio.text = vrcUser.bio
        } else {
            userProfileActivityUserBio.visibility = View.GONE
        }

        val languagesList = VRCUtil.getLanguagesList(vrcUser.tags)
        if(languagesList.isNotEmpty()) {
            userProfileActivityUserView.visibility = View.VISIBLE
            userProfileActivityLanguagesText.visibility = View.VISIBLE
            userProfileActivityLanguages.visibility = View.VISIBLE

            userProfileActivityLanguages.adapter = UserLanguagesRecyclerAdapter(baseContext, languagesList)
            val layoutManager = LinearLayoutManager(baseContext)
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL
            userProfileActivityLanguages.layoutManager = layoutManager
        } else {
            userProfileActivityUserView.visibility = View.GONE
            userProfileActivityLanguagesText.visibility = View.GONE
            userProfileActivityLanguages.visibility = View.GONE
        }

        userProfileActivityLocationImage.outlineProvider = ViewRChat.imageRadiusOutlineProvider

        vrcUser.location?.let { friendLocation ->
            when (friendLocation) {
                "offline" -> {
                    userProfileActivityUserStatusView.visibility = View.GONE
                    userProfileActivityLocationImage.visibility = View.GONE
                    userProfileActivityLocationTextRoot.visibility = View.GONE
                    userProfileActivityLocationText.visibility = View.GONE
                }

                "private" -> {
                    userProfileActivityLocationInstanceType.visibility = View.GONE
                    userProfileActivityLocationName.text = baseContext.resources.getString(R.string.general_private_instance)

                    val loadVRChatDisposable = CacheSystem.loadImage(baseContext, CacheSystem.CacheType.WORLD_IMAGE, "private_image", CacheSystem.VRC_ASSETS_PRIVATE_WORLD_IMAGE_URL)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            { imgFile ->
                                Picasso.get()
                                    .load(imgFile)
                                    .centerCrop()
                                    .fit()
                                    .into(userProfileActivityLocationImage)
                            },
                            {}
                        )

                    compositeDisposable.add(loadVRChatDisposable)
                }

                else -> {
                    val splitLocation = friendLocation.split(":")
                    val worldId = splitLocation[0]
                    val instanceId = splitLocation[1]

                    val loadVRChatDisposable = CacheSystem.loadVRChatWorld(baseContext, worldId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            {
                                userProfileActivityLocationName.text = it.name
                                userProfileActivityLocationInstanceType.text = VRCUtil.getInstanceTypeFromInstanceID(instanceId)

                                if(ViewRChat.isPhotographingMode) {
                                    userProfileActivityLocationName.text = baseContext.getString(R.string.photographing_mode_world_name)
                                }

                                CacheSystem.loadImage(baseContext, CacheSystem.CacheType.WORLD_IMAGE, it.id, it.thumbnailImageUrl)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                        { imgFile ->
                                            Picasso.get()
                                                .load(imgFile)
                                                .centerCrop()
                                                .fit()
                                                .into(userProfileActivityLocationImage)
                                        },
                                        {
                                        }
                                    )
                            },
                            {
                            }
                        )
                    compositeDisposable.add(loadVRChatDisposable)
                }
            }
        }

        vrcUser.bioLinks?.let {
            if(it.isNotEmpty() && it[0].isNotBlank()) {
                userProfileActivityLinksCard.visibility = View.VISIBLE

                val removeEmptyList = arrayListOf<String>()
                for (value in it) {
                    if(value.isNotBlank()) {
                        removeEmptyList.add(value)
                    }
                }
                userProfileActivityLinks.adapter = UserLinksRecyclerAdapter(baseContext, removeEmptyList, this)
                val layoutManager = LinearLayoutManager(baseContext)
                layoutManager.orientation = LinearLayoutManager.VERTICAL
                userProfileActivityLinks.layoutManager = layoutManager
            }
        }
    }
}