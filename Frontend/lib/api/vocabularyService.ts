import { api } from '../api';
import {
  VocabularyResponse,
  VocabularyListResponse,
  VocabularyCreateRequest,
  VocabularyUpdateRequest,
  StudyStatus,
} from '@/types/vocabulary';

interface ApiResponse<T> {
  resultCode: string;
  msg: string;
  data: T;
}

export const vocabularyService = {
  // 단어 등록
  createVocabulary: async (
    data: VocabularyCreateRequest
  ): Promise<VocabularyResponse> => {
    const response = await api.post<ApiResponse<VocabularyResponse>>(
      '/api/vocabularies',
      data
    );
    return response.data.data;
  },

  // 내 모든 단어 조회
  getMyVocabularies: async (): Promise<VocabularyListResponse[]> => {
    const response = await api.get<ApiResponse<VocabularyListResponse[]>>(
      '/api/vocabularies'
    );
    return response.data.data;
  },

  // 학습 상태별 단어 조회
  getVocabulariesByStatus: async (
    status: StudyStatus
  ): Promise<VocabularyListResponse[]> => {
    const response = await api.get<ApiResponse<VocabularyListResponse[]>>(
      `/api/vocabularies/status/${status}`
    );
    return response.data.data;
  },

  // 단어 검색
  searchVocabularies: async (
    keyword: string
  ): Promise<VocabularyListResponse[]> => {
    const response = await api.get<ApiResponse<VocabularyListResponse[]>>(
      '/api/vocabularies/search',
      {
        params: { keyword },
      }
    );
    return response.data.data;
  },

  // 단어 상세 조회
  getVocabulary: async (id: number): Promise<VocabularyResponse> => {
    const response = await api.get<ApiResponse<VocabularyResponse>>(
      `/api/vocabularies/${id}`
    );
    return response.data.data;
  },

  // 단어 수정
  updateVocabulary: async (
    id: number,
    data: VocabularyUpdateRequest
  ): Promise<VocabularyResponse> => {
    const response = await api.put<ApiResponse<VocabularyResponse>>(
      `/api/vocabularies/${id}`,
      data
    );
    return response.data.data;
  },

  // 단어 삭제
  deleteVocabulary: async (id: number): Promise<void> => {
    await api.delete(`/api/vocabularies/${id}`);
  },

  // 학습 상태만 변경
    updateStudyStatus: async (
    id: number,
    status: StudyStatus
    ): Promise<VocabularyResponse> => {
    const response = await api.patch<ApiResponse<VocabularyResponse>>(
        `/api/vocabularies/${id}/status`,
        null,
        {
        params: { studyStatus: status },
        }
    );
    return response.data.data;
    },
};