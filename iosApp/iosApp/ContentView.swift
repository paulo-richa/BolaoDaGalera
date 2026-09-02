import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
            // The app's background is always dark regardless of the device's own
            // light/dark setting, so the status bar must always render light
            // (white) content - otherwise a device set to light mode gets dark,
            // near-invisible status bar icons over our dark background.
            .preferredColorScheme(.dark)
    }
}



