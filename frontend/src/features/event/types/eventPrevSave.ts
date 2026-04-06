interface course {
  id: number;
  name: string;
  distanceM: number;
  price: number;
}
interface pace {
  id: number;
  name: string;
  hour: number;
  minutes: number;
}
export interface EventPrevSave {
  id: number;
  eventId: number;
  status: string;
  course: course;
  pace: pace;
}
