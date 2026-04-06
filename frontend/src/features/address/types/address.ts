export interface Address {
  id?: number;
  label: string;
  receiverName: string;
  phone: string;
  zipcode: string;
  address1: string;
  address2: string;
  memo: string;
  isDefault: boolean;
}

export interface AddressList {
  data: Address[];
}
