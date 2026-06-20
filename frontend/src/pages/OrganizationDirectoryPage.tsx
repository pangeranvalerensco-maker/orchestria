import { useEffect, useState, useMemo } from "react";
import { useAuth } from "../auth/useAuth";
import { getMembers, getDivisions, getPositions } from "../services/organizationService";
import type { MemberResponse, DivisionResponse, PositionResponse } from "../types/organization";

type Tab = "members" | "divisions" | "positions";

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
    async function fetchData() {
      if (!token) return;
      setIsLoading(true);
      setError(null);
      try {
        const [membersRes, divisionsRes, positionsRes] = await Promise.all([
          getMembers(token),
          getDivisions(token),
          getPositions(token),
        ]);
        
        setMembers(membersRes.data ?? []);
        setDivisions(divisionsRes.data ?? []);
        setPositions(positionsRes.data ?? []);
      } catch (err: any) {
        if (err.status === 401) {
          setError("Sesi tidak valid. Silakan login kembali.");
        } else if (err.status === 403) {
          setError("Anda tidak memiliki akses untuk melihat direktori organisasi.");
        } else {
          setError("Layanan organisasi sedang bermasalah. Silakan coba beberapa saat lagi.");
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
      <div className="page-content center-screen" style={{ minHeight: "50vh" }}>
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
          <div className="empty-state-icon" style={{ color: "#991b1b", background: "#fee2e2" }}>!</div>
          <h2>Akses Gagal</h2>
          <p>{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="page-content">
      <div className="page-heading" style={{ display: "block" }}>
        <p className="eyebrow" style={{ marginBottom: "8px" }}>ORGANIZATION</p>
        <h1>Direktori Organisasi</h1>
        <p>Daftar lengkap anggota, divisi, dan jabatan dalam organisasi.</p>
      </div>

      <div className="summary-grid" style={{ marginBottom: "28px" }}>
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
        <div style={{ padding: "22px 22px 0" }}>
          <div className="tabs-nav">
            <button
              className={`tab-button ${activeTab === "members" ? "active" : ""}`}
              onClick={() => setActiveTab("members")}
            >
              Anggota
            </button>
            <button
              className={`tab-button ${activeTab === "divisions" ? "active" : ""}`}
              onClick={() => setActiveTab("divisions")}
            >
              Divisi
            </button>
            <button
              className={`tab-button ${activeTab === "positions" ? "active" : ""}`}
              onClick={() => setActiveTab("positions")}
            >
              Jabatan
            </button>
          </div>
        </div>

        {activeTab === "members" && (
          <div style={{ padding: "0 22px 22px" }}>
            <div style={{ marginBottom: "16px", maxWidth: "300px" }}>
              <input
                type="text"
                placeholder="Cari nama atau email..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                style={{
                  width: "100%",
                  padding: "10px 14px",
                  borderRadius: "8px",
                  border: "1px solid #dbe3ef",
                  outline: "none"
                }}
              />
            </div>
            {filteredMembers.length === 0 ? (
              <div className="empty-state" style={{ minHeight: "200px" }}>
                <p>Tidak ada anggota yang ditemukan.</p>
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
                          <strong>{m.studentNumber || "-"}</strong>
                          <small>{m.cohort || "-"}</small>
                        </td>
                        <td>{m.major || "-"}</td>
                        <td>
                          <span className={m.active ? "status-chip status-completed" : "status-chip status-draft"}>
                            {m.status || (m.active ? "AKTIF" : "TIDAK AKTIF")}
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
          <div style={{ padding: "0 22px 22px" }}>
            {divisions.length === 0 ? (
              <div className="empty-state" style={{ minHeight: "200px" }}>
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
                        <td>{d.description || "-"}</td>
                        <td>
                          <span className={d.active ? "status-chip status-completed" : "status-chip status-draft"}>
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
          <div style={{ padding: "0 22px 22px" }}>
            {positions.length === 0 ? (
              <div className="empty-state" style={{ minHeight: "200px" }}>
                <p>Tidak ada jabatan yang ditemukan.</p>
              </div>
            ) : (
              <div className="request-table-wrapper">
                <table className="request-table">
                  <thead>
                    <tr>
                      <th>Kode</th>
                      <th>Nama Jabatan</th>
                      <th>Deskripsi / Level</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {positions.map((p) => (
                      <tr key={p.id}>
                        <td><strong>{p.code}</strong></td>
                        <td>{p.name}</td>
                        <td>
                          {p.description || "-"}
                          <small>Level: {p.levelOrder}</small>
                        </td>
                        <td>
                          <span className={p.active ? "status-chip status-completed" : "status-chip status-draft"}>
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
