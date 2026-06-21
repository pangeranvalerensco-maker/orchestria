import {
  useState,
  type FormEvent,
} from "react";

import {
  Navigate,
  useNavigate,
  Link,
} from "react-router";

import { ApiError } from "../api/http";
import { useAuth } from "../auth/useAuth";

export function LoginPage() {
  const navigate = useNavigate();

  const {
    token,
    login,
  } = useAuth();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [errorMessage, setErrorMessage] =
    useState<string | null>(null);

  const [submitting, setSubmitting] =
    useState(false);

  if (token) {
    return (
      <Navigate
        to="/dashboard"
        replace
      />
    );
  }

  async function handleSubmit(
    event: FormEvent<HTMLFormElement>,
  ) {
    event.preventDefault();

    setErrorMessage(null);
    setSubmitting(true);

    try {
      await login({
        email: email.trim().toLowerCase(),
        password,
      });

      navigate("/dashboard", {
        replace: true,
      });
    } catch (error) {
      if (error instanceof ApiError) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage(
          "Tidak dapat terhubung ke server Orchestria.",
        );
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="login-page">
      <section className="login-brand-panel">
        <div className="brand-badge">O</div>

        <div>
          <p className="eyebrow">ORGANIZATION OPERATING SYSTEM</p>
          <h1>Orchestria</h1>

          <p className="brand-description">
            Kelola alur organisasi, pengajuan,
            persetujuan, dan keuangan dalam satu sistem.
          </p>
        </div>

        <div className="brand-feature-list">
          <span>Approval bertingkat</span>
          <span>Kontrol akses berbasis role</span>
          <span>Manajemen operasional organisasi</span>
        </div>
      </section>

      <section className="login-form-panel">
        <form
          className="login-card"
          onSubmit={handleSubmit}
        >
          <div className="login-heading">
            <p className="eyebrow">SELAMAT DATANG</p>
            <h2>Masuk ke Internal OS</h2>
            <p>
              Portal ini khusus untuk anggota dan pengurus PUB yang telah terdaftar.
            </p>
          </div>

          {errorMessage && (
            <div
              className="alert alert-error"
              role="alert"
            >
              {errorMessage}
            </div>
          )}

          <label className="form-field">
            <span>Email</span>

            <input
              type="email"
              value={email}
              onChange={(event) =>
                setEmail(event.target.value)
              }
              placeholder="admin@orchestria.local"
              autoComplete="email"
              required
            />
          </label>

          <label className="form-field">
            <span>Password</span>

            <input
              type="password"
              value={password}
              onChange={(event) =>
                setPassword(event.target.value)
              }
              placeholder="Masukkan password"
              autoComplete="current-password"
              required
            />
          </label>

          <button
            className="primary-button"
            type="submit"
            disabled={submitting}
          >
            {submitting
              ? "Sedang masuk..."
              : "Masuk"}
          </button>

          <p className="login-footer">
            <Link to="/" className="login-public-link">&larr; Kembali ke Beranda Publik</Link>
            <br />
            Orchestria · Universitas Nasional PASIM
          </p>
        </form>
      </section>
    </main>
  );
}
