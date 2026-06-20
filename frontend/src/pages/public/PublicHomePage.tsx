import { useEffect, useState, useMemo } from "react";
import { Link } from "react-router";
import { getPublicStructureCurrent } from "../../services/publicOrganizationService";
import type { PublicMemberAssignmentResponse, PublicPeriodResponse } from "../../types/publicOrganization";
import { staticPrograms, staticFacilities, staticActivities, staticTestimonials } from "../../data/publicContent";

export function PublicHomePage() {
  const [period, setPeriod] = useState<PublicPeriodResponse | null>(null);
  const [structure, setStructure] = useState<PublicMemberAssignmentResponse[]>([]);
  const [isLoadingStats, setIsLoadingStats] = useState(true);

  useEffect(() => {
    async function fetchStats() {
      try {
        const res = await getPublicStructureCurrent();
        if (res.data) {
          setPeriod(res.data.period);
          setStructure(res.data.structure || []);
        }
      } catch {
        // Fallback gracefully without breaking the page
      } finally {
        setIsLoadingStats(false);
      }
    }
    fetchStats();
  }, []);

  const stats = useMemo(() => {
    if (isLoadingStats) return { members: "...", divisions: "...", positions: "..." };
    if (!structure.length) return { members: "—", divisions: "—", positions: "—" };

    const uniqueDivisions = new Set(structure.map(s => s.divisionId));
    const uniquePositions = new Set(structure.map(s => s.positionId));

    return {
      members: structure.length.toString(),
      divisions: uniqueDivisions.size.toString(),
      positions: uniquePositions.size.toString()
    };
  }, [structure, isLoadingStats]);

  // Top structure (lowest positionLevelOrder)
  const topStructure = useMemo(() => {
    const sorted = [...structure].sort((a, b) => a.positionLevelOrder - b.positionLevelOrder);
    return sorted.slice(0, 4); // Max 4 for landing page
  }, [structure]);

  return (
    <div className="public-home">
      {/* 1. HERO */}
      <section className="public-hero">
        <div className="public-hero-content">
          <p className="eyebrow">PROGRAM UNGGULAN BERSAMA</p>
          <h1>Bina Prestasi, Wujudkan Mimpi</h1>
          <p className="public-hero-desc">
            Wadah pembinaan dan organisasi mahasiswa Universitas Nasional PASIM untuk mencetak 
            pemimpin tangguh, profesional, dan berkarakter unggul di era digital.
          </p>
          <div className="public-hero-cta">
            <Link to="/about" className="public-btn public-btn-primary">Kenali PUB</Link>
            <Link to="/login" className="public-btn public-btn-outline">Masuk Anggota</Link>
          </div>
        </div>
        <div className="public-hero-visual">
          <div className="public-hero-illustration">
            <div className="illustration-box box-1"></div>
            <div className="illustration-box box-2"></div>
            <div className="illustration-box box-3"></div>
          </div>
        </div>
      </section>

      {/* 2. STATISTIK */}
      <section className="public-stats-section">
        <div className="public-container">
          <div className="public-stats-grid">
            <div className="public-stat-card">
              <span className="stat-value">{stats.members}</span>
              <span className="stat-label">Anggota Aktif</span>
            </div>
            <div className="public-stat-card">
              <span className="stat-value">{stats.divisions}</span>
              <span className="stat-label">Divisi Organisasi</span>
            </div>
            <div className="public-stat-card">
              <span className="stat-value">{stats.positions}</span>
              <span className="stat-label">Peran & Jabatan</span>
            </div>
            <div className="public-stat-card">
              <span className="stat-value">{period ? period.name : "—"}</span>
              <span className="stat-label">Periode Kepengurusan</span>
            </div>
          </div>
        </div>
      </section>

      {/* 3. PROGRAM UTAMA */}
      <section className="public-section">
        <div className="public-container">
          <div className="public-section-header">
            <h2>Program Pembinaan</h2>
            <p>Berbagai program yang kami hadirkan untuk mendukung pengembangan karakter dan kompetensi.</p>
          </div>
          <div className="public-grid-3">
            {staticPrograms.map(prog => (
              <div key={prog.id} className="public-card">
                <h3>{prog.title}</h3>
                <p>{prog.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* 4. FASILITAS */}
      <section className="public-section bg-gray">
        <div className="public-container">
          <div className="public-section-header">
            <h2>Fasilitas Anggota</h2>
            <p>Dukungan optimal bagi seluruh anggota Program Unggulan Bersama.</p>
          </div>
          <div className="public-grid-3">
            {staticFacilities.map(fac => (
              <div key={fac.id} className="public-card">
                <h3>{fac.title}</h3>
                <p>{fac.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* 5. STRUKTUR RINGKAS */}
      <section className="public-section">
        <div className="public-container">
          <div className="public-section-header">
            <h2>Pimpinan Organisasi</h2>
            <p>Pengurus utama yang bertugas mengarahkan laju roda organisasi PUB.</p>
          </div>
          
          {isLoadingStats ? (
            <div className="public-loading">Memuat data struktur...</div>
          ) : topStructure.length > 0 ? (
            <div className="public-grid-4">
              {topStructure.map(member => (
                <div key={member.memberId} className="public-profile-card">
                  <div className="profile-avatar">
                    {member.profilePhotoUrl ? (
                      <img src={member.profilePhotoUrl} alt={member.memberName} />
                    ) : (
                      <span className="avatar-initials">{member.memberName.charAt(0)}</span>
                    )}
                  </div>
                  <h4>{member.memberName}</h4>
                  <p className="profile-position">{member.positionName}</p>
                  <span className="profile-division">{member.divisionName}</span>
                </div>
              ))}
            </div>
          ) : (
            <div className="public-empty">
              <p>Struktur kepengurusan saat ini belum tersedia atau sedang dalam masa transisi.</p>
            </div>
          )}
          
          <div className="public-center-action">
            <Link to="/public/organization" className="public-btn public-btn-outline">
              Lihat Struktur Lengkap
            </Link>
          </div>
        </div>
      </section>

      {/* 6. KEGIATAN TERBARU */}
      <section className="public-section bg-gray">
        <div className="public-container">
          <div className="public-section-header">
            <h2>Kegiatan Terbaru</h2>
            <p>Dokumentasi dan agenda yang sedang berjalan dalam lingkungan PUB.</p>
          </div>
          <div className="public-grid-3">
            {staticActivities.slice(0, 3).map(act => (
              <div key={act.id} className="public-card">
                <span className="public-badge">{act.category}</span>
                <h3 style={{ marginTop: "12px", marginBottom: "8px" }}>{act.title}</h3>
                <p>{act.description}</p>
                <small className="public-meta">{act.date} • {act.status}</small>
              </div>
            ))}
          </div>
          <div className="public-center-action">
            <Link to="/activities" className="public-btn public-btn-outline">
              Lihat Semua Kegiatan
            </Link>
          </div>
        </div>
      </section>

      {/* 7. TESTIMONI */}
      {/* STATIC SEED: Konten akan diganti oleh modul HUMAS kelak */}
      <section className="public-section">
        <div className="public-container">
          <div className="public-section-header">
            <h2>Apa Kata Mereka</h2>
            <p>Cerita dan pengalaman dari keluarga besar PUB.</p>
          </div>
          <div className="public-grid-3">
            {staticTestimonials.map(testi => (
              <div key={testi.id} className="public-testi-card">
                <p className="testi-text">"{testi.text}"</p>
                <div className="testi-author">
                  <strong>{testi.name}</strong>
                  <span>{testi.role}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* 8. CTA PENUTUP */}
      <section className="public-cta-section">
        <div className="public-container">
          <h2>Siap Menjadi Bagian dari PUB?</h2>
          <p>Jika Anda adalah anggota yang terdaftar, silakan masuk ke portal internal.</p>
          <div className="public-hero-cta" style={{ justifyContent: "center" }}>
            <Link to="/public/organization" className="public-btn public-btn-outline-light">Lihat Struktur</Link>
            <Link to="/login" className="public-btn public-btn-light">Masuk Anggota</Link>
          </div>
        </div>
      </section>
    </div>
  );
}
