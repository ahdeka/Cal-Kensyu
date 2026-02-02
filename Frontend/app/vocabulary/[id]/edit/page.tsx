'use client';

import { useState, useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation';
import MainLayout from '@/components/MainLayout';
import { vocabularyService } from '@/lib/api/vocabularyService';
import { VocabularyResponse, VocabularyUpdateRequest, StudyStatus } from '@/types/vocabulary';

export default function VocabularyEditPage() {
  const router = useRouter();
  const params = useParams();
  const id = params.id as string;

  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    word: '',
    hiragana: '',
    meaning: '',
    exampleSentence: '',
    exampleTranslation: '',
    studyStatus: 'NOT_STUDIED' as StudyStatus,
  });

  useEffect(() => {
    checkLoginStatus();
  }, []);

  useEffect(() => {
    if (isLoggedIn && id) {
      fetchVocabulary();
    }
  }, [isLoggedIn, id]);

  const checkLoginStatus = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/users/me', {
        credentials: 'include',
      });
      if (response.ok) {
        setIsLoggedIn(true);
      } else {
        alert('単語を編集するにはログインが必要です');
        router.push('/login');
      }
    } catch (error) {
      alert('単語を編集するにはログインが必要です');
      router.push('/login');
    }
  };

  const fetchVocabulary = async () => {
    setLoading(true);
    try {
      const data = await vocabularyService.getVocabulary(Number(id));
      setFormData({
        word: data.word,
        hiragana: data.hiragana,
        meaning: data.meaning,
        exampleSentence: data.exampleSentence || '',
        exampleTranslation: data.exampleTranslation || '',
        studyStatus: data.studyStatus,
      });
    } catch (error) {
      console.error('単語の読込に失敗:', error);
      alert('単語の読込に失敗しました');
      router.push('/vocabulary');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
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

    setSubmitting(true);
    try {
      const submitData: VocabularyUpdateRequest = {
        word: formData.word.trim(),
        hiragana: formData.hiragana.trim(),
        meaning: formData.meaning.trim(),
        studyStatus: formData.studyStatus,
      };

      // 예문이 있을 때만 추가
      if (formData.exampleSentence && formData.exampleSentence.trim()) {
        submitData.exampleSentence = formData.exampleSentence.trim();
      }

      // 예문 번역이 있을 때만 추가
      if (formData.exampleTranslation && formData.exampleTranslation.trim()) {
        submitData.exampleTranslation = formData.exampleTranslation.trim();
      }

      await vocabularyService.updateVocabulary(Number(id), submitData);
      alert('単語を更新しました！');
      router.push(`/vocabulary/${id}`);
    } catch (error: any) {
      console.error('単語更新エラー:', error);
      const errorMsg =
        error.response?.data?.msg || '単語の更新に失敗しました';
      alert(errorMsg);
    } finally {
      setSubmitting(false);
    }
  };

  if (!isLoggedIn || loading) {
    return (
      <MainLayout>
        <div className="py-20 text-center">
          <p className="text-gray-500 text-lg">読込中...</p>
        </div>
      </MainLayout>
    );
  }

  return (
    <MainLayout>
      {/* ヘッダーセクション */}
      <section className="bg-gradient-to-r from-[#a80000] to-[#d32f2f] py-12 text-white shadow-inner">
        <div className="container mx-auto px-4 max-w-4xl">
          <h2 className="text-4xl font-extrabold mb-2 drop-shadow-md">
            ✏️ 単語を編集
          </h2>
          <p className="text-lg opacity-90">
            単語の情報を更新しましょう
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
            </div>

            {/* 例文翻訳 */}
            <div className="mb-6">
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
            </div>

            {/* 学習状態 */}
            <div className="mb-8">
              <label
                htmlFor="studyStatus"
                className="block text-sm font-bold text-gray-700 mb-2"
              >
                学習状態 <span className="text-red-500">*</span>
              </label>
              <select
                id="studyStatus"
                name="studyStatus"
                value={formData.studyStatus}
                onChange={handleChange}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#a80000] focus:border-transparent"
                required
              >
                <option value="NOT_STUDIED">📝 学習前</option>
                <option value="STUDYING">📖 学習中</option>
                <option value="COMPLETED">✅ 学習完了</option>
              </select>
              <p className="text-xs text-gray-500 mt-1">
                現在の学習状態を選択してください
              </p>
            </div>

            {/* ボタン */}
            <div className="flex gap-4">
              <button
                type="submit"
                disabled={submitting}
                className="flex-1 bg-[#a80000] text-white py-4 rounded-lg font-bold hover:bg-[#8b0000] transition-all disabled:bg-gray-400 disabled:cursor-not-allowed cursor-pointer"
              >
                {submitting ? '更新中...' : '✅ 更新する'}
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