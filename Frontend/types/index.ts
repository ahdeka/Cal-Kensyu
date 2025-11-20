// ログインリクエスト
export interface LoginRequest {
  username: string;
  password: string;
}

// 会員登録リクエスト
export interface SignupRequest {
  username: string;
  password: string;
  passwordConfirm: string;
  email: string;
  nickname: string;
}

// APIレスポンス
export interface ApiResponse<T = any> {
  resultCode: string;
  msg: string;
  data?: T;
}

// ユーザー情報
export interface User {
  id: number;
  username: string;
  email: string;
  name: string;
  nickname: string;
}