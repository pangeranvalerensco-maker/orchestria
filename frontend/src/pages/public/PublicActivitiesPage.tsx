import { useState, useMemo, useEffect } from "react";
import { staticActivities } from "../../data/publicContent";
import { publicContentService } from "../../services/publicContentService";
import type { PublicContentEntry } from '../../types/publicContent';
import { PublicContentType } from '../../types/publicContent';

export function PublicActivitiesPage() {
  const [selectedCategory, setSelectedCategory] = useState<string>("ALL");
  const [contents, setContents] = useState<PublicContentEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [categories, setCategories] = useState<string[]>([]);

  useEffect(() => {
    publicContentService.getPublished(PublicContentType.ACTIVITY)
      .then(res => {
        setContents(res);
        const uniqueCats = Array.from(new Set(res.map(c => c.category).filter(Boolean))) as string[];
        setCategories(uniqueCats.length > 0 ? uniqueCats : []);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const filteredActivities = useMemo(() => {
    if (contents.length > 0) {
      const activeContents = contents.sort((a,b) => a.displayOrder - b.displayOrder);
      if (selectedCategory === "ALL") return activeContents;
      return activeContents.filter(c => c.category === selectedCategory);
    }
    if (selectedCategory === "ALL") return staticActivities;
    return staticActivities.filter(act => act.category === selectedCategory);
  }, [selectedCategory, contents]);

  return (
    <div className="public-page">
      <div className="public-page-header">
        <div className="public-container">
          <h1>Kegiatan & Agenda</h1>
          <p>Potret aktivitas rutin dan program kerja Program Unggulan Bersama.</p>
        </div>
      </div>

      <div className="public-container public-content-wrapper">
        {contents.length === 0 && !loading && (
          <div className="public-alert-info public-demo-note-spacing">
            <strong>Catatan Demo:</strong> Data kegiatan berikut merupakan konten demo sementara.
          </div>
        )}

        {(contents.length > 0 || !loading) && (
          <div className="public-filters public-filter-spacing">
            <div className="public-category-tabs">
              <button 
                type="button"
                className={`public-tab ${selectedCategory === "ALL" ? "active" : ""}`}
                onClick={() => setSelectedCategory("ALL")}
              >
                Semua Kategori
              </button>
              {(contents.length > 0 ? categories : Array.from(new Set(staticActivities.map(a => a.category)))).map(cat => (
                <button 
                  type="button"
                  key={cat}
                  className={`public-tab ${selectedCategory === cat ? "active" : ""}`}
                  onClick={() => setSelectedCategory(cat)}
                >
                  {cat}
                </button>
              ))}
            </div>
          </div>
        )}

        {loading ? (
          <div className="public-loading">Memuat kegiatan...</div>
        ) : filteredActivities.length === 0 ? (
          <div className="public-empty">
            <p>Belum ada kegiatan untuk kategori ini.</p>
          </div>
        ) : (
          <div className="public-grid-3">
            {contents.length > 0 ? (
              (filteredActivities as PublicContentEntry[]).map(act => (
                <div key={act.id} className="public-activity-card">
                  {act.mediaUrl && <img src={act.mediaUrl} alt={act.title} className="public-activity-img" />}
                  <div className="public-activity-card-body">
                    <div className="activity-card-header">
                      <span className="public-badge">{act.category}</span>
                      {act.statusLabel && <span className="activity-status">{act.statusLabel}</span>}
                    </div>
                    <h3 className="public-activity-title">{act.title}</h3>
                    {act.eventDate && <p className="activity-date">📅 {act.eventDate}</p>}
                    <p className="activity-desc">{act.body}</p>
                  </div>
                </div>
              ))
            ) : (
              (filteredActivities as typeof staticActivities).map(act => (
                <div key={act.id} className="public-activity-card">
                  <div className="activity-card-header">
                    <span className="public-badge">{act.category}</span>
                    <span className="activity-status">{act.status}</span>
                  </div>
                  <h3 className="public-activity-title">{act.title}</h3>
                  <p className="activity-date">📅 {act.date}</p>
                  <p className="activity-desc">{act.description}</p>
                </div>
              ))
            )}
          </div>
        )}
      </div>
    </div>
  );
}
