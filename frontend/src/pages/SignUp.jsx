import React from "react";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { FcGoogle } from "react-icons/fc";
import { FaGithub } from "react-icons/fa";
import { registerUser } from "@/utils/apis/auth";
import logo from "@/assets/logo.png";


export default function SignUp() {
  const { register, handleSubmit, formState: { errors } } = useForm();
  const navigate = useNavigate();

  const onSubmit = async (data) => {
  try {
    await registerUser({ email: data.email, password: data.password });
    alert("Registration successful! Please login.");
    navigate("/login");
  } catch (err) {
    alert("Signup failed. Please try again.");
    console.log(err);
  }
};

  return (
    <div className="min-h-screen flex items-center justify-center bg-[#1E1E1E] text-gray-200 px-4">
      <div className="bg-[#252526] p-10 rounded-2xl shadow-lg w-full max-w-md">
        <div className="flex justify-center">
                 <img src={logo} alt="" className="h-10 mb-5"/> 
               </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
         
          {/* Email */}
          <div className="flex flex-col">
            <Label className="text-gray-200 text-sm font-medium mb-2">Email</Label>
            <Input
              type="email"
              placeholder="you@example.com"
              {...register("email", { required: "Email is required" })}
              className="bg-[#1E1E1E] border border-gray-800 text-gray-200 placeholder-gray-500 focus:border-gray-200 focus:ring-gray-200 py-3 px-4 rounded-md text-sm"
            />
            {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email.message}</p>}
          </div>

          {/* Password */}
          <div className="flex flex-col">
            <Label className="text-gray-200 text-sm font-medium mb-2">Password</Label>
            <Input
              type="password"
              placeholder="********"
              {...register("password", { required: "Password is required", minLength: 6 })}
              className="bg-[#1E1E1E] border border-gray-800 text-gray-200 placeholder-gray-500 focus:border-gray-200 focus:ring-gray-200 py-3 px-4 rounded-md text-sm"
            />
            {errors.password && <p className="text-red-500 text-xs mt-1">{errors.password.message}</p>}
          </div>

          {/* Sign Up Button */}
          <Button
            type="submit"
            className="w-full bg-gray-900 hover:bg-gray-800 text-gray-200 font-medium py-3 rounded-md border border-gray-800 transition-colors"
          >
            Sign Up
          </Button>

          {/* OAuth Buttons */}
          <div className="flex gap-3 mt-4">
            <Button
              type="button"
              className="flex-1 flex items-center justify-center bg-gray-900 hover:bg-gray-800 text-gray-200 border border-gray-800 rounded-md py-2 transition-colors"
              onClick={() => alert("Google login")}
            >
              <FcGoogle className="mr-2 text-lg" /> Google
            </Button>
            <Button
              type="button"
              className="flex-1 flex items-center justify-center bg-gray-900 hover:bg-gray-800 text-gray-200 border border-gray-800 rounded-md py-2 transition-colors"
              onClick={() => alert("GitHub login")}
            >
              <FaGithub className="mr-2 text-lg" /> GitHub
            </Button>
          </div>

          {/* Login Link */}
          <p className="text-center text-gray-400 mt-6 text-sm">
            Already have an account?{" "}
            <span
              onClick={() => navigate("/login")}
              className="text-gray-200 hover:underline cursor-pointer"
            >
              Login
            </span>
          </p>
        </form>
      </div>
    </div>
  );
}
