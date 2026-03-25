import SwiftUI

struct SearchView: View {
    @State private var currentIndex = 0
    @State private var dragOffset: CGFloat = 0
    
    var body: some View {
        GeometryReader { geometry in
            ZStack(alignment: .trailing) {
                
                // 1. Les vues qui défilent
                VStack(spacing: 0) {
                    PictureView()
                        .frame(width: geometry.size.width, height: geometry.size.height)
                    WheelView()
                        .frame(width: geometry.size.width, height: geometry.size.height)
                    VocalView()
                        .frame(width: geometry.size.width, height: geometry.size.height)
                }
                .offset(y: -CGFloat(currentIndex) * geometry.size.height + dragOffset)
                .animation(.spring(response: 0.4, dampingFraction: 0.8), value: dragOffset)
                .animation(.spring(response: 0.4, dampingFraction: 0.8), value: currentIndex)
                
                // 🛠️ LA CORRECTION EST ICI :
                // On utilise .gesture au lieu de .simultaneousGesture,
                // et on ajoute une distance minimum de 20 pixels.
                .gesture(
                    DragGesture(minimumDistance: 20)
                        .onChanged { value in
                            // Suit le doigt en temps réel UNIQUEMENT si c'est un swipe vertical
                            if abs(value.translation.height) > abs(value.translation.width) {
                                if (currentIndex == 0 && value.translation.height > 0) ||
                                   (currentIndex == 2 && value.translation.height < 0) {
                                    self.dragOffset = value.translation.height / 3 // Effet élastique
                                } else {
                                    self.dragOffset = value.translation.height
                                }
                            }
                        }
                        .onEnded { value in
                            // On vérifie que le mouvement final était bien vertical
                            if abs(value.translation.height) > abs(value.translation.width) {
                                let threshold = geometry.size.height * 0.15
                                
                                withAnimation(.spring(response: 0.4, dampingFraction: 0.8)) {
                                    if value.translation.height < -threshold && currentIndex < 2 {
                                        currentIndex += 1
                                    } else if value.translation.height > threshold && currentIndex > 0 {
                                        currentIndex -= 1
                                    }
                                    dragOffset = 0
                                }
                            } else {
                                // Si c'était un swipe horizontal, on remet l'offset vertical à zéro en douceur
                                withAnimation {
                                    dragOffset = 0
                                }
                            }
                        }
                )
                
                // 2. L'indicateur visuel sur le côté (Les petits points)
                VStack(spacing: 12) {
                    ForEach(0..<3, id: \.self) { index in
                        Circle()
                            .fill(currentIndex == index ? Color.blue : Color.gray.opacity(0.4))
                            .frame(width: currentIndex == index ? 10 : 8, height: currentIndex == index ? 10 : 8)
                            .animation(.easeInOut(duration: 0.3), value: currentIndex)
                    }
                }
                .padding(.trailing, 16)
            }
        }
        .edgesIgnoringSafeArea(.top)
    }
}
