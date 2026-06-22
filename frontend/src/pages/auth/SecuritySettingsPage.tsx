import { useEffect, useState, type FormEvent } from "react";
import { useAuth } from "../../auth/useAuth";
import { ApiError } from "../../api/http";
import {
  getSecuritySettings,
  requestEnableTwoFactor,
  confirmEnableTwoFactor,
  requestDisableTwoFactor,
  confirmDisableTwoFactor,
} from "../../services/authService";
import type { SecuritySettings } from "../../types/auth";

export function SecuritySettingsPage() {
  const { token, user } = useAuth();

  const [settings, setSettings] = useState<SecuritySettings | null>(null);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const [otpMode, setOtpMode] = useState<"NONE" | "ENABLE" | "DISABLE">("NONE");
  const [challengeId, setChallengeId] = useState<string | null>(null);
  const [otpCode, setOtpCode] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (token) {
      loadSettings();
    }
  }, [token]);

  async function loadSettings() {
    setLoading(true);
    try {
      const response = await getSecuritySettings(token!);
      setSettings(response.data);
    } catch (error) {
      if (error instanceof ApiError) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("Gagal memuat pengaturan keamanan.");
      }
    } finally {
      setLoading(false);
    }
  }

  async function handleRequestEnable() {
    setErrorMessage(null);
    setSuccessMessage(null);
    try {
      const response = await requestEnableTwoFactor(token!);
      setChallengeId(response.data);
      setOtpMode("ENABLE");
    } catch (error) {
      if (error instanceof ApiError) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("Gagal meminta aktivasi 2FA.");
      }
    }
  }

  async function handleRequestDisable() {
    setErrorMessage(null);
    setSuccessMessage(null);
    try {
      const response = await requestDisableTwoFactor(token!);
      setChallengeId(response.data);
      setOtpMode("DISABLE");
    } catch (error) {
      if (error instanceof ApiError) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("Gagal meminta penonaktifan 2FA.");
      }
    }
  }

  async function handleConfirmOtp(e: FormEvent) {
    e.preventDefault();
    setErrorMessage(null);
    setSubmitting(true);

    try {
      if (otpMode === "ENABLE") {
        await confirmEnableTwoFactor(token!, { challengeId: challengeId!, code: otpCode });
        setSuccessMessage("Two-Factor Authentication (2FA) berhasil diaktifkan.");
      } else if (otpMode === "DISABLE") {
        await confirmDisableTwoFactor(token!, { challengeId: challengeId!, code: otpCode });
        setSuccessMessage("Two-Factor Authentication (2FA) berhasil dinonaktifkan.");
      }
      setOtpMode("NONE");
      setChallengeId(null);
      setOtpCode("");
      await loadSettings();
    } catch (error) {
      if (error instanceof ApiError) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("Gagal memverifikasi OTP.");
      }
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return <main className="dashboard-content"><p>Memuat pengaturan...</p></main>;
  }

  return (
    <main className="dashboard-content">
      <header className="page-header">
        <div>
          <h1 className="page-title">Pengaturan Keamanan</h1>
          <p className="page-subtitle">Kelola keamanan akun, otentikasi dua faktor, dan perangkat terpercaya.</p>
        </div>
      </header>

      {errorMessage && <div className="alert alert-error mb-1-5" role="alert">{errorMessage}</div>}
      {successMessage && <div className="alert alert-success mb-1-5" role="alert">{successMessage}</div>}

      <div className="grid-gap-2-max-800">
        <section className="dashboard-card">
          <div className="card-header">
            <h3>Two-Factor Authentication (2FA)</h3>
          </div>
          <div className="card-body">
            <div className="flex-space-between-center-mb-1">
              <div>
                <p className="font-500-text-900">
                  Status: {settings?.twoFactorEnabled ? (
                    <span className="text-success-600">Aktif</span>
                  ) : (
                    <span className="text-error-600">Tidak Aktif</span>
                  )}
                </p>
                <p className="text-sm-text-500-mt-0-25">
                  Amankan akun Anda dengan mewajibkan kode verifikasi (OTP) saat login.
                </p>
                {settings?.mandatoryByRole && (
                  <p className="warning-box">
                    <strong>Perhatian:</strong> Peran Anda mewajibkan penggunaan 2FA. Harap segera aktifkan fitur ini.
                  </p>
                )}
              </div>
              
              {otpMode === "NONE" && (
                <button 
                  className={settings?.twoFactorEnabled ? "secondary-button" : "primary-button"}
                  onClick={settings?.twoFactorEnabled ? handleRequestDisable : handleRequestEnable}
                >
                  {settings?.twoFactorEnabled ? "Nonaktifkan 2FA" : "Aktifkan 2FA"}
                </button>
              )}
            </div>

            {otpMode !== "NONE" && (
              <form onSubmit={handleConfirmOtp} className="otp-form-box">
                <h4 className="mb-0-5">Verifikasi Kode OTP</h4>
                <p className="text-sm-mb-1">
                  Kami telah mengirimkan kode verifikasi ke <strong>{user?.email}</strong>.
                </p>
                <label className="form-field">
                  <span>Kode OTP</span>
                  <div className="flex-gap-1">
                    <input 
                      type="text" 
                      value={otpCode}
                      onChange={(e) => setOtpCode(e.target.value)}
                      placeholder="Masukkan 6 digit kode"
                      required
                    />
                    <button type="submit" className="primary-button" disabled={submitting || otpCode.length < 6}>
                      Konfirmasi
                    </button>
                    <button type="button" className="secondary-button" onClick={() => { setOtpMode("NONE"); setChallengeId(null); setOtpCode(""); }}>
                      Batal
                    </button>
                  </div>
                </label>
              </form>
            )}
          </div>
        </section>

        <section className="dashboard-card">
          <div className="card-header">
            <h3>Perangkat Terpercaya</h3>
          </div>
          <div className="card-body">
            <p className="mb-1">
              Anda memiliki <strong>{settings?.trustedDeviceCount || 0}</strong> perangkat yang dipercaya. Perangkat ini tidak memerlukan OTP saat login untuk jangka waktu tertentu.
            </p>
            <p className="text-sm-text-500">
              (Manajemen detail perangkat terpercaya akan hadir di pembaruan berikutnya.)
            </p>
          </div>
        </section>
      </div>
    </main>
  );
}
