import {
  DiaryCreateRequest,
  DiaryUpdateRequest,
  DiaryResponse,
  DiaryListResponse,
  ApiResponse,
} from '@/types/diary';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

class DiaryService {
  // Create diary
  async createDiary(data: DiaryCreateRequest): Promise<DiaryResponse> {
    const response = await fetch(`${API_BASE_URL}/api/diary`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.msg || 'Failed to create diary');
    }

    const result: ApiResponse<DiaryResponse> = await response.json();
    if (!result.data) {
      throw new Error('No data available');
    }
    return result.data;
  }

  // Get public diaries
  async getPublicDiaries(): Promise<DiaryListResponse[]> {
    const response = await fetch(`${API_BASE_URL}/api/diary/public`, {
      method: 'GET',
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error('Failed to retrieve public diaries');
    }

    const result: ApiResponse<DiaryListResponse[]> = await response.json();
    return result.data || [];
  }

  // Get my diaries
  async getMyDiaries(): Promise<DiaryListResponse[]> {
    const response = await fetch(`${API_BASE_URL}/api/diary/my`, {
      method: 'GET',
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error('Failed to retrieve diaries');
    }

    const result: ApiResponse<DiaryListResponse[]> = await response.json();
    return result.data || [];
  }

  // Get diary detail
  async getDiary(id: number): Promise<DiaryResponse> {
    const response = await fetch(`${API_BASE_URL}/api/diary/${id}`, {
      method: 'GET',
      credentials: 'include',
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.msg || 'Failed to retrieve diary');
    }

    const result: ApiResponse<DiaryResponse> = await response.json();
    if (!result.data) {
      throw new Error('No data available');
    }
    return result.data;
  }

  // Update diary
  async updateDiary(
    id: number,
    data: DiaryUpdateRequest
  ): Promise<DiaryResponse> {
    const response = await fetch(`${API_BASE_URL}/api/diary/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.msg || 'Failed to update diary');
    }

    const result: ApiResponse<DiaryResponse> = await response.json();
    if (!result.data) {
      throw new Error('No data available');
    }
    return result.data;
  }

  // Delete diary
  async deleteDiary(id: number): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/api/diary/${id}`, {
      method: 'DELETE',
      credentials: 'include',
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.msg || 'Failed to delete diary');
    }
  }
}

export const diaryService = new DiaryService();