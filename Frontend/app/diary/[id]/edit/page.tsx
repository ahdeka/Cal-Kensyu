'use client';

import { useState, useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation';
import MainLayout from '@/components/MainLayout';
import { diaryService } from '@/lib/api/diaryService';
import { DiaryResponse } from '@/types/diary';

export default function DiaryEditPage() {
  const router = useRouter();
  const params = useParams();
  const diaryId = Number(params.id);

  // 날짜 제한 계산
  const today = new Date().toISOString().split('T')[0];
  const oneYearAgo = new Date();
  oneYearAgo.setFullYear(oneYearAgo.getFullYear() - 1);
  const minDate = oneYearAgo.toISOString().split('T')[0];

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [diary, setDiary] = useState<DiaryResponse | null>(null);

  // フォームデータ
  const [diaryDate, setDiaryDate] = useState(today);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [isPublic, setIsPublic] = useState(true);

  useEffect(() => {
    fetchDiary();
  }, [diaryId]);

  const fetchDiary = async () => {
    setLoading(true);
    try {
      const data = await diaryService.getDiary(diaryId);
      setDiary(data);
      
      // フォームに既存データをセット
      setDiaryDate(data.diaryDate);
      setTitle(data.title);
      setContent(data.content);
      setIsPublic(data.isPublic);

      // 作成者 확인
      const currentUser = await getCurrentUser();
      if (currentUser?.username !== data.username) {
        alert('この日記を修正する権限がありません');
        router.push('/diary');
      }
    } catch (error) {
      console.error('日記の読込に失敗:', error);
      alert(error instanceof Error ? error.message : '日記の読込に失敗しました');
      router.push('/diary');
    } finally {
      setLoading(false);
    }
  };

  const getCurrentUser = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/users/me', {
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

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!title.trim() || !content.trim()) {
      alert('タイトルと内容を入力してください');
      return;
    }

    setSaving(true);

    try {
      await diaryService.updateDiary(diaryId, {
        diaryDate,
        title,
        content,
        isPublic,
      });

      alert('日記が修正されました！');
      router.push(`/diary/${diaryId}`);
    } catch (error) {
      console.error('日記修正失敗:', error);
      alert(error instanceof Error ? error.message : '日記の修正に失敗しました');
    } finally {
      setSaving(false);
    }
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
        </div>
      </MainLayout>
    );
  }

  return (
    <MainLayout>
      <section className="py-12 bg-gray-50 min-h-screen">
        <div className="container mx-auto px-4 max-w-4xl">
          <div className="bg-white rounded-lg shadow-md p-8">
            <h2 className="text-3xl font-bold text-[#a80000] mb-6">
              ✏️ 日記を編集
            </h2>

            <form onSubmit={handleSubmit} className="space-y-6">
              {/* 日付選択 */}
              <div>
                <label className="block text-gray-700 font-bold mb-2">
                  日付
                </label>
                <input
                  type="date"
                  value={diaryDate}
                  onChange={(e) => setDiaryDate(e.target.value)}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#a80000]"
                  required
                />
              </div>

              {/* タイトル */}
              <div>
                <label className="block text-gray-700 font-bold mb-2">
                  タイトル
                </label>
                <input
                  type="text"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#a80000]"
                  placeholder="タイトルを入力してください"
                  maxLength={100}
                  required
                />
                <p className="text-sm text-gray-500 mt-1">
                  {title.length}/100文字
                </p>
              </div>

              {/* 内容 */}
              <div>
                <label className="block text-gray-700 font-bold mb-2">
                  内容
                </label>
                <textarea
                  value={content}
                  onChange={(e) => setContent(e.target.value)}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#a80000] min-h-[300px] resize-vertical"
                  placeholder="内容を入力してください"
                  required
                />
              </div>

              {/* 公開設定 */}
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

              {/* ボタン */}
              <div className="flex gap-4 pt-4">
                <button
                  type="submit"
                  disabled={saving}
                  className="flex-1 bg-[#a80000] cursor-pointer text-white px-6 py-3 rounded-lg font-bold hover:bg-[#d11a1a] transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {saving ? '保存中...' : '修正を保存'}
                </button>
                <button
                  type="button"
                  onClick={() => router.back()}
                  disabled={saving}
                  className="px-6 py-3 border cursor-pointer border-gray-300 rounded-lg font-bold text-gray-700 hover:bg-gray-100 transition-all disabled:opacity-50"
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