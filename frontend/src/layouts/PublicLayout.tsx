import { useState } from "react";
import { Link, NavLink, Outlet } from "react-router";

export function PublicLayout() {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const toggleMenu = () => {
    setIsMobileMenuOpen(!isMobileMenuOpen);
  };

  const closeMenu = () => {
    setIsMobileMenuOpen(false);
  };

  return (
    <div className="public-layout">
      <header className="public-header">
        <div className="public-header-container">
          <Link to="/" className="public-brand" onClick={closeMenu}>
            <div className="public-brand-logo">PUB</div>
            <div className="public-brand-text">
              <strong>Orchestria</strong>
              <span>Program Unggulan Bersama</span>
            </div>
          </Link>

          <button 
            className="public-mobile-toggle" 
            onClick={toggleMenu}
            aria-label="Toggle menu"
          >
            {isMobileMenuOpen ? "✕" : "☰"}
          </button>

          <nav className={`public-nav ${isMobileMenuOpen ? "open" : ""}`}>
            <NavLink to="/" className="public-nav-link" onClick={closeMenu} end>Beranda</NavLink>
            <NavLink to="/about" className="public-nav-link" onClick={closeMenu}>Tentang PUB</NavLink>
            <NavLink to="/public/organization" className="public-nav-link" onClick={closeMenu}>Struktur Organisasi</NavLink>
            <NavLink to="/activities" className="public-nav-link" onClick={closeMenu}>Kegiatan</NavLink>
            
            <Link to="/login" className="public-login-btn" onClick={closeMenu}>
              Masuk Anggota
            </Link>
          </nav>
        </div>
      </header>

      <main className="public-main">
        <Outlet />
      </main>

      <footer className="public-footer">
        <div className="public-footer-container">
          <div className="public-footer-col">
            <div className="public-brand-logo" style={{ marginBottom: "16px" }}>PUB</div>
            <p>
              Program Unggulan Bersama (PUB) merupakan program pembinaan dan organisasi mahasiswa 
              di bawah naungan Universitas Nasional PASIM.
            </p>
          </div>
          <div className="public-footer-col">
            <h4>Tautan Cepat</h4>
            <ul>
              <li><Link to="/">Beranda</Link></li>
              <li><Link to="/about">Tentang PUB</Link></li>
              <li><Link to="/public/organization">Struktur Organisasi</Link></li>
              <li><Link to="/activities">Kegiatan</Link></li>
            </ul>
          </div>
          <div className="public-footer-col">
            <h4>Kontak</h4>
            <p>Universitas Nasional PASIM<br />Jl. Dakota No. 8A, Bandung, Jawa Barat</p>
          </div>
        </div>
        <div className="public-footer-bottom">
          <p>&copy; {new Date().getFullYear()} Orchestria & Program Unggulan Bersama. Hak Cipta Dilindungi.</p>
        </div>
      </footer>
    </div>
  );
}
