package com.tui.tda.hackathon2022.android

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.tui.tda.hackathon2022.shared.SpaceXSDK
import com.tui.tda.hackathon2022.shared.cache.DatabaseDriverFactory
import com.tui.tda.hackathon2022.shared.entity.RocketLaunch
import com.tui.tda.hackathon2022.shared.viewmodel.RocketLaunchListViewModel
import dev.icerock.moko.mvvm.createViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel = defaultViewModelProviderFactory.create(RocketLaunchListViewModel::class.java)

    private lateinit var launchesRecyclerView: RecyclerView
    private lateinit var progressBarView: FrameLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var searchView: SearchView

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
            viewModel.refreshLaunches()
        }

        initSearchView()

        lifecycleScope.launch {
            progressBarView.isVisible = true
            viewModel.launches.filterNotNull().collectLatest {
                if (it.isSuccess) {
                    launchesRvAdapter.launches = it.getOrThrow()
                    launchesRvAdapter.notifyDataSetChanged()
                    progressBarView.isVisible = false
                } else {
                    Toast.makeText(this@MainActivity, it.exceptionOrNull()?.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return createViewModelFactory {
            RocketLaunchListViewModel(SpaceXSDK(DatabaseDriverFactory(this)))
        }
    }

    private fun initSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.updateSearchQuery(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.updateSearchQuery(newText ?: "")
                return true
            }
        })
    }

    private fun onRocketLaunchClicked(rocketLaunch: RocketLaunch) {
        val bottomSheet = RocketLaunchDetailsDialog.newInstance(rocketLaunch)
        bottomSheet.show(supportFragmentManager, null)
    }
}