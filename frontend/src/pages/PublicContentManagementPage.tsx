import React, { useState, useEffect } from 'react';
import { useAuth } from '../auth/useAuth';
import type { PublicContentEntry, PublicContentRequest } from '../types/publicContent';
import { PublicContentType, PublicationStatus } from '../types/publicContent';
import { publicContentService } from '../services/publicContentService';
import '../public-content-admin.css';

const CONTENT_TYPES = [
  { value: PublicContentType.HERO, label: 'Hero Content', reqPerm: 'public.organization.manage' },
  { value: PublicContentType.ABOUT, label: 'About', reqPerm: 'public.organization.manage' },
  { value: PublicContentType.VISION, label: 'Vision', reqPerm: 'public.organization.manage' },
  { value: PublicContentType.MISSION, label: 'Mission', reqPerm: 'public.organization.manage' },
  { value: PublicContentType.PROGRAM, label: 'Program', reqPerm: 'public.content.manage' },
  { value: PublicContentType.FACILITY, label: 'Facility', reqPerm: 'public.content.manage' },
  { value: PublicContentType.TESTIMONIAL, label: 'Testimonial', reqPerm: 'public.content.manage' },
  { value: PublicContentType.ACTIVITY, label: 'Activity', reqPerm: 'public.activity.manage' },
  { value: PublicContentType.MEDIA, label: 'Media', reqPerm: 'public.media.manage' }
];

