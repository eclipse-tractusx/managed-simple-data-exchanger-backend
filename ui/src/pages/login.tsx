import React from "react"

function Login() {
    return (
      <div className="min-h-screen flex flex-col justify-center bg-white">
        <div className="max-w-md w-full mx-auto font-bold text-6xl text-center"> DFT
        </div>
          <div className="max-w-md w-full mx-auto mt-4 bg-white p-8 border ">
          <form action="" className="space-y-6">
            <div>
              <label htmlFor="" className="text-sm font-bold text-gray-600 block"> Email</label>
              <input type="text" className="w-full p-2 border border-gray-300 rounded mt-1" />
            </div>
            <div>
              <label htmlFor="" className="text-sm font-bold text-gray-600 block"> Password</label>
              <input type="text" className="w-full p-2 border border-gray-300 rounded mt-1" />
            </div>
            <div>
              <button className="w-full py-2 px-4 bg-blue-600 hover:bg-blue-700 rounded-md text-white text-sm">Login</button>
            </div>
          </form>
          </div>
      </div>
    );
  }
  
  export default Login;
  