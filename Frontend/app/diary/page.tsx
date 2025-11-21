'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import MainLayout from '@/components/MainLayout';

interface DiaryEntry {
  id: number;
  title: string;
  content: string;
  author: string;
  createdAt: string;
  isPublic: boolean;
  views: number;
  likes: number;
}

export default function DiaryPage() {
  const [diaries, setDiaries] = useState<DiaryEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<'all' | 'public' | 'private'>('all');

  useEffect(() => {
    fetchDiaries();
  }, [filter]);

  const fetchDiaries = async () => {
    try {
      // API 호출 로직을 여기에 추가하세요
      setLoading(false);
    } catch (error) {
      console.error('일기 로딩 실패:', error);
      setLoading(false);
    }
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

      {/* フィルターセクション */}
      {/*       
      <section className="bg-white border-b border-gray-200 py-4">
        <div className="container mx-auto px-4 max-w-6xl">
          <div className="flex gap-4">
            <button
              onClick={() => setFilter('all')}
              className={`px-4 py-2 rounded-full font-bold transition-all ${
                filter === 'all'
                  ? 'bg-[#a80000] text-white'
                  : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
            >
              すべて
            </button>
            <button
              onClick={() => setFilter('public')}
              className={`px-4 py-2 rounded-full font-bold transition-all ${
                filter === 'public'
                  ? 'bg-[#a80000] text-white'
                  : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
            >
              公開日記
            </button>
            <button
              onClick={() => setFilter('private')}
              className={`px-4 py-2 rounded-full font-bold transition-all ${
                filter === 'private'
                  ? 'bg-[#a80000] text-white'
                  : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
            >
              非公開日記
            </button>
          </div>
        </div>
      </section> 
      */}

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
                  className="block bg-white rounded-lg shadow-sm hover:shadow-md transition-all p-6 border border-gray-200"
                >
                  <div className="flex justify-between items-start mb-3">
                    <div className="flex-1">
                      <div className="flex items-center gap-3 mb-2">
                        <h3 className="text-xl font-bold text-gray-900">
                          {diary.title}
                        </h3>
                        <span
                          className={`text-xs px-3 py-1 rounded-full ${
                            diary.isPublic
                              ? 'bg-green-100 text-green-700'
                              : 'bg-gray-100 text-gray-700'
                          }`}
                        >
                          {diary.isPublic ? '公開' : '非公開'}
                        </span>
                      </div>
                      <p className="text-gray-600 line-clamp-2 mb-3">
                        {diary.content}
                      </p>
                    </div>
                  </div>
                  <div className="flex justify-between items-center text-sm text-gray-500">
                    <div className="flex items-center gap-4">
                      <span className="font-medium text-[#a80000]">
                        {diary.author}
                      </span>
                      <span>{diary.createdAt}</span>
                    </div>
                    <div className="flex items-center gap-4">
                      <span>👁️ {diary.views}</span>
                      <span>❤️ {diary.likes}</span>
                    </div>
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