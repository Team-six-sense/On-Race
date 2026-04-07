import { ApiResponse } from '@/types/api';
import {
  AccountInfo,
  EntriesHistory,
  HistoryOverview,
  OrderHistory,
  WaitingEntriesHistory,
} from '../types';

export interface IMypageService {
  // 마이페이지 API
  getAccountInfo(): Promise<ApiResponse<AccountInfo>>;
  getHistoryOverview(): Promise<ApiResponse<HistoryOverview>>;
  getEntriesHistory(): Promise<ApiResponse<EntriesHistory[]>>;
  getWaitingHistory(): Promise<ApiResponse<WaitingEntriesHistory>>;
  getOrderHistory(): Promise<ApiResponse<OrderHistory[]>>;
  getOrderDetailInfo(id: string): Promise<ApiResponse<null>>;
}
