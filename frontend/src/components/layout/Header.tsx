export default function Header() {
  return (
    <header className="border-b border-zinc-200 px-6 py-4">
      <div className="mx-auto flex max-w-7xl items-center justify-between">
        <span className="text-xl font-bold tracking-tight">EcoSync</span>
        <nav className="flex gap-6 text-sm text-zinc-600">
          <a href="/" className="hover:text-zinc-900">홈</a>
        </nav>
      </div>
    </header>
  );
}
