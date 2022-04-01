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

import React, { useState } from 'react';
import BackupOutlinedIcon from '@mui/icons-material/BackupOutlined';
import HistoryOutlinedIcon from '@mui/icons-material/HistoryOutlined';
import { COLORS } from '../constants';

const Sidebar = (props: any) => {
  const [menuIndex, setMenuIndex] = useState(0);
  const { isExpanded } = props;
  const getMenuIndex = (index = 0) => {
    setMenuIndex(index);
    props.emitMenuIndex(index);
  };

  return (
    <aside
      className={`${
        isExpanded ? 'w-64' : 'w-14'
      } will-change-width transition-width duration-300 ease-[cubic-bezier(0.2, 0, 0, 1, 0)] flex flex-col overflow-hidden z-auto order-none shadow-md`}
    >
      <div className={`${isExpanded ? 'w-64' : 'w-14 '} h-[calc(100%-4.75rem)] flex flex-col fixed`}>
        <div className="will-change-width py-6 px-0 overflow-hidden relative">
          <ul className="flex flex-col p-0 list-none overflow-hidden">
            <li
              className="flex gap-x-2 p-4 cursor-pointer items-center relative hover:bg-[#efefef]"
              onClick={() => getMenuIndex(0)}
            >
              <BackupOutlinedIcon fontSize="small" sx={{ color: `${menuIndex === 0 ? COLORS.blue : COLORS.black}` }} />
              <p
                className={`${
                  !isExpanded ? 'hidden' : 'flex'
                } will-change-display transition-width duration-300 ease-[cubic-bezier(0.2, 0, 0, 1, 0)]`}
              >
                Upload file
              </p>
            </li>
            <li
              className="flex gap-x-2 p-4 cursor-pointer items-center relative hover:bg-[#efefef]"
              onClick={() => getMenuIndex(1)}
            >
              <HistoryOutlinedIcon fontSize="small" sx={{ color: `${menuIndex === 1 ? COLORS.blue : COLORS.black}` }} />
              <p
                className={`${
                  !isExpanded ? 'hidden' : 'flex'
                } will-change-display transition-width duration-300 ease-[cubic-bezier(0.2, 0, 0, 1, 0)]`}
              >
                Upload history
              </p>
            </li>
          </ul>
        </div>
      </div>
    </aside>
  );
};
export default Sidebar;
