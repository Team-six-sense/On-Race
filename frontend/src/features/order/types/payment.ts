export interface OrderCheck {
  prepareToken: string;
  eventId: number;
  eventCourseId: number;
  eventPaceId: number;
  selectedPackageIds: number[];
  expectedFinalAmount: number;
  addressId: number;
  deliveryMemo: string;
}

export interface OrderCheckInfo {
  eventId: number;
  eventCourseId: number;
  eventPaceId: number;
}
