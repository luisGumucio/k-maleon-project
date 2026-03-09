import { Edit } from "@refinedev/antd";
import { useOperationForm, OperationFormFields } from "./form";

export const OperationEdit = () => {
  const { formProps, saveButtonProps, initialValues } = useOperationForm("edit");

  return (
    <Edit saveButtonProps={saveButtonProps}>
      <OperationFormFields formProps={formProps} initialValues={initialValues} />
    </Edit>
  );
};
