import React, { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';

import Modal      from '../../components/ui/Modal';
import Button     from '../../components/ui/Button';
import Input      from '../../components/ui/Input';
import Alert      from '../../components/ui/Alert';
import userApi    from '../../api/userApi';
import departmentApi from '../../api/departmentApi';
import {
  validateEmail, validatePassword, validatePasswordOptional,
  validateEmployeeId, validatePhoneNumber, validateRequired,
} from '../../utils/validators';
import { EMPLOYEE_LEVELS } from '../../utils/constants';

/**
 * Create / Edit user modal.
 *
 * @param {boolean}       isOpen
 * @param {Function}      onClose
 * @param {Object|null}   user      null = create mode
 * @param {Function}      onSuccess called after successful save
 * @param {Array}         roles     [{ id, name }]
 */
const UserForm = ({ isOpen, onClose, user, onSuccess, roles = [] }) => {
  const isEdit = Boolean(user);
  const [departments, setDepartments] = useState([]);
  const [apiError,    setApiError]    = useState('');

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm();

  // Load departments for dropdown
  useEffect(() => {
    if (!isOpen) return;
    departmentApi.getAllDepartments({ size: 100 })
      .then((page) => setDepartments(page?.content ?? []))
      .catch(() => {});
  }, [isOpen]);

  // Populate form when editing
  useEffect(() => {
    if (!isOpen) return;
    if (isEdit && user) {
      reset({
        firstName:     user.firstName,
        lastName:      user.lastName,
        email:         user.email,
        employeeId:    user.employeeId,
        designation:   user.designation   ?? '',
        phoneNumber:   user.phoneNumber   ?? '',
        roleId:        user.role?.id      ?? '',
        departmentId:  user.department?.id ?? '',
        managerId:     user.managerId     ?? '',
        employeeLevel: user.employeeLevel ?? 1,
      });
    } else {
      reset({ employeeLevel: 1 });
    }
    setApiError('');
  }, [isOpen, user, isEdit, reset]);

  const onSubmit = async (data) => {
    setApiError('');
    try {
      const payload = {
        ...data,
        roleId:       Number(data.roleId),
        departmentId: Number(data.departmentId),
        managerId:    data.managerId ? Number(data.managerId) : undefined,
        employeeLevel:Number(data.employeeLevel),
      };
      if (!payload.managerId) delete payload.managerId;
      if (isEdit && !payload.password)  delete payload.password;

      if (isEdit) {
        await userApi.updateUser(user.id, payload);
        toast.success('User updated successfully');
      } else {
        await userApi.createUser(payload);
        toast.success('User created successfully');
      }
      onSuccess?.();
      onClose();
    } catch (err) {
      setApiError(err?.message || 'Operation failed. Please try again.');
    }
  };

  const field = (name, opts = {}) => register(name, opts);

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEdit ? 'Edit User' : 'Create New User'}
      size="lg"
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={isSubmitting}>Cancel</Button>
          <Button type="submit" form="user-form" loading={isSubmitting}>
            {isEdit ? 'Save Changes' : 'Create User'}
          </Button>
        </>
      }
    >
      {apiError && <Alert variant="error" className="mb-4">{apiError}</Alert>}

      <form id="user-form" onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4">
        {/* Row: first + last */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input label="First Name" required error={errors.firstName?.message}
            {...field('firstName', { validate: validateRequired('First name') })} />
          <Input label="Last Name" required error={errors.lastName?.message}
            {...field('lastName', { validate: validateRequired('Last name') })} />
        </div>

        {/* Row: email + employeeId */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input label="Email" type="email" required error={errors.email?.message}
            placeholder="user@company.com"
            {...field('email', { validate: validateEmail })} />
          <Input label="Employee ID" required error={errors.employeeId?.message}
            placeholder="EMP001"
            disabled={isEdit}
            hint={isEdit ? 'Employee ID cannot be changed' : undefined}
            {...field('employeeId', { validate: validateEmployeeId })} />
        </div>

        {/* Password */}
        <Input
          label={isEdit ? 'New Password (leave blank to keep current)' : 'Password'}
          type="password"
          required={!isEdit}
          error={errors.password?.message}
          hint={!isEdit ? 'Min 8 chars, upper + lower + digit + special (@$!%*?&)' : undefined}
          {...field('password', {
            validate: isEdit ? validatePasswordOptional : validatePassword,
          })}
        />

        {/* Row: role + department */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div className="flex flex-col gap-1">
            <label className="form-label">Role <span className="text-red-500">*</span></label>
            <select className={`form-select ${errors.roleId ? 'form-input-error' : ''}`}
              {...field('roleId', { required: 'Role is required' })}>
              <option value="">Select role…</option>
              {roles.map((r) => (
                <option key={r.id} value={r.id}>{r.name.replace('ROLE_', '')}</option>
              ))}
            </select>
            {errors.roleId && <p className="form-error">{errors.roleId.message}</p>}
          </div>

          <div className="flex flex-col gap-1">
            <label className="form-label">Department <span className="text-red-500">*</span></label>
            <select className={`form-select ${errors.departmentId ? 'form-input-error' : ''}`}
              {...field('departmentId', { required: 'Department is required' })}>
              <option value="">Select department…</option>
              {departments.map((d) => (
                <option key={d.id} value={d.id}>{d.name} ({d.code})</option>
              ))}
            </select>
            {errors.departmentId && <p className="form-error">{errors.departmentId.message}</p>}
          </div>
        </div>

        {/* Row: designation + phone */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input label="Designation" placeholder="Software Engineer" error={errors.designation?.message}
            {...field('designation')} />
          <Input label="Phone Number" placeholder="+911234567890" error={errors.phoneNumber?.message}
            {...field('phoneNumber', { validate: validatePhoneNumber })} />
        </div>

        {/* Row: level + manager */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div className="flex flex-col gap-1">
            <label className="form-label">Employee Level</label>
            <select className="form-select" {...field('employeeLevel')}>
              {EMPLOYEE_LEVELS.map((l) => (
                <option key={l.value} value={l.value}>{l.label}</option>
              ))}
            </select>
          </div>
          <Input label="Manager ID" type="number" placeholder="Optional"
            hint="Enter the manager's user ID"
            {...field('managerId')} />
        </div>
      </form>
    </Modal>
  );
};

export default UserForm;