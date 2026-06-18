import {
  useEffect,
  useMemo,
  useState,
  type FormEvent,
} from "react";

import {
  Link,
  useNavigate,
} from "react-router";

import { ApiError } from "../api/http";
import { useAuth } from "../auth/useAuth";

import {
  getCurrentMemberContext,
} from "../services/organizationService";

import {
  createFundRequest,
} from "../services/requestService";

import type {
  MemberAssignment,
} from "../types/organization";

import type {
  RequestPriority,
} from "../types/request";

export function CreateRequestPage() {
  const navigate = useNavigate();
  const { token } = useAuth();

  const [assignments, setAssignments] =
    useState<MemberAssignment[]>([]);

  const [divisionId, setDivisionId] =
    useState("");

  const [title, setTitle] =
    useState("");

  const [description, setDescription] =
    useState("");

  const [activityDate, setActivityDate] =
    useState("");

  const [priority, setPriority] =
    useState<RequestPriority>("MEDIUM");

  const [loadingContext, setLoadingContext] =
    useState(true);

  const [submitting, setSubmitting] =
    useState(false);

  const [errorMessage, setErrorMessage] =
    useState<string | null>(null);

  useEffect(() => {
    async function loadContext() {
      if (!token) {
        return;
      }

      setLoadingContext(true);
      setErrorMessage(null);

      try {
        const response =
          await getCurrentMemberContext(token);

        const activeAssignments =
          response.data.activeAssignments.filter(
            (assignment) => assignment.active,
          );

        setAssignments(activeAssignments);

        if (activeAssignments.length === 1) {
          setDivisionId(
            String(activeAssignments[0].divisionId),
          );
        }
      } catch (error) {
        if (error instanceof ApiError) {
          setErrorMessage(error.message);
        } else {
          setErrorMessage(
            "Tidak dapat mengambil data organisasi.",
          );
        }
      } finally {
        setLoadingContext(false);
      }
    }

    void loadContext();
  }, [token]);

  const uniqueDivisions = useMemo(() => {
    const divisionMap =
      new Map<number, MemberAssignment>();

    for (const assignment of assignments) {
      if (!divisionMap.has(assignment.divisionId)) {
        divisionMap.set(
          assignment.divisionId,
          assignment,
        );
      }
    }

    return Array.from(divisionMap.values());
  }, [assignments]);

  async function handleSubmit(
    event: FormEvent<HTMLFormElement>,
  ) {
    event.preventDefault();

    if (!token) {
      return;
    }

    if (!divisionId) {
      setErrorMessage(
        "Pilih divisi pengajuan terlebih dahulu.",
      );
      return;
    }

    setSubmitting(true);
    setErrorMessage(null);

    try {
      await createFundRequest(token, {
        divisionId: Number(divisionId),
        title: title.trim(),
        description:
          description.trim() || undefined,
        activityDate:
          activityDate || undefined,
        priority,
      });

      navigate("/requests", {
        replace: true,
      });
    } catch (error) {
      if (error instanceof ApiError) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage(
          "Pengajuan tidak dapat dibuat.",
        );
      }
    } finally {
      setSubmitting(false);
    }
  }

  if (loadingContext) {
    return (
      <main className="page-content">
        <div className="empty-state">
          <div className="spinner" />
          <p>Memuat data organisasi...</p>
        </div>
      </main>
    );
  }

  return (
    <main className="page-content">
      <section className="page-heading">
        <div>
          <p className="eyebrow">
            PENGAJUAN DANA
          </p>

          <h1>Buat Pengajuan</h1>

          <p>
            Buat draft pengajuan operasional
            berdasarkan divisi aktifmu.
          </p>
        </div>

        <Link
          className="secondary-link-button"
          to="/requests"
        >
          Kembali
        </Link>
      </section>

      {errorMessage && (
        <div
          className="alert alert-error"
          role="alert"
        >
          {errorMessage}
        </div>
      )}

      {!uniqueDivisions.length ? (
        <section className="content-card empty-state">
          <div className="empty-state-icon">!</div>

          <h2>Assignment organisasi belum tersedia</h2>

          <p>
            Akun login belum terhubung dengan
            assignment divisi aktif.
          </p>

          <small>
            Hubungkan akun auth dengan data anggota
            dan assignment organization-service.
          </small>
        </section>
      ) : (
        <form
          className="content-card request-form"
          onSubmit={handleSubmit}
        >
          <div className="form-section">
            <div>
              <p className="eyebrow">
                INFORMASI UTAMA
              </p>

              <h2>Data Pengajuan</h2>
            </div>

            <label className="form-field">
              <span>Divisi</span>

              <select
                value={divisionId}
                onChange={(event) =>
                  setDivisionId(event.target.value)
                }
                required
              >
                <option value="">
                  Pilih divisi
                </option>

                {uniqueDivisions.map(
                  (assignment) => (
                    <option
                      key={assignment.divisionId}
                      value={assignment.divisionId}
                    >
                      {assignment.divisionName}
                    </option>
                  ),
                )}
              </select>
            </label>

            <label className="form-field">
              <span>Judul Pengajuan</span>

              <input
                type="text"
                value={title}
                onChange={(event) =>
                  setTitle(event.target.value)
                }
                maxLength={150}
                placeholder="Contoh: Konsumsi Pelatihan Java"
                required
              />
            </label>

            <label className="form-field">
              <span>Deskripsi</span>

              <textarea
                value={description}
                onChange={(event) =>
                  setDescription(event.target.value)
                }
                rows={5}
                placeholder="Jelaskan tujuan dan kebutuhan pengajuan"
              />
            </label>

            <div className="form-grid">
              <label className="form-field">
                <span>Tanggal Kegiatan</span>

                <input
                  type="date"
                  value={activityDate}
                  onChange={(event) =>
                    setActivityDate(
                      event.target.value,
                    )
                  }
                />
              </label>

              <label className="form-field">
                <span>Prioritas</span>

                <select
                  value={priority}
                  onChange={(event) =>
                    setPriority(
                      event.target
                        .value as RequestPriority,
                    )
                  }
                >
                  <option value="LOW">
                    Rendah
                  </option>

                  <option value="MEDIUM">
                    Sedang
                  </option>

                  <option value="HIGH">
                    Tinggi
                  </option>

                  <option value="URGENT">
                    Mendesak
                  </option>
                </select>
              </label>
            </div>
          </div>

          <div className="form-actions">
            <Link
              className="secondary-link-button"
              to="/requests"
            >
              Batal
            </Link>

            <button
              className="primary-button form-submit-button"
              type="submit"
              disabled={submitting}
            >
              {submitting
                ? "Menyimpan..."
                : "Simpan Draft"}
            </button>
          </div>
        </form>
      )}
    </main>
  );
}