import React from 'react';

import AccountCircleIcon from '@mui/icons-material/AccountCircle';

const Nav = () => {
  return (
    <div className="shadow-md w-full fixed top-0 left-0 bg-[#01579b] z-50">
      <div className="md:flex items-center justify-between py-4 md:px-10 grid grid-cols-2   ">
        <img src="images/logo-dft.svg" alt="DFT logo" className=" w-16 h-16 fill-white  hover:fill-teal-600" />
        <div className="font-bold text-2xl cursor-pointer flex items-center text-[#fbfcfa] ">
          Data Format Transformation
        </div>
        <AccountCircleIcon sx={{ color: '#ffffff' }} />
      </div>
    </div>
  );
};
export default Nav;
