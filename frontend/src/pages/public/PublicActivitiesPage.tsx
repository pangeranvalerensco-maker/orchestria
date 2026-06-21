import { useState, useMemo, useEffect } from "react";
import { staticActivities, activityCategories } from "../../data/publicContent";
import { publicContentService } from "../../services/publicContentService";
import type { PublicContentEntry } from '../../types/publicContent';
import { PublicContentType } from '../../types/publicContent';

export function PublicActivitiesPage() {
  const [selectedCategory, setSelectedCategory] = useState<string>("ALL");
  const [contents, setContents] = useState<PublicContentEntry[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    publicContentService.getPublishedByType(PublicContentType.ACTIVITY_PUBLICATION)
      .then(res => setContents(res))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const filteredActivities = useMemo(() => {
    // Note: Since ACTIVITY_PUBLICATION doesn't have a specific category field in the backend model, 
    // we'll just show all of them if "ALL" is selected, and fallback to static data.
    if (contents.length > 0) {
      // Ignore static categories for dynamic data since we don't have categories in PublicContentEntry
      return contents.sort((a,b) => a.displayOrder - b.displayOrder);
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

        {contents.length === 0 && (
          <div className="public-filters public-filter-spacing">
            <div className="public-category-tabs">
              <button 
                type="button"
                className={`public-tab ${selectedCategory === "ALL" ? "active" : ""}`}
                onClick={() => setSelectedCategory("ALL")}
              >
                Semua Kategori
              </button>
              {activityCategories.map(cat => (
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
              contents.map(act => (
                <div key={act.id} className="public-activity-card">
                  {act.mediaUrl && <img src={act.mediaUrl} alt={act.title} style={{ width: '100%', height: '200px', objectFit: 'cover', borderTopLeftRadius: '8px', borderTopRightRadius: '8px' }} />}
                  <div style={{ padding: '16px' }}>
                    <div className="activity-card-header">
                      <span className="public-badge">Kegiatan</span>
                    </div>
                    <h3 style={{ margin: '8px 0' }}>{act.title}</h3>
                    {act.eventDate && <p className="activity-date">📅 {act.eventDate}</p>}
                    <p className="activity-desc">{act.content}</p>
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
                  <h3>{act.title}</h3>
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
