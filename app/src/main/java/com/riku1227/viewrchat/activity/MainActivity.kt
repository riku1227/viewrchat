package com.riku1227.viewrchat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.AppLaunchChecker
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.riku1227.viewrchat.R
import com.riku1227.viewrchat.ViewRChat
import com.riku1227.viewrchat.db.CacheTimeDataDB
import com.riku1227.viewrchat.dialog.CrashReportDialog
import com.riku1227.viewrchat.system.CacheSystem
import com.riku1227.viewrchat.system.CrashDetection
import com.riku1227.viewrchat.system.ErrorHandling
import com.riku1227.viewrchat.util.SettingsUtil
import com.riku1227.viewrchat.util.VRCUtil
import com.riku1227.viewrchat.view_model.MainActivityViewModel
import com.riku1227.vrchatlin.VRChatlin
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.abs

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var gestureDetector: GestureDetector
    private val compositeDisposable = CompositeDisposable()

    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SettingsUtil.initBlackTheme(this)

        setContentView(R.layout.activity_main)
        setSupportActionBar(mainActivityToolbar)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        if(savedInstanceState == null) {
            val crashDetection = CrashDetection(baseContext)
            Thread.setDefaultUncaughtExceptionHandler(crashDetection)

            val generalPreference = ViewRChat.getGeneralPreferences(baseContext)
            val crashLog = generalPreference.getString("crash_log", "")!!

            VRChatlin.get(applicationContext).APIService(ViewRChat.getVRChatCookiePreferences(applicationContext))

            val db = CacheTimeDataDB.getInstance(applicationContext)
            val cacheMap = db.readAllData()
            val nowTime = System.currentTimeMillis() / 1000

            for(item in cacheMap) {
                val diffTime = nowTime - item.cacheTime
                if(diffTime >= CacheSystem.DB_CLEAR_TIME) {
                    db.deleteData(item.id)
                    CacheSystem.deleteCacheFile(applicationContext, item.id, item.cacheType)
                }
            }


            if(crashLog != "") {
                val reportDialog = CrashReportDialog(crashLog)
                reportDialog.show(supportFragmentManager, "CrashReportDialog")
                generalPreference.edit().putString("crash_log", "").apply()
            }

            if(!AppLaunchChecker.hasStartedFromLauncher(applicationContext)) {
                val intent = Intent(this, TutorialActivity::class.java)
                startActivityForResult(intent, TutorialActivity.REQUEST_CODE)
            } else {
                if(ViewRChat.getGeneralPreferences(baseContext).getBoolean("is_login", false)) {
                    setupNavigation()
                } else {
                    val intent = Intent(this, WebViewLoginActivity::class.java)
                    startActivityForResult(intent, WebViewLoginActivity.REQUEST_CODE)
                }
            }
        }

        gestureDetector = GestureDetector(baseContext, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if(e1 != null && e2 != null) {
                    if (e2.x - e1.x > 50 && //左から右のスライド
                        e2.y - e1.y < 50 && //上から下のスライド
                        e1.y - e2.y < 50 && //下から上のスライド
                        abs(velocityX) > 200 //スライド速度
                    ) {
                        mainActivityDrawerLayout.openDrawer(GravityCompat.START)
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        })

        val toggle = ActionBarDrawerToggle(this, mainActivityDrawerLayout, mainActivityToolbar, R.string.general_drawer_open, R.string.general_drawer_close)
        mainActivityDrawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        toggle.isDrawerSlideAnimationEnabled = false

        if(ViewRChat.getGeneralPreferences(baseContext).getBoolean("is_login", false)) {
            setupNavigation()
            setupDrawer()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            TutorialActivity.REQUEST_CODE -> {
                if(resultCode == TutorialActivity.RESULT_CODE) {
                    AppLaunchChecker.onActivityCreate(this)
                    val intent = Intent(this, WebViewLoginActivity::class.java)
                    startActivityForResult(intent, WebViewLoginActivity.REQUEST_CODE)
                } else {
                    finish()
                }
            }

            WebViewLoginActivity.REQUEST_CODE -> {
                if(resultCode == WebViewLoginActivity.RESULT_CODE) {
                    this.recreate()
                } else {
                    finish()
                }
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    private fun setupNavigation() {
        val navHost = mainActivityFragment as NavHostFragment
        val navController = navHost.navController
        val graph = navController.navInflater.inflate(R.navigation.activity_main_navigation)
        navController.graph = graph
        NavigationUI.setupWithNavController(mainActivityBottomNavigation, navController)
    }

    private fun setupDrawer() {
        if(SettingsUtil.isDarkTheme(applicationContext) && SettingsUtil.isBlackTheme(applicationContext)) {
            mainActivityNavigationDrawer?.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.black))
        }
        val headerView = mainActivityNavigationDrawer.getHeaderView(0)

        mainActivityNavigationDrawer.setNavigationItemSelectedListener(this)

        if(viewModel.loginUser == null) {
            val disposable = CacheSystem.loadVRChatUser(baseContext, CacheSystem.LOGIN_USER_ID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        viewModel.loginUser = it
                        headerView.findViewById<ImageView>(R.id.drawerMainActivityUserThumbnail).outlineProvider = ViewRChat.imageRadiusOutlineProvider
                        val disposable02 = CacheSystem.loadImage(applicationContext, CacheSystem.CacheType.USER_AVATAR_IMAGE, it.id, it.currentAvatarImageUrl)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                { imgFile ->
                                    Picasso.get()
                                        .load(imgFile)
                                        .centerCrop()
                                        .fit()
                                        .into(
                                            headerView.findViewById<ImageView>(R.id.drawerMainActivityUserThumbnail)
                                        )
                                },
                                { error ->
                                    ErrorHandling.onNetworkError(error, applicationContext, activity = this)
                                }
                            )
                        compositeDisposable.add(disposable02)

                        headerView.findViewById<TextView>(R.id.drawerMainActivityUserName).text = it.displayName
                        headerView.findViewById<TextView>(R.id.drawerMainActivityUserRank).text = VRCUtil.getTrustRank(it.tags)
                        headerView.findViewById<TextView>(R.id.drawerMainActivityFriendsCount).text = it.friends?.size.toString()
                    },
                    {
                        ErrorHandling.onNetworkError(it, applicationContext, activity = this)
                    }
                )
            compositeDisposable.add(disposable)
        } else {
            viewModel.loginUser?.let {
                headerView.findViewById<ImageView>(R.id.drawerMainActivityUserThumbnail).outlineProvider = ViewRChat.imageRadiusOutlineProvider

                val disposable02 = CacheSystem.loadImage(applicationContext, CacheSystem.CacheType.USER_AVATAR_IMAGE, it.id, it.currentAvatarImageUrl)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { imgFile ->
                            Picasso.get()
                                .load(imgFile)
                                .centerCrop()
                                .fit()
                                .into(
                                    headerView.findViewById<ImageView>(R.id.drawerMainActivityUserThumbnail)
                                )
                        },
                        { error ->
                            ErrorHandling.onNetworkError(error, applicationContext, activity = this)
                        }
                    )
                compositeDisposable.add(disposable02)

                headerView.findViewById<TextView>(R.id.drawerMainActivityUserName).text = it.displayName
                headerView.findViewById<TextView>(R.id.drawerMainActivityUserRank).text = VRCUtil.getTrustRank(it.tags)
                headerView.findViewById<TextView>(R.id.drawerMainActivityFriendsCount).text = it.friends?.size.toString()
            }
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            R.id.mainActivityDrawerSettings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }

            R.id.mainActivityDrawerProfile -> {
                val intent = Intent(this, UserProfileActivity::class.java)
                intent.putExtra("user_id", CacheSystem.LOGIN_USER_ID)
                startActivity(intent)
            }
        }

        mainActivityDrawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
