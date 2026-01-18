import "./Header.css";

export default function Header() {
  return (
    <header className="nexus-header">
      <div className="logo-container">
        <span className="logo-icon">♟️</span>
        <h1 className="logo-text">NEXUS <span className="highlight">GAMBIT</span></h1>
      </div>
      <nav className="header-nav">
        <span className="status-badge">ONLINE</span>
      </nav>
    </header>
  );
}