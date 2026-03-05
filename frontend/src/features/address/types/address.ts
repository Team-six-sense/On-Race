export interface AddressResponse {
  id: number;
  receiverName: string;
  phone: string;
  zipcode: string;
  address1: string;
  address2: string;
  memo: string;
  isDefault: boolean;
}

export interface AddressListResponse {
  data: AddressResponse[];
}
