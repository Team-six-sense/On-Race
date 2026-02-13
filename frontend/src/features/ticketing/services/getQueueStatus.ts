let currentPos = 100; // 초기값
const TOTAL = 500; // 전체 모수

export const getQueueStatus = async (eventId: string) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      // 3~15명 사이로 랜덤하게 줄어듦
      //   const decrease = Math.floor(Math.random() * 12) + 3;
      const decrease = 10;
      currentPos = Math.max(0, currentPos - decrease);

      resolve({
        position: currentPos,
        totalWaiting: TOTAL,
        expectedWaitTime: `00:${String(Math.ceil(currentPos * 0.8)).padStart(2, '0')}`,
        status: currentPos === 0 ? 'passed' : 'waiting',
        accessToken: currentPos === 0 ? 'SECURE_TOKEN_ABC' : null,
      });
    }, 800);
  });
};
