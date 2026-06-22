import { useState } from "react";
import { Link, NavLink, Outlet } from "react-router";
import logoFull from "../assets/logo_orchestria_teks.png";
import logoIcon from "../assets/logo_orchestria.png";

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
            <img src={logoFull} alt="Orchestria" style={{ height: 40, objectFit: 'contain' }} />
          </Link>

          <button 
            type="button"
            className="public-mobile-toggle" 
            onClick={toggleMenu}
            aria-label="Toggle menu"
            aria-expanded={isMobileMenuOpen}
            aria-controls="public-mobile-nav"
          >
            {isMobileMenuOpen ? "✕" : "☰"}
          </button>

          <nav id="public-mobile-nav" className={`public-nav ${isMobileMenuOpen ? "open" : ""}`}>
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
            <img src={logoFull} alt="Orchestria" className="logo-white-rect" style={{ height: 48, marginBottom: 16 }} />
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
