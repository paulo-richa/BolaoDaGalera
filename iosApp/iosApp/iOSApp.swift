import SwiftUI
import FirebaseCore
import GoogleMobileAds
import ComposeApp

@main
struct iOSApp: App {
    init() {
        FirebaseApp.configure()
        // AdMob start using the renamed classes for Swift
        MobileAds.shared.start(completionHandler: { _ in })
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
