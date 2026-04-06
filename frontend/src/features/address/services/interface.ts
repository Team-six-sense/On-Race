import { ApiResponse } from '@/types/api';
import { Address, AddressList } from '../types';

export interface IAddressService {
  // 배송지 API
  getAddress(): Promise<ApiResponse<AddressList>>;
  getDefaultAddress(): Promise<ApiResponse<Address | null>>;
  getAddressById(id: number): Promise<ApiResponse<Address>>;
  postAddress(data: Address): Promise<ApiResponse<Address>>;
  updateAddress(id: number, data: Address): Promise<ApiResponse<Address>>;
  deleteAddress(id: number): Promise<ApiResponse<null>>;
  updateDefaultAddress(id: number): Promise<ApiResponse<null>>;
}
