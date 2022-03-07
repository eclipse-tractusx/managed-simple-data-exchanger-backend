import React, { useState } from 'react';

import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import MenuOutlinedIcon from '@mui/icons-material/MenuOutlined';

const Nav = (props: any) => {
  const [isExpanded, setIsExpanded] = useState(false);
  const handleExpanded = () => {
    if (isExpanded) {
      setIsExpanded(false);
      props.getIsExpanded(false);
      return;
    }
    setIsExpanded(true);
    props.getIsExpanded(true);
  };

  return (
    <div className="shadow-md w-full fixed top-0 left-0 bg-[#01579b] z-50">
      <div className="md:flex items-center justify-between py-1 md:px-6">
        <div className="flex flex-row items-center gap-x-8">
          <span className="cursor-pointer" onClick={handleExpanded}>
            <MenuOutlinedIcon fontSize="medium" sx={{ color: '#ffffff' }} />
          </span>

          <img src="images/logo-dft.svg" alt="DFT logo" className="w-14 h-14 fill-white  hover:fill-teal-600" />
        </div>

        <div className="font-bold text-2xl cursor-pointer flex items-center text-[#fbfcfa] ">
          Data Format Transformation
        </div>
        <AccountCircleIcon sx={{ color: '#ffffff' }} />
      </div>
    </div>
  );
};
export default Nav;
