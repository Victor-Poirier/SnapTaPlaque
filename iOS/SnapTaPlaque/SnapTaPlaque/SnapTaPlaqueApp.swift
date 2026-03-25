//
//  SnapTaPlaqueApp.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 20/03/2026.
//

import SwiftUI

@main
struct SnapTaPlaqueApp: App {
    @StateObject private var sessionManager = SessionManager.shared
    
    var body: some Scene {
        WindowGroup {
            if sessionManager.isLoggedIn {
                // AFFICHE LE DASHBOARD !
                MainTabView()
            } else {
                SignInView()
            }
        }
    }
}
