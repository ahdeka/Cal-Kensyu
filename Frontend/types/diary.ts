export interface DiaryCreateRequest {
  diaryDate: string; // "2024-11-22"
  title: string;
  content: string;
  isPublic: boolean;
}

export interface DiaryUpdateRequest {
  diaryDate: string;
  title: string;
  content: string;
  isPublic: boolean;
}

export interface DiaryResponse {
  id: number;
  username: string;
  nickname: string;
  diaryDate: string;
  title: string;
  content: string;
  isPublic: boolean;
  createDate: string;
  updateDate: string;
}

export interface DiaryListResponse {
  id: number;
  nickname: string;
  diaryDate: string;
  title: string;
  contentPreview: string;
  isPublic: boolean;
  createDate: string;
}

export interface ApiResponse<T> {
  resultCode: string;
  msg: string;
  data?: T;
}