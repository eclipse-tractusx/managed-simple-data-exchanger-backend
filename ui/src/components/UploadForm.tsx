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

import React, { useRef } from 'react';
import { FileSize } from '../models/FileSize';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import CloseIcon from '@mui/icons-material/Close';
import UploadFileIcon from '@mui/icons-material/UploadFile';
import { COLORS } from '../constants';

const UploadForm = (props: any) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const { selectedFiles, uploadStatus } = props;

  const fileInputClicked = () => {
    if (fileInputRef.current) fileInputRef.current.click();
  };

  const filesSelected = () => {
    if (fileInputRef.current && fileInputRef.current.files) {
      props.getSelectedFiles(fileInputRef.current.files[0]);
    }
  };

  const fileSize = (size: number) => {
    if (size === 0) return '0 Bytes';
    const k = 1024;
    const sizes: string[] = Object.keys(FileSize);
    const i = Math.floor(Math.log(size) / Math.log(k));
    return `${parseFloat((size / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
  };

  const emitFileUpload = (e: any) => {
    props.emitFileUpload(e);
  };

  return (
    <div className="flex flex-col">
      <h2 className=" text-5xl font-sans text-[#444444] text-center mb-3">Upload a file </h2>
      <div className="border border-dashed  border-3  flex flex-row justify-center w-auto h-full items-center">
        <div className="flex flex-col gap-y-4 mx-20 ">
          <div className="py-6 px-4 flex flex-col items-center gap-x-4 relative">
            <input
              id="round"
              ref={fileInputRef}
              type="file"
              onClick={fileInputClicked}
              onChange={filesSelected}
              className="hidden"
            />
            <CloudUploadIcon sx={{ fontSize: 40, color: COLORS.grey }} />
            <h2 className=" my-1">Drag and drop your file on this page</h2>
            <h2 className=" my-1">or</h2>

            <label
              htmlFor="round"
              className="relative rounded-md cursor-pointer p-2 text-sky-500  border items-center hover:bg-[#efefef]"
            >
              CHOOSE A FILE
            </label>
          </div>
        </div>
      </div>
      {selectedFiles.length && !uploadStatus ? (
        <div className="flex flex-col mt-5 ">
          <label htmlFor="" className="font-bold text-[#000000] block mb-5  text-left ">
            Selected file
          </label>

          <div className="flex justify-between bg-[#f1f1f1] p-2">
            <div className="flex flex-row items-center gap-x-4 relative">
              <UploadFileIcon className="ml-2" />
              <div className="flex flex-row gap-x-4 items-center">
                <p className="text-md">{selectedFiles[0].name}</p>
                <p className="text-sm">({fileSize(selectedFiles[0].size)})</p>
              </div>
            </div>
            <span className="p-2 cursor-pointer">
              <button
                className="text-[#212121] text-sm"
                onClick={() => {
                  props.removeSelectedFiles(true);
                }}
              >
                <CloseIcon />
              </button>
            </span>
          </div>

          <button
            className="w-full py-2 px-4 bg-[#03a9f4] hover:bg-[#01579b] rounded-md text-white text-sm mt-5"
            onClick={emitFileUpload}
          >
            UPLOAD FILE
          </button>
        </div>
      ) : (
        <div />
      )}
    </div>
  );
};

export default UploadForm;
