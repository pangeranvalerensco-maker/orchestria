import React, { useState, useEffect } from 'react';
import { useAuth } from '../auth/useAuth';
import type { PublicContentEntry, PublicContentRequest } from '../types/publicContent';
import { PublicContentType, PublicationStatus } from '../types/publicContent';
import { publicContentService } from '../services/publicContentService';
import '../public-content-admin.css';

const CONTENT_TYPES = [
  { value: PublicContentType.ORGANIZATION_PROFILE, label: 'Profile' },
  { value: PublicContentType.PROGRAM, label: 'Program' },
  { value: PublicContentType.FACILITY, label: 'Facility' },
  { value: PublicContentType.TESTIMONIAL, label: 'Testimonial' },
  { value: PublicContentType.ACTIVITY_PUBLICATION, label: 'Activity Publication' },
  { value: PublicContentType.HERO, label: 'Hero Content' }
];

export const PublicContentManagementPage: React.FC = () => {
  const { token } = useAuth();
  const [contents, setContents] = useState<PublicContentEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingContent, setEditingContent] = useState<PublicContentEntry | null>(null);
  
  const [formData, setFormData] = useState<PublicContentRequest>({
    type: PublicContentType.ACTIVITY_PUBLICATION,
    title: '',
    content: '',
    mediaUrl: '',
    displayOrder: 0,
    eventDate: ''
  });

  const [filterType, setFilterType] = useState<string>('ALL');

  useEffect(() => {
    loadContents();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  const loadContents = async () => {
    if (!token) return;
    try {
      setLoading(true);
      setError(null);
      const data = await publicContentService.getAllContentsAdmin(token);
      setContents(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load public contents');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenModal = (content?: PublicContentEntry) => {
    if (content) {
      setEditingContent(content);
      setFormData({
        type: content.type,
        title: content.title,
        content: content.content,
        mediaUrl: content.mediaUrl || '',
        displayOrder: content.displayOrder,
        eventDate: content.eventDate || ''
      });
    } else {
      setEditingContent(null);
      setFormData({
        type: PublicContentType.ACTIVITY_PUBLICATION,
        title: '',
        content: '',
        mediaUrl: '',
        displayOrder: 0,
        eventDate: ''
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
      if (!payload.mediaUrl) delete payload.mediaUrl;

      if (editingContent) {
        await publicContentService.updateContent(editingContent.id, payload, token);
      } else {
        await publicContentService.createContent(payload, token);
      }
      handleCloseModal();
      loadContents();
    } catch (err: any) {
      setError(err.message || 'Failed to save content');
    }
  };

  const handlePublish = async (id: string) => {
    if (!token) return;
    try {
      await publicContentService.publishContent(id, token);
      loadContents();
    } catch (err: any) {
      setError(err.message || 'Failed to publish content');
    }
  };

  const handleArchive = async (id: string) => {
    if (!token) return;
    try {
      await publicContentService.archiveContent(id, token);
      loadContents();
    } catch (err: any) {
      setError(err.message || 'Failed to archive content');
    }
  };

  const handleRestore = async (id: string) => {
    if (!token) return;
    try {
      await publicContentService.restoreContent(id, token);
      loadContents();
    } catch (err: any) {
      setError(err.message || 'Failed to restore content');
    }
  };

  const handleDelete = async (id: string) => {
    if (!token) return;
    if (!window.confirm('Are you sure you want to delete this content?')) return;
    try {
      await publicContentService.deleteContent(id, token);
      loadContents();
    } catch (err: any) {
      setError(err.message || 'Failed to delete content');
    }
  };

  const filteredContents = filterType === 'ALL' 
    ? contents 
    : contents.filter(c => c.type === filterType);

  if (loading) return <div className="public-content-loading">Loading public contents...</div>;

  return (
    <div className="public-content-page">
      <div className="public-content-header">
        <h1>Public Content Management</h1>
        <button className="public-content-btn-primary" onClick={() => handleOpenModal()}>
          + Create Content
        </button>
      </div>

      {error && <div className="public-content-error">{error}</div>}

      <div className="public-content-filters">
        <div className="public-content-filter-group">
          <label>Filter by Type:</label>
          <select value={filterType} onChange={e => setFilterType(e.target.value)}>
            <option value="ALL">All Types</option>
            {CONTENT_TYPES.map(type => (
              <option key={type.value} value={type.value}>{type.label}</option>
            ))}
          </select>
        </div>
      </div>

      {filteredContents.length === 0 ? (
        <div className="public-content-empty">No contents found.</div>
      ) : (
        <div className="public-content-grid">
          {filteredContents.map(content => (
            <div key={content.id} className="public-content-card">
              {content.mediaUrl && (
                <img src={content.mediaUrl} alt={content.title} className="public-content-image" />
              )}
              <div className="public-content-card-header">
                <h3>{content.title}</h3>
                <span className={`public-content-status public-content-status-${content.status.toLowerCase()}`}>
                  {content.status}
                </span>
              </div>
              <div className="public-content-type">{content.type.replace('_', ' ')}</div>
              <p className="public-content-desc">{content.content}</p>
              
              <div className="public-content-meta">
                <span>Order: {content.displayOrder}</span>
                {content.eventDate && <span>Date: {content.eventDate}</span>}
              </div>

              <div className="public-content-actions">
                <button 
                  className="public-content-action-btn"
                  onClick={() => handleOpenModal(content)}
                >
                  Edit
                </button>
                
                {content.status === PublicationStatus.DRAFT && (
                  <button 
                    className="public-content-action-btn success"
                    onClick={() => handlePublish(content.id)}
                  >
                    Publish
                  </button>
                )}
                
                {content.status === PublicationStatus.PUBLISHED && (
                  <button 
                    className="public-content-action-btn danger"
                    onClick={() => handleArchive(content.id)}
                  >
                    Archive
                  </button>
                )}

                {content.status === PublicationStatus.ARCHIVED && (
                  <button 
                    className="public-content-action-btn success"
                    onClick={() => handleRestore(content.id)}
                  >
                    Restore
                  </button>
                )}

                {(content.status === PublicationStatus.DRAFT || content.status === PublicationStatus.ARCHIVED) && (
                  <button 
                    className="public-content-action-btn danger"
                    onClick={() => handleDelete(content.id)}
                  >
                    Delete
                  </button>
                )}
              </div>
            </div>
          ))}
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
                  name="type" 
                  value={formData.type} 
                  onChange={handleChange}
                  required
                >
                  {CONTENT_TYPES.map(type => (
                    <option key={type.value} value={type.value}>{type.label}</option>
                  ))}
                </select>
              </div>

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

              <div className="public-content-form-group">
                <label>Content</label>
                <textarea 
                  name="content" 
                  value={formData.content} 
                  onChange={handleChange} 
                  rows={4}
                  required 
                />
              </div>

              <div className="public-content-form-group">
                <label>Media URL (Optional)</label>
                <input 
                  type="text" 
                  name="mediaUrl" 
                  value={formData.mediaUrl} 
                  onChange={handleChange} 
                  placeholder="https://..."
                />
              </div>

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

              <div className="public-content-form-group">
                <label>Event Date (Optional)</label>
                <input 
                  type="date" 
                  name="eventDate" 
                  value={formData.eventDate} 
                  onChange={handleChange} 
                />
              </div>

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
    </div>
  );
};
