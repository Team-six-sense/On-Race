export const formatKoreanDate = (dateAt: string | Date | number): string => {
  if (!dateAt) return '';

  return new Date(dateAt).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
};
