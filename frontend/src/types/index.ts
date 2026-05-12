export interface ApiResponse<T> {
  data: T;
  message?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface Country {
  code: string;
  name: string;
  exchange: string;
  flag: string;
}

export interface Subscription {
  id: number;
  email: string;
  countryCodes: string[];
  calendarUrl: string;
}
