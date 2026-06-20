import { useState, useMemo } from "react";
import { staticActivities, activityCategories } from "../../data/publicContent";

export function PublicActivitiesPage() {
  const [selectedCategory, setSelectedCategory] = useState<string>("ALL");

  const filteredActivities = useMemo(() => {
    if (selectedCategory === "ALL") return staticActivities;
    return staticActivities.filter(act => act.category === selectedCategory);
  }, [selectedCategory]);

  return (
    <div className="public-page">
      <div className="public-page-header">
        <div className="public-container">
          <h1>Kegiatan & Agenda</h1>
          <p>Potret aktivitas rutin dan program kerja Program Unggulan Bersama.</p>
        </div>
      </div>

      <div className="public-container public-content-wrapper">
        <div className="public-alert-info" style={{ marginBottom: "24px" }}>
          <strong>Catatan Demo:</strong> Data kegiatan di bawah ini adalah konten *static seed* untuk demo portal publik Orchestria.
        </div>

        <div className="public-filters" style={{ marginBottom: "32px" }}>
          <div className="public-category-tabs">
            <button 
              className={`public-tab ${selectedCategory === "ALL" ? "active" : ""}`}
              onClick={() => setSelectedCategory("ALL")}
            >
              Semua Kategori
            </button>
            {activityCategories.map(cat => (
              <button 
                key={cat}
                className={`public-tab ${selectedCategory === cat ? "active" : ""}`}
                onClick={() => setSelectedCategory(cat)}
              >
                {cat}
              </button>
            ))}
          </div>
        </div>

        {filteredActivities.length === 0 ? (
          <div className="public-empty">
            <p>Belum ada kegiatan untuk kategori ini.</p>
          </div>
        ) : (
          <div className="public-grid-3">
            {filteredActivities.map(act => (
              <div key={act.id} className="public-activity-card">
                <div className="activity-card-header">
                  <span className="public-badge">{act.category}</span>
                  <span className="activity-status">{act.status}</span>
                </div>
                <h3>{act.title}</h3>
                <p className="activity-date">🗓 {act.date}</p>
                <p className="activity-desc">{act.description}</p>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
