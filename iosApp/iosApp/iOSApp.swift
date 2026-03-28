import ComposeApp
import SwiftUI

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    OlxAuthCallbackBridge.shared.publishCallback(url: url.absoluteString)
                }
        }
    }
}
