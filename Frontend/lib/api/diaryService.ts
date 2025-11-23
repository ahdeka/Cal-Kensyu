import {
  DiaryCreateRequest,
  DiaryUpdateRequest,
  DiaryResponse,
  DiaryListResponse,
  ApiResponse,
} from '@/types/diary';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

class DiaryService {
  // 일기 작성
  async createDiary(data: DiaryCreateRequest): Promise<DiaryResponse> {
    const response = await fetch(`${API_BASE_URL}/api/diary`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include', // 쿠키 포함
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.msg || '日記の作成に失敗しました');
    }

    const result: ApiResponse<DiaryResponse> = await response.json();
    if (!result.data) {
      throw new Error('データがありません');
    }
    return result.data;
  }

  // 공개 일기 목록 조회
  async getPublicDiaries(): Promise<DiaryListResponse[]> {
    const response = await fetch(`${API_BASE_URL}/api/diary/public`, {
      method: 'GET',
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error('公開日記の取得に失敗しました');
    }

    const result: ApiResponse<DiaryListResponse[]> = await response.json();
    return result.data || [];
  }

  // 내 일기 목록 조회
  async getMyDiaries(): Promise<DiaryListResponse[]> {
    const response = await fetch(`${API_BASE_URL}/api/diary/my`, {
      method: 'GET',
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error('日記の取得に失敗しました');
    }

    const result: ApiResponse<DiaryListResponse[]> = await response.json();
    return result.data || [];
  }

  // 일기 상세 조회
  async getDiary(id: number): Promise<DiaryResponse> {
    const response = await fetch(`${API_BASE_URL}/api/diary/${id}`, {
      method: 'GET',
      credentials: 'include',
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.msg || '日記の取得に失敗しました');
    }

    const result: ApiResponse<DiaryResponse> = await response.json();
    if (!result.data) {
      throw new Error('データがありません');
    }
    return result.data;
  }

  // 일기 수정
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
      throw new Error(error.msg || '日記の修正に失敗しました');
    }

    const result: ApiResponse<DiaryResponse> = await response.json();
    if (!result.data) {
      throw new Error('データがありません');
    }
    return result.data;
  }

  // 일기 삭제
  async deleteDiary(id: number): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/api/diary/${id}`, {
      method: 'DELETE',
      credentials: 'include',
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.msg || '日記の削除に失敗しました');
    }
  }
}

export const diaryService = new DiaryService();