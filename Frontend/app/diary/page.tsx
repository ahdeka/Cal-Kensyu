'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import MainLayout from '@/components/MainLayout';
import { diaryService } from '@/lib/api/diaryService';
import { DiaryListResponse } from '@/types/diary';

export default function DiaryPage() {
  const [diaries, setDiaries] = useState<DiaryListResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [view, setView] = useState<'public' | 'my'>('public');

  useEffect(() => {
    fetchDiaries();
  }, [view]);

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
    const diffDays = Math.floor(
      (now.getTime() - created.getTime()) / (1000 * 60 * 60 * 24)
    );

    if (diffDays === 0) return '今日';
    if (diffDays === 1) return '昨日';
    if (diffDays < 7) return `${diffDays}日前`;
    if (diffDays < 30) return `${Math.floor(diffDays / 7)}週間前`;
    return `${Math.floor(diffDays / 30)}ヶ月前`;
  };

  return (
    <MainLayout>
      {/* ヘッダーセクション */}
      <section className="bg-gray-100 py-6">
        <div className="container mx-auto px-4 max-w-6xl">
          <div className="flex flex-col md:flex-row justify-between items-center">
            <div>
              <h2 className="text-3xl font-bold text-[#a80000] mb-2">
                📝 みんなの日記帳
              </h2>
              <p className="text-gray-700">
                日本語で日記を書いて、学習仲間と共有しましょう
              </p>
            </div>
            <Link
              href="/diary/write"
              className="mt-4 md:mt-0 bg-[#a80000] text-white px-6 py-3 rounded-full font-bold hover:bg-[#d11a1a] hover:-translate-y-1 transition-all"
            >
              ✏️ 新しい日記を書く
            </Link>
          </div>
        </div>
      </section>

      {/* タブセクション */}
      <section className="bg-white border-b border-gray-200 py-4">
        <div className="container mx-auto px-4 max-w-6xl">
          <div className="flex gap-4">
            <button
              onClick={() => setView('public')}
              className={`px-4 py-2 rounded-full font-bold transition-all ${
                view === 'public'
                  ? 'bg-[#a80000] text-white'
                  : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
            >
              公開日記
            </button>
            <button
              onClick={() => setView('my')}
              className={`px-4 py-2 rounded-full font-bold transition-all ${
                view === 'my'
                  ? 'bg-[#a80000] text-white'
                  : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
            >
              マイ日記
            </button>
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
            <div className="space-y-6">
              {diaries.map((diary) => (
                <Link
                  key={diary.id}
                  href={`/diary/${diary.id}`}
                  className="block bg-white rounded-lg shadow-sm hover:shadow-md transition-all p-6 border border-gray-200"
                >
                  {/* 날짜와 작성자 */}
                  <div className="flex justify-between items-center mb-3">
                    <div className="flex items-center gap-3">
                      <span className="text-lg font-bold text-[#a80000]">
                        📅 {formatDate(diary.diaryDate)}
                      </span>
                      <span className="text-gray-600">👤 {diary.nickname}</span>
                    </div>
                    <span
                      className={`text-xs px-3 py-1 rounded-full ${
                        diary.isPublic
                          ? 'bg-green-100 text-green-700'
                          : 'bg-gray-100 text-gray-700'
                      }`}
                    >
                      {diary.isPublic ? '🔓 公開' : '🔒 非公開'}
                    </span>
                  </div>

                  {/* 제목 */}
                  <h3 className="text-xl font-bold text-gray-900 mb-3">
                    {diary.title}
                  </h3>

                  {/* 내용 미리보기 */}
                  <p className="text-gray-600 line-clamp-2 mb-4">
                    {diary.contentPreview}
                  </p>

                  {/* 하단 정보 */}
                  <div className="flex justify-between items-center text-sm text-gray-500">
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