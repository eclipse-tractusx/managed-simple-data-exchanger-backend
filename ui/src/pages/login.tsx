import React from 'react';
import { useForm } from 'react-hook-form';

type FormValues = {
  email: string;
  password: string;
};

function Login() {
  const { register, handleSubmit } = useForm<FormValues>();
  const onSubmit = handleSubmit(({ email, password }) => {
    console.log(email, password);
  });

  return (
    <div className="min-h-screen flex flex-col justify-center bg-white">
      <div className="max-w-md w-full mx-auto font-bold text-6xl text-center"> DFT</div>
      <div className="max-w-md w-full mx-auto mt-4 bg-white p-8 border ">
        <form action="" className="space-y-6" onSubmit={onSubmit}>
          <div>
            <label htmlFor="" className="text-sm font-bold text-gray-600 block">
              {' '}
              Email
            </label>
            <input
              {...register('email', { required: true, minLength: 5, maxLength: 80 })}
              name="email"
              type="email"
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
              type="text"
              className="w-full p-2 border border-gray-300 rounded mt-1"
            />
          </div>
          <div>
            <button className="w-full py-2 px-4 bg-blue-600 hover:bg-blue-700 rounded-md text-white text-sm">
              Login
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default Login;
