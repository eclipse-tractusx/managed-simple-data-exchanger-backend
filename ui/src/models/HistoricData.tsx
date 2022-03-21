export interface HistoricData {
    processId: string;
    csvType: CsvTypesEnum;
    numberOfItems: number;
    numberOfFailedItems: number;
    numberOfSuccededItems: number;
    status: string;
    startDate: string;
    endDate?: string;
}

enum CsvTypesEnum {
    aspect = 'ASPECT',
    childAspect = 'CHILD_ASPECT',
}