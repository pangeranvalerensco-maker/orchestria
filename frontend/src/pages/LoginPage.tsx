import {
  useState,
  type FormEvent,
  useEffect,
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
    verifyOtp,
  } = useAuth();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [errorMessage, setErrorMessage] =
    useState<string | null>(null);

  const [submitting, setSubmitting] =
    useState(false);

  // OTP State
  const [requiresOtp, setRequiresOtp] = useState(false);
  const [challengeId, setChallengeId] = useState<string | null>(null);
  const [maskedEmail, setMaskedEmail] = useState("");
  const [otpCode, setOtpCode] = useState("");
  const [rememberDevice, setRememberDevice] = useState(false);
  const [resendCountdown, setResendCountdown] = useState(0);

  useEffect(() => {
    let timer: number;
    if (resendCountdown > 0) {
      timer = window.setInterval(() => {
        setResendCountdown((prev) => prev - 1);
      }, 1000);
    }
    return () => window.clearInterval(timer);
  }, [resendCountdown]);

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
      const result = await login({
        email: email.trim().toLowerCase(),
        password,
      });

      if (result.status === 'OTP_REQUIRED') {
        setRequiresOtp(true);
        setChallengeId(result.challengeId || null);
        setMaskedEmail(result.maskedEmail || "");
        setResendCountdown(result.resendAfterSeconds || 60);
      } else {
        navigate("/dashboard", {
          replace: true,
        });
      }
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

  async function handleVerifyOtp(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!challengeId) return;

    setErrorMessage(null);
    setSubmitting(true);

    try {
      await verifyOtp({
        challengeId,
        code: otpCode,
        rememberDevice,
        deviceName: navigator.userAgent.substring(0, 200),
      });

      navigate("/dashboard", {
        replace: true,
      });
    } catch (error) {
      if (error instanceof ApiError) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("Gagal verifikasi OTP.");
      }
    } finally {
      setSubmitting(false);
    }
  }

  async function handleResendOtp() {
    if (!challengeId || resendCountdown > 0) return;
    setErrorMessage(null);
    setSubmitting(true);
    
    try {
      const response = await import("../services/authService").then(m => m.resendOtp({ challengeId }));
      setResendCountdown(response.data.resendAfterSeconds || 60);
    } catch (error) {
      if (error instanceof ApiError) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("Gagal mengirim ulang OTP.");
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
        {!requiresOtp ? (
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
              <div className="flex-space-between-center-mb-1">
                <span>Password</span>
                <Link to="/auth/forgot-password" className="forgot-password-link">Lupa password?</Link>
              </div>

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

            <div className="mt-1-text-center session-demo-link-wrapper">
              <Link to="/session-demo" className="login-public-link font-bold">Demo Stateful Session</Link>
              <p className="session-demo-link-desc">Fitur khusus demonstrasi materi pelatihan, bukan login operasional.</p>
            </div>

            <p className="login-footer">
              <Link to="/" className="login-public-link">&larr; Kembali ke Beranda Publik</Link>
              <br />
              Orchestria · Universitas Nasional PASIM
            </p>
          </form>
        ) : (
          <form
            className="login-card"
            onSubmit={handleVerifyOtp}
          >
            <div className="login-heading">
              <p className="eyebrow">VERIFIKASI KEAMANAN</p>
              <h2>Masukkan Kode OTP</h2>
              <p>
                Kode verifikasi telah dikirim ke email <strong>{maskedEmail}</strong>.
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
              <span>Kode OTP</span>

              <input
                type="text"
                value={otpCode}
                onChange={(event) =>
                  setOtpCode(event.target.value)
                }
                placeholder="Misal: 123456"
                autoComplete="one-time-code"
                required
              />
            </label>

            <label className="checkbox-remember">
              <input
                type="checkbox"
                checked={rememberDevice}
                onChange={(e) => setRememberDevice(e.target.checked)}
              />
              <span className="text-sm">Ingat perangkat ini selama 7 hari</span>
            </label>

            <button
              className="primary-button"
              type="submit"
              disabled={submitting || otpCode.length < 6}
            >
              {submitting
                ? "Memverifikasi..."
                : "Verifikasi Kode"}
            </button>
            
            <div className="mt-1-text-center">
              <button 
                type="button" 
                onClick={handleResendOtp}
                disabled={resendCountdown > 0 || submitting}
                className={resendCountdown > 0 ? "resend-button disabled" : "resend-button active"}
              >
                {resendCountdown > 0 ? `Kirim ulang kode dalam ${resendCountdown}s` : "Kirim ulang kode"}
              </button>
            </div>

            <p className="login-footer">
              <button type="button" onClick={() => setRequiresOtp(false)} className="back-button-link">
                &larr; Kembali ke halaman login
              </button>
            </p>
          </form>
        )}
      </section>
    </main>
  );
}