import { useEffect, useState, useMemo } from "react";
import { useAuth } from "../auth/useAuth";
import { getMembers, getDivisions, getPositions } from "../services/organizationService";
import { ApiError } from "../api/http";
import type { MemberResponse, DivisionResponse, PositionResponse } from "../types/organization";

type Tab = "members" | "divisions" | "positions";

function normalizeMemberStatus(member: MemberResponse): string {
  const raw = member.status?.toUpperCase();
  if (raw === "ACTIVE") return "AKTIF";
  if (raw === "INACTIVE") return "TIDAK AKTIF";
  if (raw) return raw;
  return member.active ? "AKTIF" : "TIDAK AKTIF";
}

export function OrganizationDirectoryPage() {
  const { token } = useAuth();

  const [activeTab, setActiveTab] = useState<Tab>("members");

  const [members, setMembers] = useState<MemberResponse[]>([]);
  const [divisions, setDivisions] = useState<DivisionResponse[]>([]);
  const [positions, setPositions] = useState<PositionResponse[]>([]);

  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [searchQuery, setSearchQuery] = useState("");

  useEffect(() => {
    if (!token) {
      setIsLoading(false);
      setError("Sesi tidak valid. Silakan login kembali.");
      return;
    }

    async function fetchData() {
      setIsLoading(true);
      setError(null);
      try {
        const [membersRes, divisionsRes, positionsRes] = await Promise.all([
          getMembers(token!),
          getDivisions(token!),
          getPositions(token!),
        ]);

        setMembers(membersRes.data ?? []);
        setDivisions(divisionsRes.data ?? []);
        setPositions(positionsRes.data ?? []);
      } catch (err: unknown) {
        if (err instanceof ApiError) {
          if (err.status === 401) {
            setError("Sesi tidak valid. Silakan login kembali.");
          } else if (err.status === 403) {
            setError("Anda tidak memiliki akses untuk melihat direktori organisasi.");
          } else {
            setError("Layanan organisasi sedang bermasalah. Silakan coba beberapa saat lagi.");
          }
        } else {
          setError("Terjadi kesalahan tak terduga. Silakan coba beberapa saat lagi.");
        }
      } finally {
        setIsLoading(false);
      }
    }

    fetchData();
  }, [token]);

  const filteredMembers = useMemo(() => {
    if (!searchQuery.trim()) return members;
    const query = searchQuery.toLowerCase();
    return members.filter(
      (m) =>
        m.fullName?.toLowerCase().includes(query) ||
        m.email?.toLowerCase().includes(query)
    );
  }, [members, searchQuery]);

  if (isLoading) {
    return (
      <div className="page-content org-loading-container">
        <div className="loading-card">
          <div className="spinner"></div>
          <p>Memuat direktori organisasi...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="page-content">
        <div className="empty-state">
          <div className="empty-state-icon org-error-icon">!</div>
          <h2>Akses Gagal</h2>
          <p>{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="page-content">
      <div className="page-heading org-page-heading">
        <p className="eyebrow">ORGANIZATION</p>
        <h1>Direktori Organisasi</h1>
        <p>Daftar lengkap anggota, divisi, dan jabatan dalam organisasi.</p>
      </div>

      <div className="summary-grid org-summary-grid">
        <div className="summary-card">
          <span>Total Anggota</span>
          <strong>{members.length}</strong>
        </div>
        <div className="summary-card">
          <span>Total Divisi</span>
          <strong>{divisions.length}</strong>
        </div>
        <div className="summary-card">
          <span>Total Jabatan</span>
          <strong>{positions.length}</strong>
        </div>
      </div>

      <div className="content-card request-list-card">
        <div className="org-tabs-header">
          <div className="tabs-nav">
            <button
              id="org-tab-members"
              className={`tab-button ${activeTab === "members" ? "active" : ""}`}
              onClick={() => setActiveTab("members")}
            >
              Anggota
            </button>
            <button
              id="org-tab-divisions"
              className={`tab-button ${activeTab === "divisions" ? "active" : ""}`}
              onClick={() => setActiveTab("divisions")}
            >
              Divisi
            </button>
            <button
              id="org-tab-positions"
              className={`tab-button ${activeTab === "positions" ? "active" : ""}`}
              onClick={() => setActiveTab("positions")}
            >
              Jabatan
            </button>
          </div>
        </div>

        {activeTab === "members" && (
          <div className="org-tab-content">
            <div className="org-search-wrapper">
              <input
                id="org-member-search"
                type="text"
                className="org-search-input"
                placeholder="Cari nama atau email..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            {filteredMembers.length === 0 ? (
              <div className="empty-state org-empty-state">
                <p>
                  {searchQuery
                    ? "Tidak ada anggota yang cocok dengan pencarian."
                    : "Tidak ada anggota yang ditemukan."}
                </p>
              </div>
            ) : (
              <div className="request-table-wrapper">
                <table className="request-table">
                  <thead>
                    <tr>
                      <th>Nama Lengkap</th>
                      <th>Email</th>
                      <th>NPM / Angkatan</th>
                      <th>Jurusan</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredMembers.map((m) => (
                      <tr key={m.id}>
                        <td><strong>{m.fullName}</strong></td>
                        <td>{m.email}</td>
                        <td>
                          <strong>{m.studentNumber ?? "-"}</strong>
                          <small>{m.cohort ?? "-"}</small>
                        </td>
                        <td>{m.major ?? "-"}</td>
                        <td>
                          <span
                            className={
                              m.active
                                ? "status-chip status-completed"
                                : "status-chip status-draft"
                            }
                          >
                            {normalizeMemberStatus(m)}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        {activeTab === "divisions" && (
          <div className="org-tab-content">
            {divisions.length === 0 ? (
              <div className="empty-state org-empty-state">
                <p>Tidak ada divisi yang ditemukan.</p>
              </div>
            ) : (
              <div className="request-table-wrapper">
                <table className="request-table">
                  <thead>
                    <tr>
                      <th>Kode</th>
                      <th>Nama Divisi</th>
                      <th>Deskripsi</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {divisions.map((d) => (
                      <tr key={d.id}>
                        <td><strong>{d.code}</strong></td>
                        <td>{d.name}</td>
                        <td>{d.description ?? "-"}</td>
                        <td>
                          <span
                            className={
                              d.active
                                ? "status-chip status-completed"
                                : "status-chip status-draft"
                            }
                          >
                            {d.active ? "AKTIF" : "TIDAK AKTIF"}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        {activeTab === "positions" && (
          <div className="org-tab-content">
            {positions.length === 0 ? (
              <div className="empty-state org-empty-state">
                <p>Tidak ada jabatan yang ditemukan.</p>
              </div>
            ) : (
              <div className="request-table-wrapper">
                <table className="request-table">
                  <thead>
                    <tr>
                      <th>Kode</th>
                      <th>Nama Jabatan</th>
                      <th>Deskripsi</th>
                      <th>Level</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {positions.map((p) => (
                      <tr key={p.id}>
                        <td><strong>{p.code}</strong></td>
                        <td>{p.name}</td>
                        <td>{p.description ?? "-"}</td>
                        <td>{p.levelOrder ?? "-"}</td>
                        <td>
                          <span
                            className={
                              p.active
                                ? "status-chip status-completed"
                                : "status-chip status-draft"
                            }
                          >
                            {p.active ? "AKTIF" : "TIDAK AKTIF"}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
