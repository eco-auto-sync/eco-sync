'use client'

import { useState, Suspense } from 'react'
import { useSearchParams } from 'next/navigation'
import Link from 'next/link'
import { COUNTRIES, MOCK_SUBSCRIPTION } from '@/lib/mock-data'

function CompleteContent() {
  const params = useSearchParams()
  const email = params.get('email') ?? MOCK_SUBSCRIPTION.email
  const countryCodes = (params.get('countries') ?? MOCK_SUBSCRIPTION.countryCodes.join(',')).split(',')
  const calendarUrl = MOCK_SUBSCRIPTION.calendarUrl
  const [copied, setCopied] = useState(false)

  const selectedCountries = COUNTRIES.filter((c) => countryCodes.includes(c.code))

  async function copyUrl() {
    try {
      await navigator.clipboard.writeText(calendarUrl)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    } catch {
      // fallback: select text
    }
  }

  const googleCalendarUrl = `https://calendar.google.com/calendar/r?cid=${encodeURIComponent(calendarUrl)}`

  return (
    <main className="mx-auto w-full max-w-2xl px-6 py-16">
      <div className="text-center">
        <span className="text-5xl">🎉</span>
        <h1 className="mt-4 text-3xl font-bold text-zinc-900">구독 URL이 발급되었어요!</h1>
        <p className="mt-2 text-zinc-500">
          <span className="font-medium text-zinc-700">{email}</span> 으로 등록된 구독이에요.
        </p>
      </div>

      {/* URL card */}
      <div className="mt-10 rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
        <p className="mb-2 text-sm font-medium text-zinc-700">구독 국가</p>
        <div className="flex flex-wrap gap-2">
          {selectedCountries.map((c) => (
            <span
              key={c.code}
              className="inline-flex items-center gap-1.5 rounded-full bg-emerald-50 px-3 py-1 text-sm text-emerald-700"
            >
              {c.flag} {c.name}
            </span>
          ))}
        </div>

        <p className="mb-2 mt-6 text-sm font-medium text-zinc-700">캘린더 구독 URL</p>
        <div className="flex items-stretch gap-2">
          <code className="flex-1 overflow-hidden rounded-xl border border-zinc-200 bg-zinc-50 px-4 py-3 text-xs font-mono text-zinc-600 truncate">
            {calendarUrl}
          </code>
          <button
            onClick={copyUrl}
            className="shrink-0 rounded-xl border border-zinc-200 bg-white px-4 py-3 text-sm font-medium text-zinc-700 transition-colors hover:bg-zinc-50"
          >
            {copied ? '복사됨 ✓' : '복사'}
          </button>
        </div>

        <a
          href={googleCalendarUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="mt-4 flex w-full items-center justify-center gap-2 rounded-xl bg-blue-600 py-3.5 text-sm font-semibold text-white transition-colors hover:bg-blue-700"
        >
          Google Calendar에 추가하기
        </a>
      </div>

      {/* Guide */}
      <div className="mt-6 rounded-2xl border border-zinc-200 bg-white p-6">
        <h2 className="font-semibold text-zinc-900">📅 캘린더 등록 방법</h2>
        <ol className="mt-3 list-inside list-decimal space-y-2 text-sm text-zinc-600">
          <li>위 버튼을 누르거나, URL을 복사해 Google Calendar에 붙여넣기</li>
          <li>Google Calendar → 다른 캘린더 → URL로 구독</li>
          <li>
            iOS/macOS 기본 캘린더도 동일하게 <code className="rounded bg-zinc-100 px-1 py-0.5 text-xs">webcal://</code>{' '}
            URL 붙여넣기로 추가 가능해요
          </li>
        </ol>
      </div>

      {/* Actions */}
      <div className="mt-6 flex gap-3">
        <Link
          href="/subscribe"
          className="flex-1 rounded-full border border-zinc-300 bg-white py-3 text-center text-sm font-medium text-zinc-700 transition-colors hover:bg-zinc-50"
        >
          구독 수정
        </Link>
        <button className="flex-1 rounded-full border border-red-200 py-3 text-sm font-medium text-red-500 transition-colors hover:bg-red-50">
          구독 취소
        </button>
      </div>
    </main>
  )
}

export default function CompletePage() {
  return (
    <Suspense>
      <CompleteContent />
    </Suspense>
  )
}
