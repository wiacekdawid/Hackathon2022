import SwiftUI
import shared

@main
struct iOSApp: App {
    let sdk = SpaceXSDK(databaseDriverFactory: DatabaseDriverFactory())
    var body: some Scene {
        WindowGroup {
            RocketLaunchView(viewModel: .init(sdk: sdk))
        }
    }
}
