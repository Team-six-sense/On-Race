import { ApiResponse } from '@/types/api';
import { AccountInfo } from '../types/accountInfo';
import { EventHistory } from '../types/eventHistory';
import { PaymentHistory } from '../types/paymentHistory';

export interface IMypageService {
  // 마이페이지 API
  getAccountInfo(id: number): Promise<ApiResponse<AccountInfo>>;
  getEventHistory(id: number): Promise<ApiResponse<EventHistory[]>>;
  getPaymentHistory(id: number): Promise<ApiResponse<PaymentHistory[]>>;
}
