'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import MainLayout from '@/components/MainLayout';
import { diaryService } from '@/lib/api/diaryService';
import { DiaryListResponse } from '@/types/diary';

export default function DiaryPage() {
  const router = useRouter();
  const [diaries, setDiaries] = useState<DiaryListResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [view, setView] = useState<'public' | 'my'>('public');
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  useEffect(() => {
    checkLoginStatus();
  }, []);

  useEffect(() => {
    fetchDiaries();
  }, [view]);

  const checkLoginStatus = async () => {
    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/users/me`, {
        credentials: 'include',
      });
      setIsLoggedIn(response.ok);
    } catch (error) {
      setIsLoggedIn(false);
    }
  };

  const handleViewChange = (newView: 'public' | 'my') => {
    if (newView === 'my' && !isLoggedIn) {
      alert('マイ日記を見るにはログインが必要です');
      router.push('/login');
      return;
    }
    setView(newView);
  };

  const fetchDiaries = async () => {
    setLoading(true);
    try {
      const data =
        view === 'public'
          ? await diaryService.getPublicDiaries()
          : await diaryService.getMyDiaries();
      setDiaries(data);
    } catch (error) {
      console.error('日記の読込に失敗:', error);
      alert('日記の読込に失敗しました');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ja-JP', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const getRelativeTime = (dateString: string) => {
    const now = new Date();
    const created = new Date(dateString);
    const diffMs = now.getTime() - created.getTime();
    const diffMinutes = Math.floor(diffMs / (1000 * 60));
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffMinutes < 1) return '方今';
    if (diffMinutes < 60) return `${diffMinutes}分前`;
    if (diffHours < 24) return `${diffHours}時間前`;
    if (diffDays < 7) return `${diffDays}日前`;
    if (diffDays < 30) return `${Math.floor(diffDays / 7)}週間前`;
    return `${Math.floor(diffDays / 30)}ヶ月前`;
  };

  return (
    <MainLayout>
      {/* ヘッダーセクション */}
      <section className="bg-gradient-to-r from-[#a80000] to-[#d32f2f] py-12 text-white shadow-inner">
        <div className="container mx-auto px-4 max-w-6xl">
          <div className="flex flex-col md:flex-row justify-between items-center gap-6">
            <div>
              <h2 className="text-4xl font-extrabold mb-2 drop-shadow-md">
                📝 みんなの日記帳
              </h2>
              <p className="text-lg opacity-90">
                日本語で日記を書いて共有して、学び合いましょう
              </p>
            </div>
            <Link
              href="/diary/write"
              className="bg-white text-[#a80000] px-7 py-3 rounded-full font-bold shadow-md hover:shadow-lg hover:-translate-y-1 transition-all"
            >
              ✏️ 新しい日記を書く
            </Link>
          </div>
        </div>
      </section>

      {/* タブセクション */}
      <section className="bg-white border-b border-gray-200 py-4 sticky top-[0px] z-10">
        <div className="container mx-auto px-4 max-w-6xl">
          <div className="flex gap-6 relative">
            {['public', 'my'].map((type) => (
              <button
                key={type}
                onClick={() => handleViewChange(type as 'public' | 'my')}
                className={`px-3 pb-2 cursor-pointer text-lg font-bold transition-all relative ${
                  view === type
                    ? 'text-[#a80000]'
                    : 'text-gray-500 hover:text-gray-700'
                }`}
              >
                {type === 'public' ? '公開日記' : 'マイ日記'}
                {view === type && (
                  <span className="absolute left-0 right-0 -bottom-1 h-[3px] bg-[#a80000] rounded-full animate-[widthGrow_.25s_ease]"></span>
                )}
              </button>
            ))}
          </div>
        </div>
      </section>

      {/* 日記リストセクション */}
      <section className="py-12 bg-gray-50 min-h-[calc(100vh-280px)]">
        <div className="container mx-auto px-4 max-w-6xl">
          {loading ? (
            <div className="text-center py-20">
              <p className="text-gray-500 text-lg">読込中...</p>
            </div>
          ) : diaries.length === 0 ? (
            <div className="text-center py-20">
              <p className="text-gray-500 text-lg mb-4">
                まだ日記がありません
              </p>
              <Link
                href="/diary/write"
                className="text-[#a80000] font-bold hover:underline"
              >
                最初の日記を書いてみましょう！
              </Link>
            </div>
          ) : (
            <div className="space-y-4">
              {diaries.map((diary) => (
                <Link
                  key={diary.id}
                  href={`/diary/${diary.id}`}
                  className="block bg-white rounded-lg shadow-sm hover:shadow-md transition-all p-6 border border-gray-200 hover:border-[#a80000]"
                >
                  {/* 작성자와 공개/비공개 */}
                  <div className="flex justify-between items-center mb-3">
                    <span className="text-sm text-gray-600 font-medium">
                      👤 {diary.nickname}
                    </span>
                    {view === 'my' && (
                      <span
                        className={`text-xs px-3 py-1 rounded-full font-bold ${
                          diary.isPublic
                            ? 'bg-green-100 text-green-700'
                            : 'bg-gray-200 text-gray-700'
                        }`}
                      >
                        {diary.isPublic ? '🔓 公開' : '🔒 非公開'}
                      </span>
                    )}
                  </div>

                  {/* 타이틀 */}
                  <h3 className="text-xl font-bold text-gray-900 mb-2">
                    {diary.title}
                  </h3>

                  {/* 내용 미리보기 */}
                  <p className="text-gray-600 line-clamp-2 mb-4 text-sm leading-relaxed">
                    {diary.contentPreview}
                  </p>

                  {/* 일기 날짜 | 작성 시간 */}
                  <div className="flex items-center gap-2 text-sm text-gray-500">
                    <span className="font-medium text-[#a80000]">
                      📅 {formatDate(diary.diaryDate)}
                    </span>
                    <span className="text-gray-400">|</span>
                    <span>⏰ {getRelativeTime(diary.createDate)}</span>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </div>
      </section>
    </MainLayout>
  );
}