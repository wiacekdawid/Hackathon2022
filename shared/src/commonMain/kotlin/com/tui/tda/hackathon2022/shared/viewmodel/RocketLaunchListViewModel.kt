package com.tui.tda.hackathon2022.shared.viewmodel

import com.tui.tda.hackathon2022.shared.SpaceXSDK
import com.tui.tda.hackathon2022.shared.entity.RocketLaunch
import dev.icerock.moko.mvvm.flow.CStateFlow
import dev.icerock.moko.mvvm.flow.cStateFlow
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class RocketLaunchListViewModel(private val sdk: SpaceXSDK) : ViewModel() {

    private val _launches = MutableStateFlow<Result<List<RocketLaunch>>?>(null)
    val launches: CStateFlow<Result<List<RocketLaunch>>?> = _launches.cStateFlow()

    init {
        viewModelScope.launch {
            _launches.value = runCatching {
                sdk.getLaunches(false)
            }
        }
    }

    fun refreshLaunches() {
        viewModelScope.launch {
            _launches.value = runCatching {
                sdk.getLaunches(true)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        viewModelScope.launch {
            _launches.value = runCatching {
                sdk.getLaunchesByText(query)
            }
        }
    }
}