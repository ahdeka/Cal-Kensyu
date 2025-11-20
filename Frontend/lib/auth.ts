import { api } from './api';
import { LoginRequest, SignupRequest, ApiResponse } from '@/types';

// 認証サービス
export const authService = {
  // ログイン
  async login(data: LoginRequest): Promise<ApiResponse> {
    const response = await api.post<ApiResponse>('/api/auth/login', data);
    return response.data;
  },

  // 会員登録
  async signup(data: SignupRequest): Promise<ApiResponse> {
    const response = await api.post<ApiResponse>('/api/auth/signup', data);
    return response.data;
  },

  // ログアウト
  async logout(): Promise<ApiResponse> {
    const response = await api.post<ApiResponse>('/api/auth/logout');
    return response.data;
  },

  // トークンリフレッシュ
  async refresh(): Promise<ApiResponse> {
    const response = await api.post<ApiResponse>('/api/auth/refresh');
    return response.data;
  },

  // 現在のユーザー情報取得
  async getCurrentUser(): Promise<ApiResponse> {
    try {
      const response = await api.get<ApiResponse>('/api/auth/me');
      return response.data;
    } catch (error: any) {
      return {
        resultCode: '401',
        msg: '未認証',
        data: null
      };
    }
  },
};