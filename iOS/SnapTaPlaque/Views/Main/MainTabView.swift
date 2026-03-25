//
//  MainTabView.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 21/03/2026.
//

import SwiftUI

struct MainTabView: View {
    @State private var selectedTab = 1
    
    var body: some View {
        ZStack(alignment: .bottom) {
            
            // --- CORRECTION : Un fond global qui force le remplissage jusqu'en bas ---
            Color(.systemBackground) // Utilisez .systemGroupedBackground si vous préférez un fond légèrement gris
                .ignoresSafeArea()
            
            // 1. Le contenu principal swipable horizontalement
            TabView(selection: $selectedTab) {
                HistoryView()
                    .tag(0)
                
                SearchView()
                    .tag(1)
                
                ProfileView()
                    .tag(2)
            }
            .tabViewStyle(.page(indexDisplayMode: .never))
            // On s'assure que le contenu des onglets descend aussi
            .ignoresSafeArea(edges: .bottom)
            
            // 2. Notre barre de navigation personnalisée en bas
            CustomBottomNavBar(selectedTab: $selectedTab)
                .animation(.spring(response: 0.3, dampingFraction: 0.7), value: selectedTab)
        }
        .ignoresSafeArea(.all, edges: .all) // Assure que la barre de navigation est au-dessus de la barre d'accueil iOS
    }
}

// MARK: - La Barre de Navigation Premium
struct CustomBottomNavBar: View {
    @Binding var selectedTab: Int
    
    var body: some View {
        HStack(spacing: 0) {
            NavBarItem(icon: "clock.fill", title: "Historique", isActive: selectedTab == 0) {
                selectedTab = 0 // L'animation est maintenant gérée globalement
            }
            
            Spacer()
            
            NavBarItem(icon: "magnifyingglass", title: "Rechercher", isActive: selectedTab == 1) {
                selectedTab = 1
            }
            
            Spacer()
            
            NavBarItem(icon: "person.fill", title: "Profil", isActive: selectedTab == 2) {
                selectedTab = 2
            }
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 12)
        .background(.ultraThinMaterial)
        .clipShape(Capsule())
        .shadow(color: Color.black.opacity(0.15), radius: 15, x: 0, y: 8)
        .padding(.horizontal, 30)
        // Petit ajustement pour bien la placer au-dessus de la barre d'accueil iOS
        .padding(.bottom, 10)
        .ignoresSafeArea(.all, edges: .bottom)
    }
}

// MARK: - Un bouton dynamique
struct NavBarItem: View {
    let icon: String
    let title: String
    let isActive: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                Image(systemName: icon)
                    .font(.system(size: 20, weight: isActive ? .bold : .medium))
                
                if isActive {
                    Text(title)
                        .font(.system(size: 14, weight: .bold))
                        .lineLimit(1)
                }
            }
            .foregroundColor(isActive ? .white : .primary.opacity(0.5))
            .padding(.vertical, 10)
            .padding(.horizontal, isActive ? 16 : 10)
            .background(isActive ? Color.blue : Color.clear)
            .clipShape(Capsule())
        
        }
    }
}

// Aperçu dans Xcode
struct MainTabView_Previews: PreviewProvider {
    static var previews: some View {
        MainTabView()
    }
}

