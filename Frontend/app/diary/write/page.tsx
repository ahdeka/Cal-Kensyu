'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import MainLayout from '@/components/MainLayout';

export default function DiaryWritePage() {
  const router = useRouter();
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [isPublic, setIsPublic] = useState(true);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!title.trim() || !content.trim()) {
      alert('タイトルと内容を入力してください');
      return;
    }

    setLoading(true);
    
    try {
      // API 호출 로직을 여기에 추가하세요
      // const response = await diaryService.create({ title, content, isPublic });
      
      alert('日記が保存されました！');
      router.push('/diary');
    } catch (error) {
      console.error('日記保存失敗:', error);
      alert('日記の保存に失敗しました');
    } finally {
      setLoading(false);
    }
  };

  return (
    <MainLayout>
      <section className="py-12 bg-gray-50 min-h-screen">
        <div className="container mx-auto px-4 max-w-4xl">
          <div className="bg-white rounded-lg shadow-md p-8">
            <h2 className="text-3xl font-bold text-[#a80000] mb-6">
              ✏️ 新しい日記を書く
            </h2>
            
            <form onSubmit={handleSubmit} className="space-y-6">
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
                />
              </div>

              <div>
                <label className="block text-gray-700 font-bold mb-2">
                  内容
                </label>
                <textarea
                  value={content}
                  onChange={(e) => setContent(e.target.value)}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#a80000] min-h-[300px] resize-vertical"
                  placeholder="今日の出来事や感想を書いてください"
                />
              </div>

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

              <div className="flex gap-4 pt-4">
                <button
                  type="submit"
                  disabled={loading}
                  className="flex-1 bg-[#a80000] text-white px-6 py-3 rounded-lg font-bold hover:bg-[#d11a1a] transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {loading ? '保存中...' : '日記を保存'}
                </button>
                <button
                  type="button"
                  onClick={() => router.back()}
                  className="px-6 py-3 border border-gray-300 rounded-lg font-bold text-gray-700 hover:bg-gray-100 transition-all"
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