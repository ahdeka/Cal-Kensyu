'use client';

import { useState, useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Link from 'next/link';
import MainLayout from '@/components/MainLayout';
import { vocabularyService } from '@/lib/api/vocabularyService';
import { VocabularyResponse, StudyStatus } from '@/types/vocabulary';

export default function VocabularyDetailPage() {
    const router = useRouter();
    const params = useParams();
    const id = params.id as string;

    const [showStatusOptions, setShowStatusOptions] = useState(false);
    const [vocabulary, setVocabulary] = useState<VocabularyResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);

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
                alert('単語を閲覧するにはログインが必要です');
                router.push('/login');
            }
        } catch (error) {
            alert('単語を閲覧するにはログインが必要です');
            router.push('/login');
        }
    };

    const fetchVocabulary = async () => {
        setLoading(true);
        try {
            const data = await vocabularyService.getVocabulary(Number(id));
            setVocabulary(data);
        } catch (error) {
            console.error('単語の読込に失敗:', error);
            alert('単語の読込に失敗しました');
            router.push('/vocabulary');
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async () => {
        try {
            await vocabularyService.deleteVocabulary(Number(id));
            alert('単語を削除しました');
            router.push('/vocabulary');
        } catch (error) {
            console.error('削除エラー:', error);
            alert('単語の削除に失敗しました');
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
            hour: '2-digit',
            minute: '2-digit',
        });
    };

    const handleQuickStatusChange = async (newStatus: StudyStatus) => {
        if (!vocabulary) return;

        try {
            const updated = await vocabularyService.updateStudyStatus(
                vocabulary.id,
                newStatus
            );
            setVocabulary(updated);
            setShowStatusOptions(false);
            alert('学習状態を変更しました！');
        } catch (error) {
            console.error('状態変更エラー:', error);
            alert('学習状態の変更に失敗しました');
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

    if (!vocabulary) {
        return null;
    }

    return (
        <MainLayout>
            {/* ヘッダーセクション */}
            <section className="bg-gradient-to-r from-[#a80000] to-[#d32f2f] py-12 text-white shadow-inner">
                <div className="container mx-auto px-4 max-w-4xl">
                    <Link
                        href="/vocabulary"
                        className="inline-block mb-4 text-white hover:underline"
                    >
                        ← 単語帳に戻る
                    </Link>
                    <h2 className="text-4xl font-extrabold mb-2 drop-shadow-md">
                        📖 単語の詳細
                    </h2>
                </div>
            </section>

            {/* 詳細セクション */}
            <section className="py-12 bg-gray-50 min-h-[calc(100vh-280px)]">
                <div className="container mx-auto px-4 max-w-4xl">
                    <div className="bg-white rounded-lg shadow-md p-8 border border-gray-200">
                        {/* 学習状態バッジ */}
                        <div className="mb-6 overflow-visible">
                            <div className="flex items-center gap-4 mb-3">
                                <button
                                    onClick={() => setShowStatusOptions(!showStatusOptions)}
                                    className={`inline-block text-sm px-4 py-2 rounded-full font-bold transition-all hover:shadow-md ${getStatusColor(
                                        vocabulary.studyStatus
                                    )}`}
                                >
                                    {getStatusEmoji(vocabulary.studyStatus)}{' '}
                                    {vocabulary.studyStatusDisplay}
                                    <span className="ml-2">▼</span>
                                </button>
                            </div>

                            {/* 빠른 상태 변경 버튼 */}
                            {showStatusOptions && (
                                <div className="flex gap-2 animate-[slideDown_0.3s_ease-out] py-1">
                                    <p className="text-sm text-gray-600 mr-2 self-center whitespace-nowrap">
                                        変更:
                                    </p>
                                    {(['NOT_STUDIED', 'STUDYING', 'COMPLETED'] as StudyStatus[]).map(
                                        (status, index) => (
                                            <button
                                                key={status}
                                                onClick={() => handleQuickStatusChange(status)}
                                                disabled={vocabulary.studyStatus === status}
                                                style={{ animationDelay: `${index * 0.1}s` }}
                                                className={`text-sm px-4 py-2 rounded-full font-bold transition-all animate-[slideIn_0.3s_ease-out] ${vocabulary.studyStatus === status
                                                    ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                                                    : 'bg-white border-2 border-gray-300 text-gray-700 hover:border-[#a80000] hover:bg-[#a80000] hover:text-white hover:shadow-md hover:-translate-y-1'
                                                    }`}
                                            >
                                                {status === 'NOT_STUDIED' && '📝 学習前'}
                                                {status === 'STUDYING' && '📖 学習中'}
                                                {status === 'COMPLETED' && '✅ 学習完了'}
                                            </button>
                                        )
                                    )}
                                </div>
                            )}
                        </div>

                        {/* 単語 */}
                        <div className="mb-8">
                            <h3 className="text-sm font-bold text-gray-500 mb-2">単語</h3>
                            <p className="text-5xl font-bold text-gray-900">
                                {vocabulary.word}
                            </p>
                        </div>

                        {/* ひらがな */}
                        <div className="mb-8">
                            <h3 className="text-sm font-bold text-gray-500 mb-2">
                                ひらがな（読み方）
                            </h3>
                            <p className="text-3xl font-bold text-[#a80000]">
                                {vocabulary.hiragana}
                            </p>
                        </div>

                        {/* 意味 */}
                        <div className="mb-8">
                            <h3 className="text-sm font-bold text-gray-500 mb-2">意味</h3>
                            <p className="text-2xl text-gray-800">{vocabulary.meaning}</p>
                        </div>

                        {/* 例文 */}
                        {vocabulary.exampleSentence && (
                            <div className="mb-8 bg-blue-50 p-6 rounded-lg border border-blue-200">
                                <h3 className="text-sm font-bold text-blue-700 mb-3">
                                    📝 例文
                                </h3>
                                <p className="text-xl text-gray-900 mb-4 leading-relaxed">
                                    {vocabulary.exampleSentence}
                                </p>
                                {vocabulary.exampleTranslation && (
                                    <p className="text-lg text-gray-600 leading-relaxed">
                                        {vocabulary.exampleTranslation}
                                    </p>
                                )}
                            </div>
                        )}

                        {/* 日付情報 */}
                        <div className="mb-8 pt-6 border-t border-gray-200">
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm text-gray-600">
                                <div>
                                    <span className="font-bold">登録日:</span>{' '}
                                    {formatDate(vocabulary.createDate)}
                                </div>
                                <div>
                                    <span className="font-bold">最終更新:</span>{' '}
                                    {formatDate(vocabulary.updateDate)}
                                </div>
                            </div>
                        </div>

                        {/* ボタン */}
                        <div className="flex gap-4">
                            <Link
                                href={`/vocabulary/${id}/edit`}
                                className="flex-1 bg-gray-600 text-white py-4 rounded-lg font-bold hover:bg-gray-700 transition-all text-center shadow-md hover:shadow-lg"
                            >
                                ✏️ 編集
                            </Link>
                            <button
                                onClick={() => setShowDeleteModal(true)}
                                className="flex-1 bg-[#a80000] text-white py-4 rounded-lg font-bold hover:bg-[#8b0000] transition-all shadow-md hover:shadow-lg cursor-pointer"
                            >
                                🗑️ 削除
                            </button>
                        </div>
                    </div>
                </div>
            </section>

            {/* 削除確認モーダル */}
            {showDeleteModal && (
                <div className="fixed inset-0 flex items-center justify-center z-50 px-4">
                    <div className="bg-white rounded-lg shadow-xl p-8 max-w-md w-full border-2 border-gray-200">
                        <h3 className="text-2xl font-bold mb-4 text-gray-900">
                            単語を削除しますか？
                        </h3>
                        <p className="text-gray-600 mb-6">
                            「{vocabulary.word}」を削除します。
                            <br />
                            この操作は取り消せません。
                        </p>
                        <div className="flex gap-4">
                            <button
                                onClick={handleDelete}
                                className="flex-1 bg-[#a80000] text-white py-3 rounded-lg font-bold hover:bg-[#8b0000] transition-all"
                            >
                                削除する
                            </button>
                            <button
                                onClick={() => setShowDeleteModal(false)}
                                className="flex-1 bg-gray-700 text-white py-3 rounded-lg font-bold hover:bg-gray-800 transition-all"
                            >
                                キャンセル
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </MainLayout>
    );
}