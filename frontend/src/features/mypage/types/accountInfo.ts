export interface Address {
  id: number;
  label: string;
  receiverName: string;
  phoneNumber: string;
  zipcode: string;
  address1: string;
  address2: string;
  memo: string;
  isDefault: boolean;
}
export interface AccountInfo {
  id: number;
  name: string;
  phoneNumber: string;
  email: string;
  isPassAuth: boolean;
  addressList: Address[];
}
