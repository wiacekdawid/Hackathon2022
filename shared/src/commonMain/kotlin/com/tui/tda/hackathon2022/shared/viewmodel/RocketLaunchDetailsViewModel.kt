package com.tui.tda.hackathon2022.shared.viewmodel

import com.tui.tda.hackathon2022.shared.SpaceXSDK
import com.tui.tda.hackathon2022.shared.entity.RocketLaunch
import dev.icerock.moko.mvvm.flow.CStateFlow
import dev.icerock.moko.mvvm.flow.cStateFlow
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class RocketLaunchDetailsViewModel(flightNumber: Int, private val sdk: SpaceXSDK) : ViewModel() {

    private val _rocketLaunch = MutableStateFlow<RocketLaunch?>(null)
    val rocketLaunch: CStateFlow<RocketLaunch?> = _rocketLaunch.cStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                _rocketLaunch.value = sdk.getLaunch(flightNumber)
            }
        }
    }
}