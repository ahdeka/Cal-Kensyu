import axios from 'axios';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
console.log('🔍 [API CONFIG] API_URL:', API_URL);
console.log('🔍 [API CONFIG] NEXT_PUBLIC_API_URL:', process.env.NEXT_PUBLIC_API_URL)

// API client configuration
export const api = axios.create({
  baseURL: API_URL,
  withCredentials: true, // Include cookies
  headers: {
    'Content-Type': 'application/json',
  },
});

// Prevent duplicate refresh requests
let isRefreshing = false;
let failedQueue: any[] = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

// Response interceptor (error handling)
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (originalRequest.url?.includes('/api/users/me')) {
      if (error.response?.status === 401 || error.response?.status === 403) {
        const silentError = new Error('Unauthorized');
        (silentError as any).response = error.response;
        (silentError as any).config = error.config;
        return Promise.reject(silentError);
      }
    }

    const excludedUrls = ['/api/auth/login', '/api/auth/signup'];
    if (excludedUrls.some(url => originalRequest.url?.includes(url))) {
      return Promise.reject(error);
    }

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then(() => {
          return api.request(originalRequest);
        }).catch(err => {
          return Promise.reject(err);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        await api.post('/api/auth/refresh');
        processQueue(null, null);
        isRefreshing = false;
        // Retry original request after successful refresh
        return api.request(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        isRefreshing = false;
        // Redirect to login page on refresh failure
        if (typeof window !== 'undefined') {
          window.location.href = '/login';
        }
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);