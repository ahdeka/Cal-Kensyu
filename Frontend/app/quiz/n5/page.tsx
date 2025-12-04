'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import MainLayout from '@/components/MainLayout';

// Mock 데이터
const MOCK_N5_QUIZZES = [
  {
    id: 1,
    question: '食べる',
    questionType: 'この単語の読み方は？',
    choices: ['たべる', 'のべる', 'とべる', 'かべる'],
    correctAnswer: 'たべる',
    explanation: '「食べる」は「たべる」と読みます。「食事をする」という意味です。',
  },
  {
    id: 2,
    question: 'がっこう',
    questionType: 'この単語の意味は？',
    choices: ['학교', '회사', '집', '병원'],
    correctAnswer: '학교',
    explanation: '「がっこう（学校）」は韓国語で「학교」です。',
  },
  {
    id: 3,
    question: '本',
    questionType: 'この単語の意味は？',
    choices: ['책', '펜', '노트', '가방'],
    correctAnswer: '책',
    explanation: '「本（ほん）」は「책」という意味です。',
  },
  {
    id: 4,
    question: 'あした',
    questionType: 'この単語の意味は？',
    choices: ['내일', '오늘', '어제', '모레'],
    correctAnswer: '내일',
    explanation: '「あした（明日）」は「내일」という意味です。',
  },
  {
    id: 5,
    question: '見る',
    questionType: 'この単語の読み方は？',
    choices: ['みる', 'きる', 'いる', 'ひる'],
    correctAnswer: 'みる',
    explanation: '「見る」は「みる」と読みます。「보다」という意味です。',
  },
  {
    id: 6,
    question: 'いく',
    questionType: 'この単語の意味は？',
    choices: ['가다', '오다', '돌아가다', '나가다'],
    correctAnswer: '가다',
    explanation: '「いく（行く）」は「가다」という意味です。',
  },
  {
    id: 7,
    question: '水',
    questionType: 'この単語の読み方は？',
    choices: ['みず', 'すい', 'かわ', 'うみ'],
    correctAnswer: 'みず',
    explanation: '「水」は「みず」と読みます。「물」という意味です。',
  },
  {
    id: 8,
    question: 'おおきい',
    questionType: 'この単語の意味は？',
    choices: ['크다', '작다', '길다', '짧다'],
    correctAnswer: '크다',
    explanation: '「おおきい（大きい）」は「크다」という意味です。',
  },
  {
    id: 9,
    question: '友達',
    questionType: 'この単語の読み方は？',
    choices: ['ともだち', 'ゆうだち', 'ゆだち', 'ともたち'],
    correctAnswer: 'ともだち',
    explanation: '「友達」は「ともだち」と読みます。「친구」という意味です。',
  },
  {
    id: 10,
    question: 'かう',
    questionType: 'この単語の意味は？',
    choices: ['사다', '팔다', '주다', '받다'],
    correctAnswer: '사다',
    explanation: '「かう（買う）」は「사다」という意味です。',
  },
];

export default function N5QuizPage() {
  const router = useRouter();
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [selectedAnswer, setSelectedAnswer] = useState<string | null>(null);
  const [isAnswered, setIsAnswered] = useState(false);
  const [score, setScore] = useState(0);
  const [showResult, setShowResult] = useState(false);

  const currentQuestion = MOCK_N5_QUIZZES[currentQuestionIndex];
  const isCorrect = selectedAnswer === currentQuestion.correctAnswer;

  const handleAnswer = (choice: string) => {
    if (isAnswered) return;

    setSelectedAnswer(choice);
    setIsAnswered(true);

    if (choice === currentQuestion.correctAnswer) {
      setScore(score + 1);
    }
  };

  const handleNext = () => {
    if (currentQuestionIndex < MOCK_N5_QUIZZES.length - 1) {
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
  };

  // 결과 화면
  if (showResult) {
    const percentage = (score / MOCK_N5_QUIZZES.length) * 100;
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
                  {score} / {MOCK_N5_QUIZZES.length}
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
                {currentQuestionIndex + 1} / {MOCK_N5_QUIZZES.length}
              </div>
            </div>
          </div>

          {/* 진행바 */}
          <div className="bg-gray-200 rounded-full h-3 mb-8">
            <div
              className="bg-green-500 h-3 rounded-full transition-all duration-300"
              style={{
                width: `${((currentQuestionIndex + 1) / MOCK_N5_QUIZZES.length) * 100}%`,
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
                {currentQuestionIndex < MOCK_N5_QUIZZES.length - 1
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