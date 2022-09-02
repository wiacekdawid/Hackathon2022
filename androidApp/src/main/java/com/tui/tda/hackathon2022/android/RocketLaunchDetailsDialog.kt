package com.tui.tda.hackathon2022.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tui.tda.hackathon2022.shared.SpaceXSDK
import com.tui.tda.hackathon2022.shared.cache.DatabaseDriverFactory
import com.tui.tda.hackathon2022.shared.entity.RocketLaunch
import com.tui.tda.hackathon2022.shared.viewmodel.RocketLaunchDetailsViewModel
import dev.icerock.moko.mvvm.createViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class RocketLaunchDetailsDialog : BottomSheetDialogFragment() {

    companion object {
        private const val KEY_ROCKET_LAUNCH_FLIGHT_NUMBER = "rocket_launch_flight_number"

        fun newInstance(rocketLaunch: RocketLaunch): RocketLaunchDetailsDialog {
            return RocketLaunchDetailsDialog().apply {
                arguments = bundleOf(KEY_ROCKET_LAUNCH_FLIGHT_NUMBER to rocketLaunch.flightNumber)
            }
        }
    }

    private val viewModel by lazy { defaultViewModelProviderFactory.create(RocketLaunchDetailsViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_rocket_launch, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val missionNameTextView = view.findViewById<TextView>(R.id.missionName)
        val launchYearTextView = view.findViewById<TextView>(R.id.launchYear)
        val rocketNameTextView = view.findViewById<TextView>(R.id.rocketName)
        val launchSuccessTextView = view.findViewById<TextView>(R.id.launchSuccess)
        val missionDetailsLabelTextView = view.findViewById<TextView>(R.id.detailsLabel)
        val missionDetailsTextView = view.findViewById<TextView>(R.id.details)
        val viewArticleButton = view.findViewById<Button>(R.id.articleButton)

        lifecycleScope.launch {
            viewModel.rocketLaunch.filterNotNull().collectLatest { launch ->
                missionNameTextView.text = launch.missionName
                launchYearTextView.text = getString(R.string.launch_year_field, launch.launchYear.toString())
                rocketNameTextView.text = getString(R.string.rocket_name_field, launch.rocket.name)
                if (launch.details == null) {
                    missionDetailsLabelTextView.isGone = true
                    missionDetailsTextView.isGone = true
                } else {
                    missionDetailsTextView.text = launch.details
                }

                val launchSuccess = launch.launchSuccess
                if (launchSuccess != null) {
                    if (launchSuccess) {
                        launchSuccessTextView.text = getString(R.string.successful)
                        launchSuccessTextView.setTextColor((ContextCompat.getColor(requireContext(), R.color.colorSuccessful)))
                    } else {
                        launchSuccessTextView.text = getString(R.string.unsuccessful)
                        launchSuccessTextView.setTextColor((ContextCompat.getColor(requireContext(), R.color.colorUnsuccessful)))
                    }
                } else {
                    launchSuccessTextView.text = getString(R.string.no_data)
                    launchSuccessTextView.setTextColor((ContextCompat.getColor(requireContext(), R.color.colorNoData)))
                }

                val articleUrl = launch.links.articleUrl
                if (articleUrl == null) {
                    viewArticleButton.isGone = true
                } else {
                    viewArticleButton.isVisible = true
                    viewArticleButton.setOnClickListener {
                        openArticle(articleUrl)
                    }
                }
            }
        }
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        val flightNumber = arguments?.getInt(KEY_ROCKET_LAUNCH_FLIGHT_NUMBER) ?: error("No rocket_launch_flight_number parameter specified")
        return createViewModelFactory {
            RocketLaunchDetailsViewModel(flightNumber, SpaceXSDK(DatabaseDriverFactory(requireContext())))
        }
    }

    private fun openArticle(link: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(link)
        }
        startActivity(intent)
    }
}