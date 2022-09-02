package com.tui.tda.hackathon2022.android

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.tui.tda.hackathon2022.shared.SpaceXSDK
import com.tui.tda.hackathon2022.shared.cache.DatabaseDriverFactory
import com.tui.tda.hackathon2022.shared.entity.RocketLaunch
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private val mainScope = MainScope()

    private lateinit var launchesRecyclerView: RecyclerView
    private lateinit var progressBarView: FrameLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var searchView: SearchView

    private val sdk = SpaceXSDK(DatabaseDriverFactory(this))

    private val launchesRvAdapter = LaunchesRvAdapter(listOf(), this::onRocketLaunchClicked)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "SpaceX Launches"
        setContentView(R.layout.activity_main)

        launchesRecyclerView = findViewById(R.id.launchesListRv)
        progressBarView = findViewById(R.id.progressBar)
        swipeRefreshLayout = findViewById(R.id.swipeContainer)
        searchView = findViewById(R.id.search_bar)

        launchesRecyclerView.adapter = launchesRvAdapter
        launchesRecyclerView.layoutManager = LinearLayoutManager(this)

        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = false
            displayLaunches(true)
        }

        displayLaunches(false)
        initSearchView()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }

    private fun displayLaunches(needReload: Boolean) {
        progressBarView.isVisible = true
        mainScope.launch {
            kotlin.runCatching {
                sdk.getLaunches(needReload)
            }.onSuccess {
                launchesRvAdapter.launches = it
                launchesRvAdapter.notifyDataSetChanged()
            }.onFailure {
                Toast.makeText(this@MainActivity, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
            progressBarView.isVisible = false
        }
    }

    private var searchJob: Job? = null

    private fun initSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchJob = mainScope.launch {
                    delay(500)
                    kotlin.runCatching {
                        sdk.getLaunchesByText(query)
                    }.onSuccess {
                        launchesRvAdapter.launches = it
                        launchesRvAdapter.notifyDataSetChanged()
                    }.onFailure {
                        Toast.makeText(this@MainActivity, it.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchJob = mainScope.launch {
                    delay(500)
                    kotlin.runCatching {
                        sdk.getLaunchesByText(newText)
                    }.onSuccess {
                        launchesRvAdapter.launches = it
                        launchesRvAdapter.notifyDataSetChanged()
                    }.onFailure {
                        Toast.makeText(this@MainActivity, it.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                }
                return true
            }

        })
    }

    private fun onRocketLaunchClicked(rocketLaunch: RocketLaunch) {
        val bottomSheet = RocketLaunchDetailsDialog.newInstance(rocketLaunch)
        bottomSheet.show(supportFragmentManager, null)
    }
}