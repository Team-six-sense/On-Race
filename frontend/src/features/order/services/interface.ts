import { ApiResponse } from '@/types/api';
import { OrderCheck, OrderCheckInfo, PaymentConfirm } from '../types';

export interface IOrderService {
  getOrder(): Promise<ApiResponse<void>>;
  getOrderDetails(id: string): Promise<ApiResponse<void>>;
  postOrderConfirm(id: string): Promise<ApiResponse<void>>;
  postOrderCheckout(data: OrderCheck): Promise<ApiResponse<void>>;
  postOrderCheckoutInfo(data: OrderCheckInfo): Promise<ApiResponse<void>>;
  postPaymentConfirm(data: PaymentConfirm): Promise<ApiResponse<void>>;
}
