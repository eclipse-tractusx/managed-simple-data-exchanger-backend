import DangerousOutlinedIcon from '@mui/icons-material/DangerousOutlined';
import CloseOutlinedIcon from '@mui/icons-material/CloseOutlined';

const Notification = (props: any) => {
  const { errorMessage } = props;
  return (
    <section className="flex justify-between p-4 bg-red-300">
      <div className="flex flex-row items-center gap-x-2">
        <DangerousOutlinedIcon sx={{ color: '#dc2626' }} />
        <p className="text-md text-red-600">{errorMessage}</p>
      </div>
      <span className="mr-12 cursor-pointer" onClick={() => props.clear()}>
        <CloseOutlinedIcon fontSize="medium" />
      </span>
    </section>
  );
};
export default Notification;
