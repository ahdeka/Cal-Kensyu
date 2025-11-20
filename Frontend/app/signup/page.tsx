'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
// import { authService } from '@/lib/auth';

interface SignupRequest {
  username: string;
  password: string;
  passwordConfirm: string;
  email: string;
  nickname: string;
}

export default function SignupPage() {
  const router = useRouter();
  const [formData, setFormData] = useState<SignupRequest>({
    username: '',
    password: '',
    passwordConfirm: '',
    email: '',
    nickname: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    // パスワード確認検証
    if (formData.password !== formData.passwordConfirm) {
      setError('パスワードが一致しません');
      return;
    }

    setLoading(true);

    try {
      // const response = await authService.signup(formData);
      
      // if (response.resultCode === '201') {
      //   alert('会員登録が完了しました!');
      //   router.push('/login');
      // } else {
      //   setError(response.msg || '会員登録に失敗しました');
      // }
      
      // デモ用の処理
      alert('会員登録が完了しました!');
      router.push('/login');
    } catch (err: any) {
      setError(err.response?.data?.msg || '会員登録エラーが発生しました');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 py-8">
      <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
        <div className="text-center mb-6">
          <Link href="/" className="text-3xl font-bold text-[#a80000]">
            NihonGo!
          </Link>
        </div>
        
        <h1 className="text-2xl font-bold text-center mb-6 text-gray-800">会員登録</h1>
        
        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label className="block text-gray-700 text-sm font-bold mb-2">
              ID
            </label>
            <input
              type="text"
              name="username"
              value={formData.username}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:border-[#a80000]"
              required
            />
          </div>

          <div className="mb-4">
            <label className="block text-gray-700 text-sm font-bold mb-2">
              Password
            </label>
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:border-[#a80000]"
              required
            />
          </div>

          <div className="mb-4">
            <label className="block text-gray-700 text-sm font-bold mb-2">
              Password確認
            </label>
            <input
              type="password"
              name="passwordConfirm"
              value={formData.passwordConfirm}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:border-[#a80000]"
              required
            />
          </div>

          <div className="mb-4">
            <label className="block text-gray-700 text-sm font-bold mb-2">
              Email
            </label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:border-[#a80000]"
              required
            />
          </div>

          <div className="mb-6">
            <label className="block text-gray-700 text-sm font-bold mb-2">
              ニックネーム
            </label>
            <input
              type="text"
              name="nickname"
              value={formData.nickname}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:border-[#a80000]"
              required
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-[#a80000] text-white font-bold py-2 px-4 rounded-lg hover:bg-[#d11a1a] disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
          >
            {loading ? '登録中...' : '登録'}
          </button>
        </form>

        <div className="mt-4 text-center">
          <p className="text-gray-600">
            すでにアカウントをお持ちですか？{' '}
            <Link href="/login" className="text-[#a80000] hover:underline font-bold">
              ログイン
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}