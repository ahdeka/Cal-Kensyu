'use client';

import { useState, useEffect, useRef } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Link from 'next/link';
import MainLayout from '@/components/MainLayout';
import { quizService } from '@/lib/api/quizService';
import { QuizQuestion, JlptLevel } from '@/types/quiz';

// 레벨별 색상 설정
const LEVEL_COLORS = {
  N5: {
    primary: 'green',
    gradient: 'from-green-50 to-blue-50',
    button: 'bg-green-500 hover:bg-green-600',
    progress: 'bg-green-500',
    badge: 'bg-green-500',
  },
  N4: {
    primary: 'blue',
    gradient: 'from-blue-50 to-indigo-50',
    button: 'bg-blue-500 hover:bg-blue-600',
    progress: 'bg-blue-500',
    badge: 'bg-blue-500',
  },
  N3: {
    primary: 'yellow',
    gradient: 'from-yellow-50 to-orange-50',
    button: 'bg-yellow-500 hover:bg-yellow-600',
    progress: 'bg-yellow-500',
    badge: 'bg-yellow-500',
  },
  N2: {
    primary: 'orange',
    gradient: 'from-orange-50 to-red-50',
    button: 'bg-orange-500 hover:bg-orange-600',
    progress: 'bg-orange-500',
    badge: 'bg-orange-500',
  },
  N1: {
    primary: 'red',
    gradient: 'from-red-50 to-pink-50',
    button: 'bg-red-500 hover:bg-red-600',
    progress: 'bg-red-500',
    badge: 'bg-red-500',
  },
};

