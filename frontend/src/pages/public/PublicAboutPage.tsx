import { useState, useEffect } from "react";
import { Link } from "react-router";
import { publicContentService } from "../../services/publicContentService";
import type { PublicContentEntry } from '../../types/publicContent';
import { PublicContentType } from '../../types/publicContent';

export function PublicAboutPage() {
  const [about, setAbout] = useState<PublicContentEntry | null>(null);
  const [vision, setVision] = useState<PublicContentEntry | null>(null);
  const [mission, setMission] = useState<PublicContentEntry | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      publicContentService.getPublished(PublicContentType.ABOUT),
      publicContentService.getPublished(PublicContentType.VISION),
      publicContentService.getPublished(PublicContentType.MISSION)
    ]).then(([aboutRes, visionRes, missionRes]) => {
      if (aboutRes.length > 0) setAbout(aboutRes[0]);
      if (visionRes.length > 0) setVision(visionRes[0]);
      if (missionRes.length > 0) setMission(missionRes[0]);
    }).catch(() => {})
    .finally(() => setLoading(false));
  }, []);

  return (
    <div className="public-page">
      <div className="public-page-header">
        <div className="public-container">
          <h1>Tentang PUB</h1>
          <p>Mengenal lebih dekat Program Unggulan Bersama (PUB).</p>
        </div>
      </div>

      <div className="public-container public-content-wrapper">
        {(!about && !vision && !mission) && !loading && (
          <div className="public-alert-info">
            <strong>Catatan Demo:</strong> Konten profil ini merupakan versi ringkas untuk demo Orchestria dan dapat disesuaikan dengan profil resmi PUB.
          </div>
        )}

        <section className="public-about-section">
          <h2>Profil PUB</h2>
          {about ? (
            <div>
              {about.mediaUrl && <img src={about.mediaUrl} alt={about.title} className="public-profile-img" />}
              <h3>{about.title}</h3>
              <div className="public-profile-content">{about.body}</div>
            </div>
          ) : (
            <p>
              Program Unggulan Bersama (PUB) adalah program beasiswa dan pembinaan yang berada di bawah
              naungan Universitas Nasional PASIM. Program ini dirancang khusus untuk memfasilitasi mahasiswa
              berprestasi dan memiliki semangat belajar tinggi agar mampu menjadi talenta profesional di industri.
            </p>
          )}
        </section>

        {(vision || mission) ? (
          <section className="public-about-section">
            <h2>Visi & Misi</h2>
            <div className="public-grid-2">
              <div className="public-card">
                <h3>{vision?.title || 'Visi'}</h3>
                <div className="public-profile-content">{vision?.body || 'Belum ada visi.'}</div>
              </div>
              <div className="public-card">
                <h3>{mission?.title || 'Misi'}</h3>
                <div className="public-profile-content">{mission?.body || 'Belum ada misi.'}</div>
              </div>
            </div>
          </section>
        ) : !about && (
          <>
            <section className="public-about-section">
              <h2>Visi & Misi</h2>
              <div className="public-grid-2">
                <div className="public-card">
                  <h3>Visi</h3>
                  <p>
                    Menjadi pusat pembinaan generasi muda yang unggul dalam akhlak, intelektual, dan kompetensi 
                    teknologi yang siap bersaing di tingkat global.
                  </p>
                </div>
                <div className="public-card">
                  <h3>Misi</h3>
                  <ul className="public-list">
                    <li>Menyelenggarakan pembinaan karakter dan kepemimpinan.</li>
                    <li>Memberikan fasilitas pendidikan dan asrama yang memadai.</li>
                    <li>Menyediakan kurikulum pelatihan teknologi yang relevan dengan industri.</li>
                    <li>Membangun kolaborasi dan kemandirian finansial anggota.</li>
                  </ul>
                </div>
              </div>
            </section>

            <section className="public-about-section">
              <h2>Nilai Utama</h2>
              <div className="public-grid-3">
                <div className="public-value-card">
                  <div className="value-icon">🤝</div>
                  <h4>Kekeluargaan</h4>
                  <p>Membangun ikatan solidaritas yang kuat antar anggota dan alumni.</p>
                </div>
                <div className="public-value-card">
                  <div className="value-icon">💡</div>
                  <h4>Inovasi</h4>
                  <p>Mendorong kreativitas dan keberanian menciptakan solusi teknologi baru.</p>
                </div>
                <div className="public-value-card">
                  <div className="value-icon">🛡️</div>
                  <h4>Disiplin</h4>
                  <p>Menjunjung tinggi komitmen, etos kerja, dan tanggung jawab organisasi.</p>
                </div>
              </div>
            </section>
          </>
        )}

        <section className="public-about-section">
          <h2>Sistem Orchestria</h2>
          <p>
            Platform <strong>Orchestria</strong> membagi layanannya menjadi dua lapisan untuk menjamin keamanan 
            dan transparansi:
          </p>
          <ul className="public-list">
            <li>
              <strong>Public Portal:</strong> Lapisan terbuka bagi masyarakat umum untuk melihat profil, struktur, 
              dan agenda organisasi secara transparan.
            </li>
            <li>
              <strong>Internal Organization OS:</strong> Sistem manajemen tertutup yang dikendalikan melalui 
              mekanisme <em>Role-Based Access Control</em> (RBAC) bagi para pengurus untuk mengelola persetujuan, 
              keuangan, arsip, dan operasional internal.
            </li>
          </ul>
        </section>

        <div className="public-center-action public-center-action-spacing">
          <Link to="/login" className="public-btn public-btn-primary">Masuk ke Internal OS</Link>
        </div>
      </div>
    </div>
  );
}
