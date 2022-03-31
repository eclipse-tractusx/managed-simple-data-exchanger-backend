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

import { useNavigate } from 'react-router-dom';

import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import MenuOutlinedIcon from '@mui/icons-material/MenuOutlined';
import MenuItem from '@mui/material/MenuItem';
import Menu from '@mui/material/Menu';
import { Logout } from '@mui/icons-material';
import { COLORS } from '../constants';

const Nav = (props: any) => {
  const [isExpanded, setIsExpanded] = useState(false);
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const navigate = useNavigate();

  const handleExpanded = () => {
    if (isExpanded) {
      setIsExpanded(false);
      props.getIsExpanded(false);
      return;
    }
    setIsExpanded(true);
    props.getIsExpanded(true);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const logout = () => {
    localStorage.clear();
    navigate('/login');
  };

  return (
    <div className="shadow-md w-full fixed top-0 left-0 bg-[#01579b] z-50">
      <div className="md:flex items-center justify-between py-1 md:px-4">
        <div className="flex flex-row items-center gap-x-8">
          <span className="cursor-pointer" onClick={handleExpanded}>
            <MenuOutlinedIcon fontSize="medium" sx={{ color: COLORS.white }} />
          </span>

          <img src="images/logo-dft.svg" alt="DFT logo" className="w-14 h-14 fill-white  hover:fill-teal-600" />
        </div>

        <div className="font-bold text-2xl cursor-pointer flex items-center text-[#fbfcfa] ">
          Data Format Transformation
        </div>
        <span className="cursor-pointer" onClick={handleMenu}>
          <AccountCircleIcon sx={{ color: COLORS.white }} />
        </span>
        <Menu
          id="menu-appbar"
          anchorEl={anchorEl}
          anchorOrigin={{
            vertical: 'top',
            horizontal: 'right',
          }}
          keepMounted
          transformOrigin={{
            vertical: 'top',
            horizontal: 'right',
          }}
          open={Boolean(anchorEl)}
          onClose={handleClose}
        >
          <MenuItem onClick={logout}>
            <span>
              <Logout /> &nbsp; Logout
            </span>
          </MenuItem>
        </Menu>
      </div>
    </div>
  );
};
export default Nav;
