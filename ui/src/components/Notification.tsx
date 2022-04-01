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

import DangerousOutlinedIcon from '@mui/icons-material/DangerousOutlined';
import CloseOutlinedIcon from '@mui/icons-material/CloseOutlined';
import { COLORS } from '../constants';

const Notification = (props: any) => {
  const { errorMessage } = props;
  return (
    <section className="flex justify-between p-4 bg-red-300">
      <div className="flex flex-row items-center gap-x-2">
        <DangerousOutlinedIcon sx={{ color: COLORS.danger }} />
        <p className="text-md text-red-600">{errorMessage}</p>
      </div>
      <span className="mr-12 cursor-pointer" onClick={() => props.clear()}>
        <CloseOutlinedIcon fontSize="medium" />
      </span>
    </section>
  );
};
export default Notification;
