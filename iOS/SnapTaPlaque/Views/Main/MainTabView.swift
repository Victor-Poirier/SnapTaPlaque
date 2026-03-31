//
//  MainTabView.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 21/03/2026.
//

import SwiftUI

/// Composant racinaire de l'application gérant la navigation principale via des onglets.
///
/// `MainTabView` encapsule les vues principales de l'application (`HistoryView`, `SearchView` et `ProfileView`)
/// dans un pager swipable en s'appuyant sur un menu de navigation personnalisé et flottant placé au bas de l'écran.
struct MainTabView: View {
    
    /// Suit l'index actuellement actif (0 = Historique, 1 = Recherche, 2 = Profil).
    /// Est initialisé à 1 pour ouvrir l'application directement sur l'onglet de Recherche (Le scanner).
    @State private var selectedTab = 1
    
    /// Le rendu déclaratif configurant la pagination entre les trois vues de premier niveau.
    var body: some View {
        ZStack(alignment: .bottom) {
            
            Color(.systemBackground)
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

/// Le menu de navigation flottant (Dock) personnalisé affiché en bas de l'écran.
///
/// Cette vue remplace avantageusement le style iOS basique en introduisant
/// une barre encapsulée dans une forme `Capsule` avec un effet translucide (`.ultraThinMaterial`).
struct CustomBottomNavBar: View {
    
    /// Une liaison avec la variable parente qui gère l'état global de tabulation en affichant un effet d'animation.
    @Binding var selectedTab: Int
    
    var body: some View {
        HStack(spacing: 0) {
            NavBarItem(icon: "clock.fill", title: "Historique", isActive: selectedTab == 0) {
                selectedTab = 0
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
        .padding(.bottom, 10)
        .ignoresSafeArea(.all, edges: .bottom)
    }
}

// MARK: - Un bouton dynamique

/// Élément individuel interactif constituant l'un des onglets de `CustomBottomNavBar`.
///
/// Ce composant réagit visuellement à l'état `isActive`. Lorsqu'il est sélectionné, sa forme
/// s'étire via un fond bleu encerclant et affichant dynamiquement la propriété texte du `title`
/// à côté de l'image vectorielle SF Symbols.
struct NavBarItem: View {
    
    /// Le nom de l'icône à exploiter depuis la bibliothèque SF Symbols (ex: `"clock.fill"`).
    let icon: String
    
    /// L'étiquette de texte s'animant sur l'interface si l'élément est actif.
    let title: String
    
    /// Condition booléenne vérifiant si le bouton représente actuellement l'onglet en surbrillance.
    let isActive: Bool
    
    /// Une closure (bloc de fonction) exécutée localement par SwiftUI lors du tap sur l'objet ciblé.
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
