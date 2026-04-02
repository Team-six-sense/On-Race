// services/queueService.ts

let currentPos = 100; // 초기 대기 번호
const TOTAL = 500;

export const getQueueStatus = async (eventId: string) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      const decrease = Math.floor(Math.random() * 5) + 15; // 3~15명 랜덤 감소
      currentPos = Math.max(0, currentPos - decrease);

      resolve({
        position: currentPos,
        totalWaiting: TOTAL,
        expectedWaitTime: Math.ceil(currentPos * 0.8), // 초 단위로 변경 (포맷팅은 UI에서)
        status: currentPos === 0 ? 'passed' : 'waiting',
        accessToken: currentPos === 0 ? 'SECURE_TOKEN_ABC' : null,
      });
    }, 500);
  });
};
