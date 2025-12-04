// Frontend/app/quiz/n5/page.tsx
'use client';

import { useState, useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import MainLayout from '@/components/MainLayout';
import { quizService } from '@/lib/api/quizService';
import { QuizQuestion } from '@/types/quiz';

export default function N5QuizPage() {
  const router = useRouter();
  const hasCheckedAuth = useRef(false);
  const hasFetchedQuiz = useRef(false);

  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [checkingAuth, setCheckingAuth] = useState(true);
  const [loading, setLoading] = useState(true);
  const [quizzes, setQuizzes] = useState<QuizQuestion[]>([]);
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [selectedAnswer, setSelectedAnswer] = useState<string | null>(null);
  const [isAnswered, setIsAnswered] = useState(false);
  const [score, setScore] = useState(0);
  const [showResult, setShowResult] = useState(false);

  useEffect(() => {
    if (!hasCheckedAuth.current) {
      hasCheckedAuth.current = true;
      checkLoginStatus();
    }
  }, []);

  useEffect(() => {
    if (isLoggedIn && !hasFetchedQuiz.current) {
      hasFetchedQuiz.current = true;
      fetchQuizzes();
    }
  }, [isLoggedIn]);

  const checkLoginStatus = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/auth/me', {
        credentials: 'include',
      });

      if (response.ok) {
        setIsLoggedIn(true);
      } else {
        alert('問題演習を利用するにはログインが必要です');
        router.push('/login');
      }
    } catch (error) {
      console.error('認証確認エラー:', error);
      alert('問題演習を利用するにはログインが必要です');
      router.push('/login');
    } finally {
      setCheckingAuth(false);
    }
  };

  const fetchQuizzes = async () => {
    setLoading(true);
    try {
      const data = await quizService.getQuizByLevel('N5', 10);
      console.log('Quiz data loaded:', data); // 디버깅용
      setQuizzes(data);
    } catch (error: any) {
      console.error('クイズ読込エラー:', error);
      if (error.response?.status === 401) {
        alert('ログインが必要です');
        router.push('/login');
      } else {
        alert('クイズの読込に失敗しました');
      }
    } finally {
      setLoading(false);
    }
  };

  // ✅ 중요: currentQuestion을 안전하게 가져오기
  const currentQuestion = quizzes[currentQuestionIndex] || null;
  const isCorrect = currentQuestion ? selectedAnswer === currentQuestion.correctAnswer : false;

  const handleAnswer = (choice: string) => {
    if (isAnswered || !currentQuestion) return;

    setSelectedAnswer(choice);
    setIsAnswered(true);

    if (choice === currentQuestion.correctAnswer) {
      setScore(score + 1);
    }
  };

  const handleNext = () => {
    if (currentQuestionIndex < quizzes.length - 1) {
      setCurrentQuestionIndex(currentQuestionIndex + 1);
      setSelectedAnswer(null);
      setIsAnswered(false);
    } else {
      setShowResult(true);
    }
  };

  const handleRestart = () => {
    setCurrentQuestionIndex(0);
    setSelectedAnswer(null);
    setIsAnswered(false);
    setScore(0);
    setShowResult(false);
    hasFetchedQuiz.current = false;
    fetchQuizzes();
  };

  // 로딩 중
  if (checkingAuth || loading) {
    return (
      <MainLayout>
        <section className="min-h-screen bg-gradient-to-br from-green-50 to-blue-50 flex items-center justify-center">
          <div className="text-center">
            <div className="text-4xl mb-4">📚</div>
            <p className="text-gray-500 text-lg">読込中...</p>
          </div>
        </section>
      </MainLayout>
    );
  }

  // ✅ 퀴즈 데이터 없음 체크 추가
  if (!loading && quizzes.length === 0) {
    return (
      <MainLayout>
        <section className="min-h-screen bg-gradient-to-br from-green-50 to-blue-50 flex items-center justify-center">
          <div className="text-center">
            <div className="text-4xl mb-4">😢</div>
            <p className="text-gray-700 text-lg mb-4">クイズデータがありません</p>
            <Link
              href="/quiz"
              className="bg-green-500 hover:bg-green-600 text-white px-6 py-3 rounded-xl font-bold transition-all inline-block"
            >
              レベル選択に戻る
            </Link>
          </div>
        </section>
      </MainLayout>
    );
  }

  // ✅ currentQuestion이 null인 경우 체크
  if (!currentQuestion && !showResult) {
    return (
      <MainLayout>
        <section className="min-h-screen bg-gradient-to-br from-green-50 to-blue-50 flex items-center justify-center">
          <div className="text-center">
            <div className="text-4xl mb-4">⚠️</div>
            <p className="text-gray-700 text-lg mb-4">問題を読み込めませんでした</p>
            <button
              onClick={() => router.push('/quiz')}
              className="bg-green-500 hover:bg-green-600 text-white px-6 py-3 rounded-xl font-bold transition-all"
            >
              レベル選択に戻る
            </button>
          </div>
        </section>
      </MainLayout>
    );
  }

  // 결과 화면
  if (showResult) {
    const percentage = (score / quizzes.length) * 100;
    return (
      <MainLayout>
        <section className="min-h-screen bg-gradient-to-br from-green-50 to-blue-50 py-16">
          <div className="container mx-auto px-4 max-w-2xl">
            <div className="bg-white rounded-2xl shadow-xl p-8 text-center">
              <div className="mb-8">
                <div className="text-6xl mb-4">
                  {percentage >= 80 ? '🎉' : percentage >= 60 ? '😊' : '💪'}
                </div>
                <h2 className="text-3xl font-bold text-gray-900 mb-2">
                  お疲れ様でした！
                </h2>
                <p className="text-gray-600">JLPT N5 問題演習</p>
              </div>

              <div className="bg-green-50 rounded-xl p-8 mb-8">
                <div className="text-5xl font-bold text-green-600 mb-2">
                  {score} / {quizzes.length}
                </div>
                <div className="text-xl text-gray-700">
                  正解率: {percentage.toFixed(0)}%
                </div>
              </div>

              <div className="space-y-3 mb-8">
                {percentage >= 80 && (
                  <p className="text-lg text-gray-700">
                    素晴らしい！N5レベルをよく理解しています！🌟
                  </p>
                )}
                {percentage >= 60 && percentage < 80 && (
                  <p className="text-lg text-gray-700">
                    良い結果です！もう少し練習すれば完璧です！👍
                  </p>
                )}
                {percentage < 60 && (
                  <p className="text-lg text-gray-700">
                    もう一度チャレンジしてみましょう！💪
                  </p>
                )}
              </div>

              <div className="flex gap-4">
                <button
                  onClick={handleRestart}
                  className="flex-1 bg-green-500 hover:bg-green-600 text-white font-bold py-4 rounded-xl transition-all cursor-pointer"
                >
                  もう一度挑戦
                </button>
                <Link
                  href="/quiz"
                  className="flex-1 bg-gray-500 hover:bg-gray-600 text-white font-bold py-4 rounded-xl transition-all text-center leading-[3rem]"
                >
                  レベル選択に戻る
                </Link>
              </div>
            </div>
          </div>
        </section>
      </MainLayout>
    );
  }

  // 퀴즈 화면
  return (
    <MainLayout>
      <section className="min-h-screen bg-gradient-to-br from-green-50 to-blue-50 py-8">
        <div className="container mx-auto px-4 max-w-3xl">
          {/* 헤더 */}
          <div className="mb-6">
            <Link
              href="/quiz"
              className="inline-block text-gray-600 hover:text-gray-900 mb-4"
            >
              ← レベル選択に戻る
            </Link>
            <div className="flex items-center justify-between">
              <h1 className="text-2xl font-bold text-gray-900">
                JLPT N5 問題演習
              </h1>
              <div className="bg-green-500 text-white px-4 py-2 rounded-full font-bold">
                {currentQuestionIndex + 1} / {quizzes.length}
              </div>
            </div>
          </div>

          {/* 진행바 */}
          <div className="bg-gray-200 rounded-full h-3 mb-8">
            <div
              className="bg-green-500 h-3 rounded-full transition-all duration-300"
              style={{
                width: `${((currentQuestionIndex + 1) / quizzes.length) * 100}%`,
              }}
            />
          </div>

          {/* 문제 카드 */}
          <div className="bg-white rounded-2xl shadow-xl p-8 mb-6">
            {/* 문제 타입 */}
            <p className="text-sm text-gray-500 mb-4 text-center">
              {currentQuestion.questionType}
            </p>

            {/* 문제 */}
            <div className="bg-gradient-to-br from-green-50 to-blue-50 rounded-xl p-8 mb-8">
              <p className="text-5xl font-bold text-center text-gray-900">
                {currentQuestion.question}
              </p>
            </div>

            {/* 선택지 */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
              {currentQuestion.choices.map((choice, index) => {
                let buttonClass =
                  'p-6 text-xl rounded-xl border-2 font-bold transition-all cursor-pointer ';

                if (!isAnswered) {
                  buttonClass +=
                    'border-gray-300 hover:border-green-500 hover:bg-green-50 hover:scale-105';
                } else if (choice === currentQuestion.correctAnswer) {
                  buttonClass +=
                    'border-green-500 bg-green-100 text-green-700 scale-105';
                } else if (choice === selectedAnswer) {
                  buttonClass +=
                    'border-red-500 bg-red-100 text-red-700';
                } else {
                  buttonClass += 'border-gray-200 bg-gray-50 text-gray-400';
                }

                return (
                  <button
                    key={index}
                    onClick={() => handleAnswer(choice)}
                    disabled={isAnswered}
                    className={buttonClass}
                  >
                    {choice}
                    {isAnswered && choice === currentQuestion.correctAnswer && (
                      <span className="ml-2">✅</span>
                    )}
                    {isAnswered &&
                      choice === selectedAnswer &&
                      choice !== currentQuestion.correctAnswer && (
                        <span className="ml-2">❌</span>
                      )}
                  </button>
                );
              })}
            </div>

            {/* 설명 (답변 후) */}
            {isAnswered && (
              <div
                className={`p-6 rounded-xl mb-6 animate-[slideDown_0.3s_ease-out] ${
                  isCorrect ? 'bg-green-50 border-2 border-green-200' : 'bg-red-50 border-2 border-red-200'
                }`}
              >
                <p
                  className={`font-bold text-lg mb-2 ${
                    isCorrect ? 'text-green-700' : 'text-red-700'
                  }`}
                >
                  {isCorrect ? '🎉 正解です！' : '❌ 不正解です'}
                </p>
                <p className="text-gray-700">{currentQuestion.explanation}</p>
              </div>
            )}

            {/* 다음 버튼 */}
            {isAnswered && (
              <button
                onClick={handleNext}
                className="w-full bg-green-500 hover:bg-green-600 text-white font-bold py-4 rounded-xl transition-all cursor-pointer animate-[slideUp_0.3s_ease-out]"
              >
                {currentQuestionIndex < quizzes.length - 1
                  ? '次の問題へ →'
                  : '結果を見る 🎯'}
              </button>
            )}
          </div>

          {/* 현재 점수 */}
          <div className="text-center text-gray-600">
            現在のスコア: {score} / {currentQuestionIndex + (isAnswered ? 1 : 0)}
          </div>
        </div>
      </section>
    </MainLayout>
  );
}