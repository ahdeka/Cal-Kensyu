'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { authService } from '@/lib/auth';

export default function Header() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userInfo, setUserInfo] = useState<{ nickname: string } | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    checkLoginStatus();
  }, []);

  const checkLoginStatus = async () => {
    try {
      const response = await authService.getCurrentUser();
      
      if (response.resultCode === '200' && response.data) {
        setIsLoggedIn(true);
        setUserInfo(response.data);
      } else {
        setIsLoggedIn(false);
        setUserInfo(null);
      }
    } catch (error) {
      setIsLoggedIn(false);
      setUserInfo(null);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = async () => {
    try {
      await authService.logout();
      setIsLoggedIn(false);
      setUserInfo(null);
      window.location.href = '/';
    } catch (error) {
      console.error('Logout error:', error);
      alert('ログアウトに失敗しました');
    }
  };

  return (
    <header className="border-b border-gray-200 bg-white">
      <div className="container mx-auto px-4 py-4 max-w-6xl">
        <div className="flex flex-col md:flex-row justify-between items-center">
          <h1 className="text-3xl font-bold text-[#a80000] mb-4 md:mb-0">
            <Link href="/">NihonGo!</Link>
          </h1>
          
          <nav className="mb-4 md:mb-0">
            <ul className="flex flex-wrap justify-center gap-6">
              <li>
                <Link href="/#about" className="text-gray-700 hover:text-[#a80000] transition-colors">
                  サイトについて
                </Link>
              </li>
              <li>
                <Link href="/#lessons" className="text-gray-700 hover:text-[#a80000] transition-colors">
                  レッスン
                </Link>
              </li>
              <li>
                <Link href="/#community" className="text-gray-700 hover:text-[#a80000] transition-colors">
                  コミュニティ
                </Link>
              </li>
              <li>
                <Link href="/#contact" className="text-gray-700 hover:text-[#a80000] transition-colors">
                  お問い合わせ
                </Link>
              </li>
            </ul>
          </nav>
          
          <div className="min-w-[150px]">
            {loading ? (
              <div className="text-gray-400">読込中...</div>
            ) : isLoggedIn && userInfo ? (
              <div className="flex items-center gap-4">
                <span className="text-gray-700">
                  {userInfo.nickname}さん
                </span>
                <button
                  onClick={handleLogout}
                  className="border border-[#a80000] cursor-pointer text-[#a80000] px-4 py-2 rounded hover:bg-[#a80000] hover:text-white transition-all"
                >
                  ログアウト
                </button>
              </div>
            ) : (
              <Link 
                href="/login" 
                className="border border-[#a80000] text-[#a80000] px-4 py-2 rounded hover:bg-[#a80000] hover:text-white transition-all inline-block"
              >
                ログイン / 新規登録
              </Link>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}