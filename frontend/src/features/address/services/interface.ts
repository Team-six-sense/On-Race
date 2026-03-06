import { ApiResponse } from '@/types/api';
import { AddressResponse, AddressListResponse } from '../types';

export interface IAddressService {
  // 배송지 API
  getAddress(): Promise<ApiResponse<AddressListResponse>>;
  getAddressById(id: string): Promise<ApiResponse<AddressResponse>>;
  createAddress(data: AddressResponse): Promise<ApiResponse<AddressResponse>>;
  updateAddress(
    id: string,
    data: AddressResponse,
  ): Promise<ApiResponse<AddressResponse>>;
  deleteAddress(id: string): Promise<ApiResponse<any>>;
  updateDefaultAddress(id: string): Promise<ApiResponse<any>>;
}
