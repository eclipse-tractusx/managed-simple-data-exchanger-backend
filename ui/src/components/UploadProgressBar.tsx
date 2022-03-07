import React from 'react';
import { CircularProgressbar, buildStyles } from 'react-circular-progressbar';
import 'react-circular-progressbar/dist/styles.css';

interface Props {
  uploadProgress: number;
}

const UploadProgressBar: React.FC<Props> = ({ uploadProgress }) => {
  return (
    <div className="w-screen  h-screen fixed flex   top-0 left-0 bg-black opacity-80 z-50">
      <div className="flex flex-col justify-center items-center h-full w-full ">
        <div className="flex w-96 h-96">
          {' '}
          <CircularProgressbar
            value={uploadProgress}
            text={`${uploadProgress}% uploaded`}
            styles={buildStyles({
              textSize: '10px',
              pathColor: 'teal',
              textColor: '#0063de',
            })}
          />
        </div>
      </div>
    </div>
  );
};

export default UploadProgressBar;
