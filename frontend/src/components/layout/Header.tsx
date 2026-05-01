import Link from 'next/link'

export default function Header() {
  return (
    <header className="border-b border-zinc-200 bg-white px-6 py-4">
      <div className="mx-auto flex max-w-4xl items-center justify-between">
        <Link href="/" className="text-xl font-bold tracking-tight text-zinc-900 hover:text-emerald-600 transition-colors">
          EcoSync
        </Link>
        <nav className="flex items-center gap-6 text-sm text-zinc-600">
          <Link href="/subscribe" className="hover:text-zinc-900 transition-colors">구독하기</Link>
          <Link href="/my" className="hover:text-zinc-900 transition-colors">내 구독</Link>
        </nav>
      </div>
    </header>
  )
}
