export const COUNTRIES = [
  { code: "KR", name: "한국", exchange: "KRX", flag: "🇰🇷" },
  { code: "US", name: "미국", exchange: "NYSE / NASDAQ", flag: "🇺🇸" },
  { code: "JP", name: "일본", exchange: "JPX", flag: "🇯🇵" },
  { code: "CN", name: "중국", exchange: "SHSE", flag: "🇨🇳" },
  { code: "HK", name: "홍콩", exchange: "HKEX", flag: "🇭🇰" },
  { code: "GB", name: "영국", exchange: "LSE", flag: "🇬🇧" },
  { code: "DE", name: "독일", exchange: "XETRA", flag: "🇩🇪" },
];

export const MOCK_SUBSCRIPTION = {
  id: 1,
  email: "user@example.com",
  countryCodes: ["KR", "US"],
  calendarToken: "abc123-def456-ghi789",
  calendarUrl: "webcal://localhost:8080/api/calendar/abc123-def456-ghi789/subscribe",
};
