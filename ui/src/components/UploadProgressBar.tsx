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

import React from 'react';
import { CircularProgressbar, buildStyles } from 'react-circular-progressbar';
import 'react-circular-progressbar/dist/styles.css';
import { COLORS } from '../constants';

interface Props {
  uploadProgress: number;
}

const UploadProgressBar: React.FC<Props> = ({ uploadProgress }) => {
  return (
    <div className="w-screen h-screen fixed flex top-0 left-0 bg-white opacity-40 z-50">
      <div className="flex flex-col justify-center items-center h-full w-full ">
        <div className="flex w-96 h-96">
          <CircularProgressbar
            value={uploadProgress}
            text={`${uploadProgress}% uploaded`}
            styles={buildStyles({
              textSize: '10px',
              pathColor: 'teal',
              textColor: COLORS.blue,
            })}
          />
        </div>
      </div>
    </div>
  );
};

export default UploadProgressBar;
