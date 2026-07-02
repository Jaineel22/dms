import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { User, Mail, Phone, Briefcase, Building2, Shield, Lock, Calendar, Hash } from 'lucide-react';
import toast from 'react-hot-toast';

import PageHeader  from '../../components/common/PageHeader';
import Card        from '../../components/ui/Card';
import Button      from '../../components/ui/Button';
import Input       from '../../components/ui/Input';
import Alert       from '../../components/ui/Alert';
import useAuth     from '../../hooks/useAuth';
import userApi     from '../../api/userApi';
import { getInitials, getAvatarBg, getStatusColor, getRoleColor } from '../../utils/helpers';
import { formatDate, formatRole, formatEmployeeLevel, formatDateTime } from '../../utils/formatters';
import { validatePassword, validateRequired } from '../../utils/validators';

/* ── Info row helper ── */
const InfoRow = ({ icon: Icon, label, value }) => (
  <div className="flex items-start gap-3 py-3 border-b border-slate-100 last:border-0">
    <div className="w-8 h-8 rounded-lg bg-slate-100 flex items-center justify-center shrink-0 mt-0.5">
      <Icon size={15} className="text-slate-500" />
    </div>
    <div className="flex-1 min-w-0">
      <p className="text-xs text-slate-400 font-medium uppercase tracking-wide">{label}</p>
      <p className="text-sm font-medium text-slate-700 mt-0.5 break-all">{value || '—'}</p>
    </div>
  </div>
);

/* ── Change Password sub-form ── */
const ChangePasswordForm = ({ userId }) => {
  const [apiError, setApiError] = useState('');
  const [success,  setSuccess]  = useState(false);
  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors, isSubmitting },
  } = useForm();

  const newPwd = watch('newPassword');

  const onSubmit = async ({ oldPassword, newPassword }) => {
    setApiError('');
    setSuccess(false);
    try {
      await userApi.changePassword(userId, oldPassword, newPassword);
      toast.success('Password changed successfully');
      setSuccess(true);
      reset();
    } catch (err) {
      setApiError(err?.message || 'Failed to change password. Check your current password.');
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4 mt-2">
      {apiError && <Alert variant="error">{apiError}</Alert>}
      {success  && <Alert variant="success">Password changed successfully.</Alert>}

      <Input
        label="Current Password"
        type="password"
        required
        autoComplete="current-password"
        error={errors.oldPassword?.message}
        leftIcon={<Lock size={15} />}
        {...register('oldPassword', { validate: validateRequired('Current password') })}
      />

      <Input
        label="New Password"
        type="password"
        required
        autoComplete="new-password"
        hint="Min 8 chars, upper + lower + digit + special character"
        error={errors.newPassword?.message}
        leftIcon={<Lock size={15} />}
        {...register('newPassword', { validate: validatePassword })}
      />

      <Input
        label="Confirm New Password"
        type="password"
        required
        autoComplete="new-password"
        error={errors.confirmPassword?.message}
        leftIcon={<Lock size={15} />}
        {...register('confirmPassword', {
          validate: (v) => v === newPwd || 'Passwords do not match',
        })}
      />

      <div className="flex justify-end">
        <Button type="submit" loading={isSubmitting} size="sm">
          Update Password
        </Button>
      </div>
    </form>
  );
};

