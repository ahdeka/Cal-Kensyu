import { api } from './api';
import { LoginRequest, SignupRequest, ApiResponse } from '@/types';

// Authentication Service
export const authService = {
  // Login
  async login(data: LoginRequest): Promise<ApiResponse> {
    const response = await api.post<ApiResponse>('/api/auth/login', data);
    return response.data;
  },

  // Signup
  async signup(data: SignupRequest): Promise<ApiResponse> {
    const response = await api.post<ApiResponse>('/api/auth/signup', data);
    return response.data;
  },

  // Logout
  async logout(): Promise<ApiResponse> {
    const response = await api.post<ApiResponse>('/api/auth/logout');
    return response.data;
  },

  // Refresh token
  async refresh(): Promise<ApiResponse> {
    const response = await api.post<ApiResponse>('/api/auth/refresh');
    return response.data;
  },

  // Get current user info
  async getCurrentUser(): Promise<ApiResponse> {
    try {
      const response = await api.get<ApiResponse>('/api/auth/me');
      return response.data;
    } catch (error: any) {
      return {
        resultCode: '401',
        msg: 'Unauthorized',
        data: null
      };
    }
  },
};