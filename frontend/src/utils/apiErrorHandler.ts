import { ApiError } from "../api/http";

export function getErrorMessage(err: unknown, defaultMessage: string = "Layanan organisasi sedang bermasalah."): string {
  if (err instanceof ApiError) {
    switch (err.status) {
      case 401: return "Sesi tidak valid. Silakan login kembali.";
      case 403: return "Anda tidak memiliki izin untuk mengelola organisasi.";
      case 404: return "Data organisasi tidak ditemukan.";
      case 409: return "Data tidak dapat diubah karena sedang digunakan atau mengalami konflik.";
      case 400: return err.message; // gunakan pesan backend
      default: return err.message || defaultMessage;
    }
  }
  return defaultMessage;
}
