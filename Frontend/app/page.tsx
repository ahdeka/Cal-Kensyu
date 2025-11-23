'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import MainLayout from '@/components/MainLayout';

export default function HomePage() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    checkLoginStatus();
  }, []);

  const checkLoginStatus = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/auth/me', {
        credentials: 'include',
      });
      setIsLoggedIn(response.ok);
    } catch (error) {
      setIsLoggedIn(false);
    } finally {
      setLoading(false);
    }
  };

  return (
    <MainLayout>
      {/* ヒーローセクション */}
      <section className="bg-gray-100 text-center py-24">
        <div className="container mx-auto px-4">
          <h2 className="text-4xl md:text-5xl font-bold text-[#a80000] mb-6">
            美しい日本語の世界へ、ようこそ。
          </h2>
          <p className="text-xl mb-10 text-gray-700">
            日記、単語帳、問題演習で楽しく日本語を学びましょう。
          </p>
          
          {/* 로그인 상태에 따라 버튼 표시/숨김 */}
          {!loading && !isLoggedIn && (
            <Link 
              href="/signup" 
              className="inline-block bg-[#a80000] text-white px-8 py-3 rounded-full text-lg font-bold hover:bg-[#d11a1a] hover:-translate-y-1 transition-all"
            >
              今すぐ学習を始める
            </Link>
          )}
        </div>
      </section>

      {/* 特徴セクション */}
      <section id="features" className="py-20 bg-white">
        <div className="container mx-auto px-4 max-w-6xl">
          <h3 className="text-3xl font-bold text-center text-[#a80000] mb-12">
            このサイトでできること
          </h3>
          
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="bg-gray-100 p-8 rounded-lg shadow-sm hover:shadow-md transition-shadow">
              <h4 className="text-2xl font-bold text-[#a80000] mb-4">
                📝 日記作成
              </h4>
              <p className="text-gray-700 mb-3">
                日本語で自由に日記を書いて、表現力を磨きましょう。
              </p>
              <p className="text-sm text-gray-600">
                公開・非公開を選択でき、他の学習者と交流することもできます。
              </p>
            </div>
            
            <div className="bg-gray-100 p-8 rounded-lg shadow-sm hover:shadow-md transition-shadow">
              <h4 className="text-2xl font-bold text-[#a80000] mb-4">
                📚 マイ単語帳
              </h4>
              <p className="text-gray-700 mb-3">
                漢字、読み方、例文を登録して、自分だけの単語帳を作成。
              </p>
              <p className="text-sm text-gray-600">
                いつでもどこでも復習できる、あなた専用の学習ツールです。
              </p>
            </div>
            
            <div className="bg-gray-100 p-8 rounded-lg shadow-sm hover:shadow-md transition-shadow relative">
              <div className="absolute top-4 right-4 bg-yellow-400 text-xs font-bold px-3 py-1 rounded-full">
                準備中
              </div>
              <h4 className="text-2xl font-bold text-[#a80000] mb-4">
                ✏️ 問題演習
              </h4>
              <p className="text-gray-700 mb-3">
                日記と単語帳を基にした問題や、JLPT N5～N1対策問題。
              </p>
              <p className="text-sm text-gray-600">
                実力試しとレベルアップに最適な学習機能です。
              </p>
            </div>
          </div>
        </div>
      </section>
    </MainLayout>
  );
}