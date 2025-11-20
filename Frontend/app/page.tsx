import Link from 'next/link';
import MainLayout from '@/components/MainLayout';

export default function HomePage() {
  return (
    <MainLayout>
      {/* ヒーローセクション */}
      <section className="bg-gray-100 text-center py-24">
        <div className="container mx-auto px-4">
          <h2 className="text-4xl md:text-5xl font-bold text-[#a80000] mb-6">
            美しい日本語の世界へ、ようこそ。
          </h2>
          <p className="text-xl mb-10 text-gray-700">
            初心者から上級者まで、あなたのレベルに合わせた最適な学習プランを提供します。
          </p>
          <Link 
            href="#lessons" 
            className="inline-block bg-[#a80000] text-white px-8 py-3 rounded-full text-lg font-bold hover:bg-[#d11a1a] hover:-translate-y-1 transition-all"
          >
            今すぐ学習を始める
          </Link>
        </div>
      </section>

      {/* 特徴セクション */}
      <section id="features" className="py-20">
        <div className="container mx-auto px-4 max-w-6xl">
          <h3 className="text-3xl font-bold text-center text-[#a80000] mb-12">
            このサイトでできること
          </h3>
          
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="bg-gray-100 p-8 rounded-lg shadow-sm">
              <h4 className="text-2xl font-bold text-[#a80000] mb-4">
                📝 豊富な教材
              </h4>
              <p className="text-gray-700">
                文法、語彙、聴解など、多岐にわたる高品質な教材を網羅。
              </p>
            </div>
            
            <div className="bg-gray-100 p-8 rounded-lg shadow-sm">
              <h4 className="text-2xl font-bold text-[#a80000] mb-4">
                🗣️ ネイティブとの会話
              </h4>
              <p className="text-gray-700">
                オンラインレッスンで実践的な会話力を楽しく身につけましょう。
              </p>
            </div>
            
            <div className="bg-gray-100 p-8 rounded-lg shadow-sm">
              <h4 className="text-2xl font-bold text-[#a80000] mb-4">
                ✨ 進捗管理
              </h4>
              <p className="text-gray-700">
                学習の進捗を視覚化し、モチベーションを維持できます。
              </p>
            </div>
          </div>
        </div>
      </section>
    </MainLayout>
  );
}