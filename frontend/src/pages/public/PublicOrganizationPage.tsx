import { useEffect, useState, useMemo } from "react";
import { getPublicStructureCurrent } from "../../services/publicOrganizationService";
import type { PublicMemberAssignmentResponse, PublicPeriodResponse } from "../../types/publicOrganization";

export function PublicOrganizationPage() {
  const [period, setPeriod] = useState<PublicPeriodResponse | null>(null);
  const [structure, setStructure] = useState<PublicMemberAssignmentResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [searchQuery, setSearchQuery] = useState("");
  const [selectedDivision, setSelectedDivision] = useState<string>("ALL");

  useEffect(() => {
    async function fetchData() {
      try {
        const res = await getPublicStructureCurrent();
        if (res.data) {
          setPeriod(res.data.period);
          setStructure(res.data.structure || []);
        }
      } catch {
        setError("Gagal memuat struktur organisasi. Silakan coba lagi.");
      } finally {
        setIsLoading(false);
      }
    }
    fetchData();
  }, []);

  const divisions = useMemo(() => {
    const unique = new Set(structure.map((s) => s.divisionName));
    return Array.from(unique).sort();
  }, [structure]);

  const filteredStructure = useMemo(() => {
    return structure.filter((s) => {
      const matchSearch = s.memberName.toLowerCase().includes(searchQuery.toLowerCase());
      const matchDiv = selectedDivision === "ALL" || s.divisionName === selectedDivision;
      return matchSearch && matchDiv;
    });
  }, [structure, searchQuery, selectedDivision]);

  // Group by division
  const groupedStructure = useMemo(() => {
    const groups: Record<string, PublicMemberAssignmentResponse[]> = {};
    for (const member of filteredStructure) {
      if (!groups[member.divisionName]) {
        groups[member.divisionName] = [];
      }
      groups[member.divisionName].push(member);
    }

    // Sort members in each division by positionLevelOrder
    for (const div in groups) {
      groups[div].sort((a, b) => a.positionLevelOrder - b.positionLevelOrder);
    }

    return groups;
  }, [filteredStructure]);

  if (isLoading) {
    return (
      <div className="public-page">
        <div className="public-loading-screen">
          <div className="spinner"></div>
          <p>Memuat direktori organisasi publik...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="public-page">
      <div className="public-page-header">
        <div className="public-container">
          <h1>Struktur Organisasi</h1>
          <p>
            {period 
              ? `Pengurus Aktif Periode ${period.name}` 
              : "Direktori lengkap pimpinan dan anggota divisi yang aktif."}
          </p>
        </div>
      </div>

      <div className="public-container public-content-wrapper">
        {error ? (
          <div className="public-alert-error">{error}</div>
        ) : (
          <>
            <div className="public-filters">
              <input
                type="text"
                placeholder="Cari nama anggota..."
                className="public-input"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
              <select
                className="public-select"
                value={selectedDivision}
                onChange={(e) => setSelectedDivision(e.target.value)}
              >
                <option value="ALL">Semua Divisi</option>
                {divisions.map((div) => (
                  <option key={div} value={div}>
                    {div}
                  </option>
                ))}
              </select>
            </div>

            {Object.keys(groupedStructure).length === 0 ? (
              <div className="public-empty">
                <p>Tidak ada pengurus yang sesuai dengan kriteria pencarian.</p>
              </div>
            ) : (
              <div className="public-structure-groups">
                {Object.entries(groupedStructure).map(([divName, members]) => (
                  <div key={divName} className="public-structure-group">
                    <h3 className="group-title">{divName}</h3>
                    <div className="public-grid-4">
                      {members.map((member) => (
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
                          {member.cohort && <span className="profile-cohort">Angkatan {member.cohort}</span>}
                          {member.major && <small className="profile-major">{member.major}</small>}
                        </div>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
