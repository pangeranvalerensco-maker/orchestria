import { useState, type FormEvent, useEffect } from "react";
import { Link, useNavigate } from "react-router";
import { ApiError } from "../../api/http";
import { forgotPassword, verifyForgotPassword } from "../../services/authService";

export function ForgotPasswordPage() {
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [challengeId, setChallengeId] = useState<string | null>(null);
  const [otpCode, setOtpCode] = useState("");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
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

  async function handleRequestReset(e: FormEvent) {
    e.preventDefault();
    setErrorMessage(null);
    setSuccessMessage(null);
    setSubmitting(true);

    try {
      const response = await forgotPassword({ email: email.trim().toLowerCase() });
      if (response.data) {
        setChallengeId(response.data as unknown as string);
        setResendCountdown(60);
      } else {
        setSuccessMessage(response.message || "Instruksi reset password telah dikirim.");
      }
    } catch (error) {
      if (error instanceof ApiError) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("Terjadi kesalahan saat meminta reset password.");
      }
    } finally {
      setSubmitting(false);
    }
  }

  async function handleVerifyOtp(e: FormEvent) {
    e.preventDefault();
    if (!challengeId) return;

    setErrorMessage(null);
    setSubmitting(true);

    try {
      const response = await verifyForgotPassword({ challengeId, code: otpCode });
      navigate(`/auth/reset-password?token=${response.data.resetToken}`);
    } catch (error) {
      if (error instanceof ApiError) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("Kode OTP tidak valid atau kadaluarsa.");
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
      const response = await import("../../services/authService").then(m => m.resendOtp({ challengeId }));
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
      <section className="login-form-panel-centered">
        {!challengeId ? (
          <form className="login-card login-card-centered" onSubmit={handleRequestReset}>
            <div className="login-heading">
              <p className="eyebrow">PEMULIHAN AKUN</p>
              <h2>Lupa Password</h2>
              <p>Masukkan email terdaftar Anda untuk menerima instruksi reset password.</p>
            </div>

            {errorMessage && <div className="alert alert-error" role="alert">{errorMessage}</div>}
            {successMessage && <div className="alert alert-success" role="alert">{successMessage}</div>}

            <label className="form-field">
              <span>Email</span>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </label>

            <button className="primary-button" type="submit" disabled={submitting}>
              {submitting ? "Mengirim..." : "Kirim Instruksi"}
            </button>
            
            <p className="login-footer">
              <Link to="/login" className="login-public-link">&larr; Kembali ke halaman login</Link>
            </p>
          </form>
        ) : (
          <form className="login-card login-card-centered" onSubmit={handleVerifyOtp}>
            <div className="login-heading">
              <p className="eyebrow">VERIFIKASI KEAMANAN</p>
              <h2>Masukkan Kode OTP</h2>
              <p>Kode OTP reset password telah dikirim ke email <strong>{email}</strong>.</p>
            </div>

            {errorMessage && <div className="alert alert-error" role="alert">{errorMessage}</div>}

            <label className="form-field">
              <span>Kode OTP</span>
              <input
                type="text"
                value={otpCode}
                onChange={(e) => setOtpCode(e.target.value)}
                placeholder="Misal: 123456"
                required
              />
            </label>

            <button className="primary-button" type="submit" disabled={submitting || otpCode.length < 6}>
              {submitting ? "Memverifikasi..." : "Lanjutkan"}
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
              <button type="button" onClick={() => setChallengeId(null)} className="back-button-link">
                &larr; Gunakan email lain
              </button>
            </p>
          </form>
        )}
      </section>
    </main>
  );
}
