import React from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import getHash from '../modules/Hash';
interface Props {
  setIsAuth: any;
  setIsAuthError: React.Dispatch<React.SetStateAction<boolean>>;
  isAuthError: boolean;
}

type FormValues = {
  username: string;
  password: string;
};

const Login: React.FC<Props> = ({ setIsAuth, setIsAuthError, isAuthError }) => {
  const { register, handleSubmit } = useForm<FormValues>();
  const navigate = useNavigate();
  const onSubmit = handleSubmit(({ username, password }) => {
    if (getHash(password) === window._env_.PASS && username === window._env_.USERNAME) {
      setIsAuth(true);
      navigate('/dashboard');
    } else {
      setIsAuthError(true);
    }
  });

  return (
    <div className="min-h-screen flex flex-col justify-center bg-white bg-fixed bg-[url('../public/images/login.jpg')] bg-cover">
      <div className="max-w-md w-full mx-auto font-bold text-6xl text-center">DFT</div>
      <div className="max-w-md w-full mx-auto mt-4 bg-white p-8 border ">
        <form action="" className="space-y-6" onSubmit={onSubmit}>
          <div>
            <label htmlFor="" className="text-sm font-bold text-gray-600 block">
              {' '}
              Username
            </label>
            <input
              {...register('username', { required: true, minLength: 5, maxLength: 80 })}
              name="username"
              type="text"
              className="w-full p-2 border border-gray-300 rounded mt-1"
            />
          </div>
          <div>
            <label htmlFor="" className="text-sm font-bold text-gray-600 block">
              {' '}
              Password
            </label>
            <input
              {...register('password', { required: true, minLength: 4, maxLength: 80 })}
              name="password"
              type="password"
              className="w-full p-2 border border-gray-300 rounded mt-1"
            />
          </div>
          <div>
            <button className="w-full py-2 px-4 bg-blue-600 hover:bg-blue-700 rounded-md text-white text-sm">
              Login
            </button>
          </div>
          {isAuthError ? (
            <div>
              <label htmlFor="" className="text-sm font-bold text-red-600 block text-center ">
                {' '}
                Username or Password is Incorrect
              </label>
            </div>
          ) : (
            <div />
          )}
        </form>
      </div>
    </div>
  );
};

export default Login;
