// Copyright 2022 Catena-X
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import React, { SyntheticEvent, useEffect, useState } from 'react';
import { styled } from '@mui/material/styles';
import Nav from '../components/NavBar';
import Sidebar from '../components/Sidebar';
import Timer from '../components/Timer';
import UploadForm from '../components/UploadForm';
import { FileType } from '../models/FileType';
import { File } from '../models/File';
import { CsvTypes, ProcessReport, Status } from '../models/ProcessReport';

import UploadFileOutlinedIcon from '@mui/icons-material/UploadFileOutlined';
import Notification from '../components/Notification';
import dft from '../api/dft';
import CheckCircleOutlineOutlinedIcon from '@mui/icons-material/CheckCircleOutlineOutlined';
import CloseIcon from '@mui/icons-material/Close';
import StickyHeadTable from '../components/Table';
import CircularProgress from '@mui/material/CircularProgress';
import Grid from '@mui/material/Grid';
import Button, { ButtonProps } from '@mui/material/Button';
import { HighlightOffOutlined, Refresh, ReportGmailerrorredOutlined } from '@mui/icons-material';
import { formatDate } from '../utils/utils';
import { COLORS } from '../constants';

const Dashboard: React.FC = () => {
  const [isExpanded, setIsExpanded] = useState(false);
  const [menuIndex, setMenuIndex] = useState(0);
  const [selectedFiles, setSelectedFiles] = useState<File[]>([]);
  const [isDragging, setIsDragging] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [uploadStatus, setUploadStatus] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [tableData, setTableData] = useState<ProcessReport[]>([]);
  const [rowsPerPage, setRowsPerPage] = useState<number>(10);
  const [page, setPage] = useState<number>(0);
  const [totalElements, setTotalElements] = useState<number>(0);
  const [currentUploadData, setUploadData] = useState<ProcessReport>({
    processId: '',
    csvType: CsvTypes.unknown,
    numberOfItems: 0,
    numberOfFailedItems: 0,
    numberOfSucceededItems: 0,
    status: Status.inProgress,
    startDate: '',
    endDate: undefined,
  });
  let dragCounter = 0;

  const refreshTable = () => {
    dft.get(`/processing-report?page=${page}&pageSize=${rowsPerPage}`).then(response => {
      setTableData(response.data.items);
      setTotalElements(response.data.totalItems);
    });
  };

  useEffect(() => {
    (async () => {
      refreshTable();
    })();
  }, [page, rowsPerPage]);

  const handleExpanded = (expanded: boolean) => {
    setIsExpanded(expanded);
  };

  const validateFile = (file: File) => {
    const validTypes: string[] = Object.values(FileType);
    return validTypes.includes(file.type) || file.name.endsWith('.csv');
  };

  const handleFiles = (file: File) => {
    setUploadStatus(false);
    setUploading(false);
    const maxFileSize = window._env_.FILESIZE;
    if (validateFile(file) && file.size < maxFileSize) {
      setSelectedFiles([file]);
    } else {
      file.invalid = true;
      setErrorMessage('File not permitted');
    }
  };

  const dragEnter = (e: any) => {
    e.preventDefault();
    e.stopPropagation();
    dragCounter++;
    if (e.dataTransfer.items && e.dataTransfer.items.length > 0) {
      setIsDragging(true);
    }
  };

  const dragLeave = (e: any) => {
    e.preventDefault();
    e.stopPropagation();
    dragCounter--;
    if (dragCounter > 0) return;
    setIsDragging(false);
  };

  const removeSelectedFiles = (clearState: boolean) => {
    if (clearState) setSelectedFiles([]);
    setUploadStatus(false);
    setUploading(false);
  };

  const fileDrop = (e: any) => {
    e.preventDefault();
    const files = e.dataTransfer.files;
    if (files.length && files.length < 2 && selectedFiles.length === 0) {
      handleFiles(files[0]);
      setUploadStatus(false);
      setUploading(false);
    } else if (files.length && files.length < 2 && selectedFiles.length > 0) {
      setSelectedFiles([files[0]]);
      setUploadStatus(false);
      setUploading(false);
    } else {
      setErrorMessage('Only one file is permitted');
    }
    setIsDragging(false);
  };

  const getMenuIndex = (index = 0) => {
    setMenuIndex(index);
    if (index === 1) {
      refreshTable();
    }
  };

  const clearUpload = () => {
    setTimeout(() => {
      setUploading(false);
      setUploadStatus(true);
    }, 1000);
  };

  const uploadFile = (e: any) => {
    e.preventDefault();
    setUploading(true);
    setUploadData({
      processId: '',
      csvType: CsvTypes.unknown,
      numberOfItems: 0,
      numberOfFailedItems: 0,
      numberOfSucceededItems: 0,
      status: Status.inProgress,
      startDate: '',
      endDate: undefined,
    });
    const formData = new FormData();
    formData.append('file', selectedFiles[0] as any);

    dft
      .post('/upload', formData)
      .then(resp => {
        const processId = resp.data;

        // first call
        dft.get(`/processing-report/${processId}`).then(r => {
          setUploadData(r.data);
          if (r && r.data && r.data.status !== Status.completed && r.data.status !== Status.failed) {
            // if status !== 'COMPLETED' && status !== 'FAILED' -> set interval with 10 seconds to refresh data
            const interval = setInterval(
              () =>
                dft.get(`/processing-report/${processId}`).then(result => {
                  setUploadData(result.data);
                  if (
                    result &&
                    result.data &&
                    (result.data.status === Status.completed || result.data.status === Status.failed)
                  ) {
                    clearInterval(interval);
                    clearUpload();
                  }
                }),
              2000,
            );
          } else {
            clearUpload();
          }
        });
      })
      .catch(err => console.error(err));
  };

  const ColorButton = styled(Button)<ButtonProps>(() => ({
    color: COLORS.white,
    backgroundColor: COLORS.blue,
    '&:hover': {
      backgroundColor: COLORS.white,
      color: COLORS.blue,
    },
  }));

  // TODO: Replace this logic with routes
  const layout = () => {
    if (menuIndex === 0) {
      return (
        <div className="flex flex-1 flex-col items-center justify-center min-w-0 relative">
          <div className="flex-[1_0_0%] flex order-1">
            <div className="flex flex-col items-center justify-center">
              {uploading ? (
                <div className="text-center">
                  <CircularProgress size={100} />
                  <Timer />
                  <span>
                    Upload started at: {currentUploadData.startDate ? formatDate(currentUploadData.startDate) : '-'}
                  </span>
                </div>
              ) : null}
              {!uploading && (
                <UploadForm
                  getSelectedFiles={(files: any) => handleFiles(files)}
                  selectedFiles={selectedFiles}
                  removeSelectedFiles={removeSelectedFiles}
                  uploadStatus={uploadStatus}
                  emitFileUpload={(e: any) => uploadFile(e)}
                />
              )}
              {uploadStatus && currentUploadData.status === Status.failed && (
                <div className={'flex justify-between bg-red-100 p-4 w-full mt-4'}>
                  <div className="flex items-center gap-x-2">
                    <span title="Failed">
                      <HighlightOffOutlined sx={{ color: COLORS.danger }} />
                    </span>
                    <p className="text-md">{selectedFiles[0].name}</p>
                  </div>
                  <span className="cursor-pointer" onClick={() => setUploadStatus(false)}>
                    <CloseIcon />
                  </span>
                </div>
              )}
              {uploadStatus &&
                currentUploadData.status === Status.completed &&
                currentUploadData.numberOfFailedItems === 0 && (
                  <div className={'flex justify-between bg-lime-200 p-4 w-full mt-4'}>
                    <div className="flex items-center gap-x-2">
                      <span title="Completed">
                        <CheckCircleOutlineOutlinedIcon sx={{ color: COLORS.success }} />
                      </span>
                      <p className="text-md">{selectedFiles[0].name}</p>
                    </div>
                    <span className="cursor-pointer" onClick={() => setUploadStatus(false)}>
                      <CloseIcon />
                    </span>
                  </div>
                )}
              {uploadStatus &&
                currentUploadData.status === Status.completed &&
                currentUploadData.numberOfFailedItems > 0 && (
                  <div className={'flex justify-between bg-orange-100 p-4 w-full mt-4'}>
                    <div className="flex items-center gap-x-2">
                      <span title="Completed with warnings">
                        <ReportGmailerrorredOutlined sx={{ color: COLORS.warning }} />
                      </span>
                      <p className="text-md">{selectedFiles[0].name}</p>
                    </div>
                    <span className="cursor-pointer" onClick={() => setUploadStatus(false)}>
                      <CloseIcon />
                    </span>
                  </div>
                )}
            </div>
          </div>
        </div>
      );
    } else {
      return (
        <div className="flex-1 py-6 px-20">
          <Grid container spacing={2}>
            <Grid item xs={6}>
              <h1 className="flex flex-row text-bold text-3xl">Upload History</h1>
            </Grid>
            <Grid item xs={6} className="text-right">
              <ColorButton variant="contained" onClick={() => refreshTable()}>
                <span>
                  <Refresh />
                  &nbsp; Refresh
                </span>
              </ColorButton>
            </Grid>
          </Grid>
          <div className="mt-8">
            <StickyHeadTable
              rows={tableData}
              page={page}
              rowsPerPage={rowsPerPage}
              totalElements={totalElements}
              setPage={setPage}
              setRowsPerPage={setRowsPerPage}
            />
          </div>
        </div>
      );
    }
  };

  return (
    <div
      className="max-w-screen-4xl my-0 mx-auto overflow-y-auto overflow-x-hidden h-screen block"
      onDragOver={(e: SyntheticEvent) => e.preventDefault()}
      onDragEnter={dragEnter}
      onDragLeave={dragLeave}
      onDrop={fileDrop}
    >
      {!isDragging && (
        <main className="flex-1 flex flex-row justify-start min-h-screen pt-16 relative">
          <Nav getIsExpanded={(expanded: boolean) => handleExpanded(expanded)} />
          <div className="flex">
            <Sidebar isExpanded={isExpanded} emitMenuIndex={(index: number) => getMenuIndex(index)} />
          </div>
          {errorMessage !== '' && (
            <div className={`${isExpanded ? 'left-64' : 'left-14'} absolute top-16 z-50 w-screen`}>
              <Notification errorMessage={errorMessage} clear={() => setErrorMessage('')} />
            </div>
          )}

          <div className="flex w-screen">{layout()}</div>
        </main>
      )}

      {isDragging && (
        <div className="relative w-full h-full bg-[#03a9f4]">
          <div className="inset-x-0 inset-y-1/2 absolute z-5 flex flex-col justify-center gap-y-2 text-center">
            <span>
              <UploadFileOutlinedIcon style={{ fontSize: 60 }} sx={{ color: COLORS.white }} />
            </span>
            <h1 className="text-4xl text-white">Drop it like it's hot :)</h1>
            <p className="text-lg text-white">Upload your file by dropping it in this window</p>
          </div>
        </div>
      )}
    </div>
  );
};

export default Dashboard;
