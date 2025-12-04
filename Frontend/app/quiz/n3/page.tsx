// app/quiz/n3/page.tsx
'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import MainLayout from '@/components/MainLayout';

// Mock 데이터 (N3는 더 어려운 단어)
const MOCK_N3_QUIZZES = [
  {
    id: 1,
    question: '設立',
    questionType: 'この単語の読み方は？',
    choices: ['せつりつ', 'せつりゅう', 'せいりつ', 'せいりゅう'],
    correctAnswer: 'せつりつ',
    explanation: '「設立」は「せつりつ」と読みます。「설립하다」という意味です。',
  },
  {
    id: 2,
    question: 'こうりつ',
    questionType: 'この単語の意味は？',
    choices: ['효율', '공립', '고립', '합리'],
    correctAnswer: '効率',
    explanation: '「こうりつ（効率）」は「효율」という意味です。',
  },
  {
    id: 3,
    question: '貿易',
    questionType: 'この単語の読み方は？',
    choices: ['ぼうえき', 'ぼえき', 'もえき', 'ぼうやく'],
    correctAnswer: 'ぼうえき',
    explanation: '「貿易」は「ぼうえき」と読みます。「무역」という意味です。',
  },
  {
    id: 4,
    question: 'せいさく',
    questionType: 'この単語の意味は？',
    choices: ['정책', '제작', '생산', '제책'],
    correctAnswer: '정책',
    explanation: '「せいさく（政策）」は「정책」という意味です。',
  },
  {
    id: 5,
    question: '普及',
    questionType: 'この単語の読み方は？',
    choices: ['ふきゅう', 'ふきょう', 'ほきゅう', 'ほきょう'],
    correctAnswer: 'ふきゅう',
    explanation: '「普及」は「ふきゅう」と読みます。「보급」という意味です。',
  },
  {
    id: 6,
    question: 'ほしょう',
    questionType: 'この単語の意味は？',
    choices: ['보장', '보상', '보호', '보존'],
    correctAnswer: '보장',
    explanation: '「ほしょう（保障）」は「보장」という意味です。',
  },
  {
    id: 7,
    question: '傾向',
    questionType: 'この単語の読み方は？',
    choices: ['けいこう', 'けこう', 'きょうこう', 'きこう'],
    correctAnswer: 'けいこう',
    explanation: '「傾向」は「けいこう」と読みます。「경향」という意味です。',
  },
  {
    id: 8,
    question: 'きぼ',
    questionType: 'この単語の意味は？',
    choices: ['규모', '희망', '기본', '기부'],
    correctAnswer: '규모',
    explanation: '「きぼ（規模）」は「규모」という意味です。',
  },
  {
    id: 9,
    question: '著しい',
    questionType: 'この単語の読み方は？',
    choices: ['いちじるしい', 'あきらかしい', 'あらわしい', 'しるしい'],
    correctAnswer: 'いちじるしい',
    explanation: '「著しい」は「いちじるしい」と読みます。「현저하다」という意味です。',
  },
  {
    id: 10,
    question: 'かくだい',
    questionType: 'この単語の意味は？',
    choices: ['확대', '확장', '확보', '확인'],
    correctAnswer: '확대',
    explanation: '「かくだい（拡大）」は「확대」という意味です。',
  },
];

export default function N3QuizPage() {
  const router = useRouter();
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [selectedAnswer, setSelectedAnswer] = useState<string | null>(null);
  const [isAnswered, setIsAnswered] = useState(false);
  const [score, setScore] = useState(0);
  const [showResult, setShowResult] = useState(false);

  const currentQuestion = MOCK_N3_QUIZZES[currentQuestionIndex];
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
    if (currentQuestionIndex < MOCK_N3_QUIZZES.length - 1) {
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
    const percentage = (score / MOCK_N3_QUIZZES.length) * 100;
    return (
      <MainLayout>
        <section className="min-h-screen bg-gradient-to-br from-yellow-50 to-orange-50 py-16">
          <div className="container mx-auto px-4 max-w-2xl">
            <div className="bg-white rounded-2xl shadow-xl p-8 text-center">
              <div className="mb-8">
                <div className="text-6xl mb-4">
                  {percentage >= 80 ? '🎉' : percentage >= 60 ? '😊' : '💪'}
                </div>
                <h2 className="text-3xl font-bold text-gray-900 mb-2">
                  お疲れ様でした！
                </h2>
                <p className="text-gray-600">JLPT N3 問題演習</p>
              </div>

              <div className="bg-yellow-50 rounded-xl p-8 mb-8">
                <div className="text-5xl font-bold text-yellow-600 mb-2">
                  {score} / {MOCK_N3_QUIZZES.length}
                </div>
                <div className="text-xl text-gray-700">
                  正解率: {percentage.toFixed(0)}%
                </div>
              </div>

              <div className="space-y-3 mb-8">
                {percentage >= 80 && (
                  <p className="text-lg text-gray-700">
                    素晴らしい！N3レベルをよく理解しています！🌟
                  </p>
                )}
                {percentage >= 60 && percentage < 80 && (
                  <p className="text-lg text-gray-700">
                    良い結果です！もう少し練習すれば完璧です！👍
                  </p>
                )}
                {percentage < 60 && (
                  <p className="text-lg text-gray-700">
                    N3は難しいですね。もう一度チャレンジしてみましょう！💪
                  </p>
                )}
              </div>

              <div className="flex gap-4">
                <button
                  onClick={handleRestart}
                  className="flex-1 bg-yellow-500 hover:bg-yellow-600 text-white font-bold py-4 rounded-xl transition-all cursor-pointer"
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
      <section className="min-h-screen bg-gradient-to-br from-yellow-50 to-orange-50 py-8">
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
                JLPT N3 問題演習
              </h1>
              <div className="bg-yellow-500 text-white px-4 py-2 rounded-full font-bold">
                {currentQuestionIndex + 1} / {MOCK_N3_QUIZZES.length}
              </div>
            </div>
          </div>

          {/* 진행바 */}
          <div className="bg-gray-200 rounded-full h-3 mb-8">
            <div
              className="bg-yellow-500 h-3 rounded-full transition-all duration-300"
              style={{
                width: `${((currentQuestionIndex + 1) / MOCK_N3_QUIZZES.length) * 100}%`,
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
            <div className="bg-gradient-to-br from-yellow-50 to-orange-50 rounded-xl p-8 mb-8">
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
                    'border-gray-300 hover:border-yellow-500 hover:bg-yellow-50 hover:scale-105';
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
                className="w-full bg-yellow-500 hover:bg-yellow-600 text-white font-bold py-4 rounded-xl transition-all cursor-pointer animate-[slideUp_0.3s_ease-out]"
              >
                {currentQuestionIndex < MOCK_N3_QUIZZES.length - 1
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