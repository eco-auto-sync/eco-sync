'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { COUNTRIES } from '@/lib/mock-data'
import CountryCard from '@/components/ui/CountryCard'

export default function SubscribePage() {
  const router = useRouter()
  const [email, setEmail] = useState('')
  const [selected, setSelected] = useState<string[]>([])
  const [emailError, setEmailError] = useState('')

  function toggle(code: string) {
    setSelected((prev) =>
      prev.includes(code) ? prev.filter((c) => c !== code) : [...prev, code],
    )
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setEmailError('올바른 이메일 주소를 입력해주세요.')
      return
    }
    if (selected.length === 0) return
    router.push(
      `/subscribe/complete?email=${encodeURIComponent(email)}&countries=${selected.join(',')}`,
    )
  }

  const canSubmit = email.length > 0 && selected.length > 0

  return (
    <main className="mx-auto w-full max-w-2xl px-6 py-16">
      <h1 className="text-3xl font-bold text-zinc-900">국가 선택</h1>
      <p className="mt-2 text-zinc-500">
        이메일과 관심 국가를 선택하면 ICS 캘린더 URL을 발급해드려요.
      </p>

      <form onSubmit={handleSubmit} className="mt-10 space-y-8">
        {/* Email */}
        <div>
          <label className="mb-1.5 block text-sm font-medium text-zinc-700">이메일</label>
          <input
            type="email"
            value={email}
            onChange={(e) => {
              setEmail(e.target.value)
              setEmailError('')
            }}
            placeholder="your@email.com"
            className="w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition-colors focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
          />
          {emailError && <p className="mt-1.5 text-sm text-red-500">{emailError}</p>}
        </div>

        {/* Country grid */}
        <div>
          <p className="mb-3 block text-sm font-medium text-zinc-700">
            관심 국가 선택{' '}
            <span className="font-normal text-zinc-400">(1개 이상)</span>
            {selected.length > 0 && (
              <span className="ml-2 rounded-full bg-emerald-100 px-2 py-0.5 text-xs text-emerald-700">
                {selected.length}개 선택됨
              </span>
            )}
          </p>
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4">
            {COUNTRIES.map((c) => (
              <CountryCard
                key={c.code}
                country={c}
                selected={selected.includes(c.code)}
                onToggle={() => toggle(c.code)}
              />
            ))}
          </div>
        </div>

        <button
          type="submit"
          disabled={!canSubmit}
          className="w-full rounded-full bg-zinc-900 py-3.5 text-sm font-semibold text-white transition-colors hover:bg-zinc-700 disabled:cursor-not-allowed disabled:opacity-40"
        >
          구독 URL 발급받기
        </button>
      </form>
    </main>
  )
}
