'use client';

import { useState, useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import MainLayout from '@/components/MainLayout';
import { vocabularyService } from '@/lib/api/vocabularyService';
import { VocabularyListResponse, StudyStatus } from '@/types/vocabulary';

export default function VocabularyPage() {
  const router = useRouter();
  const hasCheckedAuth = useRef(false);

  const [vocabularies, setVocabularies] = useState<VocabularyListResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<'all' | StudyStatus>('all');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [checkingAuth, setCheckingAuth] = useState(true);

  useEffect(() => {
    if (!hasCheckedAuth.current) {
      hasCheckedAuth.current = true;
      checkLoginStatus();
    }
  }, []);

  useEffect(() => {
    if (isLoggedIn) {
      fetchVocabularies();
    }
  }, [filter, isLoggedIn]);

  const checkLoginStatus = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/auth/me', {
        credentials: 'include',
      });

      if (response.ok) {
        setIsLoggedIn(true);
      } else {
        alert('単語帳を利用するにはログインが必要です');
        router.push('/login');
      }
    } catch (error) {
      console.error('認証確認エラー:', error);
      alert('単語帳を利用するにはログインが必要です');
      router.push('/login');
    } finally {
      setCheckingAuth(false);
    }
  };


  if (checkingAuth) {
    return null;
  }

  if (!isLoggedIn) {
    return null;
  }

  const fetchVocabularies = async () => {
    setLoading(true);
    try {
      let data: VocabularyListResponse[];
      if (filter === 'all') {
        data = await vocabularyService.getMyVocabularies();
      } else {
        data = await vocabularyService.getVocabulariesByStatus(filter);
      }
      setVocabularies(data);
    } catch (error) {
      console.error('単語の読込に失敗:', error);
      alert('単語の読込に失敗しました');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchKeyword.trim()) {
      fetchVocabularies();
      return;
    }

    setLoading(true);
    try {
      const data = await vocabularyService.searchVocabularies(searchKeyword);
      setVocabularies(data);
    } catch (error) {
      console.error('検索に失敗:', error);
      alert('検索に失敗しました');
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status: StudyStatus) => {
    switch (status) {
      case 'NOT_STUDIED':
        return 'bg-gray-100 text-gray-700';
      case 'STUDYING':
        return 'bg-yellow-100 text-yellow-700';
      case 'COMPLETED':
        return 'bg-green-100 text-green-700';
      default:
        return 'bg-gray-100 text-gray-700';
    }
  };

  const getStatusEmoji = (status: StudyStatus) => {
    switch (status) {
      case 'NOT_STUDIED':
        return '📝';
      case 'STUDYING':
        return '📖';
      case 'COMPLETED':
        return '✅';
      default:
        return '📝';
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

  if (!isLoggedIn) {
    return null;
  }

  return (
    <MainLayout>
      {/* ヘッダーセクション */}
      <section className="bg-gradient-to-r from-[#a80000] to-[#d32f2f] py-12 text-white shadow-inner">
        <div className="container mx-auto px-4 max-w-6xl">
          <div className="flex flex-col md:flex-row justify-between items-center gap-6">
            <div>
              <h2 className="text-4xl font-extrabold mb-2 drop-shadow-md">
                📚 マイ単語帳
              </h2>
              <p className="text-lg opacity-90">
                日本語の単語を登録して効率的に学習しましょう
              </p>
            </div>
            <Link
              href="/vocabulary/add"
              className="bg-white text-[#a80000] px-7 py-3 rounded-full font-bold shadow-md hover:shadow-lg hover:-translate-y-1 transition-all"
            >
              ➕ 新しい単語を追加
            </Link>
          </div>
        </div>
      </section>

      {/* 検索バーセクション */}
      <section className="bg-white border-b border-gray-200 py-6">
        <div className="container mx-auto px-4 max-w-6xl">
          <form onSubmit={handleSearch} className="flex gap-3">
            <input
              type="text"
              placeholder="単語を検索..."
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              className="flex-1 px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#a80000] focus:border-transparent"
            />
            <button
              type="submit"
              className="bg-[#a80000] text-white px-6 py-3 rounded-lg font-bold hover:bg-[#8b0000] transition-all cursor-pointer"
            >
              🔍 検索
            </button>
            {searchKeyword && (
              <button
                type="button"
                onClick={() => {
                  setSearchKeyword('');
                  fetchVocabularies();
                }}
                className="bg-gray-500 text-white px-6 py-3 rounded-lg font-bold hover:bg-gray-600 transition-all cursor-pointer"
              >
                ✖ クリア
              </button>
            )}
          </form>
        </div>
      </section>

      {/* フィルタータブセクション */}
      <section className="bg-white border-b border-gray-200 py-4 sticky top-[0px] z-10">
        <div className="container mx-auto px-4 max-w-6xl">
          <div className="flex gap-4 overflow-x-auto">
            {[
              { value: 'all', label: '全て' },
              { value: 'NOT_STUDIED', label: '学習前' },
              { value: 'STUDYING', label: '学習中' },
              { value: 'COMPLETED', label: '学習完了' },
            ].map((item) => (
              <button
                key={item.value}
                onClick={() => setFilter(item.value as 'all' | StudyStatus)}
                className={`px-5 py-2 rounded-full font-bold transition-all whitespace-nowrap ${filter === item.value
                  ? 'bg-[#a80000] text-white shadow-md'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                  }`}
              >
                {item.label}
                {filter === item.value && (
                  <span className="ml-2">
                    ({vocabularies.length})
                  </span>
                )}
              </button>
            ))}
          </div>
        </div>
      </section>

      {/* 単語リストセクション */}
      <section className="py-12 bg-gray-50 min-h-[calc(100vh-400px)]">
        <div className="container mx-auto px-4 max-w-6xl">
          {loading ? (
            <div className="text-center py-20">
              <p className="text-gray-500 text-lg">読込中...</p>
            </div>
          ) : vocabularies.length === 0 ? (
            <div className="text-center py-20">
              <p className="text-gray-500 text-lg mb-4">
                {searchKeyword
                  ? '検索結果がありません'
                  : 'まだ単語がありません'}
              </p>
              <Link
                href="/vocabulary/add"
                className="text-[#a80000] font-bold hover:underline"
              >
                最初の単語を追加してみましょう！
              </Link>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {vocabularies.map((vocab) => (
                <Link
                  key={vocab.id}
                  href={`/vocabulary/${vocab.id}`}
                  className="block bg-white rounded-lg shadow-sm hover:shadow-md transition-all p-5 border border-gray-200 hover:border-[#a80000]"
                >
                  {/* 学習状態バッジ */}
                  <div className="flex justify-between items-start mb-3">
                    <span
                      className={`text-xs px-3 py-1 rounded-full font-bold ${getStatusColor(
                        vocab.studyStatus
                      )}`}
                    >
                      {getStatusEmoji(vocab.studyStatus)}{' '}
                      {vocab.studyStatusDisplay}
                    </span>
                  </div>

                  {/* 単語 */}
                  <h3 className="text-2xl font-bold text-gray-900 mb-1">
                    {vocab.word}
                  </h3>

                  {/* ひらがな */}
                  <p className="text-lg text-[#a80000] mb-2 font-medium">
                    {vocab.hiragana}
                  </p>

                  {/* 意味 */}
                  <p className="text-gray-700 mb-3 line-clamp-2">
                    {vocab.meaning}
                  </p>

                  {/* 登録日 */}
                  <div className="text-xs text-gray-500 mt-auto pt-3 border-t border-gray-100">
                    📅 {formatDate(vocab.createDate)}
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