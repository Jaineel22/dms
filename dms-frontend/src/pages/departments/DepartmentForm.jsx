import React, { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';

import Modal          from '../../components/ui/Modal';
import Button         from '../../components/ui/Button';
import Input          from '../../components/ui/Input';
import Alert          from '../../components/ui/Alert';
import departmentApi  from '../../api/departmentApi';
import {
  validateRequired,
  validateDeptCode,
  validateMaxLength,
} from '../../utils/validators';

/**
 * Create / Edit department modal.
 *
 * @param {boolean}       isOpen
 * @param {Function}      onClose
 * @param {Object|null}   department   null → create mode
 * @param {Function}      onSuccess    called after successful save
 */
const DepartmentForm = ({ isOpen, onClose, department, onSuccess }) => {
  const isEdit = Boolean(department);
  const [apiError, setApiError] = useState('');

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm();

  /* Populate form on open */
  useEffect(() => {
    if (!isOpen) return;
    if (isEdit && department) {
      reset({
        name:        department.name        ?? '',
        code:        department.code        ?? '',
        description: department.description ?? '',
      });
    } else {
      reset({ name: '', code: '', description: '' });
    }
    setApiError('');
  }, [isOpen, department, isEdit, reset]);

  /* Auto-generate code from name when creating */
  const nameValue = watch('name');
  useEffect(() => {
    if (isEdit) return;
    if (!nameValue) return;
    const suggested = nameValue
      .toUpperCase()
      .replace(/[^A-Z0-9\s]/g, '')
      .trim()
      .split(/\s+/)
      .map((w) => w.slice(0, 3))
      .join('_')
      .slice(0, 10);
    setValue('code', suggested, { shouldValidate: false });
  }, [nameValue, isEdit, setValue]);

  const onSubmit = async (data) => {
    setApiError('');
    try {
      const payload = {
        name:        data.name.trim(),
        code:        data.code.trim().toUpperCase(),
        description: data.description?.trim() || null,
      };

      if (isEdit) {
        await departmentApi.updateDepartment(department.id, payload);
        toast.success('Department updated successfully');
      } else {
        await departmentApi.createDepartment(payload);
        toast.success('Department created successfully');
      }

      onSuccess?.();
      onClose();
    } catch (err) {
      setApiError(err?.message || 'Operation failed. Please try again.');
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEdit ? 'Edit Department' : 'Create Department'}
      size="sm"
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={isSubmitting}>
            Cancel
          </Button>
          <Button
            type="submit"
            form="dept-form"
            loading={isSubmitting}
          >
            {isEdit ? 'Save Changes' : 'Create Department'}
          </Button>
        </>
      }
    >
      {apiError && (
        <Alert variant="error" className="mb-4">
          {apiError}
        </Alert>
      )}

      <form id="dept-form" onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4">

        {/* Name */}
        <Input
          label="Department Name"
          required
          placeholder="e.g. Human Resources"
          error={errors.name?.message}
          {...register('name', {
            validate: (v) =>
              validateRequired('Department name')(v) === true
                ? validateMaxLength(100, 'Name')(v)
                : validateRequired('Department name')(v),
          })}
        />

        {/* Code */}
        <Input
          label="Department Code"
          required
          placeholder="e.g. HR"
          hint="2–10 uppercase letters, digits or underscores"
          error={errors.code?.message}
          disabled={isEdit}
          {...register('code', {
            validate: validateDeptCode,
            onChange: (e) => {
              e.target.value = e.target.value.toUpperCase();
            },
          })}
        />
        {isEdit && (
          <p className="text-xs text-slate-400 -mt-2">
            Department code cannot be changed after creation.
          </p>
        )}

        {/* Description */}
        <div className="flex flex-col gap-1">
          <label className="form-label">
            Description
            <span className="ml-1 font-normal text-slate-400">(optional)</span>
          </label>
          <textarea
            rows={3}
            placeholder="Brief description of this department's responsibilities…"
            className={`form-input resize-none ${errors.description ? 'form-input-error' : ''}`}
            {...register('description', {
              validate: validateMaxLength(500, 'Description'),
            })}
          />
          {errors.description && (
            <p className="form-error">{errors.description.message}</p>
          )}
        </div>

      </form>
    </Modal>
  );
};

export default DepartmentForm;