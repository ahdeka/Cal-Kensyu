import Link from 'next/link';

export default function HomePage() {
  return (
    <div className="min-h-screen bg-white">
      {/* ヘッダー */}
      <header className="border-b border-gray-200">
        <div className="container mx-auto px-4 py-4 max-w-6xl">
          <div className="flex flex-col md:flex-row justify-between items-center">
            <h1 className="text-3xl font-bold text-[#a80000] mb-4 md:mb-0">
              <Link href="/">NihonGo!</Link>
            </h1>
            
            <nav className="mb-4 md:mb-0">
              <ul className="flex flex-wrap justify-center gap-6">
                <li><Link href="#about" className="text-gray-700 hover:text-[#a80000] transition-colors">サイトについて</Link></li>
                <li><Link href="#lessons" className="text-gray-700 hover:text-[#a80000] transition-colors">レッスン</Link></li>
                <li><Link href="#community" className="text-gray-700 hover:text-[#a80000] transition-colors">コミュニティ</Link></li>
                <li><Link href="#contact" className="text-gray-700 hover:text-[#a80000] transition-colors">お問い合わせ</Link></li>
              </ul>
            </nav>
            
            <Link 
              href="/login" 
              className="border border-[#a80000] text-[#a80000] px-4 py-2 rounded hover:bg-[#a80000] hover:text-white transition-all"
            >
              ログイン / 新規登録
            </Link>
          </div>
        </div>
      </header>

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

      {/* フッター */}
      <footer className="bg-gray-800 text-white text-center py-6">
        <div className="container mx-auto px-4">
          <p>&copy; 2025 にほんごラーニング. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
}