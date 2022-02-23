export interface File {
  name: string;
  lastModified: number;
  lasModifiedDate: Date;
  type: string;
  webkitRelativePath: string;
  size: number;
  invalid: boolean;
}
