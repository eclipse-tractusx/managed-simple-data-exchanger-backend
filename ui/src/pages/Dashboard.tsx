import React, { SyntheticEvent, useState } from 'react';
import Nav from '../components/NavBar';
import Sidebar from '../components/Sidebar';
import UploadForm from '../components/UploadForm';
import { FileType } from '../models/FileType';

const Dashboard: React.FC = () => {
  const [isExpanded, setIsExpanded] = useState(false);
  const handleExpanded = (expanded: boolean) => {
    setIsExpanded(expanded);
  };
  const [selectedFiles, setSelectedFiles] = useState<File[]>([]);
  const [isDragging, setIsDragging] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState('');
  let dragCounter = 0;

  const validateFile = (file: File) => {
    const validTypes: string[] = Object.values(FileType);
    return validTypes.includes(file.type);
  };

  const handleFiles = (file: File) => {
    /*if (validateFile(file)) {
      setSelectedFiles([...selectedFiles, file]);
    } else {
      file.invalid = true;
      setErrorMessage('File not permitted');
    } */
    setSelectedFiles([...selectedFiles, file]);
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

  const fileDrop = (e: any) => {
    e.preventDefault();
    const files = e.dataTransfer.files;
    if (files.length && files.length < 2 && selectedFiles.length === 0) {
      handleFiles(files[0]);
    } else {
      setErrorMessage('Only one file is permitted');
    }
    setIsDragging(false);
  };

  const removeSelectedFiles = (clearState: boolean) => {
    if (clearState) setSelectedFiles([]);
  };
  return (
    <div
      className="@apply max-w-screen-4xl my-0 mx-auto overflow-y-auto overflow-x-hidden h-screen block"
      onDragOver={(e: SyntheticEvent) => e.preventDefault()}
      onDragEnter={dragEnter}
      onDragLeave={dragLeave}
      onDrop={fileDrop}
    >
      {!isDragging && (
        <main className="flex-1 min-h-screen pt-16 flex flex-row justify-start">
          <Nav getIsExpanded={(expanded: boolean) => handleExpanded(expanded)} />
          <div className="flex">
            <Sidebar isExpanded={isExpanded} />
          </div>
          <div className="flex flex-1 flex-col items-center justify-center min-w-0 relative">
            <div className="flex-[1_0_0%] order-1">
              <div className="flex flex-col items-center justify-center">
                <UploadForm
                  getSelectedFiles={(files: any) => handleFiles(files)}
                  selectedFiles={selectedFiles}
                  removeSelectedFiles={removeSelectedFiles}
                />
                <h1 className="text-center text-white text-5xl">Drag & Drop files here or upload via form</h1>
              </div>
            </div>
          </div>
        </main>
      )}

      {isDragging && (
        <div className="relative w-full h-full bg-[#03a9f4]">
          <div className="inset-x-0 inset-y-1/2 absolute z-5 flex flex-col justify-center gap-y-2 text-center">
            <h1 className="text-4xl">Drop it like it's hot :)</h1>
            <p className="text-lg">Upload files by dropping them in this window</p>
          </div>
        </div>
      )}
    </div>
  );
};

export default Dashboard;
