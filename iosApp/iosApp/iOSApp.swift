import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    private let store = PlatformIosKt.doNewLoanStore()

    var body: some Scene {
        WindowGroup {
            ContentView(store: store)
        }
    }
}
