import Link from 'next/link';

export default function Header() {
  return (
    <header className="border-b border-gray-200 bg-white">
      <div className="container mx-auto px-4 py-4 max-w-6xl">
        <div className="flex flex-col md:flex-row justify-between items-center">
          <h1 className="text-3xl font-bold text-[#a80000] mb-4 md:mb-0">
            <Link href="/">NihonGo!</Link>
          </h1>
          
          <nav className="mb-4 md:mb-0">
            <ul className="flex flex-wrap justify-center gap-6">
              <li>
                <Link href="/#about" className="text-gray-700 hover:text-[#a80000] transition-colors">
                  サイトについて
                </Link>
              </li>
              <li>
                <Link href="/#lessons" className="text-gray-700 hover:text-[#a80000] transition-colors">
                  レッスン
                </Link>
              </li>
              <li>
                <Link href="/#community" className="text-gray-700 hover:text-[#a80000] transition-colors">
                  コミュニティ
                </Link>
              </li>
              <li>
                <Link href="/#contact" className="text-gray-700 hover:text-[#a80000] transition-colors">
                  お問い合わせ
                </Link>
              </li>
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
  );
}