import axios from 'axios';

// APIクライアントの設定
export const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080',
  withCredentials: true, // クッキーを含める
  headers: {
    'Content-Type': 'application/json',
  },
});

// レスポンスインターセプター（エラー処理）
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // 401エラーの場合、トークンリフレッシュを試みる
      try {
        await api.post('/api/auth/refresh');
        // リフレッシュ成功後、元のリクエストを再試行
        return api.request(error.config);
      } catch (refreshError) {
        // リフレッシュ失敗の場合、ログインページへリダイレクト
        if (typeof window !== 'undefined') {
          window.location.href = '/login';
        }
      }
    }
    return Promise.reject(error);
  }
);