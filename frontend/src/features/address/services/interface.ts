import { ApiResponse } from '@/types/api';
import { Address, AddressList } from '../types';

export interface IAddressService {
  // 배송지 API
  getAddress(): Promise<ApiResponse<AddressList>>;
  getDefaultAddress(): Promise<ApiResponse<Address | null>>;
  getAddressById(id: string): Promise<ApiResponse<Address>>;
  postAddress(data: Address): Promise<ApiResponse<Address>>;
  updateAddress(id: string, data: Address): Promise<ApiResponse<Address>>;
  deleteAddress(id: string): Promise<ApiResponse<null>>;
  updateDefaultAddress(id: string): Promise<ApiResponse<null>>;
}
