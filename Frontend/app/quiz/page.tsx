'use client';

import { useState, useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';
import MainLayout from '@/components/MainLayout';

type JLPTLevel = 'N5' | 'N4' | 'N3' | 'N2' | 'N1';

interface LevelInfo {
  level: JLPTLevel;
  title: string;
  description: string;
  color: string;
  hoverColor: string;
  bgColor: string;
  difficulty: string;
}

const JLPT_LEVELS: LevelInfo[] = [
  {
    level: 'N5',
    title: 'JLPT N5',
    description: '基礎レベル - 約800語',
    color: 'bg-green-500',
    hoverColor: 'hover:bg-green-600',
    bgColor: 'bg-green-50',
    difficulty: '初級',
  },
  {
    level: 'N4',
    title: 'JLPT N4',
    description: '初級レベル - 約1,500語',
    color: 'bg-blue-500',
    hoverColor: 'hover:bg-blue-600',
    bgColor: 'bg-blue-50',
    difficulty: '初級',
  },
  {
    level: 'N3',
    title: 'JLPT N3',
    description: '中級レベル - 約3,700語',
    color: 'bg-yellow-500',
    hoverColor: 'hover:bg-yellow-600',
    bgColor: 'bg-yellow-50',
    difficulty: '中級',
  },
  {
    level: 'N2',
    title: 'JLPT N2',
    description: '中上級レベル - 約6,000語',
    color: 'bg-orange-500',
    hoverColor: 'hover:bg-orange-600',
    bgColor: 'bg-orange-50',
    difficulty: '中上級',
  },
  {
    level: 'N1',
    title: 'JLPT N1',
    description: '上級レベル - 約10,000語',
    color: 'bg-red-500',
    hoverColor: 'hover:bg-red-600',
    bgColor: 'bg-red-50',
    difficulty: '上級',
  },
];

export default function QuizPage() {
  const router = useRouter();
  const hasCheckedAuth = useRef(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [checkingAuth, setCheckingAuth] = useState(true);
  const [selectedLevel, setSelectedLevel] = useState<JLPTLevel | null>(null);

  useEffect(() => {
    if (!hasCheckedAuth.current) {
      hasCheckedAuth.current = true;
      checkLoginStatus();
    }
  }, []);

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

  const handleLevelSelect = (level: JLPTLevel) => {
    setSelectedLevel(level);
    // 추후 퀴즈 시작 페이지로 라우팅
    router.push(`/quiz/${level.toLowerCase()}`);
  };

  if (checkingAuth) {
    return (
      <MainLayout>
        <div className="min-h-screen flex items-center justify-center">
          <p className="text-gray-500 text-lg">読込中...</p>
        </div>
      </MainLayout>
    );
  }

  return (
    <MainLayout>
      {/* ヒーローセクション */}
      <section className="bg-gradient-to-br from-[#a80000] via-[#8b0000] to-[#6b0000] text-white py-16">
        <div className="container mx-auto px-4 max-w-6xl text-center">
          <h1 className="text-5xl font-bold mb-4">
            ✏️ JLPT単語問題演習
          </h1>
          <p className="text-xl mb-2 text-gray-100">
            レベル別に日本語能力試験(JLPT)の単語を学習しましょう
          </p>
          <p className="text-lg text-gray-200">
            N5からN1まで、あなたのレベルに合わせて練習できます
          </p>
        </div>
      </section>

      {/* レベル選択セクション */}
      <section className="py-16 bg-gray-50 min-h-[calc(100vh-400px)]">
        <div className="container mx-auto px-4 max-w-6xl">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-gray-900 mb-4">
              レベルを選択してください
            </h2>
            <p className="text-gray-600 text-lg">
              自分のレベルに合った問題に挑戦しましょう
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {JLPT_LEVELS.map((levelInfo, index) => (
              <button
                key={levelInfo.level}
                onClick={() => handleLevelSelect(levelInfo.level)}
                className={`${levelInfo.bgColor} border-2 border-gray-200 rounded-2xl p-8 
                  hover:shadow-xl hover:scale-105 hover:-translate-y-2 
                  transition-all duration-300 cursor-pointer text-left
                  animate-[fadeIn_0.5s_ease-out]`}
                style={{ animationDelay: `${index * 0.1}s` }}
              >
                {/* レベルバッジ */}
                <div className="flex items-center justify-between mb-4">
                  <span
                    className={`${levelInfo.color} text-white text-sm font-bold px-4 py-2 rounded-full`}
                  >
                    {levelInfo.difficulty}
                  </span>
                  <span className="text-4xl">📚</span>
                </div>

                {/* レベルタイトル */}
                <h3 className="text-3xl font-bold text-gray-900 mb-3">
                  {levelInfo.title}
                </h3>

                {/* 説明 */}
                <p className="text-gray-700 text-lg mb-4">
                  {levelInfo.description}
                </p>

                {/* 開始ボタン */}
                <div
                  className={`${levelInfo.color} ${levelInfo.hoverColor} 
                    text-white text-center font-bold py-3 rounded-lg 
                    transition-colors mt-4`}
                >
                  問題を始める →
                </div>
              </button>
            ))}
          </div>

          {/* 説明セクション */}
          <div className="mt-16 bg-white rounded-2xl shadow-sm border border-gray-200 p-8">
            <h3 className="text-2xl font-bold text-gray-900 mb-6 text-center">
              📖 JLPTとは？
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-gray-700">
              <div className="space-y-3">
                <p className="leading-relaxed">
                  <strong className="text-[#a80000]">JLPT（日本語能力試験）</strong>は、
                  日本語を母語としない人の日本語能力を測定する試験です。
                </p>
                <p className="leading-relaxed">
                  N5からN1まで5つのレベルがあり、
                  <strong>N5が最も易しく、N1が最も難しい</strong>レベルです。
                </p>
              </div>
              <div className="space-y-3">
                <p className="leading-relaxed">
                  このページでは、各レベルに必要な単語を
                  <strong className="text-[#a80000]">問題形式</strong>で
                  学習することができます。
                </p>
                <p className="leading-relaxed">
                  自分の目標に合わせてレベルを選び、
                  楽しく日本語の単語力を高めましょう！
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>
    </MainLayout>
  );
}