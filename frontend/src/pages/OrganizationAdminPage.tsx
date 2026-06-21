import { useState, useEffect } from "react";
import { useAuth } from "../auth/useAuth";
import {
  getMembers, getDivisions, getPositions, getPeriods, getMemberAssignments,
  deleteMember, deleteDivision, deletePosition, deletePeriod, deleteMemberAssignment
} from "../services/organizationService";
import type {
  MemberResponse, DivisionResponse, PositionResponse, OrganizationPeriodResponse, MemberAssignment
} from "../types/organization";
import { getErrorMessage } from "../utils/apiErrorHandler";

import { AdminTabs, ADMIN_TABS } from "../components/organization-admin/AdminTabs";
import { Modal } from "../components/organization-admin/Modal";
import { MemberForm } from "../components/organization-admin/MemberForm";
import { DivisionForm } from "../components/organization-admin/DivisionForm";
import { PositionForm } from "../components/organization-admin/PositionForm";
import { PeriodForm } from "../components/organization-admin/PeriodForm";
import { AssignmentForm } from "../components/organization-admin/AssignmentForm";

type OrganizationAdminEditable =
  | MemberResponse
  | DivisionResponse
  | PositionResponse
  | OrganizationPeriodResponse
  | MemberAssignment;

export function OrganizationAdminPage() {
  const { token } = useAuth();
  
  const [activeTab, setActiveTab] = useState(ADMIN_TABS.MEMBERS);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [dataWarning, setDataWarning] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  
  const [members, setMembers] = useState<MemberResponse[]>([]);
  const [divisions, setDivisions] = useState<DivisionResponse[]>([]);
  const [positions, setPositions] = useState<PositionResponse[]>([]);
  const [periods, setPeriods] = useState<OrganizationPeriodResponse[]>([]);
  const [assignments, setAssignments] = useState<MemberAssignment[]>([]);

  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const [periodFilter, setPeriodFilter] = useState("");
  const [divisionFilter, setDivisionFilter] = useState("");

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalType, setModalType] = useState<"create" | "edit">("create");
  const [editData, setEditData] = useState<OrganizationAdminEditable | null>(null);

  const loadData = async (tab?: string) => {
    if (!token) {
      setLoading(false);
      setError("Sesi tidak valid. Silakan login kembali.");
      return;
    }
    setLoading(true);
    setError(null);
    setDataWarning(null);

    try {
      const targetTab = tab || activeTab;
      
      const promises: Promise<any>[] = [
        getMembers(token),
        getDivisions(token),
        getPositions(token),
        getPeriods(token)
      ];

      if (targetTab === ADMIN_TABS.ASSIGNMENTS) {
        promises.push(getMemberAssignments(token));
      }

      const results = await Promise.allSettled(promises);
      let hasPartialFailure = false;

      if (results[0].status === "fulfilled") setMembers(results[0].value.data);
      else { setMembers([]); hasPartialFailure = true; }

      if (results[1].status === "fulfilled") setDivisions(results[1].value.data);
      else { setDivisions([]); hasPartialFailure = true; }

      if (results[2].status === "fulfilled") setPositions(results[2].value.data);
      else { setPositions([]); hasPartialFailure = true; }

      if (results[3].status === "fulfilled") setPeriods(results[3].value.data);
      else { setPeriods([]); hasPartialFailure = true; }

      if (targetTab === ADMIN_TABS.ASSIGNMENTS) {
        if (results[4].status === "fulfilled") setAssignments(results[4].value.data);
        else { setAssignments([]); hasPartialFailure = true; }
      }

      if (hasPartialFailure) {
        setDataWarning("Sebagian data organisasi belum dapat dimuat.");
      }
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadData();
  }, [token]);

  const handleTabChange = (tab: string) => {
    setActiveTab(tab);
    setSearchQuery("");
    setStatusFilter("");
    setPeriodFilter("");
    setDivisionFilter("");
    setError(null);
    setSuccessMsg(null);
    void loadData(tab);
  };

  const handleDelete = async (id: number, name: string, type: string) => {
    if (!token) return;
    let confirmMsg = `Hapus ${type.toLowerCase()} ${name} dari data organisasi?`;
    if (type === "Penempatan") confirmMsg = "Hapus penempatan anggota ini dari struktur organisasi?";

    if (!window.confirm(confirmMsg + "\n\nCatatan: Endpoint backend dapat menonaktifkan atau menghapus data sesuai implementasi service.")) return;

    setError(null);
    setSuccessMsg(null);
    setLoading(true);
    try {
      if (type === "Anggota") await deleteMember(token, id);
      else if (type === "Divisi") await deleteDivision(token, id);
      else if (type === "Jabatan") await deletePosition(token, id);
      else if (type === "Periode") await deletePeriod(token, id);
      else if (type === "Penempatan") await deleteMemberAssignment(token, id);
      
      setSuccessMsg(`Data berhasil dihapus/dinonaktifkan.`);
      void loadData(activeTab);
    } catch (err) {
      setError(getErrorMessage(err));
      setLoading(false);
    }
  };

  const handleOpenModal = (type: "create" | "edit", data?: OrganizationAdminEditable) => {
    setModalType(type);
    setEditData(data || null);
    setIsModalOpen(true);
    setError(null);
    setSuccessMsg(null);
  };

  const handleFormSuccess = () => {
    setIsModalOpen(false);
    setSuccessMsg("Data berhasil disimpan.");
    void loadData(activeTab);
  };

  const renderSummaryCards = () => {
    const totalMembers = members.length;
    const activeMembers = members.filter(m => m.status === "ACTIVE").length;
    const totalDivisions = divisions.length;
    const activePeriods = periods.filter(p => p.currentPeriod).length;

    return (
      <section className="summary-grid org-admin-summary-grid">
        <div className="summary-card">
          <span>Total Anggota</span>
          <strong>{totalMembers}</strong>
        </div>
        <div className="summary-card">
          <span>Anggota Aktif</span>
          <strong>{activeMembers}</strong>
        </div>
        <div className="summary-card">
          <span>Total Divisi</span>
          <strong>{totalDivisions}</strong>
        </div>
        <div className="summary-card">
          <span>Periode Aktif</span>
          <strong>{activePeriods > 0 ? activePeriods : "Belum Ada"}</strong>
        </div>
      </section>
    );
  };

  const renderContent = () => {
    if (loading && members.length === 0) {
      return <div className="empty-state"><div className="spinner" /><p>Memuat data...</p></div>;
    }

    if (activeTab === ADMIN_TABS.MEMBERS) {
      const filtered = members.filter(m => {
        const matchesSearch = !searchQuery || 
          m.fullName.toLowerCase().includes(searchQuery.toLowerCase()) || 
          m.email.toLowerCase().includes(searchQuery.toLowerCase()) || 
          (m.studentNumber && m.studentNumber.toLowerCase().includes(searchQuery.toLowerCase()));
        const matchesStatus = !statusFilter || m.status === statusFilter;
        return matchesSearch && matchesStatus;
      });

      return (
        <section className="content-card">
          <div className="org-admin-flex-between">
            <div className="org-admin-search-bar org-admin-toolbar">
              <input type="text" placeholder="Cari nama, email, NIM..." value={searchQuery} onChange={e => setSearchQuery(e.target.value)} />
              <select value={statusFilter} onChange={e => setStatusFilter(e.target.value)}>
                <option value="">Semua Status</option>
                <option value="ACTIVE">Aktif</option>
                <option value="INACTIVE">Tidak Aktif</option>
                <option value="ALUMNI">Alumni</option>
              </select>
            </div>
            <button type="button" className="primary-button org-admin-primary-button-auto" onClick={() => handleOpenModal("create")}>+ Tambah Anggota</button>
          </div>
          
          <div className="request-table-wrapper org-admin-table-spacing">
            <table className="request-table">
              <thead>
                <tr>
                  <th>Nama</th>
                  <th>Email / NIM</th>
                  <th>Angkatan / Jurusan</th>
                  <th>Status / Publik</th>
                  <th>Aksi</th>
                </tr>
              </thead>
              <tbody>
                {filtered.length > 0 ? filtered.map(m => (
                  <tr key={m.id}>
                    <td><strong>{m.fullName}</strong></td>
                    <td>{m.email} <small>{m.studentNumber || "-"}</small></td>
                    <td>{m.cohort || "-"} <small>{m.major ? `${m.major} ${m.campusClass ? `(${m.campusClass})` : ''}` : "-"}</small></td>
                    <td>
                      <span className={`status-badge status-${m.status.toLowerCase()}`}>{m.status}</span>
                      <small>{m.publicVisible ? "Publik" : "Sembunyi"}</small>
                    </td>
                    <td>
                      <div className="org-admin-action-group">
                        <button type="button" className="org-admin-table-action-btn" onClick={() => handleOpenModal("edit", m)}>Edit</button>
                        <button type="button" className="org-admin-table-action-btn danger" onClick={() => handleDelete(m.id, m.fullName, "Anggota")}>Hapus</button>
                      </div>
                    </td>
                  </tr>
                )) : <tr><td colSpan={5} className="empty-budget">Tidak ada data anggota.</td></tr>}
              </tbody>
            </table>
          </div>
        </section>
      );
    }

    if (activeTab === ADMIN_TABS.DIVISIONS) {
      return (
        <section className="content-card">
          <div className="org-admin-flex-between">
            <h2 className="org-admin-section-title">Daftar Divisi</h2>
            <button type="button" className="primary-button org-admin-primary-button-auto" onClick={() => handleOpenModal("create")}>+ Tambah Divisi</button>
          </div>
          <div className="request-table-wrapper org-admin-table-spacing">
            <table className="request-table">
              <thead>
                <tr>
                  <th>Kode</th>
                  <th>Nama</th>
                  <th>Deskripsi</th>
                  <th>Urutan / Publik</th>
                  <th>Aksi</th>
                </tr>
              </thead>
              <tbody>
                {divisions.length > 0 ? [...divisions].sort((a,b) => (a.displayOrder || 0) - (b.displayOrder || 0)).map(d => (
                  <tr key={d.id}>
                    <td><strong>{d.code}</strong></td>
                    <td>{d.name}</td>
                    <td>{d.description || "-"}</td>
                    <td>{d.displayOrder || 0} <small>{d.publicVisible ? "Publik" : "Sembunyi"}</small></td>
                    <td>
                      <div className="org-admin-action-group">
                        <button type="button" className="org-admin-table-action-btn" onClick={() => handleOpenModal("edit", d)}>Edit</button>
                        <button type="button" className="org-admin-table-action-btn danger" onClick={() => handleDelete(d.id, d.name, "Divisi")}>Hapus</button>
                      </div>
                    </td>
                  </tr>
                )) : <tr><td colSpan={5} className="empty-budget">Tidak ada data divisi.</td></tr>}
              </tbody>
            </table>
          </div>
        </section>
      );
    }

    if (activeTab === ADMIN_TABS.POSITIONS) {
      return (
        <section className="content-card">
          <div className="org-admin-flex-between">
            <h2 className="org-admin-section-title">Daftar Jabatan</h2>
            <button type="button" className="primary-button org-admin-primary-button-auto" onClick={() => handleOpenModal("create")}>+ Tambah Jabatan</button>
          </div>
          <div className="request-table-wrapper org-admin-table-spacing">
            <table className="request-table">
              <thead>
                <tr>
                  <th>Kode</th>
                  <th>Nama</th>
                  <th>Deskripsi</th>
                  <th>Level / Publik</th>
                  <th>Aksi</th>
                </tr>
              </thead>
              <tbody>
                {positions.length > 0 ? [...positions].sort((a,b) => (a.levelOrder || 0) - (b.levelOrder || 0)).map(p => (
                  <tr key={p.id}>
                    <td><strong>{p.code}</strong></td>
                    <td>{p.name}</td>
                    <td>{p.description || "-"}</td>
                    <td>Level {p.levelOrder || 0} <small>{p.publicVisible ? "Publik" : "Sembunyi"}</small></td>
                    <td>
                      <div className="org-admin-action-group">
                        <button type="button" className="org-admin-table-action-btn" onClick={() => handleOpenModal("edit", p)}>Edit</button>
                        <button type="button" className="org-admin-table-action-btn danger" onClick={() => handleDelete(p.id, p.name, "Jabatan")}>Hapus</button>
                      </div>
                    </td>
                  </tr>
                )) : <tr><td colSpan={5} className="empty-budget">Tidak ada data jabatan.</td></tr>}
              </tbody>
            </table>
          </div>
        </section>
      );
    }

    if (activeTab === ADMIN_TABS.PERIODS) {
      return (
        <section className="content-card">
          <div className="org-admin-flex-between">
            <h2 className="org-admin-section-title">Daftar Periode Kepengurusan</h2>
            <button type="button" className="primary-button org-admin-primary-button-auto" onClick={() => handleOpenModal("create")}>+ Tambah Periode</button>
          </div>
          <div className="request-table-wrapper org-admin-table-spacing">
            <table className="request-table">
              <thead>
                <tr>
                  <th>Nama Periode</th>
                  <th>Tanggal Mulai</th>
                  <th>Tanggal Selesai</th>
                  <th>Status / Publik</th>
                  <th>Aksi</th>
                </tr>
              </thead>
              <tbody>
                {periods.length > 0 ? [...periods].sort((a,b) => (b.startDate || "").localeCompare(a.startDate || "")).map(p => (
                  <tr key={p.id}>
                    <td><strong>{p.name}</strong></td>
                    <td>{p.startDate || "-"}</td>
                    <td>{p.endDate || "-"}</td>
                    <td>
                      {p.currentPeriod ? <span className="status-badge status-completed">Current</span> : "Lalu"}
                      <small>{p.publicVisible ? "Publik" : "Sembunyi"}</small>
                    </td>
                    <td>
                      <div className="org-admin-action-group">
                        <button type="button" className="org-admin-table-action-btn" onClick={() => handleOpenModal("edit", p)}>Edit</button>
                        <button type="button" className="org-admin-table-action-btn danger" onClick={() => handleDelete(p.id, p.name, "Periode")}>Hapus</button>
                      </div>
                    </td>
                  </tr>
                )) : <tr><td colSpan={5} className="empty-budget">Tidak ada data periode.</td></tr>}
              </tbody>
            </table>
          </div>
        </section>
      );
    }

    if (activeTab === ADMIN_TABS.ASSIGNMENTS) {
      const filtered = assignments.filter(a => {
        const matchesSearch = !searchQuery || 
          a.memberName.toLowerCase().includes(searchQuery.toLowerCase()) || 
          (a.memberEmail && a.memberEmail.toLowerCase().includes(searchQuery.toLowerCase()));
        const matchesPeriod = !periodFilter || a.periodId.toString() === periodFilter;
        const matchesDivision = !divisionFilter || a.divisionId.toString() === divisionFilter;
        const matchesStatus = !statusFilter || a.status === statusFilter;
        return matchesSearch && matchesPeriod && matchesDivision && matchesStatus;
      });

      // Sort: current period, divisi, positionLevelOrder, nama anggota
      const sorted = [...filtered].sort((a, b) => {
        const periodA = periods.find(p => p.id === a.periodId);
        const periodB = periods.find(p => p.id === b.periodId);
        const isCurrentA = periodA?.currentPeriod ? 1 : 0;
        const isCurrentB = periodB?.currentPeriod ? 1 : 0;
        if (isCurrentA !== isCurrentB) return isCurrentB - isCurrentA;
        if (a.divisionName !== b.divisionName) return a.divisionName.localeCompare(b.divisionName);
        const levelA = a.positionLevelOrder || 999;
        const levelB = b.positionLevelOrder || 999;
        if (levelA !== levelB) return levelA - levelB;
        return a.memberName.localeCompare(b.memberName);
      });

      return (
        <section className="content-card">
          <div className="org-admin-flex-between">
            <div className="org-admin-search-bar org-admin-toolbar">
              <input type="text" placeholder="Cari nama/email..." value={searchQuery} onChange={e => setSearchQuery(e.target.value)} />
              <select value={periodFilter} onChange={e => setPeriodFilter(e.target.value)}>
                <option value="">Semua Periode</option>
                {periods.map(p => <option key={p.id} value={p.id}>{p.name} {p.currentPeriod ? "(Current)" : ""}</option>)}
              </select>
              <select value={divisionFilter} onChange={e => setDivisionFilter(e.target.value)}>
                <option value="">Semua Divisi</option>
                {divisions.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
              </select>
              <select value={statusFilter} onChange={e => setStatusFilter(e.target.value)}>
                <option value="">Semua Status</option>
                <option value="ACTIVE">Aktif</option>
                <option value="INACTIVE">Tidak Aktif</option>
              </select>
            </div>
            <button type="button" className="primary-button org-admin-primary-button-auto" onClick={() => handleOpenModal("create")}>+ Tambah Penempatan</button>
          </div>
          <div className="request-table-wrapper org-admin-table-spacing">
            <table className="request-table">
              <thead>
                <tr>
                  <th>Anggota</th>
                  <th>Periode</th>
                  <th>Divisi</th>
                  <th>Jabatan</th>
                  <th>Status</th>
                  <th>Aksi</th>
                </tr>
              </thead>
              <tbody>
                {sorted.length > 0 ? sorted.map(a => (
                  <tr key={a.id}>
                    <td><strong>{a.memberName}</strong><small>{a.memberEmail}</small></td>
                    <td>{a.periodName}</td>
                    <td>{a.divisionName} <small>{a.divisionCode}</small></td>
                    <td>{a.positionName} <small>Level {a.positionLevelOrder || '-'}</small></td>
                    <td><span className={`status-badge status-${a.status.toLowerCase()}`}>{a.status}</span></td>
                    <td>
                      <div className="org-admin-action-group">
                        <button type="button" className="org-admin-table-action-btn" onClick={() => handleOpenModal("edit", a)}>Edit</button>
                        <button type="button" className="org-admin-table-action-btn danger" onClick={() => handleDelete(a.id, a.memberName, "Penempatan")}>Hapus</button>
                      </div>
                    </td>
                  </tr>
                )) : <tr><td colSpan={6} className="empty-budget">Tidak ada data penempatan.</td></tr>}
              </tbody>
            </table>
          </div>
        </section>
      );
    }

    return null;
  };

  const getModalTitle = () => {
    const action = modalType === "create" ? "Tambah" : "Edit";
    let entity = "";
    if (activeTab === ADMIN_TABS.MEMBERS) entity = "Anggota";
    else if (activeTab === ADMIN_TABS.DIVISIONS) entity = "Divisi";
    else if (activeTab === ADMIN_TABS.POSITIONS) entity = "Jabatan";
    else if (activeTab === ADMIN_TABS.PERIODS) entity = "Periode Kepengurusan";
    else if (activeTab === ADMIN_TABS.ASSIGNMENTS) entity = "Penempatan Anggota";
    return `${action} ${entity}`;
  };

  return (
    <main className="page-content">
      <section className="page-heading">
        <div>
          <p className="eyebrow">ORGANIZATION MANAGEMENT</p>
          <h1>Kelola Organisasi</h1>
          <p>Kelola anggota, struktur, periode kepengurusan, dan penempatan organisasi.</p>
        </div>
      </section>

      {error && <div className="alert alert-error" role="alert">{error}</div>}
      {dataWarning && <div className="alert alert-warning" role="alert">{dataWarning}</div>}
      {successMsg && <div className="alert alert-success" role="alert">{successMsg}</div>}

      {renderSummaryCards()}

      <div className="org-admin-table-spacing">
        <AdminTabs activeTab={activeTab} onTabChange={handleTabChange} />
        {renderContent()}
      </div>

      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={getModalTitle()}>
        {token && isModalOpen && activeTab === ADMIN_TABS.MEMBERS && (
          <MemberForm token={token} initialData={editData as MemberResponse} onSuccess={handleFormSuccess} onCancel={() => setIsModalOpen(false)} />
        )}
        {token && isModalOpen && activeTab === ADMIN_TABS.DIVISIONS && (
          <DivisionForm token={token} initialData={editData as DivisionResponse} onSuccess={handleFormSuccess} onCancel={() => setIsModalOpen(false)} />
        )}
        {token && isModalOpen && activeTab === ADMIN_TABS.POSITIONS && (
          <PositionForm token={token} initialData={editData as PositionResponse} onSuccess={handleFormSuccess} onCancel={() => setIsModalOpen(false)} />
        )}
        {token && isModalOpen && activeTab === ADMIN_TABS.PERIODS && (
          <PeriodForm token={token} initialData={editData as OrganizationPeriodResponse} onSuccess={handleFormSuccess} onCancel={() => setIsModalOpen(false)} />
        )}
        {token && isModalOpen && activeTab === ADMIN_TABS.ASSIGNMENTS && (
          <AssignmentForm 
            token={token} 
            initialData={editData as MemberAssignment} 
            members={members}
            divisions={divisions}
            positions={positions}
            periods={periods}
            onSuccess={handleFormSuccess} 
            onCancel={() => setIsModalOpen(false)} 
          />
        )}
      </Modal>
    </main>
  );
}
