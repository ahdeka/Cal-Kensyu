'use client';

import { useState, useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Link from 'next/link';
import MainLayout from '@/components/MainLayout';
import { diaryService } from '@/lib/api/diaryService';
import { DiaryResponse } from '@/types/diary';

export default function DiaryDetailPage() {
  const router = useRouter();
  const params = useParams();
  const diaryId = Number(params.id);

  const [diary, setDiary] = useState<DiaryResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [isOwner, setIsOwner] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  useEffect(() => {
    fetchDiary();
  }, [diaryId]);

  const fetchDiary = async () => {
    setLoading(true);
    try {
      const data = await diaryService.getDiary(diaryId);
      setDiary(data);
      
      // 현재 로그인한 사용자 정보 확인 (추후 auth context에서 가져오기)
      // 임시로 username 비교
      const currentUser = await getCurrentUser();
      setIsOwner(currentUser?.username === data.username);
    } catch (error) {
      console.error('日記の読込に失敗:', error);
      alert(error instanceof Error ? error.message : '日記の読込に失敗しました');
      router.push('/diary');
    } finally {
      setLoading(false);
    }
  };

  // 현재 사용자 정보 가져오기 (임시 - 실제로는 auth context 사용)
  const getCurrentUser = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/auth/me', {
        credentials: 'include',
      });
      if (response.ok) {
        const result = await response.json();
        return result.data;
      }
    } catch (error) {
      console.error('ユーザー情報取得失敗:', error);
    }
    return null;
  };

  const handleDelete = async () => {
    try {
      await diaryService.deleteDiary(diaryId);
      alert('日記が削除されました');
      router.push('/diary');
    } catch (error) {
      console.error('日記削除失敗:', error);
      alert(error instanceof Error ? error.message : '日記の削除に失敗しました');
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ja-JP', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      weekday: 'long',
    });
  };

  const formatDateTime = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleString('ja-JP', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (loading) {
    return (
      <MainLayout>
        <div className="py-20 text-center">
          <p className="text-gray-500 text-lg">読込中...</p>
        </div>
      </MainLayout>
    );
  }

  if (!diary) {
    return (
      <MainLayout>
        <div className="py-20 text-center">
          <p className="text-gray-500 text-lg">日記が見つかりません</p>
          <Link
            href="/diary"
            className="text-[#a80000] font-bold hover:underline mt-4 inline-block"
          >
            日記リストに戻る
          </Link>
        </div>
      </MainLayout>
    );
  }

  return (
    <>
      <MainLayout>
        <section className="py-12 bg-gray-50 min-h-screen">
          <div className="container mx-auto px-4 max-w-4xl">
            {/* 戻るボタン */}
            <div className="mb-6">
              <button
                onClick={() => router.push('/diary')}
                className="text-gray-600 hover:text-[#a80000] font-medium flex items-center gap-2"
              >
                ← 戻る
              </button>
            </div>

            {/* メインコンテンツ */}
            <article className="bg-white rounded-lg shadow-md p-8">
              {/* ヘッダー */}
              <header className="border-b border-gray-200 pb-6 mb-6">
                <div className="flex justify-between items-start mb-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-3">
                      <span className="text-xl font-bold text-[#a80000]">
                        📅 {formatDate(diary.diaryDate)}
                      </span>
                      {!diary.isPublic && (
                        <span className="text-sm px-3 py-1 rounded-full bg-gray-100 text-gray-700">
                          🔒 非公開
                        </span>
                      )}
                    </div>
                    <h1 className="text-3xl font-bold text-gray-900 mb-4">
                      {diary.title}
                    </h1>
                    <div className="flex items-center gap-4 text-sm text-gray-600">
                      <span className="font-medium">👤 {diary.nickname}</span>
                      <span>作成: {formatDateTime(diary.createDate)}</span>
                      {diary.updateDate !== diary.createDate && (
                        <span>更新: {formatDateTime(diary.updateDate)}</span>
                      )}
                    </div>
                  </div>

                  {/* 編集・削除ボタン（作成者のみ） */}
                  {isOwner && (
                    <div className="flex gap-2 ml-4">
                      <Link
                        href={`/diary/${diary.id}/edit`}
                        className="px-4 py-2 bg-blue-500 text-white rounded-lg font-medium hover:bg-blue-600 transition-all"
                      >
                        ✏️ 編集
                      </Link>
                      <button
                        onClick={() => setShowDeleteModal(true)}
                        className="px-4 py-2 bg-red-500 cursor-pointer text-white rounded-lg font-medium hover:bg-red-600 transition-all"
                      >
                        🗑️ 削除
                      </button>
                    </div>
                  )}
                </div>
              </header>

              {/* 本文 */}
              <div className="prose max-w-none">
                <div className="text-gray-800 leading-relaxed whitespace-pre-wrap">
                  {diary.content}
                </div>
              </div>
            </article>
          </div>
        </section>
      </MainLayout>
      {/* 削除確認モーダル */}
      {showDeleteModal && (
        <div className="fixed inset-0 bg-black/30 backdrop-blur-sm flex items-center justify-center z-50">
          <div className="bg-white rounded-2xl shadow-[0_8px_30px_rgba(0,0,0,0.12)] border border-gray-200 p-8 max-w-md w-full mx-4 animate-fadeIn">
            <h3 className="text-2xl font-bold text-gray-900 mb-4 text-center">
              本当に削除しますか？
            </h3>
            <p className="text-gray-600 mb-6 text-center">
              この操作は取り消せません。
            </p>
            <div className="flex gap-4">
              <button
                onClick={handleDelete}
                className="flex-1 bg-red-500 cursor-pointer hover:bg-red-600 text-white px-6 py-3 rounded-xl font-bold transition-all"
              >
                削除する
              </button>
              <button
                onClick={() => setShowDeleteModal(false)}
                className="flex-1 bg-gray-200 cursor-pointer hover:bg-gray-300 text-gray-700 px-6 py-3 rounded-xl font-bold transition-all"
              >
                キャンセル
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}