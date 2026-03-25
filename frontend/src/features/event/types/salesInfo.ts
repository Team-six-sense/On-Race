export interface RefundPolicy {
  isRefundable: boolean;
  isTransferable: boolean;
  refundStartAt: string;
  refundEndAt: string;
  nonRefundableAt: string;
  cancellationFee: string;
  refundPolicy: string;
  weatherRefund: string;
}

export interface Delivery {
  deliveryTarget: string;
  deliveryMethod: string;
  deliveryStartAt: string;
  deliveryEndAt: string;
  deliveryFee: string;
  deliveryArea: string;
  addressChangePeriod: string;
  deliveryCompensation: string;
}

export interface Seller {
  sellerName: string;
  businessNo: string;
  ecommerceNo: string;
  isEcommerceMediator: boolean;
  customerService: string;
  sellerAddress: string;
}

export interface SalesInfo {
  refundPolicy: RefundPolicy;
  delivery: Delivery;
  seller: Seller;
}
