import { useStopwatch } from 'react-timer-hook';

const Timer = () => {
  const { seconds, minutes } = useStopwatch({ autoStart: true });

  return (
    <div style={{ textAlign: 'center' }}>
      <div style={{ fontSize: '50px' }}>
        <span>{minutes < 10 ? `0${minutes}` : minutes}</span>:<span>{seconds < 10 ? `0${seconds}` : seconds}</span>
      </div>
    </div>
  );
};
export default Timer;
