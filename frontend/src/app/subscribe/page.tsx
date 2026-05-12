'use client'

import { useState, useEffect, Suspense } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import CountryCard from '@/components/ui/CountryCard'
import { api } from '@/lib/api'
import { useCountries } from '@/hooks/useCountries'

function SubscribeContent() {
  const router = useRouter()
  const params = useSearchParams()

  const editId = params.get('id') ? Number(params.get('id')) : null
  const editEmail = params.get('email') ?? ''
  const editCountries = params.get('countries')?.split(',').filter(Boolean) ?? []

  const { countries, loading: countriesLoading } = useCountries()
  const [email, setEmail] = useState(editEmail)
  const [selected, setSelected] = useState<string[]>(editCountries)
  const [emailError, setEmailError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (editEmail) setEmail(editEmail)
  }, [editEmail])

  useEffect(() => {
    if (editCountries.length > 0) setSelected(editCountries)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params])

  function toggle(code: string) {
    setSelected((prev) =>
      prev.includes(code) ? prev.filter((c) => c !== code) : [...prev, code],
    )
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setEmailError('올바른 이메일 주소를 입력해주세요.')
      return
    }
    if (selected.length === 0) return

    setSubmitting(true)
    setError('')

    try {
      let id: number
      let calendarUrl: string

      if (editId) {
        const res = await api.put<{ id: number; calendarUrl: string }>(
          `/api/subscriptions/${editId}`,
          { countryCodes: selected },
        )
        id = res.id
        calendarUrl = res.calendarUrl
      } else {
        const res = await api.post<{ id: number; calendarUrl: string }>('/api/subscriptions', {
          email,
          countryCodes: selected,
        })
        id = res.id
        calendarUrl = res.calendarUrl
      }

      router.push(
        `/subscribe/complete?id=${id}&email=${encodeURIComponent(email)}&countries=${selected.join(',')}&calendarUrl=${encodeURIComponent(calendarUrl)}`,
      )
    } catch {
      setError('구독 처리 중 오류가 발생했어요. 다시 시도해주세요.')
    } finally {
      setSubmitting(false)
    }
  }

  const canSubmit = email.length > 0 && selected.length > 0 && !submitting

  return (
    <main className="mx-auto w-full max-w-2xl px-6 py-16">
      <h1 className="text-3xl font-bold text-zinc-900">
        {editId ? '구독 수정' : '국가 선택'}
      </h1>
      <p className="mt-2 text-zinc-500">
        {editId
          ? '구독 국가를 변경하면 캘린더가 자동으로 업데이트돼요.'
          : '이메일과 관심 국가를 선택하면 ICS 캘린더 URL을 발급해드려요.'}
      </p>

      <form onSubmit={handleSubmit} className="mt-10 space-y-8">
        <div>
          <label className="mb-1.5 block text-sm font-medium text-zinc-700">이메일</label>
          <input
            type="email"
            value={email}
            onChange={(e) => {
              setEmail(e.target.value)
              setEmailError('')
            }}
            readOnly={!!editId}
            placeholder="your@email.com"
            className={`w-full rounded-xl border border-zinc-300 px-4 py-3 text-sm outline-none transition-colors focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100 ${
              editId ? 'cursor-default bg-zinc-50 text-zinc-500' : 'bg-white'
            }`}
          />
          {emailError && <p className="mt-1.5 text-sm text-red-500">{emailError}</p>}
        </div>

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
          {countriesLoading ? (
            <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4">
              {Array.from({ length: 7 }).map((_, i) => (
                <div
                  key={i}
                  className="h-[104px] animate-pulse rounded-2xl border-2 border-zinc-200 bg-zinc-100"
                />
              ))}
            </div>
          ) : (
            <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4">
              {countries.map((c) => (
                <CountryCard
                  key={c.code}
                  country={c}
                  selected={selected.includes(c.code)}
                  onToggle={() => toggle(c.code)}
                />
              ))}
            </div>
          )}
        </div>

        {error && <p className="text-sm text-red-500">{error}</p>}

        <button
          type="submit"
          disabled={!canSubmit}
          className="w-full rounded-full bg-zinc-900 py-3.5 text-sm font-semibold text-white transition-colors hover:bg-zinc-700 disabled:cursor-not-allowed disabled:opacity-40"
        >
          {submitting ? '처리 중...' : editId ? '구독 수정하기' : '구독 URL 발급받기'}
        </button>
      </form>
    </main>
  )
}

export default function SubscribePage() {
  return (
    <Suspense>
      <SubscribeContent />
    </Suspense>
  )
}
