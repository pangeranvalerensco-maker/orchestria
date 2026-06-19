import {
  useCallback,
  useEffect,
  useMemo,
  useState,
  type FormEvent,
} from "react";

import { ApiError } from "../api/http";
import { useAuth } from "../auth/useAuth";
import {
  assignUserRole,
  getAdminRoles,
  getAdminUsers,
  registerUser,
  removeUserRole,
} from "../services/adminService";
import type { RoleSummary } from "../types/admin";
import type { User } from "../types/auth";

function readableRole(roleName: string) {
  return roleName
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

export function AdminUsersPage() {
  const { token, user: currentUser } = useAuth();
  const [users, setUsers] = useState<User[]>([]);
  const [roles, setRoles] = useState<RoleSummary[]>([]);
  const [search, setSearch] = useState("");
  const [roleSelections, setRoleSelections] = useState<Record<number, string>>({});
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [busyAction, setBusyAction] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    if (!token) return;

    setLoading(true);
    setErrorMessage(null);

    try {
      const [usersResponse, rolesResponse] = await Promise.all([
        getAdminUsers(token),
        getAdminRoles(token),
      ]);

      const activeRoles = rolesResponse.data
        .filter((role) => role.active)
        .sort((left, right) => left.name.localeCompare(right.name));

      setUsers(usersResponse.data);
      setRoles(activeRoles);
      setRoleSelections({});
    } catch (error) {
      setErrorMessage(
        error instanceof ApiError
          ? error.message
          : "Data akun dan role tidak dapat dimuat.",
      );
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    void loadData();
  }, [loadData]);

  const filteredUsers = useMemo(() => {
    const normalizedSearch = search.trim().toLowerCase();
    if (!normalizedSearch) return users;

    return users.filter((account) =>
      account.fullName.toLowerCase().includes(normalizedSearch)
      || account.email.toLowerCase().includes(normalizedSearch)
      || account.roles.some((role) => role.toLowerCase().includes(normalizedSearch)),
    );
  }, [search, users]);

  function replaceUser(updatedUser: User) {
    setUsers((current) => current.map((account) =>
      account.id === updatedUser.id ? updatedUser : account,
    ));
  }

  async function handleRegister(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!token) return;

    setSubmitting(true);
    setErrorMessage(null);
    setSuccessMessage(null);

    try {
      const response = await registerUser(token, {
        fullName: fullName.trim(),
        email: email.trim().toLowerCase(),
        password,
      });

      setUsers((current) => [...current, response.data]);
      setFullName("");
      setEmail("");
      setPassword("");
      setSuccessMessage(`Akun ${response.data.fullName} berhasil dibuat sebagai ANGGOTA.`);
    } catch (error) {
      setErrorMessage(
        error instanceof ApiError
          ? error.message
          : "Akun tidak dapat dibuat.",
      );
    } finally {
      setSubmitting(false);
    }
  }

  async function handleAssignRole(account: User) {
    if (!token) return;

    const roleName = roleSelections[account.id];
    const roleAvailable = roles.some(
      (role) => role.name === roleName && !account.roles.includes(role.name),
    );

    if (!roleName || !roleAvailable) {
      setErrorMessage("Pilih role tambahan terlebih dahulu.");
      return;
    }

    const actionKey = `assign-${account.id}-${roleName}`;
    setBusyAction(actionKey);
    setErrorMessage(null);
    setSuccessMessage(null);

    try {
      const response = await assignUserRole(token, account.id, roleName);
      replaceUser(response.data);
      setRoleSelections((current) => ({
        ...current,
        [account.id]: "",
      }));
      setSuccessMessage(`${readableRole(roleName)} ditambahkan ke ${account.fullName}.`);
    } catch (error) {
      setErrorMessage(
        error instanceof ApiError
          ? error.message
          : "Role tidak dapat ditambahkan.",
      );
    } finally {
      setBusyAction(null);
    }
  }

  async function handleRemoveRole(account: User, roleName: string) {
    if (!token) return;

    const actionKey = `remove-${account.id}-${roleName}`;
    setBusyAction(actionKey);
    setErrorMessage(null);
    setSuccessMessage(null);

    try {
      const response = await removeUserRole(token, account.id, roleName);
      replaceUser(response.data);
      setRoleSelections((current) => ({
        ...current,
        [account.id]: "",
      }));
      setSuccessMessage(`${readableRole(roleName)} dihapus dari ${account.fullName}.`);
    } catch (error) {
      setErrorMessage(
        error instanceof ApiError
          ? error.message
          : "Role tidak dapat dihapus.",
      );
    } finally {
      setBusyAction(null);
    }
  }

  return (
    <main className="page-content">
      <section className="page-heading">
        <div>
          <p className="eyebrow">SUPER ADMIN</p>
          <h1>Manajemen Akun</h1>
          <p>Buat akun internal dan atur role berdasarkan tanggung jawab pengurus.</p>
        </div>
      </section>

      {errorMessage && <div className="alert alert-error" role="alert">{errorMessage}</div>}
      {successMessage && <div className="alert alert-success" role="status">{successMessage}</div>}

      <section className="content-card admin-account-form">
        <div className="card-heading">
          <div>
            <p className="eyebrow">AKUN BARU</p>
            <h2>Tambah Pengguna</h2>
          </div>
          <span className="table-chip">Role awal: ANGGOTA</span>
        </div>

        <form onSubmit={(event) => void handleRegister(event)}>
          <div className="form-grid admin-form-grid">
            <label className="form-field">
              <span>Nama Lengkap</span>
              <input
                value={fullName}
                onChange={(event) => setFullName(event.target.value)}
                required
                maxLength={150}
              />
            </label>
            <label className="form-field">
              <span>Email</span>
              <input
                type="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                required
                maxLength={150}
              />
            </label>
            <label className="form-field">
              <span>Password Awal</span>
              <input
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                required
                minLength={6}
                maxLength={100}
              />
            </label>
          </div>
          <div className="form-actions">
            <button
              className="primary-button form-submit-button"
              type="submit"
              disabled={submitting}
            >
              {submitting ? "Membuat..." : "Buat Akun"}
            </button>
          </div>
        </form>
      </section>

      <section className="content-card admin-users-card">
        <div className="card-heading admin-users-heading">
          <div>
            <p className="eyebrow">AKSES SISTEM</p>
            <h2>Daftar Pengguna</h2>
            <p className="admin-role-help">
              Chip biru menunjukkan role aktif. Dropdown hanya digunakan untuk memilih role tambahan.
            </p>
          </div>
          <label className="admin-search-field">
            <span>Cari</span>
            <input
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Nama, email, atau role"
            />
          </label>
        </div>

        {loading ? (
          <div className="empty-state">
            <div className="spinner" />
            <p>Memuat akun...</p>
          </div>
        ) : filteredUsers.length ? (
          <div className="admin-user-list">
            {filteredUsers.map((account) => {
              const availableRoles = roles.filter(
                (role) => !account.roles.includes(role.name),
              );
              const selectedRole = roleSelections[account.id] ?? "";
              const isCurrentAccount = account.id === currentUser?.id;

              return (
                <article className="admin-user-card" key={account.id}>
                  <div className="admin-user-identity">
                    <div className="avatar">{account.fullName.charAt(0).toUpperCase()}</div>
                    <div>
                      <strong>{account.fullName}</strong>
                      <small>{account.email}</small>
                    </div>
                    <span className={account.active ? "status-badge" : "table-chip"}>
                      {account.active ? "Aktif" : "Nonaktif"}
                    </span>
                  </div>

                  <div className="admin-role-list">
                    {account.roles
                      .slice()
                      .sort()
                      .map((roleName) => (
                        <span className="admin-role-chip" key={roleName}>
                          {readableRole(roleName)}
                          <button
                            type="button"
                            aria-label={`Hapus role ${roleName}`}
                            disabled={
                              busyAction !== null
                              || account.roles.length === 1
                              || (isCurrentAccount && roleName === "SUPER_ADMIN")
                            }
                            onClick={() => void handleRemoveRole(account, roleName)}
                          >
                            {busyAction === `remove-${account.id}-${roleName}` ? "…" : "×"}
                          </button>
                        </span>
                      ))}
                  </div>

                  <div className="admin-role-action">
                    <select
                      aria-label={`Pilih role tambahan untuk ${account.fullName}`}
                      value={selectedRole}
                      disabled={!availableRoles.length || busyAction !== null}
                      onChange={(event) => setRoleSelections((current) => ({
                        ...current,
                        [account.id]: event.target.value,
                      }))}
                    >
                      <option value="" disabled>
                        {availableRoles.length
                          ? "Pilih role tambahan"
                          : "Semua role sudah dimiliki"}
                      </option>
                      {availableRoles.map((role) => (
                        <option value={role.name} key={role.id}>
                          {readableRole(role.name)}
                        </option>
                      ))}
                    </select>
                    <button
                      className="secondary-button"
                      type="button"
                      disabled={!selectedRole || busyAction !== null}
                      onClick={() => void handleAssignRole(account)}
                    >
                      {busyAction === `assign-${account.id}-${selectedRole}`
                        ? "Menambahkan..."
                        : "Tambahkan Role"}
                    </button>
                  </div>
                </article>
              );
            })}
          </div>
        ) : (
          <div className="empty-state">
            <h2>Akun tidak ditemukan</h2>
            <p>Ubah kata pencarian atau buat akun baru.</p>
          </div>
        )}
      </section>
    </main>
  );
}
