export const formatKoreanDate = (
  dateAt: string | Date | number,
  showTime: boolean = false, // 시간 표시 여부 옵션 추가
): string => {
  if (!dateAt) return '';

  const date = new Date(dateAt);
  if (isNaN(date.getTime())) return '';

  // 기본 날짜 포맷: 2026. 04. 26. (일)
  const datePart = new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    weekday: 'short',
  }).format(date);

  if (!showTime) {
    return datePart;
  }

  // 시간 포함 포맷: 2026. 04. 26. (일) 오전 9시
  const timePart = new Intl.DateTimeFormat('ko-KR', {
    hour: 'numeric',
    // minute: '2-digit', // 분까지 필요하면 주석 해제
    hour12: true,
  }).format(date);

  return `${datePart} ${timePart}`;
};
