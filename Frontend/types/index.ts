// Login request
export interface LoginRequest {
  username: string;
  password: string;
}

// Signup request
export interface SignupRequest {
  username: string;
  password: string;
  passwordConfirm: string;
  email: string;
  nickname: string;
}

// API response
export interface ApiResponse<T = any> {
  resultCode: string;
  msg: string;
  data?: T;
}

// User information
export interface User {
  id: number;
  username: string;
  email: string;
  name: string;
  nickname: string;
}

export interface UserInfo {
  username: string;
  nickname: string;
  email: string;
  role: string;
}