export const PublicContentManagementPage: React.FC = () => {
  const { token, hasPermission } = useAuth();
  const [contents, setContents] = useState<PublicContentEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingContent, setEditingContent] = useState<PublicContentEntry | null>(null);
  const [contentToDelete, setContentToDelete] = useState<PublicContentEntry | null>(null);
  
  const [formData, setFormData] = useState<PublicContentRequest>({
    contentType: PublicContentType.ACTIVITY,
    title: '',
    subtitle: '',
    body: '',
    category: '',
    statusLabel: '',
    eventDate: '',
    mediaUrl: '',
    linkUrl: '',
    authorName: '',
    authorRole: '',
    displayOrder: 0
  });

  const [filterType, setFilterType] = useState<string>('ALL');
  const [filterStatus, setFilterStatus] = useState<string>('ALL');

  const availableTypes = CONTENT_TYPES.filter(t => hasPermission(t.reqPerm));

  useEffect(() => {
    loadContents();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token, filterType, filterStatus]);

  const loadContents = async () => {
    if (!token) return;
    try {
      setLoading(true);
      setError(null);
      const typeParam = filterType !== 'ALL' ? (filterType as PublicContentType) : undefined;
      const statusParam = filterStatus !== 'ALL' ? (filterStatus as PublicationStatus) : undefined;
      const data = await publicContentService.getAllContents(token, typeParam, statusParam);
      setContents(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load public contents');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenModal = (content?: PublicContentEntry) => {
    setError(null);
    if (content) {
      setEditingContent(content);
      setFormData({
        contentType: content.contentType,
        title: content.title,
        subtitle: content.subtitle || '',
        body: content.body || '',
        category: content.category || '',
        statusLabel: content.statusLabel || '',
        eventDate: content.eventDate || '',
        mediaUrl: content.mediaUrl || '',
        linkUrl: content.linkUrl || '',
        authorName: content.authorName || '',
        authorRole: content.authorRole || '',
        displayOrder: content.displayOrder
      });
    } else {
      setEditingContent(null);
      setFormData({
        contentType: availableTypes.length > 0 ? availableTypes[0].value : PublicContentType.ACTIVITY,
        title: '',
        subtitle: '',
        body: '',
        category: '',
        statusLabel: '',
        eventDate: '',
        mediaUrl: '',
        linkUrl: '',
        authorName: '',
        authorRole: '',
        displayOrder: 0
      });
    }
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingContent(null);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'displayOrder' ? parseInt(value) || 0 : value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token) return;

    try {
      const payload = { ...formData };
      if (!payload.eventDate) delete payload.eventDate;

      if (editingContent) {
        await publicContentService.updateContent(editingContent.id, payload, token);
      } else {
        await publicContentService.createContent(payload, token);
      }
      handleCloseModal();
      loadContents();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save content');
    }
  };

  const handlePublish = async (id: string) => {
    if (!token) return;
    try {
      await publicContentService.publishContent(id, token);
      loadContents();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to publish content');
    }
  };

  const handleArchive = async (id: string) => {
    if (!token) return;
    try {
      await publicContentService.archiveContent(id, token);
      loadContents();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to archive content');
    }
  };

  const handleRestore = async (id: string) => {
    if (!token) return;
    try {
      await publicContentService.restoreDraftContent(id, token);
      loadContents();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to restore content');
    }
  };

  const handleDelete = (content: PublicContentEntry) => {
    setContentToDelete(content);
  };

  const confirmDelete = async () => {
    if (!token || !contentToDelete) return;
    try {
      await publicContentService.deleteContent(contentToDelete.id, token);
      setContentToDelete(null);
      loadContents();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete content');
      setContentToDelete(null);
    }
  };

  const canManageType = (type: PublicContentType) => {
    const matchedType = CONTENT_TYPES.find(t => t.value === type);
    return matchedType ? hasPermission(matchedType.reqPerm) : false;
  };

  const showBodyField = ['ABOUT', 'VISION', 'MISSION', 'PROGRAM', 'FACILITY', 'TESTIMONIAL', 'ACTIVITY'].includes(formData.contentType);
  const showTitleField = ['HERO', 'ABOUT', 'VISION', 'MISSION', 'PROGRAM', 'FACILITY', 'ACTIVITY', 'MEDIA'].includes(formData.contentType);
  const showSubtitleField = ['HERO', 'PROGRAM', 'ACTIVITY'].includes(formData.contentType);
  const showCategoryField = ['ACTIVITY'].includes(formData.contentType);
  const showStatusLabelField = ['ACTIVITY'].includes(formData.contentType);
  const showEventDateField = ['ACTIVITY'].includes(formData.contentType);
  const showMediaUrlField = ['HERO', 'ABOUT', 'PROGRAM', 'FACILITY', 'ACTIVITY', 'MEDIA'].includes(formData.contentType);
  const showLinkUrlField = ['HERO', 'PROGRAM', 'ACTIVITY', 'MEDIA'].includes(formData.contentType);
  const showAuthorField = ['TESTIMONIAL'].includes(formData.contentType);

  if (loading && contents.length === 0) return <div className="public-content-loading">Loading public contents...</div>;

  return (
    <div className="public-content-page">
      <div className="public-content-header">
        <h1>Public Content Management</h1>
        {availableTypes.length > 0 && (
          <button className="public-content-btn-primary" onClick={() => handleOpenModal()}>
            + Create Content
          </button>
        )}
      </div>

      {error && <div className="public-content-error">{error}</div>}

      <div className="public-content-filters">
        <div className="public-content-filter-group">
          <label>Type:</label>
          <select value={filterType} onChange={e => setFilterType(e.target.value)}>
            <option value="ALL">All Types</option>
            {CONTENT_TYPES.map(type => (
              <option key={type.value} value={type.value}>{type.label}</option>
            ))}
          </select>
        </div>
        <div className="public-content-filter-group">
          <label>Status:</label>
          <select value={filterStatus} onChange={e => setFilterStatus(e.target.value)}>
            <option value="ALL">All Status</option>
            <option value="DRAFT">DRAFT</option>
            <option value="PUBLISHED">PUBLISHED</option>
            <option value="ARCHIVED">ARCHIVED</option>
          </select>
        </div>
      </div>

      {contents.length === 0 ? (
        <div className="public-content-empty">No contents found.</div>
      ) : (
        <div className="public-content-grid">
          {contents.map(content => {
            const isAuthorized = canManageType(content.contentType);

            return (
              <div key={content.id} className="public-content-card">
                {content.mediaUrl && (
                  <img src={content.mediaUrl} alt={content.title} className="public-content-image" />
                )}
                <div className="public-content-card-header">
                  <h3>{content.title || content.authorName || 'No Title'}</h3>
                  <span className={`public-content-status public-content-status-${content.publicationStatus.toLowerCase()}`}>
                    {content.publicationStatus}
                  </span>
                </div>
                <div className="public-content-type">{content.contentType.replace('_', ' ')}</div>
                
                <p className="public-content-desc">
                  {content.body ? (content.body.length > 100 ? content.body.substring(0, 100) + '...' : content.body) : 'No content'}
                </p>
                
                <div className="public-content-meta">
                  <span>Order: {content.displayOrder}</span>
                  {content.eventDate && <span>Date: {content.eventDate}</span>}
                </div>

                {isAuthorized && (
                  <div className="public-content-actions">
                    {content.publicationStatus === PublicationStatus.DRAFT && (
                      <button 
                        className="public-content-action-btn"
                        onClick={() => handleOpenModal(content)}
                      >
                        Edit
                      </button>
                    )}
                    
                    {content.publicationStatus === PublicationStatus.DRAFT && (
                      <button 
                        className="public-content-action-btn success"
                        onClick={() => handlePublish(content.id)}
                      >
                        Publish
                      </button>
                    )}
                    
                    {content.publicationStatus === PublicationStatus.PUBLISHED && (
                      <button 
                        className="public-content-action-btn danger"
                        onClick={() => handleArchive(content.id)}
                      >
                        Archive
                      </button>
                    )}

                    {content.publicationStatus === PublicationStatus.ARCHIVED && (
                      <button 
                        className="public-content-action-btn success"
                        onClick={() => handleRestore(content.id)}
                      >
                        Restore
                      </button>
                    )}

                    {(content.publicationStatus === PublicationStatus.DRAFT || content.publicationStatus === PublicationStatus.ARCHIVED) && (
                      <button 
                        className="public-content-action-btn danger"
                        onClick={() => handleDelete(content)}
                      >
                        Delete
                      </button>
                    )}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}

      {isModalOpen && (
        <div className="public-content-modal-overlay">
          <div className="public-content-modal-content">
            <div className="public-content-modal-header">
              <h2>{editingContent ? 'Edit Content' : 'Create Content'}</h2>
              <button className="public-content-close-btn" onClick={handleCloseModal}>&times;</button>
            </div>
            
            <form onSubmit={handleSubmit} className="public-content-form">
              <div className="public-content-form-group">
                <label>Content Type</label>
                <select 
                  name="contentType" 
                  value={formData.contentType} 
                  onChange={handleChange}
                  disabled={!!editingContent}
                  required
                >
                  {availableTypes.map(type => (
                    <option key={type.value} value={type.value}>{type.label}</option>
                  ))}
                </select>
              </div>

              {showTitleField && (
                <div className="public-content-form-group">
                  <label>Title</label>
                  <input 
                    type="text" 
                    name="title" 
                    value={formData.title} 
                    onChange={handleChange} 
                    required 
                  />
                </div>
              )}

              {showSubtitleField && (
                <div className="public-content-form-group">
                  <label>Subtitle (Optional)</label>
                  <input 
                    type="text" 
                    name="subtitle" 
                    value={formData.subtitle} 
                    onChange={handleChange} 
                  />
                </div>
              )}

              {showBodyField && (
                <div className="public-content-form-group">
                  <label>Body Content</label>
                  <textarea 
                    name="body" 
                    value={formData.body} 
                    onChange={handleChange} 
                    rows={4}
                    required 
                  />
                </div>
              )}

              {showCategoryField && (
                <div className="public-content-form-group">
                  <label>Category</label>
                  <input 
                    type="text" 
                    name="category" 
                    value={formData.category} 
                    onChange={handleChange} 
                    placeholder="e.g. SEMINAR, WORKSHOP"
                    required
                  />
                </div>
              )}

              {showStatusLabelField && (
                <div className="public-content-form-group">
                  <label>Status Label (Optional)</label>
                  <input 
                    type="text" 
                    name="statusLabel" 
                    value={formData.statusLabel} 
                    onChange={handleChange} 
                    placeholder="e.g. REGISTRATION OPEN"
                  />
                </div>
              )}

              {showMediaUrlField && (
                <div className="public-content-form-group">
                  <label>Media URL {formData.contentType === 'MEDIA' ? '' : '(Optional)'}</label>
                  <input 
                    type="text" 
                    name="mediaUrl" 
                    value={formData.mediaUrl} 
                    onChange={handleChange} 
                    placeholder="https://..."
                    required={formData.contentType === 'MEDIA'}
                  />
                </div>
              )}

              {showLinkUrlField && (
                <div className="public-content-form-group">
                  <label>Action Link URL (Optional)</label>
                  <input 
                    type="text" 
                    name="linkUrl" 
                    value={formData.linkUrl} 
                    onChange={handleChange} 
                    placeholder="https://..."
                  />
                </div>
              )}

              {showAuthorField && (
                <div className="public-content-form-group">
                  <label>Author Name</label>
                  <input 
                    type="text" 
                    name="authorName" 
                    value={formData.authorName} 
                    onChange={handleChange} 
                    required 
                  />
                </div>
              )}

              {showAuthorField && (
                <div className="public-content-form-group">
                  <label>Author Role (Optional)</label>
                  <input 
                    type="text" 
                    name="authorRole" 
                    value={formData.authorRole} 
                    onChange={handleChange} 
                  />
                </div>
              )}

              <div className="public-content-form-group">
                <label>Display Order</label>
                <input 
                  type="number" 
                  name="displayOrder" 
                  value={formData.displayOrder} 
                  onChange={handleChange} 
                  min={0}
                  required 
                />
              </div>

              {showEventDateField && (
                <div className="public-content-form-group">
                  <label>Event Date</label>
                  <input 
                    type="date" 
                    name="eventDate" 
                    value={formData.eventDate} 
                    onChange={handleChange} 
                    required
                  />
                </div>
              )}

              <div className="public-content-modal-actions">
                <button type="button" className="public-content-btn-secondary" onClick={handleCloseModal}>
                  Cancel
                </button>
                <button type="submit" className="public-content-btn-primary">
                  Save
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {contentToDelete && (
        <div className="public-content-modal-overlay">
          <div className="public-content-modal-content public-content-modal-sm">
            <div className="public-content-modal-header">
              <h2>Confirm Delete</h2>
            </div>
            <p className="public-content-confirm-text">Are you sure you want to delete {contentToDelete.title || 'this content'}?</p>
            <div className="public-content-modal-actions">
              <button className="public-content-btn-secondary" onClick={() => setContentToDelete(null)}>Cancel</button>
              <button className="public-content-btn-primary danger" onClick={confirmDelete}>Delete</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
