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

export interface CourseDetails {
  id: number;
  name: string;
  mapImg: string;
  distanceMeter: number;
  price: number;
  courseCapacity: number;
  paces: Pace[];
  timeLimit: number;
  waterSource: number;
  altitude: number;
  courseRoute: string;
}

interface Package {
  id: number;
  name: string;
  price: number;
  description: string;
}

export interface ThumbnailImg {
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
  courses: CourseDetails[];
  packages: Package[];
  thumbnailImg: ThumbnailImg[];
  detailImg: DetailImg[];
}