export default function QuizLevelPage() {
  const router = useRouter();
  const params = useParams();
  const level = (params.level as string)?.toUpperCase() as JlptLevel;
  const hasCheckedAuth = useRef(false);
  const hasFetchedQuiz = useRef(false);

  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [checkingAuth, setCheckingAuth] = useState(true);
  const [loading, setLoading] = useState(false);
  const [showQuestionCountSelect, setShowQuestionCountSelect] = useState(true);
  const [selectedQuestionCount, setSelectedQuestionCount] = useState(10);
  const [quizzes, setQuizzes] = useState<QuizQuestion[]>([]);
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [selectedAnswer, setSelectedAnswer] = useState<string | null>(null);
  const [isAnswered, setIsAnswered] = useState(false);
  const [score, setScore] = useState(0);
  const [showResult, setShowResult] = useState(false);

  // 유효한 레벨인지 확인
  const isValidLevel = ['N5', 'N4', 'N3', 'N2', 'N1'].includes(level);
  const colors = isValidLevel ? LEVEL_COLORS[level] : LEVEL_COLORS.N5;

  useEffect(() => {
    if (!hasCheckedAuth.current) {
      hasCheckedAuth.current = true;
      checkLoginStatus();
    }
  }, []);

  useEffect(() => {
    if (!isValidLevel && !checkingAuth) {
      alert('無効なレベルです');
      router.push('/quiz');
    }
  }, [isValidLevel, checkingAuth, router]);

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

  const fetchQuizzes = async (count: number) => {
    setLoading(true);
    try {
      const data = await quizService.getQuizByLevel(level, count);
      console.log('Quiz data loaded:', data);
      setQuizzes(data);
      setShowQuestionCountSelect(false);
    } catch (error: any) {
      console.error('クイズ読込エラー:', error);
      if (error.response?.status === 401) {
        alert('ログインが必要です');
        router.push('/login');
      } else {
        alert(error.response?.data?.msg || 'クイズの読込に失敗しました');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleStartQuiz = () => {
    fetchQuizzes(selectedQuestionCount);
  };

  const currentQuestion = quizzes[currentQuestionIndex] || null;
  const isCorrect = currentQuestion
    ? selectedAnswer === currentQuestion.correctAnswer
    : false;

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
    setShowQuestionCountSelect(true);
    setQuizzes([]);
  };

  // 로딩 중
  if (checkingAuth || loading) {
    return (
      <MainLayout>
        <section className={`min-h-screen bg-gradient-to-br ${colors.gradient} flex items-center justify-center`}>
          <div className="text-center">
            <div className="text-4xl mb-4">📚</div>
            <p className="text-gray-500 text-lg">読込中...</p>
          </div>
        </section>
      </MainLayout>
    );
  }

  // 문제 개수 선택 화면
  if (showQuestionCountSelect) {
    return (
      <MainLayout>
        <section className={`min-h-screen bg-gradient-to-br ${colors.gradient} py-16`}>
          <div className="container mx-auto px-4 max-w-2xl">
            <Link
              href="/quiz"
              className="inline-block text-gray-600 hover:text-gray-900 mb-6"
            >
              ← レベル選択に戻る
            </Link>

            <div className="bg-white rounded-2xl shadow-xl p-8">
              <div className="text-center mb-8">
                <h1 className="text-3xl font-bold text-gray-900 mb-2">
                  JLPT {level} 問題演習
                </h1>
                <p className="text-gray-600">問題数を選択してください</p>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
                {[10, 20, 30].map((count) => (
                  <button
                    key={count}
                    onClick={() => setSelectedQuestionCount(count)}
                    className={`p-6 rounded-xl border-2 transition-all ${
                      selectedQuestionCount === count
                        ? `${colors.button} text-white border-transparent`
                        : 'border-gray-300 hover:border-gray-400'
                    }`}
                  >
                    <div className="text-3xl font-bold mb-2">{count}問</div>
                    <div className="text-sm">
                      {count === 10 && '約5分'}
                      {count === 20 && '約10分'}
                      {count === 30 && '約15分'}
                    </div>
                  </button>
                ))}
              </div>

              <button
                onClick={handleStartQuiz}
                className={`w-full ${colors.button} text-white font-bold py-4 rounded-xl transition-all`}
              >
                問題演習を始める
              </button>
            </div>
          </div>
        </section>
      </MainLayout>
    );
  }

  // 퀴즈 데이터 없음
  if (!loading && quizzes.length === 0) {
    return (
      <MainLayout>
        <section className={`min-h-screen bg-gradient-to-br ${colors.gradient} flex items-center justify-center`}>
          <div className="text-center">
            <div className="text-4xl mb-4">😢</div>
            <p className="text-gray-700 text-lg mb-4">クイズデータがありません</p>
            <Link
              href="/quiz"
              className={`${colors.button} text-white px-6 py-3 rounded-xl font-bold transition-all inline-block`}
            >
              レベル選択に戻る
            </Link>
          </div>
        </section>
      </MainLayout>
    );
  }

  // currentQuestion이 null인 경우
  if (!currentQuestion && !showResult) {
    return (
      <MainLayout>
        <section className={`min-h-screen bg-gradient-to-br ${colors.gradient} flex items-center justify-center`}>
          <div className="text-center">
            <div className="text-4xl mb-4">⚠️</div>
            <p className="text-gray-700 text-lg mb-4">問題を読み込めませんでした</p>
            <button
              onClick={() => router.push('/quiz')}
              className={`${colors.button} text-white px-6 py-3 rounded-xl font-bold transition-all`}
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
        <section className={`min-h-screen bg-gradient-to-br ${colors.gradient} py-16`}>
          <div className="container mx-auto px-4 max-w-2xl">
            <div className="bg-white rounded-2xl shadow-xl p-8 text-center">
              <div className="mb-8">
                <div className="text-6xl mb-4">
                  {percentage >= 80 ? '🎉' : percentage >= 60 ? '😊' : '💪'}
                </div>
                <h2 className="text-3xl font-bold text-gray-900 mb-2">
                  お疲れ様でした！
                </h2>
                <p className="text-gray-600">JLPT {level} 問題演習</p>
              </div>

              <div className={`${colors.gradient} rounded-xl p-8 mb-8`}>
                <div className={`text-5xl font-bold text-${colors.primary}-600 mb-2`}>
                  {score} / {quizzes.length}
                </div>
                <div className="text-xl text-gray-700">
                  正解率: {percentage.toFixed(0)}%
                </div>
              </div>

              <div className="space-y-3 mb-8">
                {percentage >= 80 && (
                  <p className="text-lg text-gray-700">
                    素晴らしい！{level}レベルをよく理解しています！🌟
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
                  className={`flex-1 ${colors.button} text-white font-bold py-4 rounded-xl transition-all cursor-pointer`}
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
      <section className={`min-h-screen bg-gradient-to-br ${colors.gradient} py-8`}>
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
                JLPT {level} 問題演習
              </h1>
              <div className={`${colors.badge} text-white px-4 py-2 rounded-full font-bold`}>
                {currentQuestionIndex + 1} / {quizzes.length}
              </div>
            </div>
          </div>

          {/* 진행바 */}
          <div className="bg-gray-200 rounded-full h-3 mb-8">
            <div
              className={`${colors.progress} h-3 rounded-full transition-all duration-300`}
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
            <div className={`bg-gradient-to-br ${colors.gradient} rounded-xl p-8 mb-8`}>
              <p className="text-5xl font-bold text-center text-gray-900">
                {currentQuestion.question}
              </p>
            </div>

            {/* 선택지 */}
            <div className="grid grid-cols-1 gap-3 mb-6">
              {currentQuestion.choices.map((choice, index) => {
                const isSelected = selectedAnswer === choice;
                const isCorrectChoice = choice === currentQuestion.correctAnswer;

                let buttonStyle = 'bg-white border-2 border-gray-300 hover:border-gray-400';

                if (isAnswered) {
                  if (isCorrectChoice) {
                    // 정답은 항상 초록색
                    buttonStyle = 'bg-green-500 hover:bg-green-500 text-white border-transparent';
                  } else if (isSelected && !isCorrect) {
                    // 오답은 항상 빨간색
                    buttonStyle = 'bg-red-500 hover:bg-red-500 text-white border-transparent';
                  }
                }

                return (
                  <button
                    key={index}
                    onClick={() => handleAnswer(choice)}
                    disabled={isAnswered}
                    className={`${buttonStyle} p-4 rounded-xl text-left transition-all text-lg font-medium disabled:cursor-not-allowed`}
                  >
                    {choice}
                  </button>
                );
              })}
            </div>

            {/* 해설 */}
            {isAnswered && (
              <div
                className={`${
                  isCorrect ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200'
                } border-2 rounded-xl p-6 mb-6`}
              >
                <div className="flex items-start gap-3">
                  <div className="text-2xl">{isCorrect ? '✅' : '❌'}</div>
                  <div className="flex-1">
                    <p className="font-bold text-lg mb-2">
                      {isCorrect ? '正解！' : '不正解'}
                    </p>
                    {currentQuestion.explanation && (
                      <p className="text-gray-700">{currentQuestion.explanation}</p>
                    )}
                  </div>
                </div>
              </div>
            )}

            {/* 다음 버튼 */}
            {isAnswered && (
              <button
                onClick={handleNext}
                className={`w-full ${colors.button} text-white font-bold py-4 rounded-xl transition-all`}
              >
                {currentQuestionIndex < quizzes.length - 1 ? '次の問題' : '結果を見る'}
              </button>
            )}
          </div>
        </div>
      </section>
    </MainLayout>
  );
}