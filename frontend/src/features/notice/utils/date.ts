export const isNewNotice = (dateString: string): boolean => {
  const noticeDate = new Date(dateString);
  const thresholdDate = new Date('2024-09-15'); // 예제 기준일
  return noticeDate > thresholdDate;
};
