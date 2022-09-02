//
//  RocketLaunchViewModel.swift
//  iosApp
//
//  Created by Ojars Iljesans on 02/09/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import SwiftUI
import shared

final class RocketLaunchViewModel: ObservableObject {
    enum LoadableLaunches {
        case loading
        case result([RocketLaunch])
        case error(String)
    }
    
    @Published var launches = LoadableLaunches.loading
    let sdk: SpaceXSDK
    
    init(sdk: SpaceXSDK) {
        self.sdk = sdk
        self.loadLaunches(forceReload: false)
    }
    
    func loadLaunches(forceReload: Bool) {
        self.launches = .loading
        sdk.getLaunches(forceReload: forceReload, completionHandler: { launches, error in
            if let launches = launches {
                self.launches = .result(launches)
            } else {
                self.launches = .error(error?.localizedDescription ?? "error")
            }
        })
    }
}

extension RocketLaunch: Identifiable { }
