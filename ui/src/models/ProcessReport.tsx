export interface ProcessReport {
  processId: string;
  csvType: CsvTypes;
  numberOfItems: number;
  numberOfFailedItems: number;
  numberOfSucceededItems: number;
  status: Status;
  startDate: string;
  endDate?: string;
}

export enum Status {
  completed = 'COMPLETED',
  failed = 'FAILED',
  inProgress = 'IN_PROGRESS',
}

export enum CsvTypes {
  aspect = 'ASPECT',
  childAspect = 'CHILD_ASPECT',
  unknown = 'UNKNOWN',
}
