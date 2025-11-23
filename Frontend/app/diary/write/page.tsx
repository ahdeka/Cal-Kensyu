'use client';

import { useState, useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';
import MainLayout from '@/components/MainLayout';
import { diaryService } from '@/lib/api/diaryService';

export default function DiaryWritePage() {
  const router = useRouter();
  const hasCheckedAuth = useRef(false);
  
  // 날짜 제한 계산
  const today = new Date().toISOString().split('T')[0];
  const oneYearAgo = new Date();
  oneYearAgo.setFullYear(oneYearAgo.getFullYear() - 1);
  const minDate = oneYearAgo.toISOString().split('T')[0];

  const [diaryDate, setDiaryDate] = useState(today);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [isPublic, setIsPublic] = useState(true);
  const [loading, setLoading] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [checkingAuth, setCheckingAuth] = useState(true);

  useEffect(() => {
    if (!hasCheckedAuth.current) {
      hasCheckedAuth.current = true;
      checkLoginStatus();
    }
  }, []);

  const checkLoginStatus = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/auth/me', {
        credentials: 'include',
      });
      
      if (response.ok) {
        setIsLoggedIn(true);
      } else {
        alert('日記を書くにはログインが必要です');
        router.push('/login');
      }
    } catch (error) {
      console.error('認証確認エラー:', error);
      alert('日記を書くにはログインが必要です');
      router.push('/login');
    } finally {
      setCheckingAuth(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!title.trim() || !content.trim()) {
      alert('タイトルと内容を入力してください');
      return;
    }

    setLoading(true);

    try {
      await diaryService.createDiary({
        diaryDate,
        title,
        content,
        isPublic,
      });

      alert('日記が保存されました！');
      router.push('/diary');
    } catch (error) {
      console.error('日記保存失敗:', error);
      alert(error instanceof Error ? error.message : '日記の保存に失敗しました');
    } finally {
      setLoading(false);
    }
  };

  // 로그인 확인 중일 때
  if (checkingAuth) {
    return (
      <MainLayout>
        <div className="py-20 text-center">
          <p className="text-gray-500 text-lg">確認中...</p>
        </div>
      </MainLayout>
    );
  }

  // 로그인 안 되어 있으면 빈 화면 (리다이렉트 중)
  if (!isLoggedIn) {
    return null;
  }

  return (
    <MainLayout>
      <section className="py-12 bg-gray-50 min-h-screen">
        <div className="container mx-auto px-4 max-w-4xl">
          <div className="bg-white rounded-lg shadow-md p-8">
            <h2 className="text-3xl font-bold text-[#a80000] mb-6">
              ✏️ 新しい日記を書く
            </h2>

            <form onSubmit={handleSubmit} className="space-y-6">
              {/* 날짜 선택 */}
              <div>
                <label className="block text-gray-700 font-bold mb-2">
                  日付
                </label>
                <input
                  type="date"
                  value={diaryDate}
                  onChange={(e) => setDiaryDate(e.target.value)}
                  lang="ja-JP"
                  className="px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#a80000] cursor-pointer w-auto"
                  required
                />
              </div>

              {/* 제목 */}
              <div>
                <label className="block text-gray-700 font-bold mb-2">
                  タイトル
                </label>
                <input
                  type="text"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#a80000]"
                  placeholder="今日のタイトルを入力してください"
                  maxLength={100}
                  required
                />
                <p className="text-sm text-gray-500 mt-1">
                  {title.length}/100文字
                </p>
              </div>

              {/* 내용 */}
              <div>
                <label className="block text-gray-700 font-bold mb-2">
                  内容
                </label>
                <textarea
                  value={content}
                  onChange={(e) => setContent(e.target.value)}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#a80000] min-h-[300px] resize-vertical"
                  placeholder="今日の出来事や感想を書いてください"
                  required
                />
              </div>

              {/* 공개 설정 */}
              <div className="flex items-center gap-3">
                <input
                  type="checkbox"
                  id="isPublic"
                  checked={isPublic}
                  onChange={(e) => setIsPublic(e.target.checked)}
                  className="w-5 h-5 text-[#a80000] focus:ring-[#a80000]"
                />
                <label htmlFor="isPublic" className="text-gray-700">
                  この日記を公開する（他の学習者が見ることができます）
                </label>
              </div>

              {/* 버튼 */}
              <div className="flex gap-4 pt-4">
                <button
                  type="submit"
                  disabled={loading}
                  className="flex-1 bg-[#a80000] cursor-pointer text-white px-6 py-3 rounded-lg font-bold hover:bg-[#d11a1a] transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {loading ? '保存中...' : '日記を保存'}
                </button>
                <button
                  type="button"
                  onClick={() => router.push('/diary')}
                  className="px-6 py-3 border cursor-pointer border-gray-300 rounded-lg font-bold text-gray-700 hover:bg-gray-100 transition-all"
                >
                  キャンセル
                </button>
              </div>
            </form>
          </div>
        </div>
      </section>
    </MainLayout>
  );
}