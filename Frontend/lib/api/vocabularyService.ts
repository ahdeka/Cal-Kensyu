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
  // Register vocabulary
  createVocabulary: async (
    data: VocabularyCreateRequest
  ): Promise<VocabularyResponse> => {
    const response = await api.post<ApiResponse<VocabularyResponse>>(
      '/api/vocabularies',
      data
    );
    return response.data.data;
  },

  // Get all my vocabularies
  getMyVocabularies: async (): Promise<VocabularyListResponse[]> => {
    const response = await api.get<ApiResponse<VocabularyListResponse[]>>(
      '/api/vocabularies'
    );
    return response.data.data;
  },

  // Get vocabularies by study status
  getVocabulariesByStatus: async (
    status: StudyStatus
  ): Promise<VocabularyListResponse[]> => {
    const response = await api.get<ApiResponse<VocabularyListResponse[]>>(
      `/api/vocabularies/status/${status}`
    );
    return response.data.data;
  },

  // Search vocabularies
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

  // Get vocabulary details
  getVocabulary: async (id: number): Promise<VocabularyResponse> => {
    const response = await api.get<ApiResponse<VocabularyResponse>>(
      `/api/vocabularies/${id}`
    );
    return response.data.data;
  },

  // Update vocabulary
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

  // Delete vocabulary
  deleteVocabulary: async (id: number): Promise<void> => {
    await api.delete(`/api/vocabularies/${id}`);
  },

  // Update study status only
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