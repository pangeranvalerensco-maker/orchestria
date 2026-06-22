import { useState, type FormEvent } from "react";
import { Link, useNavigate, useSearchParams } from "react-router";
import { ApiError } from "../../api/http";
import { resetPassword } from "../../services/authService";

export function ResetPasswordPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");

  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  if (!token) {
    return (
      <main className="login-page">
        <section className="login-form-panel-centered">
          <div className="login-card login-card-centered-text">
            <h2>Akses Ditolak</h2>
            <p className="mt-1-mb-1">Token reset password tidak ditemukan.</p>
            <Link to="/auth/forgot-password" className="primary-button d-inline-block-no-underline">
              Minta Reset Ulang
            </Link>
          </div>
        </section>
      </main>
    );
  }

  async function handleReset(e: FormEvent) {
    e.preventDefault();
    setErrorMessage(null);
    setSuccessMessage(null);
    
    if (newPassword !== confirmPassword) {
      setErrorMessage("Konfirmasi password tidak cocok.");
      return;
    }

    setSubmitting(true);

    try {
      const response = await resetPassword({ resetToken: token!, newPassword, confirmPassword });
      setSuccessMessage(response.message || "Password berhasil direset.");
      setTimeout(() => navigate("/login"), 3000);
    } catch (error) {
      if (error instanceof ApiError) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("Terjadi kesalahan.");
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="login-page">
      <section className="login-form-panel-centered">
        <form className="login-card login-card-centered" onSubmit={handleReset}>
          <div className="login-heading">
            <p className="eyebrow">PEMULIHAN AKUN</p>
            <h2>Buat Password Baru</h2>
            <p>Masukkan password baru untuk akun Anda.</p>
          </div>

          {errorMessage && <div className="alert alert-error" role="alert">{errorMessage}</div>}
          {successMessage && <div className="alert alert-success" role="alert">{successMessage}</div>}

          <label className="form-field">
            <span>Password Baru</span>
            <input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              placeholder="Minimal 8 karakter"
              required
            />
          </label>

          <label className="form-field">
            <span>Konfirmasi Password</span>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="Ketik ulang password"
              required
            />
          </label>

          <button className="primary-button" type="submit" disabled={submitting || !!successMessage}>
            {submitting ? "Memproses..." : "Reset Password"}
          </button>
          
          <p className="login-footer">
            <Link to="/login" className="login-public-link">&larr; Kembali ke halaman login</Link>
          </p>
        </form>
      </section>
    </main>
  );
}
