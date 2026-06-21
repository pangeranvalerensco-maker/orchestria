export const PublicContentType = {
  ORGANIZATION_PROFILE: 'ORGANIZATION_PROFILE',
  PROGRAM: 'PROGRAM',
  FACILITY: 'FACILITY',
  TESTIMONIAL: 'TESTIMONIAL',
  ACTIVITY_PUBLICATION: 'ACTIVITY_PUBLICATION',
  HERO: 'HERO'
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
  type: PublicContentType;
  title: string;
  content: string;
  mediaUrl?: string;
  displayOrder: number;
  status: PublicationStatus;
  publishedAt?: string; // ISO-8601 UTC format
  eventDate?: string; // ISO-8601 UTC format
  createdAt: string; // ISO-8601 UTC format
  updatedAt: string; // ISO-8601 UTC format
}

export interface PublicContentRequest {
  type: PublicContentType;
  title: string;
  content: string;
  mediaUrl?: string;
  displayOrder: number;
  eventDate?: string; // e.g. "YYYY-MM-DD"
}
