import React, { useRef, useState } from 'react';
import { FileSize } from '../models/FileSize';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import CloseIcon from '@mui/icons-material/Close';
import UploadFileIcon from '@mui/icons-material/UploadFile';
import axios, { AxiosRequestConfig, AxiosResponse } from 'axios';
import UploadProgressBar from './UploadProgressBar';

const UploadForm = (props: any) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const { selectedFiles } = props;
  const [uploadProgress, updateUploadProgress] = useState(0);
  const [uploadStatus, setUploadStatus] = useState(false);
  const [uploading, setUploading] = useState(false);

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
    const sizes = Object.keys(FileSize);
    const i = Math.floor(Math.log(size) / Math.log(k));
    return parseFloat((size / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]);
  };

  const fileType = (fileName: string) => {
    return fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length) || fileName;
  };

  const getBase64 = (img: Blob, callback: any) => {
    const reader = new FileReader();
    // FileReader API Spec: https://developer.mozilla.org/en-US/docs/Web/API/FileReader/FileReader
    reader.addEventListener('load', () => callback(reader.result));
    reader.readAsDataURL(img);
  };

  const handleFileUpload = (e: any) => {
    e.preventDefault();

    /* if (!isValidFileType(file.type)) {
        alert('Only csv files are allowed');
        return;
    } */

    setUploading(true);
    const formData = new FormData();
    formData.append('File', selectedFiles[0]);
    const config: AxiosRequestConfig = {
      method: 'post',
      url: 'http://3.66.97.83:8080/api/upload',

      headers: {
        'Content-Type': 'multipart/form-data',
        'Access-Control-Allow-Origin': '*',
      },
      data: formData,
      onUploadProgress: (ev: ProgressEvent) => {
        const progress = (ev.loaded / ev.total) * 100;
        updateUploadProgress(Math.round(progress));
      },
    };
    console.log(`${JSON.stringify(config)}`);

    axios(config)
      .then(resp => {
        console.log(JSON.stringify(resp.data));
        setUploadStatus(true);
        setUploading(false);
      })
      .catch(err => console.error(err));
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
            <CloudUploadIcon sx={{ fontSize: 40, color: '#444' }} />
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
      {uploading ? <UploadProgressBar uploadProgress={uploadProgress} /> : null}
      {selectedFiles.length ? (
        <div className="flex flex-col mt-5 ">
          <label htmlFor="" className="font-bold text-[#000000] block mb-5  text-left ">
            Selected file
          </label>

          <div className="flex flex-row items-center gap-x-4 relative bg-[#f1f1f1] p-2">
            <UploadFileIcon className="ml-2" />
            <div className="flex flex-row gap-x-4 items-center">
              <p className="text-md">{selectedFiles[0].name}</p>
              <p className="text-sm">({fileSize(selectedFiles[0].size)})</p>
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
            onClick={handleFileUpload}
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
