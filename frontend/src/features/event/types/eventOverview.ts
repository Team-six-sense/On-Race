interface entry {
  id: number;
  status: string;
  selectedCourseId: number;
  selectedPaceId: number;
}

interface pace {
  id: number;
  name: string;
  hour: number;
  minutes: number;
  capacity: number;
}
interface courses {
  id: number;
  name: string;
  distanceM: number;
  price: number;
  paces: pace[];
}

interface rateInfo {
  entryCount: number;
  capacity: number;
  competitionRate: number;
  fillRatePercent: number;
  price: number;
}

export interface EventOverview {
  hasEntry: boolean;
  entry: entry;
  courses: courses[];
  rateInfo: rateInfo;
}
