'use client'

import { useState } from 'react'
import Link from 'next/link'
import { api } from '@/lib/api'
import { useCountries } from '@/hooks/useCountries'
import type { Subscription } from '@/types'

export default function MyPage() {
  const [email, setEmail] = useState('')
  const [loading, setLoading] = useState(false)
  const [subscription, setSubscription] = useState<Subscription | null>(null)
  const [notFound, setNotFound] = useState(false)
  const [copied, setCopied] = useState(false)
  const [cancelling, setCancelling] = useState(false)

  const { countries } = useCountries()

  function resetResult() {
    setSubscription(null)
    setNotFound(false)
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setLoading(true)
    resetResult()

    try {
      const data = await api.get<Subscription>(
        `/api/subscriptions?email=${encodeURIComponent(email)}`,
      )
      setSubscription(data)
    } catch {
      setNotFound(true)
    } finally {
      setLoading(false)
    }
  }

  async function copyUrl() {
    if (!subscription) return
    try {
      await navigator.clipboard.writeText(subscription.calendarUrl)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    } catch {
      // fallback
    }
  }

  async function handleCancel() {
    if (!subscription) return
    if (!confirm('구독을 취소하시겠어요?')) return
    setCancelling(true)
    try {
      await api.delete(`/api/subscriptions/${subscription.id}`)
      setSubscription(null)
      setNotFound(true)
    } catch {
      alert('구독 취소 중 오류가 발생했어요.')
    } finally {
      setCancelling(false)
    }
  }

  const selectedCountries =
    subscription && countries.length > 0
      ? countries.filter((c) => subscription.countryCodes.includes(c.code))
      : subscription?.countryCodes.map((code) => ({
          code,
          name: code,
          exchange: '',
          flag: '',
        })) ?? []

  const editUrl = subscription
    ? `/subscribe?id=${subscription.id}&email=${encodeURIComponent(subscription.email)}&countries=${subscription.countryCodes.join(',')}`
    : '/subscribe'

  return (
    <main className="mx-auto w-full max-w-2xl px-6 py-16">
      <h1 className="text-3xl font-bold text-zinc-900">내 구독</h1>
      <p className="mt-2 text-zinc-500">이메일을 입력하면 등록된 구독 정보를 확인할 수 있어요.</p>

      <form onSubmit={handleSubmit} className="mt-10 flex gap-3" onChange={resetResult}>
        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="your@email.com"
          required
          className="flex-1 rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition-colors focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
        />
        <button
          type="submit"
          disabled={loading}
          className="rounded-xl bg-zinc-900 px-6 py-3 text-sm font-semibold text-white transition-colors hover:bg-zinc-700 disabled:opacity-40"
        >
          {loading ? '조회 중...' : '조회'}
        </button>
      </form>

      {notFound && (
        <div className="mt-8 rounded-2xl border border-zinc-200 bg-white p-10 text-center shadow-sm">
          <p className="text-zinc-500">등록된 구독 정보가 없어요.</p>
          <Link
            href="/subscribe"
            className="mt-5 inline-block rounded-full bg-zinc-900 px-6 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-zinc-700"
          >
            지금 구독하기
          </Link>
        </div>
      )}

      {subscription && (
        <div className="mt-8 rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
          <p className="mb-2 text-sm font-medium text-zinc-700">구독 국가</p>
          <div className="flex flex-wrap gap-2">
            {selectedCountries.map((c) => (
              <span
                key={c.code}
                className="inline-flex items-center gap-1.5 rounded-full bg-emerald-50 px-3 py-1 text-sm text-emerald-700"
              >
                {c.flag && `${c.flag} `}{c.name}
              </span>
            ))}
          </div>

          <p className="mb-2 mt-6 text-sm font-medium text-zinc-700">캘린더 구독 URL</p>
          <div className="flex items-stretch gap-2">
            <code className="flex-1 overflow-hidden truncate rounded-xl border border-zinc-200 bg-zinc-50 px-4 py-3 text-xs font-mono text-zinc-600">
              {subscription.calendarUrl}
            </code>
            <button
              onClick={copyUrl}
              className="shrink-0 rounded-xl border border-zinc-200 bg-white px-4 py-3 text-sm font-medium text-zinc-700 transition-colors hover:bg-zinc-50"
            >
              {copied ? '복사됨 ✓' : '복사'}
            </button>
          </div>

          <div className="mt-6 flex gap-3">
            <Link
              href={editUrl}
              className="flex-1 rounded-full border border-zinc-300 bg-white py-3 text-center text-sm font-medium text-zinc-700 transition-colors hover:bg-zinc-50"
            >
              구독 수정
            </Link>
            <button
              onClick={handleCancel}
              disabled={cancelling}
              className="flex-1 rounded-full border border-red-200 py-3 text-sm font-medium text-red-500 transition-colors hover:bg-red-50 disabled:opacity-40"
            >
              {cancelling ? '취소 중...' : '구독 취소'}
            </button>
          </div>
        </div>
      )}
    </main>
  )
}
