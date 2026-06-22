import { useEffect, useState, useMemo } from "react";
import { Link } from "react-router";
import { getPublicStructureCurrent } from "../../services/publicOrganizationService";
import { publicContentService } from "../../services/publicContentService";
import type { PublicMemberAssignmentResponse, PublicPeriodResponse } from "../../types/publicOrganization";
import type { PublicContentEntry } from '../../types/publicContent';
import { PublicContentType } from '../../types/publicContent';
import { staticPrograms, staticFacilities, staticActivities, staticTestimonials } from "../../data/publicContent";

export function PublicHomePage() {
  const [period, setPeriod] = useState<PublicPeriodResponse | null>(null);
  const [structure, setStructure] = useState<PublicMemberAssignmentResponse[]>([]);
  const [isLoadingStats, setIsLoadingStats] = useState(true);

  const [contents, setContents] = useState<PublicContentEntry[]>([]);

  useEffect(() => {
    async function fetchData() {
      try {
        const [structRes, contentsRes] = await Promise.allSettled([
          getPublicStructureCurrent(),
          publicContentService.getPublished()
        ]);

        if (structRes.status === "fulfilled" && structRes.value.data) {
          setPeriod(structRes.value.data.period);
          setStructure(structRes.value.data.structure || []);
        }

        if (contentsRes.status === "fulfilled") {
          setContents(contentsRes.value);
        }
      } catch {
        // Fallback gracefully without breaking the page
      } finally {
        setIsLoadingStats(false);
      }
    }
    fetchData();
  }, []);

  const stats = useMemo(() => {
    if (isLoadingStats) return { members: "...", divisions: "...", positions: "..." };
    if (!structure.length) return { members: "—", divisions: "—", positions: "—" };

    const uniqueMembers = new Set(structure.map(s => s.memberId));
    const uniqueDivisions = new Set(structure.map(s => s.divisionId));
    const uniquePositions = new Set(structure.map(s => s.positionId));

    return {
      members: uniqueMembers.size.toString(),
      divisions: uniqueDivisions.size.toString(),
      positions: uniquePositions.size.toString()
    };
  }, [structure, isLoadingStats]);

  // Top structure (lowest positionLevelOrder)
  const topStructure = useMemo(() => {
    const sorted = [...structure].sort((a, b) => a.positionLevelOrder - b.positionLevelOrder);
    return sorted.slice(0, 4); // Max 4 for landing page
  }, [structure]);

  const heroContent = useMemo(() => contents.find(c => c.contentType === PublicContentType.HERO), [contents]);
  const programs = useMemo(() => contents.filter(c => c.contentType === PublicContentType.PROGRAM).sort((a,b) => a.displayOrder - b.displayOrder), [contents]);
  const facilities = useMemo(() => contents.filter(c => c.contentType === PublicContentType.FACILITY).sort((a,b) => a.displayOrder - b.displayOrder), [contents]);
  const activities = useMemo(() => contents.filter(c => c.contentType === PublicContentType.ACTIVITY).sort((a,b) => a.displayOrder - b.displayOrder), [contents]);
  const testimonials = useMemo(() => contents.filter(c => c.contentType === PublicContentType.TESTIMONIAL).sort((a,b) => a.displayOrder - b.displayOrder), [contents]);

  return (
    <div className="public-home">
      {/* 1. HERO */}
      <section className={`public-hero ${heroContent?.mediaUrl ? 'public-hero-dynamic' : ''}`} style={heroContent?.mediaUrl ? { backgroundImage: `url(${heroContent.mediaUrl})` } : undefined}>
        {heroContent?.mediaUrl && <div className="public-hero-overlay"></div>}
        <div className={`public-hero-content ${heroContent?.mediaUrl ? 'public-hero-content-dynamic' : ''}`}>
          <p className="eyebrow">{heroContent?.subtitle || "PROGRAM UNGGULAN BERSAMA"}</p>
          <h1>{heroContent?.title || "Bina Prestasi, Wujudkan Mimpi"}</h1>
          <p className="public-hero-desc">
            {heroContent?.body || "Wadah pembinaan dan organisasi mahasiswa Universitas Nasional PASIM untuk mencetak pemimpin tangguh, profesional, dan berkarakter unggul di era digital."}
          </p>
          <div className="public-hero-cta">
            <Link to="/about" className="public-btn public-btn-primary">Kenali PUB</Link>
            <Link to="/login" className="public-btn public-btn-outline">Masuk Anggota</Link>
          </div>
        </div>
        {!heroContent?.mediaUrl && (
          <div className="public-hero-visual">
            <div className="public-hero-illustration">
              <div className="illustration-box box-1"></div>
              <div className="illustration-box box-2"></div>
              <div className="illustration-box box-3"></div>
            </div>
          </div>
        )}
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
            {programs.length > 0 ? programs.map(prog => (
              <div key={prog.id} className="public-card">
                {prog.mediaUrl && <img src={prog.mediaUrl} alt={prog.title} className="public-card-img" />}
                <h3>{prog.title}</h3>
                <p>{prog.body}</p>
              </div>
            )) : staticPrograms.map(prog => (
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
            {facilities.length > 0 ? facilities.map(fac => (
              <div key={fac.id} className="public-card">
                {fac.mediaUrl && <img src={fac.mediaUrl} alt={fac.title} className="public-card-img" />}
                <h3>{fac.title}</h3>
                <p>{fac.body}</p>
              </div>
            )) : staticFacilities.map(fac => (
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
                <div key={`${member.memberId}-${member.positionId}-${member.divisionId}`} className="public-profile-card">
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
            <Link to="/public/organization" className="public-btn public-btn-secondary">
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
            {activities.length > 0 ? activities.slice(0, 3).map(act => (
              <div key={act.id} className="public-card">
                {act.mediaUrl && <img src={act.mediaUrl} alt={act.title} className="public-card-img" />}
                <h3 className="public-card-title-spacing">{act.title}</h3>
                <p>{act.body}</p>
                {act.eventDate && <small className="public-meta">{act.eventDate}</small>}
              </div>
            )) : staticActivities.slice(0, 3).map(act => (
              <div key={act.id} className="public-card">
                <span className="public-badge">{act.category}</span>
                <h3 className="public-card-title-spacing">{act.title}</h3>
                <p>{act.description}</p>
                <small className="public-meta">{act.date} • {act.status}</small>
              </div>
            ))}
          </div>
          <div className="public-center-action">
            <Link to="/activities" className="public-btn public-btn-secondary">
              Lihat Semua Kegiatan
            </Link>
          </div>
        </div>
      </section>

      {/* 7. TESTIMONI */}
      <section className="public-section">
        <div className="public-container">
          <div className="public-section-header">
            <h2>Apa Kata Mereka</h2>
            <p>Cerita dan pengalaman dari keluarga besar PUB.</p>
          </div>
          <div className="public-grid-3">
            {testimonials.length > 0 ? testimonials.map(testi => (
              <div key={testi.id} className="public-testi-card">
                {testi.mediaUrl && <img src={testi.mediaUrl} alt={testi.title} className="public-testi-img" />}
                <p className="testi-text">"{testi.body}"</p>
                <div className="testi-author">
                  <strong>{testi.authorName || testi.title}</strong>
                  {testi.authorRole && <span>{testi.authorRole}</span>}
                </div>
              </div>
            )) : staticTestimonials.map(testi => (
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
          <div className="public-hero-cta public-cta-actions-center">
            <Link to="/public/organization" className="public-btn public-btn-outline-light">Lihat Struktur</Link>
            <Link to="/login" className="public-btn public-btn-light">Masuk Anggota</Link>
          </div>
        </div>
      </section>
    </div>
  );
}