/* ── Main profile page ── */
const Profile = () => {
  const { user } = useAuth();

  if (!user) return null;

  const fullName   = user.fullName || `${user.firstName} ${user.lastName}`;
  const statusColors = getStatusColor(user.isActive);
  const roleColors   = getRoleColor(user.role?.name);

  return (
    <div>
      <PageHeader
        title="My Profile"
        subtitle="View and manage your account details"
      />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

        {/* ── Left: avatar + summary ── */}
        <div className="lg:col-span-1 space-y-4">

          {/* Avatar card */}
          <Card>
            <Card.Body className="flex flex-col items-center text-center py-8">
              <div className={`w-20 h-20 rounded-full flex items-center justify-center text-white text-2xl font-bold shadow-lg ${getAvatarBg(user.firstName)}`}>
                {getInitials(user.firstName, user.lastName)}
              </div>
              <h2 className="mt-4 text-lg font-bold text-slate-800">{fullName}</h2>
              <p className="text-sm text-slate-500 mt-0.5">{user.designation || 'No designation set'}</p>

              {/* Badges */}
              <div className="flex flex-wrap items-center justify-center gap-2 mt-3">
                <span className={`badge ${roleColors.bg} ${roleColors.text}`}>
                  {formatRole(user.role?.name)}
                </span>
                <span className={`badge ${statusColors.bg} ${statusColors.text}`}>
                  <span className={`w-1.5 h-1.5 rounded-full ${statusColors.dot}`} />
                  {statusColors.label}
                </span>
              </div>

              {/* Employee ID */}
              <div className="mt-4 px-3 py-1.5 bg-slate-100 rounded-lg">
                <span className="font-mono text-sm font-semibold text-slate-600">
                  {user.employeeId}
                </span>
              </div>
            </Card.Body>
          </Card>

          {/* Quick stats */}
          <Card>
            <Card.Body className="space-y-3">
              <h3 className="text-xs font-semibold text-slate-400 uppercase tracking-wide">Quick Info</h3>
              <dl className="space-y-2 text-sm">
                {[
                  ['Department',  user.department?.name],
                  ['Level',       formatEmployeeLevel(user.employeeLevel)],
                  ['Last Login',  formatDateTime(user.lastLoginAt)],
                  ['Member Since',formatDate(user.createdAt)],
                ].map(([label, value]) => (
                  <div key={label} className="flex items-start justify-between gap-2">
                    <dt className="text-slate-400 shrink-0">{label}</dt>
                    <dd className="font-medium text-slate-700 text-right">{value || '—'}</dd>
                  </div>
                ))}
              </dl>
            </Card.Body>
          </Card>
        </div>

        {/* ── Right: details + password ── */}
        <div className="lg:col-span-2 space-y-6">

          {/* Personal information */}
          <Card>
            <Card.Header title="Personal Information" />
            <Card.Body>
              <div className="divide-y divide-slate-100">
                <InfoRow icon={User}      label="Full Name"    value={fullName}         />
                <InfoRow icon={Mail}      label="Email"        value={user.email}       />
                <InfoRow icon={Phone}     label="Phone"        value={user.phoneNumber} />
                <InfoRow icon={Briefcase} label="Designation"  value={user.designation} />
                <InfoRow icon={Hash}      label="Employee ID"  value={user.employeeId}  />
              </div>
            </Card.Body>
          </Card>

          {/* Organisation */}
          <Card>
            <Card.Header title="Organisation" />
            <Card.Body>
              <div className="divide-y divide-slate-100">
                <InfoRow
                  icon={Building2}
                  label="Department"
                  value={
                    user.department
                      ? `${user.department.name} (${user.department.code})`
                      : undefined
                  }
                />
                <InfoRow
                  icon={Shield}
                  label="Role"
                  value={formatRole(user.role?.name)}
                />
                <InfoRow
                  icon={User}
                  label="Employee Level"
                  value={formatEmployeeLevel(user.employeeLevel)}
                />
                {user.managerId && (
                  <InfoRow
                    icon={User}
                    label="Manager ID"
                    value={String(user.managerId)}
                  />
                )}
              </div>
            </Card.Body>
          </Card>

          {/* Account activity */}
          <Card>
            <Card.Header title="Account Activity" />
            <Card.Body>
              <div className="divide-y divide-slate-100">
                <InfoRow icon={Calendar} label="Account Created"  value={formatDateTime(user.createdAt)}  />
                <InfoRow icon={Calendar} label="Last Updated"     value={formatDateTime(user.updatedAt)}  />
                <InfoRow icon={Calendar} label="Last Login"       value={formatDateTime(user.lastLoginAt)} />
              </div>
            </Card.Body>
          </Card>

          {/* Change password */}
          <Card>
            <Card.Header
              title="Change Password"
              subtitle="Choose a strong password with uppercase, lowercase, digits and special characters"
            />
            <Card.Body>
              <ChangePasswordForm userId={user.id} />
            </Card.Body>
          </Card>

        </div>
      </div>
    </div>
  );
};

export default Profile;