import { useAuth } from "../auth/useAuth";

export function DashboardPage() {
  const {
    user,
    hasRole,
  } = useAuth();

  const isSuperAdmin =
    hasRole("SUPER_ADMIN");

  return (
    <main className="page-content">
      <section className="welcome-card">
        <div>
          <p className="eyebrow">
            DASHBOARD
          </p>

          <h1>
            Selamat datang, {user?.fullName}
          </h1>

          <p>
            Login frontend berhasil dan profil ini
            telah dibaca dari auth-service melalui
            API Gateway.
          </p>
        </div>

        <div className="welcome-icon">
          ✓
        </div>
      </section>

      <section className="summary-grid">
        <article className="summary-card">
          <span>Status akun</span>
          <strong>
            {user?.active
              ? "Aktif"
              : "Tidak aktif"}
          </strong>
          <small>
            Status autentikasi pengguna
          </small>
        </article>

        <article className="summary-card">
          <span>Jumlah role</span>
          <strong>
            {user?.roles.length ?? 0}
          </strong>
          <small>
            Role keamanan yang dimiliki
          </small>
        </article>

        <article className="summary-card">
          <span>Jumlah permission</span>
          <strong>
            {user?.permissions.length ?? 0}
          </strong>
          <small>
            Akses fitur yang tersedia
          </small>
        </article>
      </section>

      <section className="dashboard-grid">
        <article className="content-card">
          <div className="card-heading">
            <div>
              <p className="eyebrow">
                IDENTITAS
              </p>
              <h2>Profil Pengguna</h2>
            </div>
          </div>

          <dl className="profile-list">
            <div>
              <dt>ID pengguna</dt>
              <dd>{user?.id}</dd>
            </div>

            <div>
              <dt>Nama lengkap</dt>
              <dd>{user?.fullName}</dd>
            </div>

            <div>
              <dt>Email</dt>
              <dd>{user?.email}</dd>
            </div>
          </dl>
        </article>

        <article className="content-card">
          <div className="card-heading">
            <div>
              <p className="eyebrow">
                AKSES
              </p>
              <h2>Role Pengguna</h2>
            </div>
          </div>

          <div className="chip-list">
            {user?.roles.map((role) => (
              <span
                className="chip"
                key={role}
              >
                {role}
              </span>
            ))}
          </div>

          {isSuperAdmin && (
            <div className="admin-notice">
              Akun ini dapat digunakan untuk menguji
              seluruh happy path MVP.
            </div>
          )}
        </article>

        <article className="content-card full-width">
          <div className="card-heading">
            <div>
              <p className="eyebrow">
                PERMISSION
              </p>
              <h2>Hak Akses Sistem</h2>
            </div>
          </div>

          <div className="permission-grid">
            {user?.permissions.map(
              (permission) => (
                <span
                  className="permission-item"
                  key={permission}
                >
                  {permission}
                </span>
              ),
            )}
          </div>
        </article>
      </section>
    </main>
  );
}