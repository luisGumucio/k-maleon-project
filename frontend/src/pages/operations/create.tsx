import { Create } from "@refinedev/antd";
import { useOperationForm, OperationFormFields } from "./form";

export const OperationCreate = () => {
  const { formProps, saveButtonProps } = useOperationForm("create");

  return (
    <Create saveButtonProps={saveButtonProps}>
      <OperationFormFields formProps={formProps} />
    </Create>
  );
};
