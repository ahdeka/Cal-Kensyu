'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import MainLayout from '@/components/MainLayout';
import { vocabularyService } from '@/lib/api/vocabularyService';
import { VocabularyCreateRequest } from '@/types/vocabulary';

export default function VocabularyAddPage() {
  const router = useRouter();
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState<VocabularyCreateRequest>({
    word: '',
    hiragana: '',
    meaning: '',
    exampleSentence: '',
    exampleTranslation: '',
  });

  useEffect(() => {
    checkLoginStatus();
  }, []);

  const checkLoginStatus = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/auth/me', {
        credentials: 'include',
      });
      if (response.ok) {
        setIsLoggedIn(true);
      } else {
        alert('単語を追加するにはログインが必要です');
        router.push('/login');
      }
    } catch (error) {
      alert('単語を追加するにはログインが必要です');
      router.push('/login');
    }
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.word.trim()) {
      alert('単語を入力してください');
      return;
    }
    if (!formData.hiragana.trim()) {
      alert('ひらがなを入力してください');
      return;
    }
    if (!formData.meaning.trim()) {
      alert('意味を入力してください');
      return;
    }

    setLoading(true);
    try {
      const submitData: VocabularyCreateRequest = {
        word: formData.word.trim(),
        hiragana: formData.hiragana.trim(),
        meaning: formData.meaning.trim(),
      };

      // 예문이 있을 때만 추가
      if (formData.exampleSentence && formData.exampleSentence.trim()) {
        submitData.exampleSentence = formData.exampleSentence.trim();
      }

      // 예문 번역이 있을 때만 추가
      if (formData.exampleTranslation && formData.exampleTranslation.trim()) {
        submitData.exampleTranslation = formData.exampleTranslation.trim();
      }

      await vocabularyService.createVocabulary(submitData);
      alert('単語を登録しました！');
      router.push('/vocabulary');
    } catch (error: any) {
      console.error('単語登録エラー:', error);
      const errorMsg =
        error.response?.data?.msg || '単語の登録に失敗しました';
      alert(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  if (!isLoggedIn) {
    return null;
  }

  return (
    <MainLayout>
      {/* ヘッダーセクション */}
      <section className="bg-gradient-to-r from-[#a80000] to-[#d32f2f] py-12 text-white shadow-inner">
        <div className="container mx-auto px-4 max-w-4xl">
          <h2 className="text-4xl font-extrabold mb-2 drop-shadow-md">
            ➕ 新しい単語を追加
          </h2>
          <p className="text-lg opacity-90">
            学びたい日本語の単語を登録しましょう
          </p>
        </div>
      </section>

      {/* フォームセクション */}
      <section className="py-12 bg-gray-50 min-h-[calc(100vh-280px)]">
        <div className="container mx-auto px-4 max-w-4xl">
          <form
            onSubmit={handleSubmit}
            className="bg-white rounded-lg shadow-md p-8 border border-gray-200"
          >
            {/* 単語 */}
            <div className="mb-6">
              <label
                htmlFor="word"
                className="block text-sm font-bold text-gray-700 mb-2"
              >
                単語 <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                id="word"
                name="word"
                value={formData.word}
                onChange={handleChange}
                placeholder="例: 食べる、勉強、綺麗"
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#a80000] focus:border-transparent"
                maxLength={100}
                required
              />
              <p className="text-xs text-gray-500 mt-1">
                漢字、ひらがな、カタカナで入力できます
              </p>
            </div>

            {/* ひらがな */}
            <div className="mb-6">
              <label
                htmlFor="hiragana"
                className="block text-sm font-bold text-gray-700 mb-2"
              >
                ひらがな（読み方） <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                id="hiragana"
                name="hiragana"
                value={formData.hiragana}
                onChange={handleChange}
                placeholder="例: たべる、べんきょう、きれい"
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#a80000] focus:border-transparent"
                maxLength={100}
                required
              />
              <p className="text-xs text-gray-500 mt-1">
                単語の読み方をひらがなで入力してください
              </p>
            </div>

            {/* 意味 */}
            <div className="mb-6">
              <label
                htmlFor="meaning"
                className="block text-sm font-bold text-gray-700 mb-2"
              >
                意味 <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                id="meaning"
                name="meaning"
                value={formData.meaning}
                onChange={handleChange}
                placeholder="例: 먹다、공부、예쁘다"
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#a80000] focus:border-transparent"
                maxLength={500}
                required
              />
              <p className="text-xs text-gray-500 mt-1">
                韓国語で単語の意味を入力してください
              </p>
            </div>

            {/* 例文 */}
            <div className="mb-6">
              <label
                htmlFor="exampleSentence"
                className="block text-sm font-bold text-gray-700 mb-2"
              >
                例文 <span className="text-gray-400">(選択)</span>
              </label>
              <textarea
                id="exampleSentence"
                name="exampleSentence"
                value={formData.exampleSentence}
                onChange={handleChange}
                placeholder="例: 毎日日本語を勉強します。"
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#a80000] focus:border-transparent resize-none"
                rows={3}
                maxLength={1000}
              />
              <p className="text-xs text-gray-500 mt-1">
                単語を使った例文を入力してください（任意）
              </p>
            </div>

            {/* 例文翻訳 */}
            <div className="mb-8">
              <label
                htmlFor="exampleTranslation"
                className="block text-sm font-bold text-gray-700 mb-2"
              >
                例文の翻訳 <span className="text-gray-400">(選択)</span>
              </label>
              <textarea
                id="exampleTranslation"
                name="exampleTranslation"
                value={formData.exampleTranslation}
                onChange={handleChange}
                placeholder="例: 매일 일본어를 공부합니다。"
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#a80000] focus:border-transparent resize-none"
                rows={3}
                maxLength={1000}
              />
              <p className="text-xs text-gray-500 mt-1">
                例文の韓国語翻訳を入力してください（任意）
              </p>
            </div>

            {/* ボタン */}
            <div className="flex gap-4">
              <button
                type="submit"
                disabled={loading}
                className="flex-1 bg-[#a80000] text-white py-4 rounded-lg font-bold hover:bg-[#8b0000] transition-all disabled:bg-gray-400 disabled:cursor-not-allowed cursor-pointer"
              >
                {loading ? '登録中...' : '✅ 単語を登録'}
              </button>
              <button
                type="button"
                onClick={() => router.back()}
                className="flex-1 bg-gray-500 text-white py-4 rounded-lg font-bold hover:bg-gray-600 transition-all cursor-pointer"
              >
                キャンセル
              </button>
            </div>
          </form>
        </div>
      </section>
    </MainLayout>
  );
}