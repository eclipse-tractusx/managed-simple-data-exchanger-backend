import React from 'react';
import DropZone from '../components/Dropzone';
import Nav from '../components/NavBar';
const Dashboard: React.FC = () => {
  return (
    <div className="Dashboard">
      <Nav />
      <DropZone />
    </div>
  );
};

export default Dashboard;
