/**
 * 코스 정보 인터페이스
 */

interface Pace {
  id: number;
  name: string;
  hour: number;
  minutes: number;
  capacity: number;
}

interface Course {
  id: number;
  name: string;
  mapUrl: string;
  distanceMeter: number;
  price: number;
  courseCapacity: number;
  paces: Pace[];
}

interface Package {
  id: number;
  name: string;
  price: number;
  description: string;
}

interface ThumbnailImg {
  id: number;
  type: string;
  url: string;
  sort: number;
}

export interface DetailImg {
  id: number;
  type: string;
  url: string;
  sort: number;
}
export interface EventDetails {
  id: number;
  lotteryAnnouncedAt: string;
  notice: string;
  courses: Course[];
  packages: Package[];
  thumbnailImg: ThumbnailImg[];
  detailImg: DetailImg[];
}
