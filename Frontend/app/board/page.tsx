'use client';

import { useState, useEffect, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';

interface Post {
  id: number;
  title: string;
  content: string;
  author: string;
  createdAt: string;
  viewCount: number;
  likeCount: number;
}

export default function BoardPage() {
  const router = useRouter();
  const [posts, setPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [searchTerm, setSearchTerm] = useState('');
  const [sortBy, setSortBy] = useState<string>('createdAt');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');
  const postsPerPage = 10;

  const fetchPosts = useCallback(async () => {
    try {
      setLoading(true);
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/api/posts?page=${currentPage}&size=${postsPerPage}`,
        {
          credentials: 'include',
        }
      );
      
      if (response.ok) {
        const data = await response.json();
        setPosts(data.data.content || []);
        setTotalPages(data.data.totalPages || 1);
      }
    } catch (error) {
      console.error('投稿の取得に失敗しました:', error);
    } finally {
      setLoading(false);
    }
  }, [currentPage]);

  useEffect(() => {
    fetchPosts();
  }, [fetchPosts]);

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ja-JP', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    });
  };

  // 検索フィルタリング
  const filteredPosts = posts.filter(post => {
    const matchesSearch = post.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         post.author.toLowerCase().includes(searchTerm.toLowerCase());
    return matchesSearch;
  });

  // ソート処理
  const sortedPosts = [...filteredPosts].sort((a, b) => {
    let aValue: string | number | undefined;
    let bValue: string | number | undefined;
    
    if (sortBy === 'createdAt') {
      aValue = new Date(a.createdAt).getTime();
      bValue = new Date(b.createdAt).getTime();
    } else if (sortBy === 'viewCount' || sortBy === 'likeCount') {
      aValue = a[sortBy as keyof Post] as number;
      bValue = b[sortBy as keyof Post] as number;
    } else {
      aValue = a[sortBy as keyof Post] as string;
      bValue = b[sortBy as keyof Post] as string;
    }
  
    if (sortOrder === 'asc') {
      return aValue > bValue ? 1 : -1;
    } else {
      return aValue < bValue ? 1 : -1;
    }
  });

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 pb-10">
      <section className="px-6 py-8">
        <div className="max-w-7xl mx-auto">
          {/* ヘッダー */}
          <div className="flex items-center justify-between mb-8">
            <div>
              <h1 className="text-3xl font-bold text-[#1a365d] mb-2">自由掲示板</h1>
              <p className="text-gray-600">日本語で自由に交流しましょう！</p>
            </div>
            <Link
              href="/board/create"
              className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium shadow-lg"
            >
              ✍️ 新規投稿
            </Link>
          </div>

          {/* 投稿リストカード */}
          <div className="bg-white/95 backdrop-blur-sm rounded-2xl p-6 shadow-xl">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-bold text-[#1a365d]">投稿一覧</h3>
              <div className="flex items-center gap-4">
                <div className="text-sm text-gray-600">
                  総 {sortedPosts.length}件の投稿 (全体 {posts.length}件)
                </div>
                <button
                  onClick={fetchPosts}
                  disabled={loading}
                  className="px-3 py-1 cursor-pointer bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 text-sm flex items-center gap-1"
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                  </svg>
                  更新
                </button>
              </div>
            </div>

            {/* 検索バー */}
            <div className="mb-6">
              <div className="flex-1">
                <input
                  type="text"
                  placeholder="タイトルまたは作成者で検索..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full text-gray-900 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
            </div>

            {loading ? (
              <div className="text-center py-12">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
                <p className="text-gray-600 font-medium">投稿を読み込み中...</p>
                <p className="text-sm text-gray-500 mt-2">少々お待ちください</p>
              </div>
            ) : sortedPosts.length === 0 ? (
              <div className="text-center py-8">
                <div className="text-gray-400 text-6xl mb-4">
                  {posts.length === 0 ? '📝' : '🔍'}
                </div>
                <p className="text-gray-600">
                  {posts.length === 0 ? 'まだ投稿がありません' : '検索結果がありません'}
                </p>
                <p className="text-sm text-gray-500 mt-2">
                  {posts.length === 0 
                    ? '最初の投稿を作成してみましょう！'
                    : '別の検索キーワードを試してください'
                  }
                </p>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-gray-200">
                      <th className="text-left py-3 px-4 font-medium text-gray-700">No</th>
                      <th 
                        className="text-left py-3 px-4 font-medium text-gray-700 cursor-pointer hover:bg-gray-50"
                        onClick={() => {
                          if (sortBy === 'title') {
                            setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
                          } else {
                            setSortBy('title');
                            setSortOrder('asc');
                          }
                        }}
                      >
                        <div className="flex items-center gap-1">
                          タイトル
                          {sortBy === 'title' && (
                            <span className="text-xs">
                              {sortOrder === 'asc' ? '↑' : '↓'}
                            </span>
                          )}
                        </div>
                      </th>
                      <th 
                        className="text-left py-3 px-4 font-medium text-gray-700 cursor-pointer hover:bg-gray-50"
                        onClick={() => {
                          if (sortBy === 'author') {
                            setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
                          } else {
                            setSortBy('author');
                            setSortOrder('asc');
                          }
                        }}
                      >
                        <div className="flex items-center gap-1">
                          作成者
                          {sortBy === 'author' && (
                            <span className="text-xs">
                              {sortOrder === 'asc' ? '↑' : '↓'}
                            </span>
                          )}
                        </div>
                      </th>
                      <th 
                        className="text-center py-3 px-4 font-medium text-gray-700 cursor-pointer hover:bg-gray-50"
                        onClick={() => {
                          if (sortBy === 'viewCount') {
                            setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
                          } else {
                            setSortBy('viewCount');
                            setSortOrder('desc');
                          }
                        }}
                      >
                        <div className="flex items-center justify-center gap-1">
                          閲覧
                          {sortBy === 'viewCount' && (
                            <span className="text-xs">
                              {sortOrder === 'asc' ? '↑' : '↓'}
                            </span>
                          )}
                        </div>
                      </th>
                      <th 
                        className="text-center py-3 px-4 font-medium text-gray-700 cursor-pointer hover:bg-gray-50"
                        onClick={() => {
                          if (sortBy === 'likeCount') {
                            setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
                          } else {
                            setSortBy('likeCount');
                            setSortOrder('desc');
                          }
                        }}
                      >
                        <div className="flex items-center justify-center gap-1">
                          いいね
                          {sortBy === 'likeCount' && (
                            <span className="text-xs">
                              {sortOrder === 'asc' ? '↑' : '↓'}
                            </span>
                          )}
                        </div>
                      </th>
                      <th 
                        className="text-left py-3 px-4 font-medium text-gray-700 cursor-pointer hover:bg-gray-50"
                        onClick={() => {
                          if (sortBy === 'createdAt') {
                            setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
                          } else {
                            setSortBy('createdAt');
                            setSortOrder('desc');
                          }
                        }}
                      >
                        <div className="flex items-center gap-1">
                          作成日
                          {sortBy === 'createdAt' && (
                            <span className="text-xs">
                              {sortOrder === 'asc' ? '↑' : '↓'}
                            </span>
                          )}
                        </div>
                      </th>
                      <th className="text-center py-3 px-4 font-medium text-gray-700">詳細</th>
                    </tr>
                  </thead>
                  <tbody>
                    {sortedPosts.map((post, index) => (
                      <tr 
                        key={post.id} 
                        className="border-b border-gray-100 hover:bg-blue-50 cursor-pointer transition-colors duration-200"
                        onClick={() => router.push(`/board/${post.id}`)}
                      >
                        <td className="py-3 px-4 text-gray-500">
                          {(currentPage - 1) * postsPerPage + index + 1}
                        </td>
                        <td className="py-3 px-4 text-gray-900 font-medium">
                          {post.title}
                        </td>
                        <td className="py-3 px-4 text-gray-900">
                          {post.author}
                        </td>
                        <td className="py-3 px-4 text-center text-gray-500">
                          <span className="inline-flex items-center gap-1">
                            👁️ {post.viewCount}
                          </span>
                        </td>
                        <td className="py-3 px-4 text-center text-gray-500">
                          <span className="inline-flex items-center gap-1">
                            ❤️ {post.likeCount}
                          </span>
                        </td>
                        <td className="py-3 px-4 text-gray-500">
                          {formatDate(post.createdAt)}
                        </td>
                        <td className="py-3 px-4 text-center">
                          <button 
                            onClick={(e) => {
                              e.stopPropagation();
                              router.push(`/board/${post.id}`);
                            }}
                            className="text-blue-600 cursor-pointer hover:text-blue-700 text-xs font-medium bg-blue-50 hover:bg-blue-100 px-2 py-1 rounded transition-colors"
                          >
                            詳細表示
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          {/* ページネーション */}
          {!loading && totalPages > 1 && (
            <div className="mt-6 flex justify-center">
              <nav className="flex space-x-2">
                <button
                  onClick={() => handlePageChange(currentPage - 1)}
                  disabled={currentPage === 1}
                  className="px-4 py-2 border border-gray-300 rounded-lg bg-white hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                >
                  前へ
                </button>
                
                {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
                  <button
                    key={page}
                    onClick={() => handlePageChange(page)}
                    className={`px-4 py-2 border rounded-lg transition-colors ${
                      currentPage === page
                        ? 'bg-blue-600 text-white border-blue-600'
                        : 'bg-white border-gray-300 hover:bg-gray-100'
                    }`}
                  >
                    {page}
                  </button>
                ))}
                
                <button
                  onClick={() => handlePageChange(currentPage + 1)}
                  disabled={currentPage === totalPages}
                  className="px-4 py-2 border border-gray-300 rounded-lg bg-white hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                >
                  次へ
                </button>
              </nav>
            </div>
          )}
        </div>
      </section>
    </div>
  );
}