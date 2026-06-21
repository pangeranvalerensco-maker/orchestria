interface AdminTabsProps {
  activeTab: string;
  onTabChange: (tab: string) => void;
}

export const ADMIN_TABS = {
  MEMBERS: "Anggota",
  DIVISIONS: "Divisi",
  POSITIONS: "Jabatan",
  PERIODS: "Periode",
  ASSIGNMENTS: "Penempatan",
};

export function AdminTabs({ activeTab, onTabChange }: AdminTabsProps) {
  const tabs = Object.values(ADMIN_TABS);

  return (
    <div className="org-admin-tabs">
      {tabs.map((tab) => (
        <button
          key={tab}
          type="button"
          className={`org-admin-tab ${activeTab === tab ? "active" : ""}`}
          onClick={() => onTabChange(tab)}
        >
          {tab}
        </button>
      ))}
    </div>
  );
}
