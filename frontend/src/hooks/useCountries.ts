'use client'

import { useState, useEffect } from 'react'
import { api } from '@/lib/api'
import type { Country } from '@/types'

export function useCountries() {
  const [countries, setCountries] = useState<Country[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)

  useEffect(() => {
    api
      .get<Country[]>('/api/countries')
      .then(setCountries)
      .catch(() => setError(true))
      .finally(() => setLoading(false))
  }, [])

  return { countries, loading, error }
}
