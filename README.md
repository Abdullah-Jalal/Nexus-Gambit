♟️ Nexus Gambit
<img width="2325" height="1536" alt="Gemini_Generated_Image_mx7a6vmx7a6vmx7a" src="https://github.com/user-attachments/assets/eec6e2eb-c94a-4c40-b4ae-4a8979bcb041" />

A High-Performance Full-Stack Chess Application

📖 Overview
Nexus Gambit is a robust, rule-accurate chess platform designed to bridge classic strategy with modern software engineering. By decoupling frontend interactivity from a strict backend rules engine, this application ensures tournament-level precision for every move.

The project features a sleek, dark-mode React (TypeScript) interface for fluid gameplay and a powerful Java Spring Boot backend that handles move validation, complex game states (Checkmate/Stalemate), and pathfinding algorithms. It serves as a comprehensive example of full-stack development, object-oriented design, and algorithm implementation.

✨ Key Features
🛡️ Advanced Rules Engine (Backend)
Server-Side Validation: Every move is mathematically validated against official chess rules via a REST API.

Complex Logic Handling: Full support for edge cases including Pawn Promotion, En Passant, and castling logic.

Safety Architecture: Implements a "Safety Lock" mechanism to prevent premature game-over states (e.g., preventing Fool's Mate glitches on turn 1).

Check/Checkmate Detection: Sophisticated algorithms (CheckmateDetector) simulate future board states to accurately detect checks, pins, and checkmates.

🎨 Immersive User Experience (Frontend)
Interactive Gameplay: Smooth drag-and-drop mechanics with real-time visual feedback for valid moves and captures.

Dynamic State Management: Real-time tracking of captured pieces (e.g., Captured: 4/32), move history log, and active turn indicators.

Tactical UI: A "Cyberpunk/Tactical" aesthetic featuring a deep charcoal background (#0B1026) with Neon Cyan accents (#00D4FF), designed for clarity and reduced eye strain.

Modal Interactions: Custom modals for Pawn Promotion selection and End-Game summaries (Win/Loss/Draw).

🛠️ Technical Architecture
Frontend: The Nexus
Framework: React 18 with TypeScript for type-safe component logic.

State Management: React Hooks (useState, useRef, useEffect) for managing the "living" board state.

Styling: Modular CSS & Tailwind for a responsive, theme-consistent design.

API Integration: Asynchronous communication with the Java backend via REST endpoints (src/api/chessApi.ts).

Backend: The Gambit
Framework: Java Spring Boot 3.x (REST API).

Core Logic: Object-Oriented implementation of chess pieces (Pawn, Knight, King, etc.) extending a base Piece class.

Algorithms:

Pathfinding: Custom traversal algorithms for sliding pieces (Rook, Bishop, Queen).

Simulation: "Hypothetical Board" generation to test move legality (preventing moves that leave the King in check).

Data Structures: Heavy use of ArrayLists and HashMaps for efficient board state manipulation.

🚀 Getting Started
Prerequisites
Node.js (v16+)

Java JDK (17 or 21)

Maven
Installation
Clone the Repository

git clone https://github.com/your-username/nexus-gambit.git
cd nexus-gambit

Backend Setup (Java)
cd java-backend
mvn clean install
mvn spring-boot:run
Server will start at http://localhost:8080

Frontend Setup (React)
cd client  # or your frontend folder name
npm install
npm start
Client will launch at http://localhost:3000

📸 Screenshot:
<img width="1209" height="801" alt="Screenshot 2026-01-18 005551" src="https://github.com/user-attachments/assets/4c016d14-0b28-49ff-8a9a-15678421fe82" />

🤝 Contributing
Contributions are welcome! Whether it's adding Stockfish AI integration, multiplayer WebSockets, or UI polish.

Fork the Project

Create your Feature Branch (git checkout -b feature/AmazingFeature)

Commit your Changes (git commit -m 'Add some AmazingFeature')

Push to the Branch (git push origin feature/AmazingFeature)

Open a Pull Request

📜 License
Distributed under the MIT License. See LICENSE for more information.
