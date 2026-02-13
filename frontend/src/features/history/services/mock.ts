import { MOCK_RECORD_DATA, MOCK_APPLICATIONS } from '@/mockups';
import { wrapMockResponse } from '@/utils/api';
import { IHistoryService } from './interface';

export const historyMock: IHistoryService = {
  getRecord: async (query) => wrapMockResponse(MOCK_RECORD_DATA),
  getApplication: async (query) => wrapMockResponse(MOCK_APPLICATIONS),
};

// export const portalService = {
//   searchRunnerData: async (query: string): Promise<{ record: RunnerRecord; applications: Application[] }> => {
//     // API 호출 시뮬레이션
//     return new Promise((resolve) => {
//       setTimeout(() => {
//         resolve({
//           record: MOCK_RECORD_DATA,
//           applications: MOCK_APPLICATIONS,
//         });
//       }, 500);
//     });
//   }
// };
