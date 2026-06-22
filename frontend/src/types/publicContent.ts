export const PublicContentType = {
  HERO: 'HERO',
  ABOUT: 'ABOUT',
  VISION: 'VISION',
  MISSION: 'MISSION',
  PROGRAM: 'PROGRAM',
  FACILITY: 'FACILITY',
  TESTIMONIAL: 'TESTIMONIAL',
  ACTIVITY: 'ACTIVITY',
  MEDIA: 'MEDIA'
} as const;

export type PublicContentType = typeof PublicContentType[keyof typeof PublicContentType];

export const PublicationStatus = {
  DRAFT: 'DRAFT',
  PUBLISHED: 'PUBLISHED',
  ARCHIVED: 'ARCHIVED'
} as const;

export type PublicationStatus = typeof PublicationStatus[keyof typeof PublicationStatus];

export interface PublicContentEntry {
  id: string;
  contentType: PublicContentType;
  title: string;
  subtitle?: string;
  body?: string;
  category?: string;
  statusLabel?: string;
  eventDate?: string;
  mediaUrl?: string;
  linkUrl?: string;
  authorName?: string;
  authorRole?: string;
  displayOrder: number;
  publicationStatus: PublicationStatus;
  active: boolean;
  publishedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface PublicContentRequest {
  contentType: PublicContentType;
  title: string;
  subtitle?: string;
  body?: string;
  category?: string;
  statusLabel?: string;
  eventDate?: string;
  mediaUrl?: string;
  linkUrl?: string;
  authorName?: string;
  authorRole?: string;
  displayOrder: number;
}
