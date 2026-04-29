const phases = [
  {
    label: "Phase 1",
    color: "bg-emerald-100 text-emerald-700",
    title: "국가별 증시 휴장일",
    description: "관심 국가를 선택하면 증시 휴장일을 캘린더에서 자동으로 확인할 수 있어요.",
  },
  {
    label: "Phase 2",
    color: "bg-blue-100 text-blue-700",
    title: "주요 경제지표 발표일",
    description: "금리·CPI·GDP 등 주요 경제지표 발표 일정을 구독하세요.",
  },
  {
    label: "Phase 3",
    color: "bg-amber-100 text-amber-700",
    title: "기업 실적 발표일",
    description: "관심 기업의 실적 발표일과 D-3 사전 알림을 받아보세요.",
  },
];

export default function Home() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center px-6 py-20">
      {/* Hero */}
      <section className="flex flex-col items-center gap-4 text-center">
        <span className="rounded-full bg-emerald-50 px-3 py-1 text-sm font-medium text-emerald-600">
          경제 캘린더 구독 서비스
        </span>
        <h1 className="text-5xl font-bold tracking-tight text-zinc-900">EcoSync</h1>
        <p className="max-w-xl text-lg text-zinc-500 leading-relaxed">
          관심 있는 경제 일정을 선택하면, Google Calendar 등 캘린더 앱에서 자동으로 확인할 수
          있어요.
        </p>
        <button className="mt-4 rounded-full bg-zinc-900 px-8 py-3 text-sm font-semibold text-white transition-colors hover:bg-zinc-700">
          지금 시작하기
        </button>
      </section>

      {/* Phase cards */}
      <section className="mt-20 grid w-full max-w-4xl gap-4 sm:grid-cols-3">
        {phases.map((phase) => (
          <div key={phase.label} className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
            <span className={`inline-block rounded-full px-2.5 py-0.5 text-xs font-medium ${phase.color}`}>
              {phase.label}
            </span>
            <h2 className="mt-3 text-lg font-semibold text-zinc-900">{phase.title}</h2>
            <p className="mt-2 text-sm text-zinc-500 leading-relaxed">{phase.description}</p>
          </div>
        ))}
      </section>
    </main>
  );
}
