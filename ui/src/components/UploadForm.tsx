import React, { useRef } from 'react';
import { FileSize } from '../models/FileSize';

const UploadForm = (props: any) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const { selectedFiles } = props;

  const fileInputClicked = () => {
    if (fileInputRef.current) fileInputRef.current.click();
  };
  const filesSelected = () => {
    if (fileInputRef.current && fileInputRef.current.files) props.getSelectedFiles(fileInputRef.current.files);
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

  const removeSelectedFiles = () => {
    props.removeSelectedFiles(true);
  };

  return (
    <section className="bg-white shadow-md rounded-md w-72 col-start-2 col-span-2 top-1/2 h-42">
      <div className="flex flex-col gap-y-4">
        {selectedFiles.length === 0 && (
          <div className="py-6 px-4 flex flex-row items-center gap-x-4 relative">
            <input
              id="round"
              ref={fileInputRef}
              type="file"
              onClick={fileInputClicked}
              onChange={filesSelected}
              className="hidden"
            />
            <label
              htmlFor="round"
              className="relative cursor-pointer w-10 h-10 rounded-full bg-sky-500 hover:bg-sky-600"
            >
              <i className="text-white absolute left-1 top-1">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="30" height="30">
                  <path fill="none" d="M0 0h24v24H0z" />
                  <path d="M11 11V5h2v6h6v2h-6v6h-2v-6H5v-2z" />
                </svg>
              </i>
            </label>
            <h2>Upload Files</h2>
          </div>
        )}
        {selectedFiles.length > 0 && (
          <div className="py-6 px-4 flex flex-row items-center gap-x-4 relative">
            <div className="flex flex-col gap-y-2 overflow-x-auto">
              <p className="text-md truncate w-10/12">
                {selectedFiles[0].name} ({fileType(selectedFiles[0].name)})
              </p>
              <p className="text-sm">({fileSize(selectedFiles[0].size)})</p>
            </div>
            <span className="p-2 cursor-pointer">
              <p className="text-sky-500 text-lg" onClick={removeSelectedFiles}>
                X
              </p>
            </span>
          </div>
        )}
        <div className="flex flex-col justify-center items-center border-t border-gray-300 py-4">
          <button className="bg-sky-500 hover:bg-sky-600 rounded-lg w-32 h-10 text-white uppercase">Upload</button>
        </div>
      </div>
    </section>
  );
};

export default UploadForm;
