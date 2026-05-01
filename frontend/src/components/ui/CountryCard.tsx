'use client'

type Country = { code: string; name: string; exchange: string; flag: string }

interface Props {
  country: Country
  selected: boolean
  onToggle: () => void
}

export default function CountryCard({ country, selected, onToggle }: Props) {
  return (
    <button
      type="button"
      onClick={onToggle}
      className={`relative flex flex-col gap-2 rounded-2xl border-2 p-5 text-left transition-all ${
        selected
          ? 'border-emerald-500 bg-emerald-50'
          : 'border-zinc-200 bg-white hover:border-zinc-300 hover:shadow-sm'
      }`}
    >
      <span className="text-3xl">{country.flag}</span>
      <span className="font-semibold text-zinc-900">{country.name}</span>
      <span className="text-xs text-zinc-400">{country.exchange}</span>
      {selected && (
        <span className="absolute right-3 top-3 flex h-5 w-5 items-center justify-center rounded-full bg-emerald-500 text-xs text-white">
          ✓
        </span>
      )}
    </button>
  )
}
