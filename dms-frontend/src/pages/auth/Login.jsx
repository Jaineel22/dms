import React, { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { Mail, Lock } from 'lucide-react';

import AuthLayout      from '../../components/layout/AuthLayout';
import Input           from '../../components/ui/Input';
import Button          from '../../components/ui/Button';
import Alert           from '../../components/ui/Alert';
import useAuth         from '../../hooks/useAuth';
import { ROUTES }      from '../../routes/RouteConstants';
import { validateEmail } from '../../utils/validators';

const Login = () => {
  const { login, isAuthenticated, loading } = useAuth();
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm({ defaultValues: { email: '', password: '' } });

  // Redirect if already authenticated
  useEffect(() => {
    if (isAuthenticated) navigate(ROUTES.DASHBOARD, { replace: true });
  }, [isAuthenticated, navigate]);

  const onSubmit = async ({ email, password }) => {
    try {
      await login(email, password);
    } catch (err) {
      const msg = err?.message || 'Login failed. Please try again.';
      setError('root', { message: msg });
    }
  };

  if (loading) return null; // AuthContext still hydrating

  return (
    <AuthLayout>
      <div className="mb-6">
        <h2 className="text-xl font-bold text-slate-800">Sign in to your account</h2>
        <p className="text-sm text-slate-500 mt-1">Enter your credentials to continue</p>
      </div>

      {errors.root && (
        <Alert variant="error" className="mb-5">
          {errors.root.message}
        </Alert>
      )}

      <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4">
        <Input
          label="Email address"
          type="email"
          required
          autoComplete="email"
          autoFocus
          leftIcon={<Mail size={16} />}
          placeholder="you@company.com"
          error={errors.email?.message}
          {...register('email', { validate: validateEmail })}
        />

        <Input
          label="Password"
          type="password"
          required
          autoComplete="current-password"
          leftIcon={<Lock size={16} />}
          placeholder="••••••••"
          error={errors.password?.message}
          {...register('password', { required: 'Password is required' })}
        />

        <Button
          type="submit"
          fullWidth
          size="lg"
          loading={isSubmitting}
          className="mt-6"
        >
          Sign In
        </Button>
      </form>

      <p className="mt-6 text-center text-xs text-slate-400">
        Access restricted to authorised personnel only.
        <br />Contact your administrator if you need access.
      </p>
    </AuthLayout>
  );
};

export default Login;