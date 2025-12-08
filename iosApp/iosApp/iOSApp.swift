import SwiftUI
import ComposeApp
import UIKit

struct ComposeViewController: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return KotlinToAppleBridgeKt.MainIOSViewController()
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // No updates needed
    }
}

@main
struct iOSApp: App {
    private let store = PlatformIosKt.doNewLoanStore()

    var body: some Scene {
        WindowGroup {
            ContentView(store: store)
        }
    }
}
